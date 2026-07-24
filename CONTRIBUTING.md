# Contribuindo

## Pré-requisitos

- Java 21
- Maven 3.9 ou superior
- Docker opcional para execução local

## Fluxo de contribuição

1. Abra uma issue ou obtenha concordância prévia para mudanças de regra financeira, tributária ou contrato de API.
2. Crie uma branch curta a partir de `main`.
3. Escreva ou atualize testes antes da implementação para mudanças de domínio.
4. Execute `mvn -B -ntp verify` e o Checkstyle definido no workflow de CI.
5. Abra um pull request usando o template obrigatório.

## Commits convencionais

Use [Conventional Commits](https://www.conventionalcommits.org/):

```text
feat(finance): calculate incremental price adjustment
fix(tax): prevent negative split eligible debit
docs(architecture): clarify day-count basis
test(api): add golden regression scenario
chore(ci): update dependency scan action
```

Tipos permitidos: `feat`, `fix`, `docs`, `test`, `refactor`, `perf`, `build`, `ci`, `chore` e `security`.

## Regras inegociáveis do domínio

- Use `BigDecimal` para valores monetários, taxas e percentuais; não introduza `float` ou `double`.
- Preserve funções puras nos motores Tributário, Financeiro e de Decisão.
- Não introduza relógio do sistema, I/O, banco de dados ou HTTP no núcleo de domínio.
- Mantenha conversão de JSON e validação de borda exclusivamente na ACL.
- Mudanças em fórmulas exigem atualização de `ARCHITECTURE.md`, testes de limite e revisão do Golden Case.
- Dados de produção, clientes, segredos e identificadores pessoais nunca devem ser enviados em issues, commits ou testes.

## Revisão

Toda alteração exige CI verde e aprovação dos CODEOWNERS aplicáveis. Mudanças de fórmula, arredondamento, critérios de risco, contrato público ou código de erro requerem revisão de mantenedores do domínio e atualização de release notes.

## Secrets de CI (maintainers)

O job OWASP Dependency-Check aceita, de forma opcional, a secret **`NVD_API_KEY`** (Actions → Secrets and variables → Actions). Obtenha a chave gratuita em [NVD — Request an API Key](https://nvd.nist.gov/developers/request-an-api-key). Sem ela o CI continua funcionando; com ela, reduz falhas por rate limit (HTTP 429) ao atualizar a base de CVEs.

CodeQL (Java + TypeScript) e o pipeline de Release **não usam** essa secret e **não exigem** alteração em Releases ou Packages do GitHub.
