# RefArch Gateway

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

By default, routes require authentication through oAuth2 and manage the session between the client and gateway using
cookies.
The gateway then maps the session cookie to a JWT before routing it.

Beside the default behaviour there are some special route prefixes which are handled different:

- `/public/**`: All `OPTIONS` and `GET` requests are routed without security.
- `/clients/**`: Uses JWT for authenticating incoming requests instead of session cookies.

## Profiles

| Profile           | Description                                                                                       |
|-------------------|---------------------------------------------------------------------------------------------------|
| `json-logging`    | Switches logging from textual to JSON output.                                                     |
| `no-security`     | Disables complete security like authentication, authorization, csrf etc. Routing works as normal. |
| `hazelcast-local` | Configures Spring Session Hazelcast for connection via localhost (i.e. local development).        |
| `hazelcast-k8s`   | Configures Spring Session Hazelcast for usage in Kubernetes/OpenShift cluster.                    |

## Configuration

| Var                                                      | Description                                        | Example                                                                 |
|----------------------------------------------------------|----------------------------------------------------|-------------------------------------------------------------------------|
| `SPRING_PROFILES_ACTIVE`                                 | See profiles                                       | `local,hazelcast-local`                                                 |
| `SSO_ISSUER_URL`                                         | Url of the oAuth2 server used for authentication.  | `https://sso.muenchen.de/auth/realms/muenchen.de`                       |
| `SSO_CLIENT_ID`                                          | OAuth2 client id used for authentication.          |                                                                         |
| `SSO_CLIENT_SECRET`                                      | OAuth2 client secret used for authentication.      |                                                                         |
| `SPRING_CLOUD_GATEWAY_ROUTES_<index>_ID`                 | Id of a route definition.                          | `backend`                                                               |
| `SPRING_CLOUD_GATEWAY_ROUTES_<index>_URI`                | The uri to route to if this route matches.         | `http://backend-service:8080/`                                          |
| `SPRING_CLOUD_GATEWAY_ROUTES_<index>_PREDICATES_<index>` | Route predicates i.e. matcher.                     | `Path=/api/backend-service/**`                                          |
| `SPRING_CLOUD_GATEWAY_ROUTES_<index>_FILTERS_<index>`    | List of filters applied to the route.              | `RewritePath=/api/backend-service/(?<urlsegments>.*), /$\{urlsegments}` |
| `ALLOWED_ORIGINS_PUBLIC` (optional)                      | List of urls allowed as origin for public routes.  | `https://*.muenchen.de,http://localhost:*`                              |
| `ALLOWED_ORIGINS_CLIENTS` (optional)                     | List of urls allowed as origin for clients routes. | `https://*.muenchen.de,http://localhost:*`                              |
| `REFARCH_SECURITY_CSRFWHITELISTED_<index>` (optional)    | List of routes to disable csrf protection for.     | `/example/**`                                                           |
