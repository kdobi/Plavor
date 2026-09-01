# Plavor

## Documentation

- [ERD](docs/erd.md)
- [GitHub Quality Gates](docs/github-quality-gates.md)

## Home Server Deployment

The production compose file runs PostgreSQL, the Spring Boot backend, and a Caddy web image in the same Docker network. The web image builds the React frontend and serves the compiled files while proxying API traffic to the backend.

Uploaded product images are stored in the `plavor-uploads` Docker volume and exposed through the backend under `/uploads/**`. The database stores only the image URL, not the binary image file.

1. Create an environment file on the server:

   ```bash
   cp docker/prod.env.example .env
   ```

2. Edit `.env` and set real values for the database password, JWT secret, Kakao credentials, and production domain.

3. Stop any old standalone Caddy container that was created outside this compose file:

   ```bash
   docker ps
   docker stop caddy
   docker rm caddy
   ```

4. Start the production stack:

   ```bash
   docker compose --env-file .env -f docker-compose.prod.yml up -d --build
   ```

5. Check the deployment:

   ```bash
   curl http://localhost:8080/actuator/health
   curl https://$PLAVOR_DOMAIN/actuator/health
   curl https://$PLAVOR_DOMAIN/api/products
   ```

Do not use `docker compose down -v` after real production data exists. The `-v` option deletes named volumes, including PostgreSQL data and uploaded product images.

## Automatic Production Deployment

Production deployment is handled by `.github/workflows/deploy-prod.yml`.

The workflow runs after the `CI` workflow succeeds on `main`. It can also be started manually from the `main` branch with `workflow_dispatch`.

The home server needs a GitHub Actions self-hosted runner with the `plavor-prod` label. The runner host must have Docker, the Docker Compose plugin, and access to `/home/kdobi/Plavor/.env`.

The deployment workflow checks out the release source in the GitHub Actions runner workspace, reads the production environment values from `/home/kdobi/Plavor/.env`, then runs:

```bash
docker compose --project-name plavor --env-file /home/kdobi/Plavor/.env -f docker-compose.prod.yml config
docker compose --project-name plavor --env-file /home/kdobi/Plavor/.env -f docker-compose.prod.yml build
docker compose --project-name plavor --env-file /home/kdobi/Plavor/.env -f docker-compose.prod.yml up -d --remove-orphans
```

The fixed Compose project name keeps the Docker network and named volumes stable even though GitHub Actions runs from its own workspace.

Recommended production flow:

```text
feat/* -> develop -> main -> CI success -> production deploy
```
