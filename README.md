# Plavor

## Documentation

- [ERD](docs/erd.md)
- [GitHub Quality Gates](docs/github-quality-gates.md)

## Home Server Deployment

The production compose file runs PostgreSQL, the Spring Boot backend, and a Caddy web image in the same Docker network. The web image builds the React frontend and serves the compiled files while proxying API traffic to the backend.

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

Do not use `docker compose down -v` after real production data exists. The `-v` option deletes the PostgreSQL volume.
