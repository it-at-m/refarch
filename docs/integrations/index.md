# Integrations

Integrations are ready to use implementations for accessing often used services in own applications without re-implementing the same logic. 
These enable reduced coding effort and maintenance as well as standardized programming interfaces.

## Integrations

- [s3-integration](./s3.md): For CRUD operations on a S3 storage. Also used for file handling
  in other integrations.
- [email-integration](./email.md): For sending text and HTML emails with attachments. Uses
  s3-integration for file handling.
- [dms-integration](./dms.md): For CRUD operations on a DMS system in specific fabasoft. Uses
  s3-integration for file handling.
- [cosys-integration](./cosys.md): For creating documents with coSys. Uses
  s3-integration for file handling.
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
