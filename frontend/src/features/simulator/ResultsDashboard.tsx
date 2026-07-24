import { formatCurrency, formatNumber, formatPercentage } from '../../lib/decimal';
import type { ReadinessStatus, SimulationResponse } from '../../types/api';

const statusContent: Record<ReadinessStatus, { label: string; tone: string }> = {
  CONFORTAVEL: { label: 'Operação confortável', tone: 'success' },
  ZONA_DE_ATENCAO: { label: 'Zona de atenção', tone: 'warning' },
  ALERTA_CRITICO: { label: 'Alerta crítico', tone: 'danger' },
};

export function ResultsDashboard({ result, isLoading }: { result: SimulationResponse | null; isLoading: boolean }) {
  if (!result) {
    return <section className="panel empty-state" aria-live="polite"><div className="empty-icon" aria-hidden="true">▥</div><h2>{isLoading ? 'Calculando a simulação…' : 'Pronto para analisar'}</h2><p>{isLoading ? 'O motor financeiro está avaliando o cenário informado.' : 'Preencha os parâmetros e execute uma simulação para transformar os números da API em uma leitura financeira clara.'}</p></section>;
  }

  const { financialImpact, taxResult, decisionResult } = result;
  const status = statusContent[decisionResult.readinessStatus] ?? { label: decisionResult.readinessStatus, tone: 'neutral' };
  const metrics = [
    ['Valor retido pelo Split', formatCurrency(taxResult.simulatedSplitWithheldAmount)],
    ['Pior saldo projetado', formatCurrency(financialImpact.minimumSplitProjectedCashBalance)],
    ['Cash gap', formatCurrency(financialImpact.cashGap)],
    ['Reajuste estimado', formatPercentage(financialImpact.estimatedPriceAdjustmentPercentage)],
  ];

  return <section className="panel results" aria-live="polite" aria-label="Resultado da simulação">
    <div className={`status ${status.tone}`}><div><p className="eyebrow">{decisionResult.riskLevel} risco</p><h2>{status.label}</h2><p>{decisionResult.analyticalMessage}</p></div></div>
    <div className="result-body">
      <div className="main-metric"><p>ICOF de liquidez</p><strong>{formatNumber(financialImpact.liquidityFixedObligationCoverageIndex)}</strong><span>Índice de cobertura das obrigações fixas</span></div>
      <div className="metric-grid">{metrics.map(([label, value]) => <article className="metric" key={label}><p>{label}</p><strong>{value}</strong></article>)}</div>
      <details className="details-result"><summary>Ver memória de cálculo retornada pela API</summary><dl><div><dt>Débito tributário bruto</dt><dd>{formatCurrency(taxResult.grossTaxDebit)}</dd></div><div><dt>Saldo mínimo sem Split</dt><dd>{formatCurrency(financialImpact.minimumBaselineProjectedCashBalance)}</dd></div><div><dt>Impacto financeiro líquido</dt><dd>{formatCurrency(financialImpact.netFinancialImpact)}</dd></div><div><dt>ICOF operacional</dt><dd>{formatNumber(financialImpact.operationalFixedObligationCoverageIndex)}</dd></div></dl></details>
    </div>
  </section>;
}
