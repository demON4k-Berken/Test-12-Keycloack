package com.itm.space.backendresources.service;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserServiceImplIntegrationTest extends BaseIntegrationTest {

    private String userId;

    @Autowired
    private Keycloak keycloak;

    @Test
    @WithMockUser(roles = "MODERATOR")
    void createUserSuccess() throws Exception {
        UserRequest request = new UserRequest(
                "Vova",
                "demON4k@google.com",
                "1234",
                "Vova",
                "Paschenya"
        );
        mvc.perform(requestWithContent(post("/api/users"), request))
                .andExpect(status().isOk());
        userId = keycloak.realm("ITM").users().search(request.getUsername()).get(0).getId();
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void createUserPasswordTooShort() throws Exception {
        UserRequest request = new UserRequest(
                "testuser",
                "test@example.com",
                "1",
                "Test",
                "User"
        );
        mvc.perform(requestWithContent(post("/api/users"), request))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void getUserByIdSuccess() throws Exception {
        String id = "b4d7a64b-ce72-40c4-981b-eb9b2a102128";
        mvc.perform(get("/api/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Владимир"));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (userId != null) {
            keycloak.realm("ITM").users().get(userId).remove();
        }
    }
}
