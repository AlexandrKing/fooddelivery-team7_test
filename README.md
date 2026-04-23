# FoodDelivery

Для запуска backend используйте Spring Boot приложение `FoodDeliveryApplication`
или Maven команду `./mvnw spring-boot:run` (`.\mvnw.cmd spring-boot:run` на Windows).

## Testing

### Backend tests (Spring Boot / Maven)

- Test profile: `test` (`src/test/resources/application-test.yml`, H2 in-memory DB)
- Local command (Windows): `.\mvnw.cmd -Dspring.profiles.active=test test`
- Local command (Linux/macOS): `./mvnw -Dspring.profiles.active=test test`

### Frontend tests (Vitest)

- Directory: `frontend`
- Local command: `npm --prefix frontend test`

## CI

- GitHub Actions workflow: `.github/workflows/tests.yml`
- Runs backend tests with Java 17 + Maven Wrapper
- Runs frontend tests with Node 20 + `npm test`
