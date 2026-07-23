package br.com.splitpayment.decision;

import java.math.BigDecimal;
import java.util.Objects;

/** Stateless pure Domain Service for the complete liquidity and cash-balance risk matrix. */
public final class DecisionEngine {
    private static final BigDecimal ATTENTION_THRESHOLD = new BigDecimal("1.00");
    private static final BigDecimal COMFORTABLE_THRESHOLD = new BigDecimal("1.30");

    public DecisionResult evaluate(DecisionInput input) {
        Objects.requireNonNull(input, "input is required.");
        BigDecimal coverage = input.liquidityFixedObligationCoverageIndex();
        BigDecimal minimumCashBalance = input.minimumProjectedCashBalance();
        BigDecimal minimumCashReserve = input.minimumCashReserve();

        boolean insufficientCoverage = coverage.compareTo(ATTENTION_THRESHOLD) < 0;
        boolean cashRupture = minimumCashBalance.signum() < 0;
        boolean belowReserve = minimumCashBalance.compareTo(minimumCashReserve) < 0;

        if (insufficientCoverage || cashRupture) {
            return new DecisionResult(
                    ReadinessStatus.ALERTA_CRITICO,
                    RiskLevel.ALTO,
                    criticalMessage(insufficientCoverage, cashRupture)
            );
        }

        if (coverage.compareTo(COMFORTABLE_THRESHOLD) < 0 || belowReserve) {
            return new DecisionResult(
                    ReadinessStatus.ZONA_DE_ATENCAO,
                    RiskLevel.MEDIO,
                    attentionMessage(coverage.compareTo(COMFORTABLE_THRESHOLD) < 0, belowReserve)
            );
        }

        return new DecisionResult(
                ReadinessStatus.CONFORTAVEL,
                RiskLevel.BAIXO,
                "ICOF de liquidez saudável e menor saldo de caixa projetado igual ou superior à reserva mínima."
        );
    }

    private String criticalMessage(boolean insufficientCoverage, boolean cashRupture) {
        if (insufficientCoverage && cashRupture) {
            return "ICOF de liquidez insuficiente e ruptura de caixa projetada no cenário split.";
        }
        if (cashRupture) {
            return "ICOF de liquidez saudável, mas ruptura de caixa projetada no cenário split.";
        }
        return "ICOF de liquidez inferior a 1,00 no cenário simulado.";
    }

    private String attentionMessage(boolean coverageInAttentionRange, boolean belowReserve) {
        if (coverageInAttentionRange && belowReserve) {
            return "ICOF de liquidez em zona de atenção e menor saldo de caixa abaixo da reserva mínima.";
        }
        if (belowReserve) {
            return "ICOF de liquidez saudável, mas menor saldo de caixa abaixo da reserva mínima.";
        }
        return "ICOF de liquidez entre 1,00 e 1,30 no cenário simulado.";
    }
}
