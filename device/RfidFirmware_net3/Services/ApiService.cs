using System;
using System.Net.Http;
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
                var nonceRequestJson = JsonSerializer.Serialize(new NonceRequest(RsaKeyUtils.GetPublicKeyPem()));
                var nonceContent = new StringContent(nonceRequestJson, Encoding.UTF8, "application/json");

                var nonceResponse = await _httpClient.PostAsync("request-nonce", nonceContent);

                if (!nonceResponse.IsSuccessStatusCode)
                {
                    _logger.LogWarning("Failed to get nonce. Status: {Status}", nonceResponse.StatusCode);
                    return false;
                }

                var nonceResponseJson = await nonceResponse.Content.ReadAsStringAsync();
                var nonceObj = JsonSerializer.Deserialize<NonceResponse>(nonceResponseJson);

                if (nonceObj == null || string.IsNullOrEmpty(nonceObj.Nonce))
                {
                    _logger.LogWarning("Invalid nonce response.");
                    return false;
                }

                // 2. Sign nonce
                var signedNonce = RsaKeyUtils.SignNonce(nonceObj.Nonce);

                // 3. Request jwt
                var authRequest = new SignedNonceRequest(RsaKeyUtils.GetPublicKeyPem(), signedNonce);

                var authJson = JsonSerializer.Serialize(authRequest);
                var authContent = new StringContent(authJson, Encoding.UTF8, "application/json");

                var authResponse = await _httpClient.PostAsync("auth/verify", authContent);

                if (!authResponse.IsSuccessStatusCode)
                {
                    _logger.LogWarning("JWT authentication failed. Status: {Status}", authResponse.StatusCode);
                    return false;
                }

                var authResponseJson = await authResponse.Content.ReadAsStringAsync();
                var authObj = JsonSerializer.Deserialize<JwtDto>(authResponseJson);

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
    }
}
