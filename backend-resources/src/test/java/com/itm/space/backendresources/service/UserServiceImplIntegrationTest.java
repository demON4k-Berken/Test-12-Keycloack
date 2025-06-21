package com.itm.space.backendresources.service;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.exception.BackendResourcesException;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @MockBean
    private Keycloak keycloak;

    @MockBean
    private RealmResource realmResource;

    @MockBean
    private UsersResource usersResource;

    @MockBean
    private UserResource userResource;

    @Test
    void createUserSuccess() {
        UserRequest request = new UserRequest(
                "Vova",
                "demON4k@google.com",
                "1",
                "Vova",
                "Paschenya"
        );

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(201);
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);
        when(response.getLocation()).thenReturn(URI.create("/users/123"));

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

        userService.createUser(request);

        verify(usersResource, times(1)).create(any(UserRepresentation.class));
    }

    @Test
    void createUserKeycloakThrowsException() {
        UserRequest request = new UserRequest(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User"
        );

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(400);
        when(response.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);

        WebApplicationException ex = new WebApplicationException(
                "Bad request",
                Response.status(Response.Status.BAD_REQUEST).build()
        );

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class)))
                .thenThrow(ex);

        BackendResourcesException exception = assertThrows(
                BackendResourcesException.class,
                () -> userService.createUser(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void getUserByIdSuccess() {
        UUID userId = UUID.randomUUID();
        UserRepresentation userRep = new UserRepresentation();
        userRep.setFirstName("Vova");
        userRep.setLastName("Paschenya");
        userRep.setEmail("vova@gmail.com");

        RoleRepresentation role = new RoleRepresentation();
        role.setName("user-role");
        GroupRepresentation group = new GroupRepresentation();
        group.setName("user-group");

        RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
        MappingsRepresentation mappingsRep = mock(MappingsRepresentation.class);

        when(roleMappingResource.getAll()).thenReturn(mappingsRep);
        when(mappingsRep.getRealmMappings()).thenReturn(List.of(role));

        when(userResource.roles()).thenReturn(roleMappingResource);
        when(userResource.groups()).thenReturn(List.of(group));
        when(userResource.toRepresentation()).thenReturn(userRep);

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId.toString())).thenReturn(userResource);

        UserResponse response = userService.getUserById(userId);

        assertEquals("Vova", response.getFirstName());
        assertEquals("Paschenya", response.getLastName());
        assertEquals("vova@gmail.com", response.getEmail());
        assertTrue(response.getRoles().contains("user-role"));
        assertTrue(response.getGroups().contains("user-group"));
    }

    @Test
    void getUserByIdUserNotFoundThrowsException() {
        UUID userId = UUID.randomUUID();
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId.toString()))
                .thenThrow(new RuntimeException("User not found"));

        BackendResourcesException exception = assertThrows(
                BackendResourcesException.class,
                () -> userService.getUserById(userId)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("User not found"));
    }
}
