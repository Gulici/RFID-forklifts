using System;
using System.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using RfidFirmware.Models;
using RfidFirmware.Services.Interfaces;
using RfidFirmware.Utils;

namespace RfidFirmware.Services
{
    public class ApiService : IApiService
    {
        private readonly ILogger<ApiService> _logger;
        private readonly HttpClient _httpClient;

        private string _jwt;

        public ApiService(
            ILogger<ApiService> logger,
            HttpClient httpClient)
        {
            _logger = logger;
            _httpClient = httpClient;
        }

        public async Task<bool> LoginDeviceAsync()
        {
            try
            {
                // 1. Request nonce
                var nonceRequestJson = JsonSerializer.Serialize(new NonceRequest(RsaKeyUtils.GetPublicKeyPem()), new JsonSerializerOptions
                {
                    PropertyNamingPolicy =JsonNamingPolicy.CamelCase
                });

                var nonceContent = new StringContent(nonceRequestJson, Encoding.UTF8, "application/json");

                var nonceResponse = await _httpClient.PostAsync("auth/request-nonce", nonceContent);

                if (!nonceResponse.IsSuccessStatusCode)
                {
                    _logger.LogWarning("Failed to get nonce. Status: {Status}", nonceResponse.StatusCode);
                    return false;
                }

                var nonceResponseJson = await nonceResponse.Content.ReadAsStringAsync();

                var nonceObj = JsonSerializer.Deserialize<NonceResponse>(nonceResponseJson, new JsonSerializerOptions
                {
                    PropertyNameCaseInsensitive = true
                });

                if (nonceObj == null || string.IsNullOrEmpty(nonceObj.Nonce))
                {
                    _logger.LogWarning("Invalid nonce response.");
                    return false;
                }

                // 2. Sign nonce
                var signedNonce = RsaKeyUtils.SignNonce(nonceObj.Nonce);

                // 3. Request jwt
                var authRequest = new SignedNonceRequest(RsaKeyUtils.GetPublicKeyPem(), signedNonce);

                var authJson = JsonSerializer.Serialize(authRequest, new JsonSerializerOptions
                {
                    PropertyNamingPolicy =JsonNamingPolicy.CamelCase
                });
                var authContent = new StringContent(authJson, Encoding.UTF8, "application/json");

                var authResponse = await _httpClient.PostAsync("auth/verify", authContent);

                if (!authResponse.IsSuccessStatusCode)
                {
                    _logger.LogWarning("JWT authentication failed. Status: {Status}", authResponse.StatusCode);
                    return false;
                }

                var authResponseJson = await authResponse.Content.ReadAsStringAsync();
                var authObj = JsonSerializer.Deserialize<JwtDto>(authResponseJson, new JsonSerializerOptions
                {
                    PropertyNameCaseInsensitive = true
                });

                if (authObj == null || string.IsNullOrEmpty(authObj.Jwt))
                {
                    _logger.LogWarning("Invalid JWT response.");
                    return false;
                }

                _jwt = authObj.Jwt;

                _logger.LogInformation("Device authenticated successfully.");
                return true;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error during LoginDeviceAsync");
                return false;
            }
        }


        public async Task<bool> RegisterDeviceAsync(string username, string password, string deviceName, string publicKey)
        {
            var registerDto = new DeviceRegisterDto(username, password, deviceName, publicKey);

            var json = JsonSerializer.Serialize(registerDto, new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase
            });

            var content = new StringContent(json, Encoding.UTF8, "application/json");
            try
            {
                var response = await _httpClient.PostAsync("devices", content);

                if (response.IsSuccessStatusCode)
                {
                    _logger.LogInformation("Device registered");
                    return true;
                }

                var responseContent = await response.Content.ReadAsStringAsync();
                _logger.LogWarning("Device register error. Code: {StatusCode}, Response: {Response}",
                    response.StatusCode, responseContent);

                return false;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Exception durring device register");
                return false;
            }
        }

        public async Task<bool> SendTagAsync(Tag tag)
        {
            // 1. Parse jwt claims and create dto
            if (!TryParseJwtClaims(_jwt, out var deviceId, out var firmId))
            {
                _logger.LogWarning("JWT invalid or missing claims. Trying to refresh...");
                var loginSuccess = await LoginDeviceAsync();
                if (!loginSuccess || !TryParseJwtClaims(_jwt, out deviceId, out firmId))
                {
                    _logger.LogError("JWT still invalid after re-login.");
                    return false;
                }
            }

            var locationDto = new DeviceLocationDto
            {
                EpcCode = tag.Epc,
                FirmId = firmId,
                Id = deviceId
            };


            // 2. Prepare data
            var json = JsonSerializer.Serialize(locationDto, new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase
            });

            // 3. Send post request with tag in body
            try
            {
                var response = await SendAuthorizedPutAsync("devices/updateLocation", json, _jwt);

                // If UNAUTH then try again after relogging
                if (response.StatusCode == System.Net.HttpStatusCode.Unauthorized)
                {
                    _logger.LogWarning("JWT unauthorized. Trying to re-login and retry...");

                    var loginSuccess = await LoginDeviceAsync();
                    if (!loginSuccess)
                        return false;

                    response = await SendAuthorizedPutAsync("devices/updateLocation", json, _jwt);
                }


                if (response.IsSuccessStatusCode)
                {
                    _logger.LogInformation("Location updated");
                    return true;
                }

                var responseContent = await response.Content.ReadAsStringAsync();
                _logger.LogWarning("Location update error. Code: {StatusCode}, Response: {Response}",
                    response.StatusCode, responseContent);

                return false;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Exception durring update location");
                return false;
            }
        }

        private bool TryParseJwtClaims(string jwt, out Guid deviceId, out Guid firmId)
        {
            deviceId = Guid.Empty;
            firmId = Guid.Empty;

            try
            {
                var handler = new JwtSecurityTokenHandler();
                var jwtObj = handler.ReadJwtToken(jwt);

                var deviceIdClaim = jwtObj.Claims.FirstOrDefault(c => c.Type == "deviceId")?.Value;
                var firmIdClaim = jwtObj.Claims.FirstOrDefault(c => c.Type == "firmId")?.Value;

                if (Guid.TryParse(deviceIdClaim, out deviceId) && Guid.TryParse(firmIdClaim, out firmId))
                    return true;

                return false;
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "Failed to parse JWT");
                return false;
            }
        }

        private async Task<HttpResponseMessage> SendAuthorizedPutAsync(string url, string jsonContent, string jwt)
        {
            var request = new HttpRequestMessage(HttpMethod.Put, url)
            {
                Content = new StringContent(jsonContent, Encoding.UTF8, "application/json")
            };
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", jwt);

            return await _httpClient.SendAsync(request);
        }
    }
}
