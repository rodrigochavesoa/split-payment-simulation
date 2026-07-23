# Política de Segurança

## Reporte responsável

Não abra uma issue pública para vulnerabilidades, exposição de segredo, bypass de validação, manipulação de cálculos financeiros ou vazamento de dados.

Use o formulário privado de [GitHub Security Advisory](https://github.com/rodrigochavesoa/split-payment-simulation/security/advisories/new) do repositório.

Inclua, quando possível:

- descrição e impacto;
- versão ou commit afetado;
- passos mínimos de reprodução com dados sintéticos;
- evidências, logs sanitizados e mitigação sugerida.

## Processo de tratamento

1. Confirmação de recebimento em até 3 dias úteis.
2. Triagem de severidade e impacto financeiro/operacional.
3. Correção, teste de regressão e revisão de segurança.
4. Publicação coordenada da correção e crédito ao pesquisador, quando autorizado.

## Escopo prioritário

- desserialização e validação da ACL;
- precisão decimal e integridade dos cálculos;
- dependências Maven e imagens de contêiner;
- exposição de credenciais, logs ou dados de cenários financeiros;
- controles de CI/CD e cadeias de suprimento.
