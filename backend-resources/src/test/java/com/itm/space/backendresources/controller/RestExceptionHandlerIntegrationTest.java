package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.exception.BackendResourcesException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(RestExceptionHandlerIntegrationTest.TestController.class) // Регистрируем контроллер
public class RestExceptionHandlerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void handleBackendResourcesExceptionReturnsResponse() throws Exception {
        mvc.perform(get("/api/trigger-exception"))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertEquals("Test exception", result.getResponse().getContentAsString()))
                .andExpect(result ->
                        assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus()));
    }

    @RestController
    static class TestController { // Делаем статическим
        @GetMapping("/api/trigger-exception")
        public void triggerException() {
            throw new BackendResourcesException("Test exception", HttpStatus.NOT_FOUND);
        }
    }
}
