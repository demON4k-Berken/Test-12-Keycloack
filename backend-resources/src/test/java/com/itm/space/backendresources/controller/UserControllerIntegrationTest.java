package com.itm.space.backendresources.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest extends BaseIntegrationTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @WithMockUser(roles = "MODERATOR")
    @Test
    void createUserValidRequest() throws Exception {
        UserRequest request = new UserRequest(
                "demON4k",
                "demON4k@gmail.com",
                "1234",
                "Vova",
                "Paschenya");

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService, times(1)).createUser(any(UserRequest.class));
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void createUserInvalidRequest() throws Exception {
        UserRequest invalidRequest = new UserRequest(
                "",
                "demON4k-email",
                "123",
                "",
                "");

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
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
        UUID userId = UUID.randomUUID();
        UserResponse response = new UserResponse(
                "Vova",
                "Paschenya",
                "demON4k@google.com",
                List.of("ROLE_USER"),
                List.of("GroupA")
        );
        when(userService.getUserById(userId)).thenReturn(response);
        mvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Vova"))
                .andExpect(jsonPath("$.lastName").value("Paschenya"))
                .andExpect(jsonPath("$.email").value("demON4k@google.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.groups[0]").value("GroupA"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "MODERATOR")
    void helloEndpointReturnsUsername() throws Exception {
        mvc.perform(get("/api/users/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("testuser"));
    }
}
