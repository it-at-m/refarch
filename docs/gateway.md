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
- Health and metrics endpoints

### Routing

Routes are configured via environment variables as listed under [Configuration](#configuration).

By default, routes require authentication through OAuth 2.0 and manage the session between the client and gateway using
cookies.
The gateway then maps the session cookie to a JWT before routing it.

Beside the default behaviour there are some special route prefixes which are handled different:

- `/public/**`: All `OPTIONS` and `GET` requests are routed without security.
- `/clients/**`: Uses JWT for authenticating incoming requests instead of session cookies.

## Configuration

| Var                                                      | Description                                                     | Example                                                                 |
|----------------------------------------------------------|-----------------------------------------------------------------|-------------------------------------------------------------------------|
| `SPRING_PROFILES_ACTIVE`                                 | See [profiles](#profiles)                                       | `local,hazelcast-local`                                                 |
| `SPRING_CLOUD_GATEWAY_ROUTES_<index>_ID`                 | ID of a route definition.                                       | `backend`                                                               |
| `SPRING_CLOUD_GATEWAY_ROUTES_<index>_URI`                | The URI to route to if this route matches.                      | `http://backend-service:8080/`                                          |
| `SPRING_CLOUD_GATEWAY_ROUTES_<index>_PREDICATES_<index>` | Route predicates i.e. matcher.                                  | `Path=/api/backend-service/**`                                          |
| `SPRING_CLOUD_GATEWAY_ROUTES_<index>_FILTERS_<index>`    | List of filters applied to the route.                           | `RewritePath=/api/backend-service/(?<urlsegments>.*), /$\{urlsegments}` |
| `REFARCH_HAZELCAST_SERVICENAME`                          | Kubernetes service name for when using profile `hazelcast-k8s`. |                                                                         |
| `ALLOWED_ORIGINS_PUBLIC` (optional)                      | List of URIs allowed as origin for public routes.               | `https://*.example.com,http://localhost:*`                              |
| `ALLOWED_ORIGINS_CLIENTS` (optional)                     | List of URIs allowed as origin for clients routes.              | `https://*.example.com,http://localhost:*`                              |
| `REFARCH_SECURITY_CSRFWHITELISTED_<index>` (optional)    | List of routes to disable CSRF protection for.                  | `/example/**`                                                           |

### Security

For authentication via SSO, OAuth 2.0 needs to be configured.
See following example or the [according Spring documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html).

Alternatively the `no-security` profile can be used.

```yaml
spring:
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
|-------------------|------------------------------------------------------------------------------------------------------|
| `json-logging`    | Switches logging from textual to JSON output.                                                        |
| `no-security`     | Disables all security mechanisms (e.g. authentication, authorization, CSRF) Routing works as normal. |
| `hazelcast-local` | Configures Spring Session Hazelcast for connection via localhost (i.e. local development).           |
| `hazelcast-k8s`   | Configures Spring Session Hazelcast for usage in Kubernetes/OpenShift cluster.                       |

### Hazelcast

Beside the already mentioned properties Hazelcast also has the following requirements.

#### Modular java
See https://docs.hazelcast.com/hazelcast/5.5/getting-started/install-hazelcast#using-modular-java

Following Java options need to be set.
For the gateway image this can be done with `JAVA_OPTS_APPEND`.
```
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