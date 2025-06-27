package com.itm.space.backendresources.service;

//import com.itm.space.backendresources.BaseIntegrationTest;
//import com.itm.space.backendresources.api.request.UserRequest;
//import com.itm.space.backendresources.api.response.UserResponse;
//import com.itm.space.backendresources.exception.BackendResourcesException;
//import org.junit.jupiter.api.Test;
//import org.keycloak.admin.client.Keycloak;
//import org.keycloak.admin.client.resource.RealmResource;
//import org.keycloak.admin.client.resource.RoleMappingResource;
//import org.keycloak.admin.client.resource.UserResource;
//import org.keycloak.admin.client.resource.UsersResource;
//import org.keycloak.representations.idm.GroupRepresentation;
//import org.keycloak.representations.idm.MappingsRepresentation;
//import org.keycloak.representations.idm.RoleRepresentation;
//import org.keycloak.representations.idm.UserRepresentation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.HttpStatus;
//
//import javax.ws.rs.WebApplicationException;
//import javax.ws.rs.core.Response;
//import java.net.URI;
//import java.util.List;
//import java.util.UUID;
//
//import static javax.ws.rs.core.Response.status;
//import static org.apache.http.client.methods.RequestBuilder.post;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
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
