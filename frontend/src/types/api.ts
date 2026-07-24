export type ReadinessStatus = 'CONFORTAVEL' | 'ZONA_DE_ATENCAO' | 'ALERTA_CRITICO';

export interface SimulationRequest {
  scenarioId: string;
  referenceDate: string;
  rulesetVersion: string;
  operations: Operation[];
  financialScenario: FinancialScenario;
}

export interface Operation {
  operationId: string;
  taxPolicyReference: string;
  taxableBase: string;
  effectiveTaxRate: string;
  alreadyExtinguishedTaxAmount: string;
  splitEligiblePercentage: string;
  settlementDate: string;
  baselineTaxDueDate: string;
  fixedCostCashOutflow: string;
  variableCostCashOutflow: string;
  netRevenues: string;
  revenueAdjustableAmount: string;
}

export interface FinancialScenario {
  fundingRate: string;
  fundingRatePeriod: 'DAY' | 'MONTH' | 'YEAR';
  dayCountBasis: 30 | 360 | 365;
  initialAvailableCash: string;
  minimumCashReserve: string;
  incrementalVariableCostPercentage: string;
  incrementalPaymentFeePercentage: string;
  incrementalCommissionPercentage: string;
}

export interface SimulationResponse {
  scenarioId: string;
  referenceDate: string;
  taxResult: TaxResult;
  financialImpact: FinancialImpact;
  decisionResult: DecisionResult;
}

export interface TaxResult {
  grossTaxDebit: string;
  splitEligibleAmount: string;
  simulatedSplitWithheldAmount: string;
}

export interface FinancialImpact {
  totalNetRevenues: string;
  totalFixedCostCashOutflow: string;
  totalVariableCostCashOutflow: string;
  netFinancialImpact: string;
  taxFloatLossCost: string;
  taxPaymentPostponementBenefit: string;
  operationalFixedObligationCoverageIndex: string;
  liquidityFixedObligationCoverageIndex: string;
  minimumBaselineProjectedCashBalance: string;
  minimumSplitProjectedCashBalance: string;
  cashGap: string;
  estimatedPriceAdjustmentPercentage: string;
}

export interface DecisionResult {
  readinessStatus: ReadinessStatus;
  riskLevel: 'BAIXO' | 'MEDIO' | 'ALTO';
  analyticalMessage: string;
}

export interface ApiErrorBody {
  status: number;
  errorCode: string;
  errors: Array<{ field: string; message: string }>;
}
