import type { ApiErrorBody, SimulationRequest, SimulationResponse } from '../types/api';

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '';
const endpoint = `${apiBaseUrl}/api/v1/simulations/float-impact`;

export class SimulationApiError extends Error {
  constructor(public readonly body: ApiErrorBody, public readonly status: number) {
    super(body.errorCode);
  }
}

export async function simulate(request: SimulationRequest, signal?: AbortSignal): Promise<SimulationResponse> {
  const response = await fetch(endpoint, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify(request),
    signal,
  });

  if (!response.ok) {
    const body = (await response.json().catch(() => null)) as ApiErrorBody | null;
    throw new SimulationApiError(
      body ?? { status: response.status, errorCode: 'HTTP-ERROR', errors: [] },
      response.status,
    );
  }
  return response.json() as Promise<SimulationResponse>;
}
