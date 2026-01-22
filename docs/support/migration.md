# Migration

Following are migration guides for breaking changes introduced in the RefArch which require some more work.

## Split gateway and frontend

Main change: separated centrally maintained gateway -> separate image for frontend

### Prerequisites

In general the migration can be done if the gateway was never modified (e.g. some custom filters).

Even if some changes were introduces there is the possibility to migrate, by either switching to some other already
included feature (e.g. [client routes](../gateway.md#routing)) or by contributing the changes to the central gateway if they can also be useful
for other projects.

### Steps

The migration consists of two parts: extracting the frontend and using the centralized gateway

#### Frontend

- Extract the frontend code into a separate repo or folder (in a mono repo)
- Copy `Dockerfile` and nginx config `docker/nginx/` from [`refarch-frontend`](https://github.com/it-at-m/refarch-templates/tree/main/refarch-frontend) template
- Adopt and configure `maven-node-build.yml` and `npm-release.yml` GitHub Actions workflows, see [`.github`](https://github.com/it-at-m/refarch-templates/tree/main/.github/workflows)
  - For internal GitLab migration see [according docs](https://git.muenchen.de/ccse/cicd/docs-gitlab-runner/-/blob/main-2.x/Migration.adoc?ref_type=heads#user-content-migration-refarch-api-gateway-integriertes-frontend-eigener-service-f%C3%BCr-frontend)

#### Gateway

For local development adopt the [development stack](../templates/develop.md#container-engine) (containing the gateway) from
the RefArch templates. Changing of the routes might be required to match the previous setup by modifying the environment
variables inside the Docker compose file.

For deploying the central Gateway use the [according container image](../gateway.md#usage) and compare your working config with the `application.yml`,
`application-local.yml` (inside [gateway code `resources` folder](https://github.com/it-at-m/refarch/tree/main/refarch-gateway/src/main/resources))
and [configuration section](../gateway.md#configuration) to find properties which need to be migrated.

# S3-Integration v3

## Removed without replacement

- separate s3 service (image: `refarch/s3-integration-rest-service`)
- file size and type validation
- delete folder method

## Migrate

- Replace `DocumentStorageFileRepository` with `FileOperationsInPort`
  - Some methods were merged
- Replace `DocumentStorageFolderRepository` with `FolderOperationsInPort`
  - Some methods were merged
