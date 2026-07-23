# Cash Flow Impact Simulation Engine — Arquitetura e Modelo Matemático

## 1. Propósito e escopo

Esta aplicação simula o impacto de caixa da retenção antecipada de tributos em um cenário de Split Payment. Ela recebe um lote de operações, calcula a parcela tributária segregada, projeta o caixa nos cenários Base e Split, mede a cobertura de obrigações fixas e estima o reajuste de preço necessário para recompor uma reserva mínima de caixa.

O resultado é uma **simulação determinística**: os resultados dependem exclusivamente do payload, da `rulesetVersion` declarada e das regras implementadas. Não há relógio do sistema, banco de dados ou chamadas externas no núcleo de domínio.

## 2. Arquitetura de domínio

```text
HTTP API
  -> ACL: validação, desserialização e conversão segura
  -> Tax Engine
  -> Finance Engine
  -> Decision Engine
  -> HTTP Response + auditTrail
```

### 2.1 API e Anti-Corruption Layer (ACL)

A API recebe valores monetários, percentuais e taxas **como strings JSON**. A ACL valida a sintaxe decimal, rejeita vírgulas, notação científica, letras e números JSON nativos para os campos de precisão. Depois converte os valores para `BigDecimal`, datas para `LocalDate` e enums para os Value Objects do domínio.

Essa fronteira impede que detalhes de HTTP, Jackson ou `double` contaminem os motores puros. A ACL também preserva os metadados de auditoria recebidos:

- `rulesetVersion`: versão declarada do conjunto de regras do cenário;
- `taxPolicyReference`: referência de política tributária declarada para cada operação.

Esses metadados retornam inalterados em `auditTrail`; nesta versão, são referências auditáveis e não regras legais interpretadas automaticamente.

### 2.2 Tax Engine

Responsável apenas pela matemática tributária parametrizada por operação. Para cada evento, calcula o débito bruto, desconta o tributo já extinto e aplica o percentual elegível ao Split. Ao fim, retorna eventos individuais e totais agregados.

### 2.3 Finance Engine

Responsável pela diferença temporal entre a data de liquidação e o vencimento-base do tributo, pelos ICOFs, pela projeção diária de caixa, pelo gap de caixa e pelo reajuste de preço. Não conhece HTTP, JSON ou regras de classificação de risco.

### 2.4 Decision Engine

Recebe exclusivamente indicadores financeiros consolidados e devolve status, risco e mensagem analítica. A regra de decisão não calcula valores monetários e não modifica os resultados dos outros motores.

## 3. Precisão, arredondamento e determinismo

Todos os valores financeiros e percentuais do domínio usam `BigDecimal`. O núcleo não usa `float` ou `double` para cálculos monetários, percentuais, raízes ou divisões.

- Contexto de cálculo intermediário: `MathContext.DECIMAL128`.
- Escala de saída: quatro casas decimais.
- Arredondamento de saída: `HALF_EVEN`.
- Identidade de todas as agregações: `BigDecimal.ZERO`.

O arredondamento ocorre ao produzir indicadores de saída, não a cada soma intermediária. Isso reduz a propagação de erro de arredondamento em lotes de operações.

## 4. Modelo tributário parametrizado

Para uma operação com base tributável \(B\), alíquota efetiva \(t\), tributo já extinto \(E\) e percentual elegível ao Split \(p\):

\[
D_{bruto} = B \times t
\]

\[
D_{elegivel} = \max(0, D_{bruto} - E)
\]

\[
W = D_{elegivel} \times p
\]

Onde \(W\) é o valor simulado como retido pelo Split Payment. Nenhuma alíquota é fixada pelo motor; os parâmetros são recebidos em cada operação.

## 5. Glossário matemático do Finance Engine

Os cálculos financeiros são feitos por evento tributário e depois agregados. Considere:

- \(W_i\): valor retido do evento \(i\);
- \(s_i\): `settlementDate`;
- \(v_i\): `baselineTaxDueDate`;
- \(d_i = v_i - s_i\): diferença de dias, com sinal;
- \(r_d\): taxa efetiva diária;
- \(R\): receitas líquidas totais;
- \(V\): custos variáveis totais;
- \(F\): custos fixos totais;
- \(C_0\): caixa disponível inicial;
- \(M\): reserva mínima de caixa.

### 5.1 Conversão de taxa diária

Para taxa diária informada diretamente:

\[
r_d = r_{dia}
\]

Para taxa mensal efetiva:

\[
r_d = (1 + r_{mes})^{\frac{1}{30}} - 1
\]

Para taxa anual efetiva, com base \(b \in \{30, 360, 365\}\):

\[
r_d = (1 + r_{ano})^{\frac{1}{b}} - 1
\]

### 5.2 Perda do float e benefício de postergação

O valor financeiro absoluto de um evento é:

\[
I_i = W_i \times \left((1 + r_d)^{|d_i|} - 1\right)
\]

Se o Split ocorre antes do vencimento-base \((d_i > 0)\), há perda do float:

\[
L_i = I_i, \qquad P_i = 0
\]

Se o Split ocorre após o vencimento-base \((d_i < 0)\), há benefício de postergação:

\[
L_i = 0, \qquad P_i = I_i
\]

Se \(d_i = 0\), \(W_i = 0\) ou \(r_d = 0\), ambos são zero. Os totais são:

\[
L = \sum_i L_i, \qquad P = \sum_i P_i, \qquad Impacto\ Líquido = L - P
\]

### 5.3 ICOF operacional

O Índice de Cobertura de Obrigações Fixas operacional é:

\[
ICOF_{operacional} = \frac{R - V - L}{F}
\]

Se \(F = 0\), o motor não divide por zero: interrompe o cálculo com a exceção controlada `FixedCostCoverageNotApplicableException`.

### 5.4 ICOF de liquidez

\[
ICOF_{liquidez} = \frac{C_0 + R - V - L - M}{F}
\]

O benefício de postergação é informado no resultado financeiro, mas a fórmula atual de ICOF desconta especificamente a perda do float \(L\), conforme a implementação da Release 1.0.

### 5.5 Projeção temporal de caixa

Para cada data, o ledger agrega os eventos antes de acumular o saldo. Em ambas as projeções, receitas líquidas e custos operacionais da operação são reconhecidos em `settlementDate`:

\[
\Delta O_i = ReceitaLiquida_i - CustoVariavel_i - CustoFixo_i
\]

No cenário Base, \(-W_i\) é lançado em \(v_i\). No cenário Split, \(-W_i\) é lançado em \(s_i\). O menor saldo acumulado do cenário Split é:

\[
C_{min}^{split} = \min_t\left(C_0 + \sum_{\tau \leq t}\Delta Caixa_{split}(\tau)\right)
\]

### 5.6 Gap de caixa

\[
Gap = \max(0, M - C_{min}^{split})
\]

### 5.7 Margem incremental e repasse mínimo estimado

Sejam \(m_v\), \(m_p\) e \(m_c\), respectivamente, os percentuais incrementais de custo variável, taxa de pagamento e comissão:

\[
\kappa = 1 - m_v - m_p - m_c
\]

Com \(A\) igual à receita total reajustável:

\[
Reajuste_{estimado} = \frac{Gap}{A \times \kappa}
\]

Regras de proteção:

- se \(Gap = 0\), o reajuste é \(0\);
- se \(Gap > 0\) e \(A = 0\), o cálculo não é possível;
- se \(Gap > 0\) e \(\kappa \leq 0\), o modelo econômico é inviável.

O resultado é devolvido como fração decimal: `0.5417` representa reajuste de `54,17%`.

## 6. Matriz do Decision Engine

O Motor de Decisão usa o \(ICOF_{liquidez}\), \(C_{min}^{split}\) e \(M\), nesta ordem de precedência:

| Status | Regra |
|---|---|
| `ALERTA_CRITICO` | \(ICOF_{liquidez} < 1,00\) **ou** \(C_{min}^{split} < 0\) |
| `ZONA_DE_ATENCAO` | Não crítico e \(ICOF_{liquidez} < 1,30\) **ou** \(C_{min}^{split} < M\) |
| `CONFORTAVEL` | \(ICOF_{liquidez} \geq 1,30\) **e** \(C_{min}^{split} \geq M\) |

## 7. Catálogo de erros

Erros estruturais ou de parsing são retornados como `400 Bad Request`, com códigos `VAL-*`. Erros de domínio que representam uma simulação estruturalmente válida, mas financeiramente não calculável, são retornados como `422 Unprocessable Entity`.

| Código | HTTP | Exceção | Significado | Ação esperada |
|---|---:|---|---|---|
| `FIN-001` | 422 | `FixedCostCoverageNotApplicableException` | O custo fixo agregado é zero e o ICOF não pode ser calculado. | Informar custos fixos ou tratar o índice como não aplicável. |
| `FIN-002` | 422 | `AdjustmentNotCalculableException` | Há gap de caixa, mas a receita reajustável agregada é zero. | Informar receita passível de reajuste ou adotar outra fonte de capital. |
| `FIN-003` | 422 | `UnviablePricingModelException` | \(\kappa \leq 0\); aumentar preços não gera contribuição incremental positiva. | Rever custos variáveis, taxas, comissão ou a estratégia comercial. |

## 8. Garantia de regressão

`GoldenCaseRegressionTest` executa a aplicação completa com múltiplas operações e valores esperados fixos para débito tributário, ICOFs, saldo mínimo, gap, repasse e decisão. Uma alteração nesses resultados exige revisão explícita da regra de negócio e atualização consciente do caso dourado.
