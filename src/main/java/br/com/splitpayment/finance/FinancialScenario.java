package br.com.splitpayment.finance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable calculation policy. fundingRate must be an effective rate for the declared RatePeriod.
 * MONTH uses a 30-day financial month. YEAR uses the configured DayCountBasis.
 */
public record FinancialScenario(
        BigDecimal fundingRate,
        RatePeriod fundingRatePeriod,
        DayCountBasis dayCountBasis,
        BigDecimal initialAvailableCash,
        BigDecimal minimumCashReserve,
        BigDecimal incrementalVariableCostPercentage,
        BigDecimal incrementalPaymentFeePercentage,
        BigDecimal incrementalCommissionPercentage,
        MathContext calculationContext,
        int outputScale,
        RoundingMode outputRoundingMode
) {
    public FinancialScenario {
        Objects.requireNonNull(fundingRate, "fundingRate is required.");
        Objects.requireNonNull(fundingRatePeriod, "fundingRatePeriod is required.");
        Objects.requireNonNull(dayCountBasis, "dayCountBasis is required.");
        Objects.requireNonNull(initialAvailableCash, "initialAvailableCash is required.");
        Objects.requireNonNull(minimumCashReserve, "minimumCashReserve is required.");
        Objects.requireNonNull(incrementalVariableCostPercentage, "incrementalVariableCostPercentage is required.");
        Objects.requireNonNull(incrementalPaymentFeePercentage, "incrementalPaymentFeePercentage is required.");
        Objects.requireNonNull(incrementalCommissionPercentage, "incrementalCommissionPercentage is required.");
        Objects.requireNonNull(calculationContext, "calculationContext is required.");
        Objects.requireNonNull(outputRoundingMode, "outputRoundingMode is required.");
        if (fundingRate.signum() < 0) {
            throw new InvalidFinancialScenarioException("fundingRate cannot be negative.");
        }
        if (initialAvailableCash.signum() < 0 || minimumCashReserve.signum() < 0) {
            throw new InvalidFinancialScenarioException("Cash balances cannot be negative.");
        }
        validatePercentage(incrementalVariableCostPercentage, "incrementalVariableCostPercentage");
        validatePercentage(incrementalPaymentFeePercentage, "incrementalPaymentFeePercentage");
        validatePercentage(incrementalCommissionPercentage, "incrementalCommissionPercentage");
        if (outputScale < 0) {
            throw new InvalidFinancialScenarioException("outputScale cannot be negative.");
        }
    }

    public static FinancialScenario effectiveAnnualRate(
            BigDecimal annualRate,
            DayCountBasis dayCountBasis,
            BigDecimal initialAvailableCash,
            BigDecimal minimumCashReserve,
            BigDecimal incrementalVariableCostPercentage,
            BigDecimal incrementalPaymentFeePercentage,
            BigDecimal incrementalCommissionPercentage
    ) {
        return new FinancialScenario(
                annualRate,
                RatePeriod.YEAR,
                dayCountBasis,
                initialAvailableCash,
                minimumCashReserve,
                incrementalVariableCostPercentage,
                incrementalPaymentFeePercentage,
                incrementalCommissionPercentage,
                MathContext.DECIMAL128,
                4,
                RoundingMode.HALF_EVEN
        );
    }

    private static void validatePercentage(BigDecimal value, String field) {
        if (value.signum() < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw new InvalidFinancialScenarioException(field + " must be between 0 and 1.");
        }
    }
}
