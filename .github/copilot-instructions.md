# Plavor Copilot Instructions

Plavor is a shopping mall project with a React + Vite + TypeScript frontend, a Spring Boot backend, and PostgreSQL.

## Repository Structure

- `backend/`: Spring Boot backend using Java 21 and Gradle.
- `frontend/`: React + Vite + TypeScript frontend.
- `docker-compose.yml`: local PostgreSQL for development.
- `docker-compose.prod.yml`: home-server deployment for backend + PostgreSQL.

## Backend Guidelines

- Use package `com.plavor.backend`.
- Keep API layers clear: controller, service, repository, entity, and DTO.
- Tests should use the `test` profile and `plavor_test` database.
- Do not put production secrets in YAML files. `application-prod.yaml` must use environment variables.
- Prefer explicit DTOs for API responses instead of returning JPA entities directly.
- Keep `/actuator/health` available for deployment smoke checks.

## Frontend Guidelines

- Keep TypeScript strict and avoid unused variables.
- Replace the Vite starter UI with Plavor shopping flows as features are added.
- Prefer typed API client functions when calling backend endpoints.
- Keep user-facing screens responsive for desktop and mobile.

## Review Focus

- Check for accidental secret exposure.
- Check whether database changes need migrations.
- Check API response shapes for frontend compatibility.
- Check error handling and validation on request DTOs.
- Check that tests cover meaningful backend behavior when new APIs are added.
