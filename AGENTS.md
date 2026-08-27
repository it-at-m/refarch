# RefArch

## Repository shape
- This repo is not a single build. Treat it as four separate roots:
- `refarch-gateway`: standalone Spring Boot gateway app and the only module with a Dockerfile.
- `refarch-integrations`: Maven reactor for reusable integration libraries plus runnable example/mock apps.
- `refarch-tools/refarch-java-tools`: Maven reactor that publishes the shared PMD rules (`refarch-pmd`).
- `docs`: VitePress site with its own `package.json` and `package-lock.json`.
- There is no Maven wrapper in the repo; use the system `mvn`.

## Toolchains
- Java 21 across all Maven projects.
- `docs` requires Node `>=24.11 <25`; CI uses Node 24.

## Commands
- Gateway full verification: `mvn -f refarch-gateway/pom.xml verify`
- Integrations full verification: `mvn -f refarch-integrations/pom.xml verify`
- Tools full verification: `mvn -f refarch-tools/refarch-java-tools/pom.xml verify`
- Gateway single test: `mvn -f refarch-gateway/pom.xml -Dtest=BackendRouteTest test`
- Integration submodule test: `mvn -f refarch-integrations/pom.xml -pl refarch-s3-integration/refarch-s3-integration-core -am -Dtest=E2ETest test`
- Java formatting fix for one Maven root: `mvn -f <project>/pom.xml spotless:apply`
- Docs install: `npm ci` in `docs/`
- Docs verification: `npm run lint` and `npm run build` in `docs/`
- Docs dev server: `npm run dev` in `docs/`

## Build and lint behavior
- `mvn verify` is the normal Java quality gate here; Spotless and PMD run from the parent POMs.
- `refarch-gateway` also runs SpotBugs with FindSecBugs during `verify`; `refarch-integrations` currently does not.
- CI mirrors this split: `.github/workflows/build.yml` builds `refarch-gateway`, `refarch-integrations`, and `refarch-tools/refarch-java-tools` independently. Docs build in `.github/workflows/deploy-docs.yml`.

## IDE metadata
- The project is used with IntelliJ. Do not create or commit Eclipse metadata such as `.factorypath`, `.classpath`, `.project`, or `.settings/`.

## Local development stack
- The repo-local stack is `stack/docker-compose.yml`. It only brings up Keycloak, MinIO, and Mailpit.
- Start it from `stack/` with `docker compose up -d` or equivalent; PR CI health-checks this exact stack.
- Service ports from the checked-in compose file:
- Keycloak `8100`
- MinIO API `9000`, console `9001`
- Mailpit SMTP `1025`, UI `8025`
- `stack/minio/` is mounted runtime data for MinIO; do not clean up or edit `.minio.sys` unless the task is explicitly about stack fixtures.

## Gateway-specific gotchas
- Main entrypoint: `refarch-gateway/src/main/java/de/muenchen/oss/refarch/gateway/ApiGatewayApplication.java`.
- For local host execution, `application-local.yml` points OAuth and the SSO route to `http://keycloak:8100/...`; if you use the `local` profile outside containers, `keycloak` must resolve on the host.
- If SSO is irrelevant for the task, add the `no-security` profile instead of fighting Keycloak setup.
- Hazelcast session wiring is profile-driven: `hazelcast-local` for localhost, `hazelcast-k8s` for cluster mode. `hazelcast-k8s` fails without `refarch.hazelcast.service-name`.

## Integrations structure
- `refarch-integrations` is organized by integration family (`refarch-s3-integration`, `refarch-email-integration`, `refarch-dms-integration`, `refarch-cosys-integration`, `refarch-address-integration`).
- Submodule naming is meaningful and consistent across families:
- `*-client`: API client for the external system
- `*-core`: hexagonal implementation
- `*-starter`: Spring autoconfiguration layer
- `*-example`: runnable sample app for testing/reference, not a dependency target
- `*-service`: runnable service image, not a dependency target
- DMS also contains a Fabasoft SOAP mock service app under `refarch-dms-integration-fabasoft-soap/.../refarch-dms-integration-fabasoft-mock-service`.

## Testing notes
- Gateway integration-style tests use Spring Boot + WireMock, so focused gateway tests usually do not need the external compose stack.
- `refarch-integrations/refarch-s3-integration/refarch-s3-integration-core/src/test/java/.../E2ETest.java` uses Testcontainers MinIO; Docker/Podman is required even without `stack/`.
- The email example app sends mail on `ApplicationReadyEvent` and then exits; do not expect it to stay up like a normal service.

## Conventions worth preserving
- PR template requires code, comments, and docs to be in English.
