import type { FormValues } from '../features/simulator/form';

export const initialFormValues: FormValues = {
  scenarioName: 'Cenário web',
  referenceDate: new Date().toLocaleDateString('en-CA'),
  rulesetVersion: 'v1.0.0-EC132',
  taxPolicyReference: 'LC214/2025-Art31',
  settlementDate: '2026-01-01',
  baselineTaxDueDate: '2026-01-01',
  initialAvailableCash: '5000,00',
  minimumCashReserve: '500,00',
  fixedCostCashOutflow: '500,00',
  netRevenues: '8000,00',
  taxableBase: '10000,00',
  effectiveTaxRatePercentage: '28',
  splitEligiblePercentage: '100',
  alreadyExtinguishedTaxAmount: '0',
  variableCostCashOutflow: '0',
  revenueAdjustableAmount: '8000,00',
  fundingRatePercentage: '0',
  fundingRatePeriod: 'YEAR',
  dayCountBasis: '365',
  incrementalVariableCostPercentage: '10',
  incrementalPaymentFeePercentage: '5',
  incrementalCommissionPercentage: '5',
};

export const presets: Record<'confortavel' | 'atencao' | 'critico', FormValues> = {
  confortavel: initialFormValues,
  atencao: {
    ...initialFormValues,
    scenarioName: 'Cenário de atenção',
    minimumCashReserve: '2000,00',
    fixedCostCashOutflow: '5000,00',
    netRevenues: '3000,00',
    revenueAdjustableAmount: '3000,00',
  },
  critico: {
    ...initialFormValues,
    scenarioName: 'Cenário crítico',
    initialAvailableCash: '100,00',
    minimumCashReserve: '100,00',
    netRevenues: '2000,00',
    revenueAdjustableAmount: '3000,00',
    baselineTaxDueDate: '2026-01-03',
  },
};
