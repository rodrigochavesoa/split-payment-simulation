# Changelog

Todas as mudanças relevantes deste projeto serão documentadas neste arquivo.

O formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e o versionamento segue [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [Unreleased]

## [0.2.0] - 2026-07-24

### Added

- Dashboard web React + TypeScript + Vite (`frontend/`) consumindo a API real, com presets Confortável, Atenção e Crítico.
- Guia `QUICKSTART_V1.md` e screenshots de onboarding no README raiz e em `frontend/README.md`.
- Script `scripts/verify-dev-env.sh`, `.nvmrc` (Node 22) e repositório Maven local em `.m2/repository`.
- Job **Frontend build** no CI e CodeQL para Java + JavaScript/TypeScript.

### Changed

- Pipeline Release publica imagem GHCR a cada push em `main`; tag semver criada apenas em bump de versão.
- Vite atualizado para 7.3.6 (correção de advisories do dev server).
- Dependabot ignora semver-major de Spring Boot até epic dedicado.

### Fixed

- Dockerfile usa imagem Maven no build stage e entrypoint Distroless correto (`CMD` + `java -jar`).
- Cache OWASP Dependency-Check e suporte a `NVD_API_KEY` para reduzir falhas HTTP 429 no CI.
- `frontend/.gitignore` ignora cache `.vite/`.

## [0.1.0] - 2026-07-23

### Added

- Motores Tributário, Financeiro e de Decisão isolados.
- ACL com conversão de strings decimais para `BigDecimal`.
- Simulação multioperação, projeção de caixa, ICOFs, gap e repasse estimado.
- Rastreabilidade de `rulesetVersion` e `taxPolicyReference`.
- Catálogo de erros `FIN-001`, `FIN-002` e `FIN-003`.
- Testes unitários, E2E e Golden Case de regressão.
- Estrutura de DevSecOps, governança comunitária, CI/CD e publicação Docker no GHCR.
