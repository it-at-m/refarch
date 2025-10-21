# API Gateway

RefArch gateway based on [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway).

## Usage

The gateway is released as container image `ghcr.io/it-at-m/refarch/refarch-gateway` and can be used either directly or
via the corresponding [Helm-Chart](https://github.com/it-at-m/helm-charts/tree/main/charts/refarch-gateway).

## Features

Beside the default functionality of Spring Cloud Gateway (i.e. routing) following features are preconfigured/provided:

- OAuth 2.0 based login
- Route protection
- CSRF protection with whitelist
- Cookie to JWT mapping for session management
- Session synchronisation between multiple instances via Hazelcast (see sections [Profiles](#profiles) and [Hazelcast](#hazelcast))
- Health and metrics endpoints
- Request filters (e.g. distributed tracing, error code mapper, parameter pollution, ...)

### Routing

Routes are configured via environment variables as listed under [Configuration](#configuration).

By default, routes require authentication through OAuth 2.0 and manage the session between the client and gateway using
cookies.
The gateway then maps the session cookie to a JWT before routing it.

Beside the default behaviour there are some special route prefixes which are handled different:

- `/public/**`: All requests are routed WITHOUT security.
- `/clients/**`: Uses JWT for authenticating incoming requests instead of session cookies.

## Configuration

Following is a configuration example with description and example values. See also [Security](#security) for additional security configuration.

Spring can also be configured via environment variables. See [according documentation](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.relaxed-binding.environment-variables).

```yaml
spring:
  profiles:
    active: local,hazelcast-local # see section profiles
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: backend
              uri: http://backend-service:8080/
              predicates:
                - "Path=/api/backend-service/**"
              filters:
                - RewritePath=/api/backend-service/(?<urlsegments>.*), /$\{urlsegments}
refarch:
  hazelcast:
    service-name: # Kubernetes service name for when using profile `hazelcast-k8s`
  security:
    csrf-whitelisted: # List of routes to disable CSRF protection for (optional)
      - /example/**

# Aliases for `spring.cloud.gateway.server.webflux.globalcors.cors-configurations` to allow configuration via environment variables, as the used glob patterns can't be used there
ALLOWED_ORIGINS_PUBLIC: https://*.example.com,http://localhost:* # List of URIs allowed as origin for public routes (optional)
ALLOWED_ORIGINS_CLIENTS: https://*.example.com,http://localhost:* # List of URIs allowed as origin for client routes (optional)
```

### Security

For authentication via SSO, OAuth 2.0 needs to be configured.
See following example or the [according Spring documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html).

Alternatively the `no-security` profile can be used.

```yaml
spring:
  session:
    timeout: 10h # should be same as SSO session lifetime (e.g. internally 10h); default: 30m
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: "https://sso.example/auth/realms/example"
      client:
        provider:
          sso:
            issuer-uri: ${spring.security.oauth2.resourceserver.jwt.issuer-uri}
        registration:
          sso:
            provider: sso
            client-id:
            client-secret:
            # needed for userInfo endpoint
            scope: profile, openid
```

### Profiles

| Profile           | Description                                                                                          |
| ----------------- | ---------------------------------------------------------------------------------------------------- |
| `no-security`     | Disables all security mechanisms (e.g. authentication, authorization, CSRF) Routing works as normal. |
| `hazelcast-local` | Configures Spring Session Hazelcast for connection via localhost (i.e. local development).           |
| `hazelcast-k8s`   | Configures Spring Session Hazelcast for usage in Kubernetes/OpenShift cluster.                       |

### Hazelcast

Beside the already mentioned properties Hazelcast also has the following requirements.

#### Modular Java

See <https://docs.hazelcast.com/hazelcast/5.5/getting-started/install-hazelcast#using-modular-java>

Following Java options need to be set.
For the gateway image this can be done with `JAVA_OPTS_APPEND`.

```text
--add-modules java.se \
--add-exports java.base/jdk.internal.ref=ALL-UNNAMED \
--add-opens java.base/java.lang=ALL-UNNAMED \
--add-opens java.base/java.nio=ALL-UNNAMED \
--add-opens java.base/sun.nio.ch=ALL-UNNAMED \
--add-opens java.management/sun.management=ALL-UNNAMED \
--add-opens jdk.management/com.ibm.lang.management.internal=ALL-UNNAMED \
--add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED
```

#### Kubernetes

For running Hazelcast with profile `hazelcast-k8s` in Kubernetes port `5701` needs to be accessible.
This need to be configured for the Service and Deployment.

## Migration Guide

Main change: separated centrally maintained gateway -> separate image for frontend

### Can i migrate

If the gateway was never modified -> yes

To be sure check if any custom filters were added.
If yes there is still a possibility to migrate by either using an existing feature (e.g. client routes) or if the feature
could be useful for other applications by contributing it to the centralized gateway.

### Migrate

The migration consists of two parts: extracting the frontend and using the centralized gateway

#### Frontend

- Extract the frontend code into a separate repo or folder (in a mono repo)
- Copy `Dockerfile` and nginx config `docker/nginx/` from [`refarch-frontend`](https://github.com/it-at-m/refarch-templates/tree/main/refarch-frontend) template
- Adopt and configure `maven-node-build.yml` and `npm-release.yml` GitHub Actions workflows, see [`.github`](https://github.com/it-at-m/refarch-templates/tree/main/.github/workflows)
  - For internal GitLab migration see [according docs](https://git.muenchen.de/ccse/cicd/docs-gitlab-runner/-/blob/main-2.x/Migration.adoc?ref_type=heads#user-content-migration-refarch-api-gateway-integriertes-frontend-eigener-service-f%C3%BCr-frontend)

#### Gateway

For local development adopt the [development stack](./templates/develop.md#container-engine) (containing the gateway) from
the RefArch templates. Changing of the routes might be required to match the previous setup by modifying the environment
variables inside the Docker compose file.

For deploying the central Gateway use the [according container image](#usage) and compare your working config with the `application.yml`,
`application-local.yml` (inside [gateway code `resources` folder](https://github.com/it-at-m/refarch/tree/main/refarch-gateway/src/main/resources))
and [configuration section](#configuration) to find properties which need to be migrated.
