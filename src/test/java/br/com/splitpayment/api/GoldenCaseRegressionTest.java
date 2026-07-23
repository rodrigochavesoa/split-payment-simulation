package br.com.splitpayment.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Locked financial signature for the current ruleset.
 * Any change to these values requires an explicit business-rule review.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GoldenCaseRegressionTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void preservesGoldenMultiOperationCashGapAndPricingResult() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<SimulationResponse> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/simulations/float-impact",
                HttpMethod.POST,
                new HttpEntity<>(goldenPayload(), headers),
                SimulationResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        SimulationResponse result = response.getBody();
        assertEquals("golden-2026-001", result.scenarioId());
        assertEquals("v1.0.0-EC132", result.auditTrail().rulesetVersion());
        assertEquals(2, result.auditTrail().operations().size());
        assertEquals("LC214/2025-Art31", result.auditTrail().operations().getFirst().taxPolicyReference());

        assertEquals("2800.0000", result.taxResult().grossTaxDebit());
        assertEquals("2800.0000", result.taxResult().splitEligibleAmount());
        assertEquals("2800.0000", result.taxResult().simulatedSplitWithheldAmount());

        FinancialImpactDto financial = result.financialImpact();
        assertEquals("7000.0000", financial.totalNetRevenues());
        assertEquals("500.0000", financial.totalFixedCostCashOutflow());
        assertEquals("0.0000", financial.totalVariableCostCashOutflow());
        assertEquals("0.0000", financial.netFinancialImpact());
        assertEquals("0.0000", financial.taxFloatLossCost());
        assertEquals("0.0000", financial.taxPaymentPostponementBenefit());
        assertEquals("14.0000", financial.operationalFixedObligationCoverageIndex());
        assertEquals("14.0000", financial.liquidityFixedObligationCoverageIndex());
        assertEquals("100.0000", financial.minimumBaselineProjectedCashBalance());
        assertEquals("-1200.0000", financial.minimumSplitProjectedCashBalance());
        assertEquals("1300.0000", financial.cashGap());
        assertEquals("0.5417", financial.estimatedPriceAdjustmentPercentage());

        assertEquals("ALERTA_CRITICO", result.decisionResult().readinessStatus());
        assertEquals("ALTO", result.decisionResult().riskLevel());
    }

    private static String goldenPayload() {
        return """
                {
                  "scenarioId": "golden-2026-001",
                  "referenceDate": "2026-07-23",
                  "rulesetVersion": "v1.0.0-EC132",
                  "operations": [
                    {
                      "operationId": "golden-operation-001",
                      "taxPolicyReference": "LC214/2025-Art31",
                      "taxableBase": "10000.00",
                      "effectiveTaxRate": "0.28",
                      "alreadyExtinguishedTaxAmount": "0.00",
                      "splitEligiblePercentage": "1.00",
                      "settlementDate": "2026-01-01",
                      "baselineTaxDueDate": "2026-01-03",
                      "fixedCostCashOutflow": "500.00",
                      "variableCostCashOutflow": "0.00",
                      "netRevenues": "2000.00",
                      "revenueAdjustableAmount": "3000.00"
                    },
                    {
                      "operationId": "golden-operation-002",
                      "taxPolicyReference": "LC214/2025-Art31",
                      "taxableBase": "0.00",
                      "effectiveTaxRate": "0.00",
                      "alreadyExtinguishedTaxAmount": "0.00",
                      "splitEligiblePercentage": "1.00",
                      "settlementDate": "2026-01-02",
                      "baselineTaxDueDate": "2026-01-02",
                      "fixedCostCashOutflow": "0.00",
                      "variableCostCashOutflow": "0.00",
                      "netRevenues": "5000.00",
                      "revenueAdjustableAmount": "0.00"
                    }
                  ],
                  "financialScenario": {
                    "fundingRate": "0.00",
                    "fundingRatePeriod": "YEAR",
                    "dayCountBasis": 365,
                    "initialAvailableCash": "100.00",
                    "minimumCashReserve": "100.00",
                    "incrementalVariableCostPercentage": "0.10",
                    "incrementalPaymentFeePercentage": "0.05",
                    "incrementalCommissionPercentage": "0.05"
                  }
                }
                """;
    }
}
