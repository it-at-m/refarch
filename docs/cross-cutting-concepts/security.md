# Security

This page informs about certain security related topics.

## Disable SBOM Exposing

All services developed with the [templates](/overview#Templates), as well as all ready-to-use components like the [API Gateway](/overview#api-gateway) expose a SBOM (Software Bill of Materials) endpoint at `/actuator/sbom/application` by default.

This can be a problem for closed-source projects using any of the RefArch components, as it exposes information about the software dependencies and might thus lead to expose of critical CVEs.

More information about SBOMs in general and the [CycloneDX](https://cyclonedx.org/) format we use, can be found in the [official CycloneDX documentation](https://cyclonedx.org/specification/overview/).

The following sections will describe how to disable the SBOM endpoint in the various components.

### API Gateway

The API Gateway is built using [cyclonedx-maven-plugin](https://github.com/CycloneDX/cyclonedx-maven-plugin) and the generated SBOM file is exposed via the Spring-provided [SBOM actuator endpoint](https://docs.spring.io/spring-boot/api/rest/actuator/sbom.html).

To disable the exposure the endpoint must be disabled through runtime configuration.
This can be easily achieved by e.g. setting the environment variable `MANAGEMENT_ENDPOINT_SBOM_ACCESS` to `none` or alter the underlying Spring property.

### Java-based templates

Java-based templates use the same SBOM generation as the [API Gateway](#api-gateway).

Thus, the SBOM exposure can be disabled the same way using runtime configuration.
Alternatively, it can be disabled by altering the `application.yml` file and removing `sbom` endpoint:

```yaml
management:
  endpoints:
    web:
      exposure:
        include:
          - sbom # remove this line
  endpoint:
    sbom: # remove this line
      access: read_only # remove this line
```

### Node-based templates

The SBOM file is generated using [cyclonedx-node-npm](https://github.com/CycloneDX/cyclonedx-node-npm) and directly integrated into the `npm run build` script.
It's placed inside the `dist` folder together will all other static resources and then served via the nginx server at runtime of the Docker image through a custom defined endpoint.

To disable the exposure the behaviour of the nginx server has to be altered.
This can be easily achieved by removing the following content from the file `sbom.conf` in the `docker/nginx` directory of the corresponding template.

```text
location /actuator/sbom/application {
    access_log off;
    add_header 'Content-Type' 'application/json';
    add_header 'Access-Control-Allow-Methods' 'GET, OPTIONS' always;
    add_header Cache-Control 'no-cache';
    alias /opt/app-root/src/application.cdx.json;
}
```

:::danger Important
Note that you must not remove the file `sbom.conf` altogether as this enables serving the generated SBOM file as a regular static resource.
:::
