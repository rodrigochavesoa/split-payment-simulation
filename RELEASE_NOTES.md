# Release Notes — split-payment-simulation 0.2.0

## Release 0.2.0

**Status:** pronta para validação de adoção da V1.0 com dashboard web e pipeline de segurança reforçado.

## Entregas incluídas nesta versão

### Dashboard web (V1.0)

- SPA React + TypeScript + Vite em `frontend/`, independente do serviço Spring.
- Formulário com presets **Confortável**, **Atenção** e **Crítico** alinhados ao Quick Start.
- Consumo exclusivo de `POST /api/v1/simulations/float-impact` — sem recálculo de fórmulas no navegador.
- Proxy Vite em desenvolvimento; produção via mesma origem ou `VITE_API_BASE_URL`.

### Documentação e onboarding

- `QUICKSTART_V1.md` com três cenários práticos de risco.
- README raiz e `frontend/README.md` com passo a passo de ativação e screenshots.

### DevSecOps e CI

- Job **Frontend build** (`npm ci` + `npm run build`) no GitHub Actions.
- CodeQL SAST para **Java** e **JavaScript/TypeScript**.
- OWASP Dependency-Check com cache e suporte opcional a `NVD_API_KEY`.
- Dependabot configurado para ignorar bump major de Spring Boot.

### Infraestrutura de release

- Imagem Docker da API publicada em `ghcr.io/rodrigochavesoa/split-payment-simulation` com tags `:latest`, `:v0.2.0` e SHA do commit.
- Dockerfile corrigido (Maven no build + runtime Distroless).

## Herança da Release 0.1.0 (motor Java)

Esta versão **mantém integralmente** o núcleo entregue em 0.1.0:

- Tax Engine, Finance Engine e Decision Engine determinísticos.
- Golden Case, testes E2E e contrato de precisão decimal (`BigDecimal`, strings JSON).
- Limitações de escopo: simulador parametrizado, sem cálculo fiscal legal automatizado.

Consulte `ARCHITECTURE.md` e as notas de limitação da V1.0 para o detalhe completo das regras financeiras.

## Limitações conhecidas (0.2.0)

- A imagem GHCR empacota **somente a API Java**; o dashboard é publicado separadamente (`frontend/dist/`).
- Sandbox V1.0 continua **sem autenticação** por design.
- O dashboard não substitui validação contábil, jurídica, tributária ou financeira especializada.

## Orientação de adoção

1. API: `docker pull ghcr.io/rodrigochavesoa/split-payment-simulation:v0.2.0` (ou `:latest`).
2. Dashboard: `cd frontend && npm ci && npm run build` — sirva `dist/` com proxy de `/api` para o Spring.
3. Desenvolvimento local: ver README raiz (dois terminais — `mvn spring-boot:run` + `npm run dev`).

## Sign-off

Release 0.2.0 consolida o marco de produto **API Sandbox + Dashboard V1.0**, com pipeline CI/CD e segurança alinhados ao uso público do repositório.
