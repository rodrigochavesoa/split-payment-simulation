# Visão do Produto

## Step 1 — A origem do problema: o custo do tax float

A Reforma Tributária do consumo instituiu o IBS e a CBS e prevê, na Lei Complementar nº 214/2025, o recolhimento na liquidação financeira da transação — o mecanismo conhecido como **Split Payment**. Nesse modelo, os prestadores de serviços de pagamento e as instituições operadoras segregam e recolhem os valores correspondentes ao IBS e à CBS no momento em que o pagamento é liquidado.

Essa mudança desloca o momento econômico do recolhimento para mais perto do recebimento da venda. Para muitas empresas, o tributo que antes permanecia temporariamente no caixa — o chamado **tax float**, uma parcela relevante do capital de giro — deixa de financiar o intervalo entre a liquidação da venda e o vencimento da obrigação tributária. O efeito não é apenas contábil: a empresa pode vender, gerar margem e ainda assim enfrentar uma ruptura de liquidez porque uma parcela do recebimento não estará mais disponível para financiar folha, fornecedores, custos fixos e novas operações.

O problema central que orienta este produto é, portanto:

> Como transformar a mudança do momento de recolhimento tributário em uma decisão financeira mensurável, auditável e acionável para cada empresa?

## Step 2 — O que a V1.0 resolve: o Simulador Sandbox

A V1.0 é a fundação matemática do produto. Ela implementa um domínio financeiro e tributário parametrizado, organizado segundo princípios de **Domain-Driven Design**, com separação entre `Tax Engine`, `Finance Engine` e `Decision Engine`. O núcleo usa `BigDecimal`, precisão determinística, arredondamento documentado e testes de regressão para que cada resultado possa ser explicado, reproduzido e revisado.

Em termos de produto, a V1.0 funciona como um **Simulador Sandbox Síncrono**. Times financeiros fornecem cenários manuais — operações, datas, valores, taxas, percentuais elegíveis e premissas de política — e recebem uma projeção determinística do impacto do Split Payment.

O simulador compara os fluxos Base e Split e calcula, entre outros indicadores:

- perda ou benefício do float;
- menor saldo projetado;
- **Cash Gap**, isto é, a ruptura entre a reserva mínima desejada e o caixa projetado;
- cobertura de obrigações fixas e indicadores de liquidez;
- classificação de risco;
- **Reajuste Estimado de Preço**, calculado a partir da margem incremental `κ` (Kappa) necessária para recompor o caixa.

A V1.0 não é um cálculo fiscal legal automatizado e não interpreta, por conta própria, legislação, alíquotas oficiais, créditos complexos ou regimes especiais. Ela é uma camada de raciocínio financeiro: recebe premissas explícitas, executa matemática auditável e ajuda a responder “o que acontece com o caixa se este cenário ocorrer?”.

## Step 3 — A ideia da V2.0: Middleware Tax-FinOps B2B

A V2.0 evolui o produto sem substituir a V1.0. A fundação matemática continua sendo o núcleo de cálculo e decisão; a evolução está na forma como os dados chegam até ela.

Também não pretendemos ser um clone da plataforma governamental. A responsabilidade da V2.0 é atuar como uma camada de **integração assíncrona Tax-FinOps B2B**, conectando ERPs, plataformas financeiras e empresas à Plataforma Pública oficial do Split Payment, conforme os contratos e padrões de integração publicados pelas autoridades competentes.

Na arquitetura-alvo, essa camada deverá:

1. estabelecer comunicação segura com a plataforma oficial, incluindo mTLS;
2. operar o recebimento assíncrono de eventos por Long Polling;
3. gerenciar os artefatos e compromissos operacionais de integração, incluindo MOC e ANS;
4. controlar correlação, idempotência, ordenação, retries e observabilidade dos eventos;
5. consumir os eventos tributários oficiais associados ao fluxo de pagamento e ao modelo de Split Payment Integrado;
6. normalizar e injetar esses eventos na engine determinística da V1.0;
7. devolver ao ERP ou à empresa um diagnóstico financeiro automatizado e próximo do tempo real.

O valor real da V2.0 está nessa ponte: a empresa deixa de depender exclusivamente de cenários manuais e passa a transformar eventos tributários oficiais em sinais de gestão. O resultado esperado é uma visão contínua de liquidez, exposição ao Cash Gap, pressão sobre capital de giro, necessidade de reajuste e risco operacional — sempre preservando a matemática, a rastreabilidade e a explicabilidade da V1.0.

### Princípios de evolução

- **V1.0 permanece o núcleo:** integrações não alteram silenciosamente suas fórmulas, precisão ou contratos.
- **Eventos oficiais não são automaticamente “verdade financeira”:** cada evento deverá ser validado, correlacionado e auditado antes de produzir impacto.
- **Assíncrono por desenho:** indisponibilidade, duplicidade, atraso e reprocessamento são estados normais a serem tratados explicitamente.
- **B2B e integração-first:** o produto entrega capacidade financeira a ERPs e empresas, sem assumir o papel de autoridade fiscal ou de plataforma governamental.
- **Explicabilidade antes de automação:** todo diagnóstico deve permitir rastrear o evento de origem, a versão das regras, as premissas e o cálculo resultante.

## Limites do produto

Esta visão descreve a direção da V2.0, não declara que seus conectores governamentais já estejam implementados ou homologados. O uso de qualquer diagnóstico exige validação técnica, contábil, jurídica, tributária e financeira adequada ao caso concreto. A legislação, os manuais de integração e os contratos operacionais oficiais devem ser tratados como fontes versionadas e podem evoluir.

## Referências

- [Lei Complementar nº 214/2025 — Planalto](https://planalto.gov.br/ccivil_03/leis/lcp/lcp214.htm)
- [Ato Conjunto RFB/CGIBS nº 2/2026 — Plataforma Pública do Split Payment](https://normas.receita.fazenda.gov.br/sijut2consulta/link.action?antigo=1&idAto=151582)
