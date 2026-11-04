# Nursing Home Management System Constitution

Date: 2026-11-04

## 1. Purpose

Guide changes to the existing nursing-home management application. Changes must support safe and reliable resident care, protect sensitive information, and preserve the behavior on which residents, relatives, and staff depend. Principles below describe the current project and how it should evolve; they do not claim that every desired safeguard or workflow is already implemented.

## 2. Technology Baseline

- The current build uses Java 21, Spring Boot 4.1.1, and Maven (`pom.xml`). Keep changes compatible with this baseline unless an approved requirement explicitly changes it.
- The application currently uses Spring Web MVC, Thymeleaf, Spring Data JPA/Hibernate, Spring Security, Jakarta Validation, and Microsoft SQL Server. These are the observed implementation, not a prohibition on a future, explicitly approved migration.
- SQL Server connection and Hibernate settings are currently in `src/main/resources/application.properties`. Keep credentials and environment-specific configuration out of source-controlled defaults; changes to schema-generation or SQL logging settings must consider deployment impact.
- Do not add a framework, runtime, or build system without a concrete project requirement and compatibility review.

## 3. Architecture and Boundaries

- Preserve the current Spring MVC, server-rendered web application and its package structure under `com.group6.nhms`: web controllers, services and implementations, repositories, entities, request/response DTOs, mappers, configuration, and security.
- Keep HTTP and view interaction in controllers, business workflows in services, and persistence queries in repositories, following the existing codebase patterns.
- The project uses Thymeleaf controllers and JPA repositories; do not assume or introduce a REST API or another architecture without an explicit requirement.
- Continue using DTOs and mappers where they are already part of a workflow. Do not require them for every new type or feature when that would not fit the existing use case.
- Prefer constructor dependency injection and small, cohesive changes that keep domain behavior understandable and testable.

## 4. Java Coding Standards

- Write clear, idiomatic Java compatible with Java 21 and the project's existing Spring conventions.
- Use descriptive names, consistent formatting, and focused methods. Avoid unnecessary abstractions and hidden side effects.
- Validate user-provided data at the server boundary where applicable; browser-side validation is not a substitute for server-side checks.
- Represent known domain states with the existing enums or established conventions when extending those workflows. Avoid introducing conflicting status or role values.
- Handle failures explicitly using the project's exception and feedback patterns. Do not silently ignore unexpected errors or substitute unrelated records or identities.

## 5. Data and Persistence

- Preserve the integrity of existing JPA entities, identity primary keys, foreign-key relationships, and business data. Verify effects on related entities before changing mappings or persistence behavior.
- Use transactions for workflows that update multiple related records, following existing service-layer patterns.
- Enforce business invariants in server-side logic, including resident status, bed occupancy, medication stock, billing status, and staff schedule ownership where those workflows apply.
- Avoid duplicate or contradictory records where the existing business rules require uniqueness. Do not assume a constraint exists unless it is defined or enforced by the current application.
- Treat changes to entity mappings and Hibernate schema behavior as data changes: assess compatibility with existing SQL Server data and avoid unintended data loss.
- Keep database technology aligned with the configured SQL Server deployment unless an explicit migration is approved and its data, configuration, and test impact are addressed.

## 6. Security and Privacy

- Preserve Spring Security form-login/session behavior and existing role-based access boundaries unless a requirement explicitly changes them.
- Apply least-privilege access to web routes and verify ownership before exposing resident, health, incident, or billing information to a relative account.
- Store passwords only as encoded values using the configured password encoder. Do not add fixed credentials, expose secrets, or rely on demo accounts in production workflows.
- Do not weaken authentication, authorization, account-status checks, or privacy protections as a side effect of feature work.
- Review security-sensitive configuration changes explicitly. In particular, the current security configuration disables CSRF; do not treat that setting as a security requirement or expand its use without assessing the risks to session-authenticated form submissions.

## 7. Thymeleaf UI and UX

- Build on the existing Thymeleaf templates, shared layout and fragments, Bootstrap assets, and Vietnamese-language UI.
- Keep navigation and page access consistent with the roles enforced by Spring Security; hiding a UI element is not a replacement for server-side authorization.
- For forms, provide clear labels, useful validation feedback, and error/success messages consistent with existing templates. Preserve responsive behavior and accessible native controls.
- Avoid introducing a separate frontend framework or client/server architecture without an explicit requirement.

## 8. Business Testing

- Add or update focused tests for changed business behavior, especially resident intake and bed allocation, health-record and medication workflows, billing, incidents, schedules, account handling, and authorization where affected.
- Verify important validation, state transitions, relationship ownership, and error cases, not only successful paths.
- Use the project's Maven test setup and existing test conventions. Do not claim coverage for workflows that have not been tested.
- Run the smallest relevant test set and build checks for each change; investigate failures rather than masking them.

## 9. Compatibility and Change Control

- Preserve existing user-visible routes, workflows, role behavior, database records, and template contracts unless an approved requirement calls for a behavior change.
- Make changes incrementally and check their effects across controller, service, repository, entity, DTO/mapper, template, and security layers that participate in the affected workflow.
- Do not add features based only on this constitution or on assumptions about nursing-home systems. Implement only documented requirements.
- For a compatibility-breaking change, state the reason and impact, provide an appropriate migration or transition path, and update relevant tests and documentation.

## 10. Amendment

Amend this constitution when the project's technology baseline, architecture, security model, or governance materially changes. Amendments must reflect verified project decisions and must not describe planned or desired functionality as already implemented.
