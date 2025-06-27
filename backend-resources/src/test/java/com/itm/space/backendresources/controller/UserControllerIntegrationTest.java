package com.itm.space.backendresources.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private Keycloak keycloak;

    @Test
    @WithMockUser(roles = "MODERATOR")
    void testCreateUser() throws Exception {
        UserRequest request = new UserRequest(
                "VovaP",
                "demON4k@gmail.com",
                "1234",
                "Vova",
                "Paschenya");
        mvc.perform(requestWithContent(post("/api/users"), request))
                .andExpect(status().isOk());
        UUID userId = UUID.fromString(keycloak.realm("ITM").users().search(request.getUsername()).get(0).getId());
        assertNotNull(userService.getUserById(userId));
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void createUserInvalidRequest() throws Exception {
        UserRequest request = new UserRequest(
                "",
                "demON4k-email",
                "123",
                "",
                "");
        mvc.perform(requestWithContent(post("/api/users"), request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.password").exists())
                .andExpect(jsonPath("$.firstName").exists())
                .andExpect(jsonPath("$.lastName").exists());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void getUserByIdSuccess() throws Exception {
        String id = "b4d7a64b-ce72-40c4-981b-eb9b2a102128";
        mvc.perform(get("/api/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Владимир"));
    }


    @Test
    @WithMockUser(username = "testuser", roles = "MODERATOR")
    void helloEndpointReturnsUsername() throws Exception {
        mvc.perform(get("/api/users/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("testuser"));
    }
}
