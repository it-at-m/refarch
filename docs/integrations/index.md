# Integrations

Integrations are ready to use implementations for accessing often used services in own applications without re-implementing the same logic.
These enable reduced coding effort and maintenance as well as standardized programming interfaces.

## Compatibility

The following table shows compatibility of the libraries with relevant technologies.

| Integrations Version  |   Java    | Spring Boot  |
| :-------------------: | :-------: | :----------: |
| **1**.x.x - **3**.x.x | >= **21** |  **3**.x.x   |
|       **4**.x.x       | >= **21** | >= **4**.x.x |

::: info Note
The table only lists tested constellations. Other combinations not mentioned may still work. However, please note that **no support** is available for these configurations.
:::

## Integrations

- [s3-integration](./s3.md): For CRUD operations on a S3 storage.
- [email-integration](./email.md): For sending text and HTML emails with attachments.
- [dms-integration](./dms.md): For CRUD operations on a DMS system in specific fabasoft.
- [cosys-integration](./cosys.md): For creating documents with coSys.
- [address-integration](./address.md): For reading, searching and validating addresses via the LHM address service.

## Naming conventions

The different submodules of integrations follow following naming conventions:

- `*-client`: Interface for accessing the API of the underlying service.
- `*-core`: Implementation of the base functionality of the integration in hexagonal architecture.
- `*-starter`: Provides Beans of core services.
- `*-service`: A Spring application using the starter. It is provided as container image and should not be used as dependency.
- `*-example`: An example Spring application for testing and reference. It should not be used as dependency or in real environments.

### Dependency graph

The following graph shows the relationships between the various modules and how they interact and rely on each other.

```mermaid
flowchart RL
    service --> starter --> core --> client
```
