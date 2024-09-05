# RefArch integrations

Collection of different integration which can be used as is in RefArch projects.

## Integrations

- [s3-integration](./refarch-s3-integration/README.md): For CRUD operations on a s3 storage. Also used for file handling
  in other integrations.
- [email-integration](./refarch-email-integration/README.md): For sending text and html emails with attachments. Uses
  s3-integration for file handling.
- [cosys-integration](./refarch-cosys-integration/README.md): For creating documents with cosys. Uses
  s3-integration for file handling.

## Naming conventions

The different submodules of integrations follow following naming conventions:

- `*-core`: Implementation of the base functionality of the integration in hexagonal architecture.
- `*-starter`: Provides Beans of core services.
- `*-service`: A Spring application using the starter. Is provided as container image. Should not be used as dependency.
