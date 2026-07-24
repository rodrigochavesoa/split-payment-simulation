import { useState } from 'react';
import { simulate, SimulationApiError } from './api/simulation-client';
import { initialFormValues, presets } from './data/presets';
import { ResultsDashboard } from './features/simulator/ResultsDashboard';
import { SimulationForm } from './features/simulator/SimulationForm';
import { buildSimulationRequest, type FormValues } from './features/simulator/form';
import type { SimulationResponse } from './types/api';

export default function App() {
  const [form, setForm] = useState<FormValues>(initialFormValues);
  const [result, setResult] = useState<SimulationResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const update = (field: keyof FormValues, value: string) => setForm((current) => ({ ...current, [field]: value }));
  const choosePreset = (preset: keyof typeof presets) => { setForm({ ...presets[preset] }); setError(null); };

  async function submit() {
    setError(null);
    try {
      const request = buildSimulationRequest(form);
      setIsLoading(true);
      setResult(await simulate(request));
    } catch (cause) {
      if (cause instanceof SimulationApiError) {
        const details = cause.body.errors.map((item) => `${item.field}: ${item.message}`).join(' · ');
        setError(`${cause.body.errorCode}${details ? ` — ${details}` : ''}`);
      } else if (cause instanceof Error) {
        setError(cause.message === 'Failed to fetch' ? 'Não foi possível conectar à API. Confirme que ela está em execução.' : cause.message);
      } else {
        setError('Não foi possível concluir a simulação. Tente novamente.');
      }
    } finally {
      setIsLoading(false);
    }
  }

  return <><header className="app-header"><div><p className="eyebrow">Split Payment · V1</p><h1>Simulador de impacto de caixa</h1></div><span className="api-chip">API como fonte de verdade</span></header><main className="app-main">{error && <div className="error" role="alert"><strong>Não foi possível simular.</strong> {error}</div>}<div className="workspace"><SimulationForm values={form} disabled={isLoading} onChange={update} onPreset={choosePreset} onSubmit={submit} /><ResultsDashboard result={result} isLoading={isLoading} /></div><p className="disclaimer">Simulação parametrizada: não é cálculo fiscal oficial nem aconselhamento financeiro, contábil ou jurídico.</p></main></>;
}
