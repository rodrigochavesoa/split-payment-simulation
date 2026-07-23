package br.com.splitpayment.api;

import br.com.splitpayment.decision.DecisionEngine;
import br.com.splitpayment.finance.FinanceEngine;
import br.com.splitpayment.tax.TaxEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {
    @Bean
    TaxEngine taxEngine() {
        return new TaxEngine();
    }

    @Bean
    FinanceEngine financeEngine() {
        return new FinanceEngine();
    }

    @Bean
    DecisionEngine decisionEngine() {
        return new DecisionEngine();
    }
}
