import { percentageToApiFraction, toApiDecimal } from '../../lib/decimal';
import type { SimulationRequest } from '../../types/api';

export interface FormValues {
  scenarioName: string;
  referenceDate: string;
  rulesetVersion: string;
  taxPolicyReference: string;
  settlementDate: string;
  baselineTaxDueDate: string;
  initialAvailableCash: string;
  minimumCashReserve: string;
  fixedCostCashOutflow: string;
  netRevenues: string;
  taxableBase: string;
  effectiveTaxRatePercentage: string;
  splitEligiblePercentage: string;
  alreadyExtinguishedTaxAmount: string;
  variableCostCashOutflow: string;
  revenueAdjustableAmount: string;
  fundingRatePercentage: string;
  fundingRatePeriod: 'DAY' | 'MONTH' | 'YEAR';
  dayCountBasis: '30' | '360' | '365';
  incrementalVariableCostPercentage: string;
  incrementalPaymentFeePercentage: string;
  incrementalCommissionPercentage: string;
}

export function buildSimulationRequest(values: FormValues): SimulationRequest {
  if (!values.scenarioName.trim() || !values.rulesetVersion.trim() || !values.taxPolicyReference.trim()) {
    throw new Error('Preencha a identificação, a versão de regras e a referência tributária.');
  }

  const now = crypto.randomUUID?.() ?? Date.now().toString();
  const operationId = `op-${now}`;
  return {
    scenarioId: `web-${now}`,
    referenceDate: values.referenceDate,
    rulesetVersion: values.rulesetVersion.trim(),
    operations: [{
      operationId,
      taxPolicyReference: values.taxPolicyReference.trim(),
      taxableBase: toApiDecimal(values.taxableBase),
      effectiveTaxRate: percentageToApiFraction(values.effectiveTaxRatePercentage),
      alreadyExtinguishedTaxAmount: toApiDecimal(values.alreadyExtinguishedTaxAmount),
      splitEligiblePercentage: percentageToApiFraction(values.splitEligiblePercentage),
      settlementDate: values.settlementDate,
      baselineTaxDueDate: values.baselineTaxDueDate,
      fixedCostCashOutflow: toApiDecimal(values.fixedCostCashOutflow),
      variableCostCashOutflow: toApiDecimal(values.variableCostCashOutflow),
      netRevenues: toApiDecimal(values.netRevenues),
      revenueAdjustableAmount: toApiDecimal(values.revenueAdjustableAmount),
    }],
    financialScenario: {
      fundingRate: percentageToApiFraction(values.fundingRatePercentage),
      fundingRatePeriod: values.fundingRatePeriod,
      dayCountBasis: Number(values.dayCountBasis) as 30 | 360 | 365,
      initialAvailableCash: toApiDecimal(values.initialAvailableCash),
      minimumCashReserve: toApiDecimal(values.minimumCashReserve),
      incrementalVariableCostPercentage: percentageToApiFraction(values.incrementalVariableCostPercentage),
      incrementalPaymentFeePercentage: percentageToApiFraction(values.incrementalPaymentFeePercentage),
      incrementalCommissionPercentage: percentageToApiFraction(values.incrementalCommissionPercentage),
    },
  };
}
