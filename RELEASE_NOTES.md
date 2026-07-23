# Release Notes — Cash Flow Impact Simulation Engine 1.0

## Release 1.0

**Status:** aprovado para encerramento do ciclo de desenvolvimento e validação de release.

## Entregas incluídas

- API REST para simulação de impacto de Split Payment em lote.
- ACL com validação de valores decimais recebidos como strings e conversão segura para `BigDecimal`.
- Tax Engine parametrizado por operação, com débito bruto, tributo já extinto e percentual elegível ao Split.
- Finance Engine com perda do float, benefício de postergação, bases de contagem 30/360/365, ICOF operacional e de liquidez.
- Projeções comparativas de caixa Base e Split, com identificação do menor saldo projetado.
- Cálculo de gap de caixa, margem incremental \(\kappa\) e reajuste mínimo estimado.
- Decision Engine com classificação `CONFORTAVEL`, `ZONA_DE_ATENCAO` e `ALERTA_CRITICO`.
- Rastreabilidade via `rulesetVersion` e `taxPolicyReference` retornados no `auditTrail`.
- Catálogo de erros financeiros `FIN-001`, `FIN-002` e `FIN-003`.
- Testes unitários, testes de borda HTTP, teste E2E real e Golden Case de regressão financeira.

## Contrato de precisão

Todos os valores monetários, percentuais e taxas devem ser enviados como strings decimais com ponto, por exemplo `"1250.50"` e `"0.28"`. A API rejeita vírgula, notação científica e tipos JSON numéricos nos campos protegidos. O núcleo usa `BigDecimal` com saída em quatro casas e arredondamento `HALF_EVEN`.

## Limitações conhecidas e fora de escopo

Esta Release 1.0 é um simulador financeiro parametrizado; ela **não** constitui cálculo fiscal legal automatizado. Em especial:

- Não calcula créditos tributários complexos, compensações, regimes especiais, isenções ou regras legais específicas de IBS/CBS.
- Não interpreta `taxPolicyReference`; a referência é somente metadado de auditoria.
- Não consulta legislação, serviços governamentais, alíquotas externas, índices de juros ou calendários fiscais.
- Assume uma alíquota efetiva, um percentual elegível ao Split e um valor já extinto informados por operação.
- Não suporta múltiplas moedas, conversão cambial ou exposição a variação de câmbio.
- Reconhece receitas líquidas e custos operacionais na data de liquidação; não há, nesta versão, datas separadas de vencimento para recebimentos, custos variáveis ou custos fixos.
- No ledger Base, a saída tributária usa o valor simulado como retido pelo Split; resíduos tributários fora da parcela elegível exigem evolução posterior do contrato e do modelo de fluxo.
- Não implementa autenticação, autorização, persistência de simulações, trilha de auditoria imutável, idempotência ou observabilidade operacional.
- Não substitui validação contábil, jurídica, tributária ou financeira especializada.

## Orientação de adoção

Use a `rulesetVersion` para identificar a hipótese de negócio aplicada ao cenário e `taxPolicyReference` para relacionar cada operação à política tributária que fundamentou os dados de entrada. Para qualquer alteração de fórmula, limite ou arredondamento, atualize a documentação, os testes unitários e o Golden Case no mesmo pull request.

## Official Release 1.0 Sign-off

O escopo planejado para a Release 1.0 foi concluído: os três motores de domínio, a ACL, os cálculos de caixa e precificação, a matriz de decisão, a governança de erros, a rastreabilidade e a regressão financeira estão entregues. A versão está pronta para validação final do negócio e preparação do processo formal de release.
