# Deploy

The templates include configuration files to simplify deployment of applications. Those are further explained below.

## Docker Images

All templates include a simple `Dockerfile` to build Docker images. The following base images are used:

- Java-based templates (`refarch-backend` and `refarch-eai`): [RedHat UBI OpenJDK Runtime](https://rh-openjdk.github.io/redhat-openjdk-containers/index.html)
- JavaScript-based templates (`refarch-frontend` and `refarch-webcomponent`): RedHat UBI Nginx (e.g. [`ubi10/nginx-126`](https://catalog.redhat.com/en/software/containers/ubi10/nginx-126/677d3735607921b4d7503cf3))

## Helm Chart

[Helm](https://helm.sh/) allows easy deployment of multi-container applications to a Kubernetes cluster using charts.

For RefArch-based applications, there are multiple ways to use Helm charts with increasing customizability and manual effort.

### Variant 1: Direct use of `refarch-templates` chart (recommended)

The reference architecture provides a [Helm chart](https://github.com/it-at-m/helm-charts/tree/main/charts/refarch-templates) to easily deploy RefArch-based multi-container applications by just providing a configuration file.
The release notes of this chart can be found in the [GitHub releases](https://github.com/it-at-m/helm-charts/releases?q=refarch-templates) of the it@M Helm Charts repository.

Each application container is called a "module" in the `refarch-templates` chart.
Additionally, a [RefArch API Gateway](../gateway.md) can be deployed as well.

:::details Sample configuration file

```yaml
# myapp-config.yaml
secrets:
  - name: sso-credentials
    keys:
      - URL # e.g. https://sso.example.com
      - REALM
      - CLIENTID
      - CLIENTSECRET
  - name: db-credentials
    keys:
      - URL # e.g. jdbc:postgresql://db.example.com:5432/mydb
      - USERNAME
      - PASSWORD

# Module-specific configuration for own application containers
modules:
  - name: frontend
    image:
      registry: ghcr.io
      repository: myorg/myapp-frontend
      pullPolicy: IfNotPresent
      tag: "1.0.0"
    service:
      http: true
  - name: backend
    image:
      registry: ghcr.io
      repository: myorg/myapp-backend
      pullPolicy: IfNotPresent
      tag: "1.0.0"
    envFrom:
      - prefix: SSO_
        secretRef:
          name: sso-test
      - prefix: SPRING_DATASOURCE_
        secretRef:
          name: db-credentials
    env:
      - name: TZ
        value: "Europe/Berlin"
      - name: REFARCH_SECURITY_LOGGINGMODE
        value: "all"
      # mappings, normally don't have to be changed
      - name: REFARCH_SECURITY_CLIENTID
        value: "${SSO_CLIENTID}"
      - name: REFARCH_SECURITY_USERINFOURI
        value: "${SSO_URL}/auth/realms/${SSO_REALM}/protocol/openid-connect/userinfo"
      - name: REFARCH_SECURITY_PERMISSIONS_URI
        value: "${SSO_URL}/auth/realms/${SSO_REALM}/protocol/openid-connect/token"
      - name: SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWKSETURI
        value: "${SSO_URL}/auth/realms/${SSO_REALM}/protocol/openid-connect/certs"
    service:
      http: true

# API Gateway configuration
refarch-gateway:
  envFrom:
    - prefix: SSO_
      secretRef:
        name: sso-credentials
  envAppend:
    - name: SPRING_PROFILES_ACTIVE
      value: "hazelcast-k8s"
    - name: ALLOWED_ORIGINS_PUBLIC
      value: "https://*.example.com"
    - name: SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_SSO_SCOPE
      value: "profile, openid"
    - name: SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_SSO_PROVIDER
      value: "sso"
    # mappings, normally don't have to be changed
    - name: SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI
      value: "${SSO_URL}/auth/realms/${SSO_REALM}"
    - name: SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_SSO_ISSUER_URI
      value: "${SSO_URL}/auth/realms/${SSO_REALM}"
    - name: SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_SSO_CLIENTID
      value: "${SSO_CLIENTID}"
    - name: SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_SSO_CLIENTSECRET
      value: "${SSO_CLIENTSECRET}"
  applicationYML:
    spring:
      cloud:
        gateway:
          server:
            webflux:
              routes:
                - id: "sso"
                  uri: ${SSO_URL}
                  predicates:
                    - "Path=/api/sso/userinfo"
                  filters:
                    - "RewritePath=/api/sso/userinfo, /auth/realms/${SSO_REALM}/protocol/openid-connect/userinfo"
                - id: "backend"
                  uri: "http://<HELM_RELEASE_NAME>-backend:8080/"
                  predicates:
                    - "Path=/api/myapp-backend/**"
                  filters:
                    - "RewritePath=/api/myapp-backend/(?<urlsegments>.*), /$\\{urlsegments}"
                # The catch all route needs to be at the end
                - id: "frontend"
                  uri: "http://<HELM_RELEASE_NAME>-frontend:8080/"
                  predicates:
                    - "Path=/**"
  ingress:
    enabled: true
    hosts:
      - host: myapp.example.com #
        paths:
          - path: /
            pathType: "ImplementationSpecific"
```

:::

:::info Information
Detailed information about all configuration options for the `refarch-templates` Helm chart can be found in its [README](https://github.com/it-at-m/helm-charts/tree/main/charts/refarch-templates#refarch).
:::

The configuration file can then be used to install the chart to your cluster with the following commands:

```bash
helm pull refarch-templates --version <HELM_CHART_VERSION> --repo https://it-at-m.github.io/helm-charts --untar
helm upgrade --install <HELM_RELEASE_NAME> refarch-templates --values=myapp-config.yaml --force-conflicts=true --server-side=true --rollback-on-failure
```

::: details it@M internal configuration
We provide an [internal IaC example repository](https://git.muenchen.de/ccse/refarch/refarch-iac) that implements this variant.
:::

### Variant 2: `refarch-templates` chart as dependency for an application-specific chart

The `refarch-templates` chart can be used as a dependency for application-specific charts through [Helm dependencies](https://helm.sh/docs/helm/helm_dependency/).
This enables setting specific values for the `refarch-templates` chart (e.g. image names or versions) and embedding those values statically into the application-specific chart.
Additionally, the chart can be further enhanced with custom configurations.

A simple `Chart.yaml` could look like this:

```yaml
apiVersion: v2
name: myapp-chart
description: Helm Chart for deploying my app using refarch-templates as dependency.
type: application
version: 1.0.0 # Version of your own Helm chart
dependencies:
  - name: refarch-templates
    version: 1.0.0 # Version of refarch-templates chart to use
    repository: "@it-at-m"
```

:::info Information
More information about creating Helm charts can be found in the [official Helm documentation](https://helm.sh/docs/topics/charts/).
:::

### Variant 3: Application-specific chart only

Creating a custom Helm chart allows for the definition of all required Kubernetes resources. This approach provides complete control over the configuration but requires a high level of effort.

:::danger Important
Using this variant is not recommended. It is advisable to explore variants 1 or 2 first. If any features are found to be lacking, an issue can be opened in the [it@M Helm Charts](https://github.com/it-at-m/helm-charts) repository.
:::
