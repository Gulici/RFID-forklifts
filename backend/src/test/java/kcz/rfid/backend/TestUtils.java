package kcz.rfid.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import kcz.rfid.backend.model.dto.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class TestUtils {

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    public static DeviceDto registerDevice(MockMvc mockMvc, ObjectMapper objectMapper, DeviceRegisterDto dto) throws Exception {
        String json = objectMapper.writeValueAsString(dto);
        MvcResult result = mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), DeviceDto.class);
    }

    public static String requestNonce(MockMvc mockMvc, ObjectMapper objectMapper, String publicKeyPem) throws Exception {
        NonceRequest request = new NonceRequest();
        request.setPublicKeyPem(publicKeyPem);

        MvcResult result = mockMvc.perform(post("/auth/request-nonce")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nonce").exists())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.nonce");
    }

    public static String signNonce(String nonce, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(nonce.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    public static String verifySignatureAndGetToken(MockMvc mockMvc, ObjectMapper objectMapper, String publicKeyPem, String signatureBase64) throws Exception {
        SignedNonceRequest request = new SignedNonceRequest();
        request.setPublicKeyPem(publicKeyPem);
        request.setSignatureBase64(signatureBase64);

        MvcResult result = mockMvc.perform(post("/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").exists())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.jwt");
    }

    public static String registerAndAuthorizeNewDevice(MockMvc mockMvc, ObjectMapper objectMapper, DeviceRegisterDto dto, KeyPair keyPair) throws Exception {
        registerDevice(mockMvc, objectMapper, dto);
        String nonce = TestUtils.requestNonce(mockMvc, objectMapper, dto.getPublicKey());
        String signatureBase64 = TestUtils.signNonce(nonce, keyPair.getPrivate());
        return verifySignatureAndGetToken(mockMvc, objectMapper, dto.getPublicKey(), signatureBase64);
    }
}
