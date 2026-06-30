package com.giavico.planning.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NdPlanningManagementApiTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void overviewReturnsNdPlanningManagementContract() throws Exception {
        mockMvc.perform(get("/api/planning/nd/management"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productLine").value("ND"))
                .andExpect(jsonPath("$.orders", hasSize(4)))
                .andExpect(jsonPath("$.requirements", hasSize(4)))
                .andExpect(jsonPath("$.stock", hasSize(4)))
                .andExpect(jsonPath("$.actions.length()", greaterThan(0)))
                .andExpect(jsonPath("$.importWorkflow", hasSize(4)))
                .andExpect(jsonPath("$.stats.activeOrders").value(4))
                .andExpect(jsonPath("$.stats.urgentOrders").value(1));
    }

    @Test
    void requirementsCanBeFilteredByOrderAndIncludeBackwardSchedule() throws Exception {
        mockMvc.perform(get("/api/planning/nd/requirements").param("orderId", "nd-mgmt-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderCode").value("PTE-26020032-002"))
                .andExpect(jsonPath("$[0].rawNeedKg").value(88096))
                .andExpect(jsonPath("$[0].productionDays").value(7))
                .andExpect(jsonPath("$[0].stages", hasSize(7)))
                .andExpect(jsonPath("$[0].stages[0].key").value("inoculation"))
                .andExpect(jsonPath("$[0].stages[6].key").value("qa"));
    }

    @Test
    void stockAndActionEndpointsExposeNdReadiness() throws Exception {
        mockMvc.perform(get("/api/planning/nd/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("ND-RAW-CULTURE"))
                .andExpect(jsonPath("$[0].totalQty").value(62500));

        mockMvc.perform(get("/api/planning/nd/actions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThan(0)))
                .andExpect(jsonPath("$[0].level").exists())
                .andExpect(jsonPath("$[0].titleVi").exists());
    }
}
