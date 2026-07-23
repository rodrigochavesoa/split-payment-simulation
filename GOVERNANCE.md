# Governança do Projeto

## Papéis

- **Maintainers:** preservam qualidade, releases e saúde geral do repositório.
- **Domain Maintainers:** aprovam alterações de fórmulas, critérios de risco, precisão e políticas tributárias parametrizadas.
- **Security Maintainers:** respondem a vulnerabilidades, dependências e controles de cadeia de suprimento.
- **Contributors:** propõem e implementam mudanças sob revisão.

## Tomada de decisão

Decisões ordinárias são tomadas por consenso dos revisores aplicáveis. Mudanças de alto impacto exigem aprovação explícita de pelo menos um Maintainer e um Domain Maintainer:

- fórmula financeira, arredondamento ou `dayCountBasis`;
- limiares do Decision Engine;
- contrato público, códigos de erro ou rastreabilidade;
- dependências críticas, Docker base image e políticas de CI;
- alteração do Golden Case.

Quando não houver consenso, o Maintainer responsável decide e registra a justificativa na issue ou pull request.

## Releases

Uma release requer CI verde, revisão de segurança, atualização de `CHANGELOG.md`, validação do Golden Case e revisão das limitações documentadas. Versões seguem Semantic Versioning.

## Transparência

Decisões de arquitetura e regras devem ser documentadas em pull requests, issues ou ADRs futuros. Referências tributárias recebidas no payload são auditáveis, mas não representam parecer jurídico automatizado.
