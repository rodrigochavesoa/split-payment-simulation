package br.com.splitpayment.api;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import br.com.splitpayment.finance.AdjustmentNotCalculableException;
import br.com.splitpayment.finance.FixedCostCoverageNotApplicableException;
import br.com.splitpayment.finance.UnviablePricingModelException;

@WebMvcTest(SimulationController.class)
@Import(GlobalExceptionHandler.class)
class SimulationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SimulationOrchestratorService orchestratorService;

    @Test
    void returnsOkForPerfectPayload() throws Exception {
        when(orchestratorService.simulate(any(SimulationRequest.class))).thenReturn(response());

        mockMvc.perform(post("/api/v1/simulations/float-impact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scenarioId").value("scenario-001"))
                .andExpect(jsonPath("$.taxResult.simulatedSplitWithheldAmount").value("280.0000"))
                .andExpect(jsonPath("$.decisionResult.readinessStatus").value("CONFORTAVEL"));
    }

    @Test
    void returnsUnprocessableEntityWithFin001WhenFixedCostCoverageIsNotApplicable() throws Exception {
        when(orchestratorService.simulate(any(SimulationRequest.class)))
                .thenThrow(new FixedCostCoverageNotApplicableException());

        mockMvc.perform(post("/api/v1/simulations/float-impact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.errorCode").value("FIN-001"));
    }

    @Test
    void returnsUnprocessableEntityWithFin002WhenAdjustmentCannotBeCalculated() throws Exception {
        when(orchestratorService.simulate(any(SimulationRequest.class)))
                .thenThrow(new AdjustmentNotCalculableException());

        mockMvc.perform(post("/api/v1/simulations/float-impact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("FIN-002"));
    }

    @Test
    void returnsUnprocessableEntityWithFin003WhenPricingModelIsUnviable() throws Exception {
        when(orchestratorService.simulate(any(SimulationRequest.class)))
                .thenThrow(new UnviablePricingModelException());

        mockMvc.perform(post("/api/v1/simulations/float-impact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("FIN-003"));
    }

    @Test
    void returnsBadRequestWhenMonetaryStringUsesComma() throws Exception {
        mockMvc.perform(post("/api/v1/simulations/float-impact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload().replace("\"1000.00\"", "\"1,25\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[*].field", hasItem("operations[0].taxableBase")));
    }

    @Test
    void returnsBadRequestWhenMonetaryStringContainsLetters() throws Exception {
        mockMvc.perform(post("/api/v1/simulations/float-impact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload().replace("\"1000.00\"", "\"ABC\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[*].field", hasItem("operations[0].taxableBase")));
    }

    @Test
    void returnsBadRequestWhenRequiredFieldsAreNull() throws Exception {
        String payload = """
                {
                  "scenarioId": null,
                  "referenceDate": "2026-07-23",
                  "operation": null,
                  "financialScenario": null
                }
                """;

        mockMvc.perform(post("/api/v1/simulations/float-impact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[*].field", hasItem("scenarioId")));
    }

    private static SimulationResponse response() {
        return new SimulationResponse(
                "scenario-001",
                "2026-07-23",
                new SimulationAuditDto(
                        "v1.0.0-EC132",
                        java.util.List.of(new OperationAuditDto("operation-001", "LC214/2025-Art31"))
                ),
                new TaxResultDto("280.0000", "280.0000", "280.0000"),
                new FinancialImpactDto(
                        "2000.0000", "1000.0000", "500.0000",
                        "9.8630", "9.8630", "0.0000", "1.5000", "1.7000",
                        "1000.0000", "900.0000", "0.0000", "0.0000"
                ),
                new DecisionResultDto("CONFORTAVEL", "BAIXO", "Cobertura suficiente.")
        );
    }

    private static String validPayload() {
        return """
                {
                  "scenarioId": "scenario-001",
                  "referenceDate": "2026-07-23",
                  "rulesetVersion": "v1.0.0-EC132",
                  "operations": [ {
                    "operationId": "operation-001",
                    "taxPolicyReference": "LC214/2025-Art31",
                    "taxableBase": "1000.00",
                    "effectiveTaxRate": "0.28",
                    "alreadyExtinguishedTaxAmount": "0.00",
                    "splitEligiblePercentage": "1.00",
                    "settlementDate": "2026-01-01",
                    "baselineTaxDueDate": "2026-01-31",
                    "fixedCostCashOutflow": "1000.00",
                    "variableCostCashOutflow": "500.00",
                    "netRevenues": "2000.00",
                    "revenueAdjustableAmount": "2000.00"
                  } ],
                  "financialScenario": {
                    "fundingRate": "0.365",
                    "fundingRatePeriod": "YEAR",
                    "dayCountBasis": 365,
                    "initialAvailableCash": "1000.00",
                    "minimumCashReserve": "100.00",
                    "incrementalVariableCostPercentage": "0.10",
                    "incrementalPaymentFeePercentage": "0.05",
                    "incrementalCommissionPercentage": "0.05"
                  }
                }
                """;
    }
}
