package br.com.splitpayment.finance;

import java.math.BigDecimal;
import java.math.MathContext;

final class BigDecimalMath {
    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private BigDecimalMath() {
    }

    /** Newton-Raphson nth root using only BigDecimal operations. */
    static BigDecimal nthRoot(BigDecimal value, int n, MathContext context) {
        if (n <= 0) {
            throw new InvalidFinancialScenarioException("Root degree must be positive.");
        }
        if (value.signum() < 0) {
            throw new InvalidFinancialScenarioException("Cannot calculate a real root of a negative value.");
        }
        if (value.signum() == 0 || value.compareTo(BigDecimal.ONE) == 0) {
            return value;
        }

        BigDecimal nDecimal = BigDecimal.valueOf(n);
        BigDecimal estimate = value.compareTo(BigDecimal.ONE) > 0 ? value : BigDecimal.ONE;
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-(context.getPrecision() - 2));

        for (int iteration = 0; iteration < context.getPrecision() * 4; iteration++) {
            BigDecimal denominator = estimate.pow(n - 1, context);
            BigDecimal next = estimate.multiply(BigDecimal.valueOf(n - 1), context)
                    .add(value.divide(denominator, context), context)
                    .divide(nDecimal, context);

            if (next.subtract(estimate, context).abs().compareTo(tolerance) <= 0) {
                return next;
            }
            estimate = next;
        }
        return estimate;
    }
}
