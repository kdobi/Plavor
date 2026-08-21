# Plavor

## Documentation

- [ERD](docs/erd.md)
- [GitHub Quality Gates](docs/github-quality-gates.md)

## Home Server Deployment

The production compose file runs the Spring Boot backend and PostgreSQL in the same Docker network.

1. Create an environment file on the server:

   ```bash
   cp docker/prod.env.example .env
   ```

2. Edit `.env` and set a real `POSTGRES_PASSWORD`.

3. Start the backend and database:

   ```bash
   docker compose --env-file .env -f docker-compose.prod.yml up -d --build
   ```

4. Check the backend health endpoint:

   ```bash
   curl http://SERVER_IP:8080/actuator/health
   ```

If only port 80 is forwarded, set `BACKEND_PORT=80` in `.env` and use:

```bash
curl http://SERVER_IP/actuator/health
```
