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

## Move from centralized router configuration to file-based routing

Main change: routes can now be configured more easily via the directory structure

::: info Information
This migration is fully optional.
:::

### Prerequisites

The migration can be done when using the new Vue Router v5 or higher.
For more detailed background information take a look at the official [Migrating to Vue Router 5](https://router.vuejs.org/guide/migration/v4-to-v5.html) guide
and the now integrated [Unplugin Vue Router](https://uvr.esm.is/).

### Steps

1. Update the Vite configuration (`vite.config.ts`) to include the VueRouter plugin. This enables generation of a typed interface (`route-map.d.ts`) containing all routes.
2. Update TypeScript, Prettier, ESLint and `.gitignore` configurations to make them aware of the new file.
3. Update your Vue Router configuration to include the auto-generated routes by `import routes from "vue-router/auto-routes"` and remove the old explicit imports.
4. Move your old Vue view components from `views` to a new directory named `routes`
5. Rename your old view components. The new filename will be the name of the automatically generated route.
   You can also use subfolders, e.g. `/routes/test/component.vue` will resolve to the route `/test/component`.
6. Optionally customize the name, path, meta fields and more using the `definePage` utility inside the `<script>` of your Vue component.

A complete example for the required changes can be found in [this PR](https://github.com/it-at-m/refarch-templates/pull/1393).
