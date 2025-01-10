# Integrations

Collection of different integration which can be used as is in RefArch projects.

## Integrations

- [s3-integration](./s3.md): For CRUD operations on a s3 storage. Also used for file handling
  in other integrations.
- [email-integration](./email.md): For sending text and html emails with attachments. Uses
  s3-integration for file handling.
- [dms-integration](./dms.md): For CRUD operations on a dms system in specific fabasoft. Uses
  s3-integration for file handling.
- [cosys-integration](./cosys.md): For creating documents with coSys. Uses
  s3-integration for file handling.
- [address-integration](./address.md): For reading, searching and validating addresses.

## Naming conventions

The different submodules of integrations follow following naming conventions:

- `*-core`: Implementation of the base functionality of the integration in hexagonal architecture.
- `*-starter`: Provides Beans of core services.
- `*-service`: A Spring application using the starter. Is provided as container image. Should not be used as dependency.
