package com.fudn.gateway;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
        "services.product.url=http://localhost:${wiremock.server.port}",
        "services.order.url=http://localhost:${wiremock.server.port}",
        "services.inventory.url=http://localhost:${wiremock.server.port}"
})
class ApiGatewayApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void requestShouldBeRoutedToProductService() throws Exception {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/products"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"1\",\"name\":\"iPhone 15\",\"price\":1000}]")));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("iPhone 15")));

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/api/products")));
    }

    @Test
    void postOrderShouldBeRoutedToOrderService() throws Exception {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/api/order"))
                .willReturn(WireMock.aResponse()
                        .withStatus(201)
                        .withBody("Order Placed Successfully")));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skuCode\":\"iphone_15\",\"price\":1000,\"quantity\":1}"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Order Placed Successfully"));

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/api/order")));
    }

    @Test
    void inventoryRouteShouldForwardQueryParams() throws Exception {
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/inventory"))
                .withQueryParam("skuCode", WireMock.equalTo("iphone_15"))
                .withQueryParam("quantity", WireMock.equalTo("1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("true")));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/inventory")
                        .param("skuCode", "iphone_15")
                        .param("quantity", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/inventory"))
                .withQueryParam("skuCode", WireMock.equalTo("iphone_15"))
                .withQueryParam("quantity", WireMock.equalTo("1")));
    }

    @Test
    void unmappedRouteShouldReturn404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/non-existent-route"))
                .andExpect(status().isNotFound());
    }
}
