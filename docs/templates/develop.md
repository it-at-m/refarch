# Develop

We include various development tools to simplify the development process and make development more comfortable and error-resistant.
Those tools are further explained below.

::: danger IMPORTANT
Please make sure you worked through the corresponding [Getting Started](./getting-started) instructions before proceeding.
:::

## Technologies

Key technologies used in the templates include:

### Container Engine

[Podman](https://podman.io) is suggested to run a local development stack including all necessary services. Alternatively [Docker](https://www.docker.com/)
can be used as well.

::: danger IMPORTANT
If you are developing locally, you will need to have Podman or Docker installed on your system and the stack running at all times.
Also make sure to have `keycloak` in your `hosts` file.
:::

Inside the `stack` folder, you will find a `docker-compose.yml` file that will spin up everything needed for local development.
You can spin up the stack by using the integrated container features of your favorite IDE, by using a dedicated UI
or by executing the command `podman compose up -d` or `docker compose up -d` from within the `stack` folder.

Stack components (as OCI images):

- [Keycloak](https://www.keycloak.org/): Keycloak instance as a local SSO provider
- [Keycloak Migration](https://mayope.github.io/keycloakmigration/): Migration tool to set up the local SSO provider by executing scripts upon startup, configured via `.yml` files in `stack/keycloak/migration`
- [PostgreSQL](https://www.postgresql.org): Database instance for application data
- [pgAdmin](https://www.pgadmin.org/): Database management UI pre-configured to connect to the local PostgreSQL instance
- [API Gateway](../gateway): API gateway of the RefArch, configured via [environment variables](../gateway#configuration) in `docker-compose.yml`
- [Appswitcher Server](https://github.com/it-at-m/appswitcher-server): Server component to access local development tools via the frontend UI

::: info Information
Some tools provide local Browser-based UIs. We encourage you to use the UI provided by App Switcher to open them.
:::

If you need specific tools for your project that go beyond what the stack offers by default feel free to adjust the `docker-compose.yml` file.

::: info Information
If you add new containers to your development stack, please make sure those images provide a [healthcheck](https://docs.docker.com/reference/dockerfile/#healthcheck) out of the box.
If they don't, you can add [own healthchecks](https://docs.docker.com/reference/compose-file/services/#healthcheck) in your `docker-compose.yml` file.
The health checks make sure your containers are starting properly and are healthy throughout usage.
The health of the development stack is automatically validated in GitHub CI/CD using the provided/manually added healthchecks.
:::

::: info Information
When using Podman, make sure to configure the host-gateway correctly by setting `host_containers_internal_ip = "192.168.127.254"`
in `/etc/containers/containers.conf` and use the [User-Mode-Networking](https://docs.podman.io/en/v4.7.2/markdown/podman-machine-set.1.html#user-mode-networking).
:::

::: danger IMPORTANT
If for some reason you cannot add health checks to a container (this might be the case when a barebone base image is used and the container does not have tools like `curl` or `wget`)
you need to set `skip-no-healthcheck: true` as described in the [CI/CD documentation](https://github.com/it-at-m/lhm_actions/blob/main/docs/actions.md#action-dockercompose-healthcheck) in order for the CI/CD to pass.
:::

### Vite

[Vite](https://vite.dev/) is used as the build tool for JavaScript-based projects, along with the testing framework [Vitest](https://vitest.dev/).

The following npm scripts are provided for working with those tools:

- Start Vite development server: `npm run dev`
- Run Vitest test execution: `npm run test`
- Build the Vite project (for production): `npm run build`

::: danger IMPORTANT
When you experience a refresh loop while developing with the Vite development server, please make sure to re-login via the running API Gateway.
To avoid this problem, we recommend accessing the development server using the API Gateway as a proxy.
Benefits like Hot Module Reloading (HMR) still work when tunneling.
:::

### Maven

[Maven](https://maven.apache.org/) is used as the build tool for Java-based projects.

The following maven commands are useful when working locally:

- Compile the application and execute tests: `mvn clean verify`  
  (add `-DskipTests` to skip test execution)
- Run the application: `mvn spring-boot:run -Dspring-boot.run.profiles=local`

::: info Information
Instead of compiling and running the application using the commands above, you can also use the features of your IDE directly.
:::

By default, two different Spring profiles are provided to run the application:

- `local`: Uses the local container stack to run the application and provides useful logging information while developing
- `no-security`: Disables all security mechanisms

### Component libraries

We use the following component libraries to accelerate our frontend development and standardize the look and feel of our applications:

- Development of standalone web applications and SPAs: [Vuetify](https://vuetifyjs.com/en/)
- WebComponent development for integration with [official Munich website](https://www.muenchen.de/): [PatternLab](https://it-at-m.github.io/muc-patternlab-vue/?path=/docs/getting-started--docs)

::: info Information
No explicit imports inside the `<script>` of your custom Vue components are necessary when importing Vuetify components.
This is automatically handled by Vite using the [Vuetify Vite Plugin](https://github.com/vuetifyjs/vuetify-loader/tree/master/packages/vite-plugin).
:::

### Code Quality

#### JavaScript / TypeScript / Vue

[Prettier](https://prettier.io/) and [ESLint](https://eslint.org/) are used for linting and code formatting JavaScript, TypeScript and Vue-based code.
Additionally, [vue-tsc](https://github.com/vuejs/language-tools/tree/master/packages/tsc) is used for running type-checking when working with TypeScript.

You can run those tools in combination by using the following npm scripts:

- Lint your source code: `npm run lint`
- Autofix issues: `npm run fix`

::: info Information
Not all issues are auto-fixable, so you still might have some manual work to do after running the command.
:::

The tools are configured through the respective configuration files

- Prettier: `.prettierrc.json` (points to a [centralized configuration](https://github.com/it-at-m/itm-prettier-codeformat))
- ESLint: `eslint.config.js` (configuration part of the templates)

#### Java

[Spotless](https://github.com/diffplug/spotless), [PMD](https://pmd.github.io/) and [SpotBugs](https://spotbugs.github.io/) are used for code formatting and linting Java-based code.
Additionally, [find-sec-bugs](https://github.com/find-sec-bugs/find-sec-bugs) is used to check for vulnerabilities inside your code.

Those tools are configured inside the `pom.xml` files and automatically run when executing the respective Maven phases. (e.g. `mvn verify`)
Alternatively you can also run the custom maven goals provided by those plugins:

- Run Spotless formatting check: `mvn spotless:check`
- Run Spotless formatting autofix: `mvn spotless:apply`
- Run PMD lint check: `mvn pmd:check`
- Run PMD CPD ([Copy/Paste Detector](https://pmd.github.io/pmd/pmd_userdocs_cpd.html)) check: `mvn pmd:cpd-check`
- Run SpotBugs lint check: `mvn spotbugs:check`  
  (**Note**: Requires project compilation before execution when code changes were made)

::: info Information
Issues reported by the PMD and SpotBugs are currently not auto-fixable so you still have some manual work to do.
:::

The tools are configured through the respective configuration files or configuration sections inside the `pom.xml`

- Spotless: `pom.xml` and using a [centralized configuration](https://github.com/it-at-m/itm-java-codeformat)
- PMD: `pom.xml` and using centralized configuration (more information in [Tools](/cross-cutting-concepts/tools#pmd))
- SpotBugs: `pom.xml` and `spotbugs-exclude-rules.xml` (configuration part of the templates)

::: danger IMPORTANT
Spotless downloads additional P2 dependencies from `download.eclipse.org`
to make use of the Eclipse JDT tooling required for formatting the Java code.
You might not be able to access `download.eclipse.org` from your machine directly.
This can be the case when you are behind a proxy or want to use a company internal P2 mirror.
To make the setup work in this case, add the following XML content to the Maven `settings.xml` file
inside the `<profile>` block and adjust it as needed:

```xml
<properties>
    <p2.username>my_user</p2.username>
    <p2.password>my_token_or_password</p2.password>
    <p2.mirror>registry.example.com/mycustomp2mirror/</p2.mirror>
</properties>
```

Additionally, the following properties have to be added to the `pom.xml` file to provide default values when no custom `settings.xml` is used (e.g. execution in CI environments)

```xml
<properties>
    <p2.username/>
    <p2.password/>
    <p2.mirror>download.eclipse.org</p2.mirror>
</properties>
```

To use the custom properties for the actual P2 mirror configuration, add the following content inside the `<eclipse>` section of the `spotless-maven-plugin` configuration:

```xml
<p2Mirrors>
    <p2Mirror>
        <prefix>https://download.eclipse.org</prefix>
        <url>https://${p2.username}:${p2.password}@${p2.mirror}</url>
    </p2Mirror>
</p2Mirrors>
```

:::

::: details it@M internal configuration
If you are working behind our company internal Artifactory please set `p2.mirror` mentioned above to `artifactory.muenchen.de/artifactory/download.eclipse.org/`.
:::

### Vue Dev Tools

The [Vue Dev Tools](https://devtools.vuejs.org/) provide useful features when developing with Vue.js. Those include checking and editing component state, debugging the [Pinia](https://pinia.vuejs.org/) store, testing client-side routing, inspecting page elements and way more.

The Vue Dev Tools are included as a development dependency inside the templates, so no further installation is required.

A useful feature is the inspection of elements, which allows you to click components of your webpage inside your Browser-rendered application and open the relevant part right in your IDE.
To make use of this feature, a few steps have to be made on your machine.

::: info Information
If you use Visual Studio Code, no further configuration has to be done. You can simply ignore the steps mentioned below.
:::

Steps to set up the IDE connection for Dev Tools:

1. Make sure your IDE of choice can be accessed via your terminal environment (Some installers automatically add your IDE to the `PATH` variable, for some cases you might have to add it manually)
2. Add a new environment variable for your shell environment called `LAUNCH_EDITOR` (depending on your operating system you can use files like `.bashrc` or the management feature of your OS)
3. Set the value of `LAUNCH_EDITOR` to the name of your IDE executable (e.g. `idea`, `webstorm`, `codium`, `notepad++`)
4. Make sure the environment variable is loaded (you might have to re-login into your user account depending on your OS)

::: info Information
Not all IDEs are supported right now, please check out [supported editors](https://github.com/webfansplz/vite-plugin-vue-inspector?tab=readme-ov-file#supported-editors) of the corresponding Vite plugin.
:::

### Database Migration

[Flyway](https://documentation.red-gate.com/flyway) is used as our tool for database migration.

It runs automatically when starting the backend application.
Additionally, the following maven goals can be run manually:

- Clean database: `mvn flyway:clean -Dflyway.cleanDisabled=false`
- Apply migrations: `mvn flyway:migrate`
- Reset and migrate: `mvn flyway:clean flyway:migrate -Dflyway.cleanDisabled=false`

To maintain your migration files, check the folder `db.migration` inside the `resources` folder of the Java project.
For more information about how to work with Flyway, checkout its [Getting Started guide](https://documentation.red-gate.com/flyway/getting-started-with-flyway)

### App Switcher

The [App Switcher](https://github.com/it-at-m/appswitcher-server) is a feature accessible from the app bar in the frontend.

While developing, this is especially useful to access useful development tools tied to the local container stack.
This includes the Keycloak management UI, pgAdmin to check the application database and a possibility to open Vue DevTools in a separate browser tab.

::: info Information
The configuration in the `application.yml` file (inside the `appswitcher-server` directory of the stack) can be modified to include additional tools required for your specific project setup.
:::

## Lifecycle Management (LCM)

[Renovate](https://docs.renovatebot.com/) is used to keep your dependencies up to date.

The templates by default make use of a centralized configuration we provide for RefArch-based projects. More information can be found in [Tools](/cross-cutting-concepts/tools#renovate).

::: danger IMPORTANT
To make full use of the provided configuration additional setup is needed, so please check out the corresponding [Tools documentation](/cross-cutting-concepts/tools#renovate).
:::

## Pull Requests

When a pull request (PR) is created, several tools help maintain code quality:

### Code Rabbit

**Code Rabbit** is an AI-powered code reviewer that assists with PR assessments. The configuration file can be found at the root of the templates in `.coderabbit.yaml`.

Our configuration enables automatic reviews (and follow-up reviews when changes to a PR have been made). Additionally, we set CodeRabbit in "nitpicky" mode to find all of those nasty bugs.
Feel free to customize the configuration to your own needs. More information is available in the [official documentation](https://docs.coderabbit.ai/).

::: info Information
To make CodeRabbit work, make sure that it has access to your GitHub repository.
For projects in the `it-at-m` organization, CodeRabbit automatically has access and is enabled when the configuration file is found in your repository.
:::

::: danger IMPORTANT
Code Rabbit is free to use for open-source projects. If you are developing a project with no public visibility, you might need to remove the `.coderabbit.yaml` file.
:::

### CodeQL

**CodeQL** is a GitHub tool for discovering vulnerabilities and code smells in code. More details can be found on the [official CodeQL website](https://codeql.github.com/).

The template enables CodeQL for Pull Requests and configures CodeQL to only scan for Java and JavaScript/TypeScript/Vue files by default.
For further information on how to change the configuration, please check out the documentation of the related custom [GitHub workflow](https://github.com/it-at-m/.github/blob/main/workflow-templates/codeql.yaml).

::: danger IMPORTANT
If you are using Java-based projects inside your repository, you need to add those to the `java-build-path` variable pointing to the directory of the `pom.xml` files.
:::

### Dependency Review

To ensure that only dependencies with approved licenses are included, a [global check](https://github.com/it-at-m/.github/blob/main/workflow-configs/dependency_review.yaml) is implemented.
This is enabled by default when using the templates. To learn more about the Dependency Review feature itself, please check the official [GitHub documentation](https://docs.github.com/en/code-security/supply-chain-security/understanding-your-software-supply-chain/about-dependency-review).

The allowed licenses can be viewed in the [Munich Open Source documentation](https://opensource.muenchen.de/licenses.html#integration-in-in-house-developments).

### Require PR checklist

The templates provide a workflow for validating checklist status in a PR description and the PR discussion. To merge a PR, all checklist items must be ticked off by the PR creator.

The templates by default ship with a [PR template](./organize#pull-request-template), which makes use of a checklist.

::: info Information
If some of the PR checklist items are not relevant for your PR, you should adjust the checklist inside the PR description to the specific PR changes.
If you want to disable the feature completely, you need to remove the file `.github/workflows/pr-checklist.yml`.
:::

::: danger IMPORTANT
This functionality conflicts with the [Finishing touches](https://docs.coderabbit.ai/finishing-touches/docstrings/) feature of CodeRabbit. That's why this feature of CodeRabbit is disabled inside its configuration file by default.
If you don't use "Require PR checklist" you can re-enable this functionality by altering the `.coderabbit.yaml` file.
:::

### GitHub Rulesets

It is recommended to review the rulesets for pushing and merging in the GitHub repository. Depending on the project's branching strategy, some branches should be protected to prevent force pushes and merging without approval.
More information about Rulesets can be found in the [official GitHub documentation](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/creating-rulesets-for-a-repository).

::: danger IMPORTANT
Note that the tools mentioned above (CodeRabbit, CodeQL, Dependency Review) are optional by default and are not required to pass when a PR should be merged.
We strongly encourage you to enable the status checks for those tools before being able to merge a PR. More information about status checks can be found in the [official GitHub documentation](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/collaborating-on-repositories-with-code-quality-features/about-status-checks).
Status checks are configurable as part of the rulesets.
:::

### Problem Matchers

GitHub Actions has a feature called [Problem Matchers](https://github.com/actions/toolkit/blob/main/docs/problem-matchers.md) which enables the annotation of violations inside the GUI of the PR changes.
The templates provide a default configuration for this feature inside the file `.github/problem-matcher.json`.

::: info Information
Currently, the templates only configure this feature for the [linting of GitHub workflows](./develop#ci-cd-configurations). We might extend this to other tools in the future.
:::

## CI/CD Configurations

The `.github/workflows` folder contains various GitHub workflow files. Those reference centralized actions to simplify different parts of the CI/CD process.
It also helps to keep Lifecycle Management as simple as possible as no direct dependency on third-party actions exists.

More information about the centralized actions can be found in the [lhm_actions documentation](https://github.com/it-at-m/lhm_actions/blob/main/docs/actions.md).

::: danger IMPORTANT
Note that the CI/CD setup of the templates is in a Work-In-Progress state, so its subject to change in the near future.
:::

If you have specific needs in your project that go beyond what the default workflows offer, you can adjust the workflows to your own needs.
More information can be found in the [official GitHub documentation](https://docs.github.com/en/actions).

::: info Information
The templates by default ship with the workflow `actionlint.yml`. This workflow makes use of [actionlint](https://github.com/rhysd/actionlint) and ensures your workflows
are always syntactically correct. We therefore suggest that you do not remove this file.
:::

## CODEOWNERS

The **CODEOWNERS** file (found under the `.github` directory of the template) is an essential tool in version control systems like GitHub.

It specifies who is responsible for different parts of the project, ensuring that the right people are involved in code reviews.
When modifications are made to these files, the designated owners receive a review request automatically, enhancing code quality and accountability.

Each line identifies a file or directory along with the owner(s) using their GitHub usernames or team names.

::: danger IMPORTANT
Please alter the CODEOWNERS file to list project members or team names for your own project.
Otherwise, the RefArch maintainers have to approve all your code changes.
You definitely don't want that, as we are super nitpicky when it comes to code quality. ;)
:::

To learn more about the CODEOWNERS file, please check the official [GitHub documentation](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners).

## Internationalization (i18n)

The `refarch-frontend` template uses [Vue I18n](https://vue-i18n.intlify.dev/) by default to allow easy addition of multiple languages.
Currently, the template only provides the `i18n` mechanism to centralize the definition of text content in different languages and move raw text out of the Vue components.
The feature to allow easy switching of languages for the end user will be implemented in the future.

::: info Information
If you don't want to use i18n in your application, you can either:

- **Completely remove i18n** by removing the following dependencies: `@intlify/unplugin-vue-i18n`, `vue-i18n` and `@intlify/eslint-plugin-vue-i18n` and removing the `plugins/i18n.ts` file.

  (**Note**: Some manual corrections might still be necessary)

- **Soften the requirements for incremental adoption** by disabling linting rule [@intlify/vue-i18n/no-raw-text](https://eslint-plugin-vue-i18n.intlify.dev/rules/no-raw-text.html) inside the ESLint configuration file

  (**Note**: This allows mixing raw text in Vue components with texts maintained in locale files)

:::

More information on how to reference the translation file contents in your Vue components can be found in the official VueI18n documentation.
We suggest using the [Composition API style](https://vue-i18n.intlify.dev/guide/advanced/composition.html), as it matches the general use of Composition API when developing with Vue.

### Adding a new language

The template only provides the `de` (German) locale by default. To add a new language to the application, the following steps need to be done:

1. Add the translation file (JSON-based) to the `locales` folder
2. Import the translation file in the `plugins/i18n.ts` file
3. (Optional) If using Vuetify, import the pre-defined Vuetify translations
4. Extend the `messages` object to contain the new language key combining Vuetify-based and own translations, e.g.

```javascript
const messages = {
  en: {
    $vuetify: {
      ...enVuetify,
    },
    ...enApp,
  },
};
```

::: danger Important
Each `.json` file should have the same structure and the translation keys should be provided in all files.
Otherwise, some parts of your application will fall back to the default language (`de`).
Also, always use the correct ISO-639-1 code for your language.
:::

### Key Naming Conventions

We recommend a certain structure and set of conventions to help keep translations organized, consistent, and easy to maintain.

1. **camelCase Keys**
   - Use lowerCamelCase for individual keys, e.g. `home.header`, `app.name.full`.
   - This convention aligns with lint rules and keeps keys visually distinct from normal text.
2. **Hierarchical Grouping**
   - Organize keys by feature or component, using multiple nested levels for clarity.
   - Example:

     ```json
     {
       "home": {
         "header": "Welcome!",
         "paragraph": "Some text here..."
       }
     }
     ```

3. **Consistent Section Prefixes**
   - Maintain distinct top-level sections: e.g. `common`, `app`, `views`, `components`, `domain`, etc.
   - This consistency makes it easier to quickly locate strings in a large codebase.

### Section Organization Guidelines

1. `common`: For frequent, repeated terms like “yes”/“no” or action verbs like “close,” “save,” “cancel.”
2. `app`: For global application-related strings (e.g., brand name, headers).
3. `views`: For text used within specific pages/views (e.g., views.home.header).
4. `components`: For text that appears in reusable UI components.
5. `domain`: For translating domain-specific objects, like database fields or business entities.

### Special Character Handling

- When using special or reserved JSON characters (e.g. `"`, `{`), ensure they are properly quoted if needed.
- Complex strings may require double quotes to avoid parsing or lint errors, for example:

  ```json
  {
    "name": {
      "full": "@:app.name.part1@:app.name.part2"
    }
  }
  ```

- If you need placeholders in strings, consider using interpolation features offered by vue-i18n (e.g., `{item} saved.`).
