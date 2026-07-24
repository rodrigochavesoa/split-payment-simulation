import { useId } from 'react';
import type { FormValues } from './form';

interface Props {
  values: FormValues;
  disabled: boolean;
  onChange: (field: keyof FormValues, value: string) => void;
  onPreset: (preset: 'confortavel' | 'atencao' | 'critico') => void;
  onSubmit: () => void;
}

const primaryFields: Array<{ field: keyof FormValues; label: string; hint?: string; type?: 'text' | 'date' }> = [
  { field: 'initialAvailableCash', label: 'Caixa inicial', hint: 'R$' },
  { field: 'minimumCashReserve', label: 'Reserva mínima', hint: 'R$' },
  { field: 'fixedCostCashOutflow', label: 'Custo fixo', hint: 'R$' },
  { field: 'netRevenues', label: 'Receita líquida', hint: 'R$' },
  { field: 'effectiveTaxRatePercentage', label: 'Alíquota efetiva', hint: '%' },
];

const advancedFields: Array<{ field: keyof FormValues; label: string; hint?: string; type?: 'text' | 'date' }> = [
  { field: 'scenarioName', label: 'Nome do cenário' },
  { field: 'referenceDate', label: 'Data de referência', type: 'date' },
  { field: 'rulesetVersion', label: 'Versão das regras' },
  { field: 'taxPolicyReference', label: 'Referência tributária' },
  { field: 'taxableBase', label: 'Base tributável', hint: 'R$' },
  { field: 'splitEligiblePercentage', label: 'Elegível ao Split', hint: '%' },
  { field: 'alreadyExtinguishedTaxAmount', label: 'Tributo já extinto', hint: 'R$' },
  { field: 'variableCostCashOutflow', label: 'Custo variável', hint: 'R$' },
  { field: 'revenueAdjustableAmount', label: 'Receita reajustável', hint: 'R$' },
  { field: 'settlementDate', label: 'Data de liquidação', type: 'date' },
  { field: 'baselineTaxDueDate', label: 'Vencimento base', type: 'date' },
  { field: 'fundingRatePercentage', label: 'Taxa de funding', hint: '%' },
  { field: 'incrementalVariableCostPercentage', label: 'Custo variável incremental', hint: '%' },
  { field: 'incrementalPaymentFeePercentage', label: 'Tarifa incremental', hint: '%' },
  { field: 'incrementalCommissionPercentage', label: 'Comissão incremental', hint: '%' },
];

export function SimulationForm({ values, disabled, onChange, onPreset, onSubmit }: Props) {
  const titleId = useId();

  const input = (field: keyof FormValues, label: string, hint?: string, type: 'text' | 'date' = 'text') => (
    <label className="field" key={field}>
      <span>{label}</span>
      <div className="input-wrap">
        {hint && <span aria-hidden="true">{hint}</span>}
        <input
          type={type}
          inputMode={type === 'text' && hint ? 'decimal' : undefined}
          value={values[field]}
          onChange={(event) => onChange(field, event.target.value)}
          disabled={disabled}
          required
        />
      </div>
    </label>
  );

  return (
    <section className="panel form-panel" aria-labelledby={titleId}>
      <header>
        <p className="eyebrow">Entrada</p>
        <h2 id={titleId}>Parâmetros da simulação</h2>
        <p className="muted">Os valores são enviados como texto decimal exato, conforme o contrato da API.</p>
      </header>

      <div className="preset-group" aria-label="Cenários de exemplo">
        <span>Experimente:</span>
        <button type="button" className="secondary" onClick={() => onPreset('confortavel')} disabled={disabled}>Confortável</button>
        <button type="button" className="secondary" onClick={() => onPreset('atencao')} disabled={disabled}>Atenção</button>
        <button type="button" className="secondary" onClick={() => onPreset('critico')} disabled={disabled}>Crítico</button>
      </div>

      <form onSubmit={(event) => { event.preventDefault(); onSubmit(); }} noValidate>
        <div className="field-grid">{primaryFields.map(({ field, label, hint, type }) => input(field, label, hint, type))}</div>

        <details>
          <summary>Configurar todos os parâmetros do contrato</summary>
          <p className="muted details-copy">A API recebe todos estes campos. Os valores iniciais reproduzem o cenário confortável do Quick Start.</p>
          <div className="field-grid">{advancedFields.map(({ field, label, hint, type }) => input(field, label, hint, type))}</div>
          <div className="field-grid compact-grid">
            <label className="field"><span>Período da taxa</span><select value={values.fundingRatePeriod} onChange={(event) => onChange('fundingRatePeriod', event.target.value)} disabled={disabled}><option>DAY</option><option>MONTH</option><option>YEAR</option></select></label>
            <label className="field"><span>Base de dias</span><select value={values.dayCountBasis} onChange={(event) => onChange('dayCountBasis', event.target.value)} disabled={disabled}><option value="30">30</option><option value="360">360</option><option value="365">365</option></select></label>
          </div>
        </details>

        <button className="primary submit" type="submit" disabled={disabled}>
          {disabled ? 'Calculando impacto…' : 'Simular impacto'}
        </button>
      </form>
    </section>
  );
}
