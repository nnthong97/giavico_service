package com.giavico;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GiavicoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void formulaCrudContractWorks() throws Exception {
        String response = mockMvc.perform(post("/api/formulas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "drinkName": "Monolith Mango",
                                  "marketDestination": "Vietnam",
                                  "targetBrix": 12.5,
                                  "isAcidified": true,
                                  "regionalRestrictions": ["Local trial"],
                                  "productionArea": "Pilot line",
                                  "customerSpecification": "Stable mango beverage",
                                  "baselineBOM": "BOM-001",
                                  "ingredients": [
                                    {"rawMaterialKey": "MANGO", "massPercentage": 40.0, "costProjection": 1.20},
                                    {"rawMaterialKey": "WATER", "massPercentage": 60.0, "costProjection": 0.10}
                                  ],
                                  "varianceAnalysis": "Within target.",
                                  "stabilityAlerts": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.savedFormula.uuid").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(response).path("savedFormula").path("uuid").asText();

        mockMvc.perform(get("/api/formulas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(id))
                .andExpect(jsonPath("$.ingredients.length()").value(2));

        mockMvc.perform(get("/api/formulas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(delete("/api/formulas/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void inventoryCrudAndMovementContractsWork() throws Exception {
        String key = "TEST-" + UUID.randomUUID();
        String response = mockMvc.perform(post("/api/inventory/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rawMaterialKey": "%s",
                                  "materialName": "Test puree",
                                  "category": "Fruit",
                                  "supplierName": "Giavico",
                                  "lotNumber": "LOT-1",
                                  "warehouseLocation": "V-3",
                                  "unitOfMeasure": "kg",
                                  "quantityOnHand": 10,
                                  "reorderPoint": 4,
                                  "unitCost": 2.5,
                                  "expirationDate": "2030-12-31",
                                  "status": "ACTIVE"
                                }
                                """.formatted(key)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rawMaterialKey").value(key))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(response).path("uuid").asText();

        mockMvc.perform(post("/api/inventory/items/{id}/movements", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "movementType": "ISSUE",
                                  "quantity": 3,
                                  "allowNegative": false,
                                  "referenceType": "TEST",
                                  "referenceId": "API",
                                  "reason": "Integration test",
                                  "performedBy": "Codex"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultingQuantity").value(7));

        mockMvc.perform(get("/api/inventory/items/by-raw-material-key/{key}", key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityOnHand").value(7));

        mockMvc.perform(get("/api/inventory/items/{id}/movements", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(delete("/api/inventory/items/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void chatHistoryAndProviderStatusContractsWork() throws Exception {
        mockMvc.perform(post("/api/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"user\",\"content\":\"Hello monolith\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("user"));

        mockMvc.perform(get("/api/chat/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(get("/api/chat/account/openai-key/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(true))
                .andExpect(jsonPath("$.provider").value("ollama"))
                .andExpect(jsonPath("$.model").value("test-model"));

        mockMvc.perform(delete("/api/chat/messages"))
                .andExpect(status().isOk());
    }

    @Test
    void rndDocumentCrudAndExportContractsWork() throws Exception {
        mockMvc.perform(get("/api/rnd-documents/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        String response = mockMvc.perform(post("/api/rnd-documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "SAMPLE_REPORT",
                                  "title": "Monolith sample report",
                                  "productName": "Mango drink",
                                  "formulaUuid": null,
                                  "market": "Vietnam",
                                  "owner": "R&D",
                                  "effectiveDate": "2030-01-01",
                                  "fieldValues": {
                                    "sampleCode": "S-001",
                                    "productCode": "P-001",
                                    "productName": "Mango drink",
                                    "flowChart": "Mix, pasteurize, fill"
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode document = objectMapper.readTree(response);
        String id = document.path("uuid").asText();

        mockMvc.perform(get("/api/rnd-documents/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(id));

        mockMvc.perform(get("/api/rnd-documents/{id}/print", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        mockMvc.perform(get("/api/rnd-documents/{id}/pdf", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        mockMvc.perform(put("/api/rnd-documents/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "SAMPLE_REPORT",
                                  "title": "Updated sample report",
                                  "productName": "Mango drink",
                                  "formulaUuid": null,
                                  "market": "Vietnam",
                                  "owner": "R&D",
                                  "effectiveDate": "2030-01-01",
                                  "fieldValues": {
                                    "sampleCode": "S-001",
                                    "productCode": "P-001",
                                    "productName": "Mango drink",
                                    "flowChart": "Mix, inspect, pasteurize, fill"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated sample report"));

        mockMvc.perform(delete("/api/rnd-documents/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void processRunPersistsStepStateAndFreezesCompletedData() throws Exception {
        String response = mockMvc.perform(post("/api/process-runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workflowId": "general",
                                  "title": "General process",
                                  "owner": "R&D User",
                                  "currentStepId": "email",
                                  "status": "ACTIVE",
                                  "stepStatuses": {"email": "active", "extract": "pending"},
                                  "stepData": {"email": {"subject": "Yuzu request"}},
                                  "documentRecords": {},
                                  "activityLog": []
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.stepData.email.subject").value("Yuzu request"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(response).path("uuid").asText();

        mockMvc.perform(get("/api/process-runs/latest").param("workflowId", "general"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(id));

        mockMvc.perform(get("/api/process-runs/latest"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required query parameter: workflowId."))
                .andExpect(jsonPath("$.fieldErrors.workflowId").value("Required query parameter is missing."));

        String completed = """
                {
                  "workflowId": "general",
                  "title": "General process",
                  "owner": "R&D User",
                  "currentStepId": "extract",
                  "status": "ACTIVE",
                  "stepStatuses": {"email": "done", "extract": "done"},
                  "stepData": {"email": {"subject": "Yuzu request"}, "extract": {"brix": "11.2"}},
                  "documentRecords": {"email-request": {"title": "Yuzu request"}},
                  "activityLog": [{"title": "Completed", "detail": "All steps done"}]
                }
                """;

        mockMvc.perform(post("/api/process-runs/{id}/complete", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completed))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").isNotEmpty())
                .andExpect(jsonPath("$.documentRecords.email-request.title").value("Yuzu request"));

        mockMvc.perform(put("/api/process-runs/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completed))
                .andExpect(status().isBadRequest());
    }
}
