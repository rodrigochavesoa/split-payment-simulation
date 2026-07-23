package br.com.splitpayment.decision;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DecisionEngineTest {
    private final DecisionEngine engine = new DecisionEngine();

    // Given each ICOF band and each projected-cash band, when assessed,
    // then the Discovery decision matrix assigns the expected status and risk.
    @ParameterizedTest
    @MethodSource("riskMatrix")
    void classifiesCompleteRiskMatrix(
            String icof,
            String minimumBalance,
            ReadinessStatus expectedStatus,
            RiskLevel expectedRisk
    ) {
        DecisionResult result = engine.evaluate(input(icof, minimumBalance, "100.00"));

        assertEquals(expectedStatus, result.readinessStatus());
        assertEquals(expectedRisk, result.riskLevel());
    }

    private static Stream<Arguments> riskMatrix() {
        return Stream.of(
                Arguments.of("0.99", "-1.00", ReadinessStatus.ALERTA_CRITICO, RiskLevel.ALTO),
                Arguments.of("0.99", "50.00", ReadinessStatus.ALERTA_CRITICO, RiskLevel.ALTO),
                Arguments.of("0.99", "100.00", ReadinessStatus.ALERTA_CRITICO, RiskLevel.ALTO),
                Arguments.of("1.10", "-1.00", ReadinessStatus.ALERTA_CRITICO, RiskLevel.ALTO),
                Arguments.of("1.10", "50.00", ReadinessStatus.ZONA_DE_ATENCAO, RiskLevel.MEDIO),
                Arguments.of("1.10", "100.00", ReadinessStatus.ZONA_DE_ATENCAO, RiskLevel.MEDIO),
                Arguments.of("1.30", "-1.00", ReadinessStatus.ALERTA_CRITICO, RiskLevel.ALTO),
                Arguments.of("1.30", "50.00", ReadinessStatus.ZONA_DE_ATENCAO, RiskLevel.MEDIO),
                Arguments.of("1.30", "100.00", ReadinessStatus.CONFORTAVEL, RiskLevel.BAIXO)
        );
    }

    private static DecisionInput input(String icof, String minimumBalance, String reserve) {
        return new DecisionInput(
                new BigDecimal(icof),
                new BigDecimal(minimumBalance),
                new BigDecimal(reserve)
        );
    }
}
