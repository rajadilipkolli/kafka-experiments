package com.example.outboxpattern.order.internal;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrderController.class, properties = "application.max-item-count=2")
class OrderRequestValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void configuredLimitRejectsOversizedCreateAndUpdate(boolean update) throws Exception {
        mockMvc.perform((update ? put("/api/orders/1") : post("/api/orders"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemsList\":[{}, {}, {}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations[0].field").value("itemsList"))
                .andExpect(jsonPath("$.violations[0].message").value("ItemsList must not exceed 2 items"));
        verifyNoInteractions(orderService);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void emptyItemsAreStillRejected(boolean update) throws Exception {
        mockMvc.perform((update ? put("/api/orders/1") : post("/api/orders"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemsList\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations[0].message").value("ItemsList must not be empty"));
        verifyNoInteractions(orderService);
    }
}
