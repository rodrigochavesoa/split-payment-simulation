## Summary

<!-- Explain the business and technical intent. Link the issue or decision record. -->

## Financial / tax impact

- [ ] No financial or tax formula changed.
- [ ] Formula changed and ARCHITECTURE.md, unit tests, and Golden Case were reviewed.
- [ ] Ruleset or tax-policy traceability impact was assessed.

## Validation

- [ ] `mvn -B -ntp verify` passes locally.
- [ ] Relevant unit and integration tests were added or updated.
- [ ] Boundary conditions and HALF_EVEN rounding were considered.
- [ ] No `float` or `double` was introduced into monetary or percentage calculations.

## Security and operations

- [ ] No secret, credential, personal data, or production payload is included.
- [ ] Dependency, API, configuration, and error-catalog impacts were assessed.
- [ ] Documentation and release notes were updated when applicable.
