# LHM Actions

[[toc]]

This repository contains and provides actions to

- Build Maven projects and deploy them to Maven Central
- Build npm projects and deploy them to npmjs
- Build Docker images and push them to GitHub Container Registry
- Create GitHub releases
- Build VitePress documentation and deploy it to GitHub Pages
- You can use CodeQL to identify vulnerabilities and errors in your code. The results are shown as code scanning alerts in GitHub.

## Usage

### action-actionlint

Action to lint your workflows using [actionlint](https://github.com/rhysd/actionlint).

Executes the following steps:

1. Checkout code
2. Download specified actionlint version
3. Run actionlint on your workflow files

Workflows using that action need the following permissions:

| Permission       | Purpose                      | Required |
| ---------------- | ---------------------------- | :------: |
| `contents: read` | Checkout repository contents |   Yes    |

<!-- prettier-ignore -->
```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-actionlint
  with:
    # Version of actionlint to use
    # Default: latest
    version: "latest"

    # Whether to display findings in PR UI or not
    # Default: true
    display-findings: "true"

    # Path to the problem matcher file when using display-findings: true
    # Default: .github/problem-matcher.json
    problem-matcher-path: ".github/problem-matcher.json"
```

**Note**: The usage of `display-findings: true` requires additional setup.
See [actionlint documentation](https://github.com/rhysd/actionlint/blob/main/docs/usage.md#problem-matchers) for more information.

### action-build-docs

Action to build a VitePress docs project.

Executes the following steps:

1. Enables GitHub Pages
2. Build VitePress docs project
3. Uploads the build output as an artifact.<br/>
   The uploaded artifact can be used to deploy the docs to a web page (see [action-deploy-docs](#action-deploy-docs))

Workflows using that action need the following permissions:

| Permission       | Purpose                       | Required |
| ---------------- | ----------------------------- | :------: |
| `contents: read` | Checkout respository contents |   Yes    |

<!-- prettier-ignore -->
```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-build-docs
  with:
    # Path to VitePress docs project
    # Default: ./docs
    docs-path: "./docs"

    # Node version
    # Default: 22
    node-version: "22"

    # Build command to run (e.g. "vuepress build" for VuePress projects)
    # Default: build
    build-cmd: "build"

    # VitePress output path that will be uploaded as artifact
    # Default: .vitepress/dist
    dist-path: ".vitepress/dist"
```

### action-build-image

Action to build a Docker image and push it to a registry.

Executes the following steps:

1. Checkout code
2. Login to Registry
3. Extract metadata (tags, labels) for Docker
4. Build and push image to a registry

Workflows using that action need the following permissions:

| Permission        | Purpose                             | Required |
| ----------------- | ----------------------------------- | :------: |
| `contents: read`  | Checkout repository contents        |   Yes    |
| `packages: write` | Pushes the image to GitHub Packages |   Yes    |

<!-- prettier-ignore -->
```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-build-image
  with:
    # Image registry to push image to
    # Default: ghcr.io
    registry: "ghcr.io"

    # Username to authenticate against image registry
    registry-username: ${{ github.actor }}

    # Password to authenticate against image registry
    registry-password: ${{ secrets.GITHUB_TOKEN }}

    # Tags to tag image with
    # Default: type=raw,value=latest
    image-tags: type=raw,value=latest

    # Labels to add to image  
    # Default: org.opencontainers.image.description=See ${{ github.server_url }}/${{ github.repository }}
    # Optional
    image-labels: |
      org.opencontainers.image.description=See ${{ github.server_url }}/${{ github.repository }}

    # Path to the Dockerfile to build image from
    path: ${{ github.event.inputs.app-path }}

    # Name to give the image
    image-name: ${{ github.event.inputs.app-path }}

    # Name of the artifact to download
    artifact-name: ${{ needs.release-maven.outputs.ARTIFACT_NAME }}
```

### action-checkout

Action to wrap [GitHub Action Checkout](https://github.com/actions/checkout)
using current branch with default values.

Executes the following steps:

1. Checkout current branch

Workflows using that action need the following permissions:

| Permission       | Purpose                      | Required |
| ---------------- | ---------------------------- | :------: |
| `contents: read` | Checkout repository contents |   Yes    |

<!-- prettier-ignore -->
```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-checkout
  with:
    # Persist credentials or not
    # Default: false
    persist-credentials: false
```

### action-dockercompose-healthcheck

Action to wrap [docker-compose-health-check](https://github.com/marketplace/actions/docker-compose-health-check).
This action allows validating the functionality of containers by using health checks defined in the Docker compose file.

Executes the following steps:

1. Run health check using Docker Compose file

Workflows using that action need the following permissions:

| Permission       | Purpose                      | Required |
| ---------------- | ---------------------------- | :------: |
| `contents: read` | Checkout repository contents |   Yes    |

<!-- prettier-ignore -->
```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-dockercompose-healthcheck
  with:
    # Maximum number of retry attempts
    # Default: 10
    max-retries: 10
    
    # Interval between retries in seconds
    # Default: 10
    retry-interval: 10
    
    # Path to the docker compose file
    # Default: "./" (root directory)
    compose-file-path: "./"
    
    # Name of the docker compose file
    # Default: docker-compose.yml
    compose-file-name: "docker-compose.yml"
    
    # Skip checking exited containers (useful for init containers)
    # Default: false
    skip-exited: false

    # Skip checking containers without health checks
    # Default: false
    skip-no-healthcheck: false
```

**Note**: The usage of `skip-no-healthcheck: true` is only suggested when an image inside your stack does not provide a
health check and also the [definition of a custom healthcheck](https://github.com/peter-evans/docker-compose-healthcheck)
is not possible. This could be e.g. the case when a barebone Unix image (like `alpine`) is used and tools like `wget`
or `curl` are missing.

### action-filter

GitHub Action [dorny/paths-filter](https://github.com/dorny/paths-filter) enables conditional execution of workflow steps
and jobs, based on the files modified by pull request, on a feature branch, or by the recently pushed commits.

Executes the following steps:

1. Check changed files and directories

Output parameters:

- For each filter, it sets output variable named by the filter to the text:
  - 'true' - if any of changed files matches any of filter rules
  - 'false' - if none of changed files matches any of filter rules

Workflows using that action need the following permissions:

| Permission            | Purpose                              |                                        Required                                         |
| --------------------- | ------------------------------------ | :-------------------------------------------------------------------------------------: |
| `contents: read`      | Checkout repository contents         | Optional. Necessary to detect changes against long-lived branches (main, releases etc.) |
| `pull-requests: read` | Ability to read pull request content |       Optional. Necessary to detect changes against the pull request base branch        |

<!-- prettier-ignore -->
```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-filter@main
  id: changes
  with:
    # Defines filters applied to detected changed files.
    # Each filter has a name and a list of rules.
    # Rule is a glob expression - paths of all changed
    # files are matched against it.
    # Rule can optionally specify if the file
    # should be added, modified, or deleted.
    # For each filter, there will be a corresponding output variable to
    # indicate if there's a changed file matching any of the rules.
    # Optionally, there can be a second output variable
    # set to list of all files matching the filter.
    # Filters can be provided inline as a string (containing valid YAML document),
    # or as a relative path to a file (e.g.: .github/filters.yaml).
    # Filters syntax is documented by example - see examples section.
    # default value for filters
    # filters: |
    #  java:
    #  - '**/*.java'
    #  javascript-typescript-vue:
    #  - '**/*.js'
    #  - '**/*.cjs'
    #  - '**/*.mjs'
    #  - '**/*.ts'
    #  - '**/*.cts'
    #  - '**/*.mts'
    #  - '**/*.vue'
    #  python:
    #  - '**/*.py'
    filters: |
      src:
        - 'src/**'

  # run only if some file in 'src' folder was changed
- if: steps.changes.outputs.src == 'true'
  run: ...
```

### action-codeql

Action to scan a repository using provided CodeQL language, buildmode and query scan set

Executes the following steps:

1. Checkout repository
2. Setup JDK Version
3. Initialize CodeQL for language type
4. Build using Autobuild
5. Perform CodeQL analysis for language type

Workflows using that action need the following permissions:

| Permission               | Purpose                                                    | Required |
| ------------------------ | ---------------------------------------------------------- | :------: |
| `contents: read`         | Checkout repository contents                               |   Yes    |
| `security-events: write` | Report security problems (e.g. "Security and quality" tab) |   Yes    |

<!-- prettier-ignore -->
```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-codeql
  with:
    # CodeQL language name to scan with (e.g java-kotlin, javascript-typescript, python, ...)
    codeql-language: "java-kotlin"
    
    # Build mode to use when scanning the source code (e.g. none, autobuild, manual)
    # Default: none
    codeql-buildmode: "autobuild"

    # Query set to use when analyzing the source code (e.g. default, security-extended, security-and-quality)
    # Default: security-and-quality
    codeql-query: "security-and-quality"

    # Temurin JDK version to use for autobuild (only when codeql-language is java-kotlin and codeql-build is set to autobuild)
    # Default: 21
    java-version: "21"

    # Path to scan files in
    # Default: .
    path: "."
```

### action-create-github-release

Action to create a GitHub release.

Executes the following steps:

1. Download a single artifact
2. Create a GitHub release

Workflows using that action need the following permissions:

| Permission        | Purpose                   | Required |
| ----------------- | ------------------------- | :------: |
| `contents: write` | Create the GitHub Release |   Yes    |

<!-- prettier-ignore -->
```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-create-github-release
  with:
    # Name of the artifact to download
    artifact-name: my-artifact

    # Name of a tag (e.g. sps-1.0.0 or myproject-1.0.0)
    tag-name: myrepo-1.0.0

    # Path to the artifacts (e.g. ./target/*.jar)
    artifact-path: ./target/*.jar

    # Whether it is a draft release or not
    draft: false
    
    # Whether it is a prerelease or not
    prerelease: false
    
    # If release notes should be generated
    generate-release-notes: true
```

### action-dependency-review

The dependency review action scans your pull requests for dependency changes, and will raise an error if any
vulnerabilities or invalid licenses are being introduced. It will always use the baseline configuration in
<https://github.com/it-at-m/.github/blob/main/workflow-configs/dependency_review.yaml>.

Executes the following steps:

1. Checkout repository
2. Execute dependency review check

Workflows using that action need the following permissions:

| Permission             | Purpose                       |                                             Required                                             |
| ---------------------- | ----------------------------- | :----------------------------------------------------------------------------------------------: |
| `contents: read`       | Checkout repository contents  |                                               Yes                                                |
| `pull-requests: write` | Write comment to pull request | Optional. Necessary, if configuration `comment-summary-in-pr` is set to `always` or `on-failure` |

<!-- prettier-ignore -->
```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-dependency-review
  with:
    # Additional comma separated string of packages to be ignored by the dependency check (see https://github.com/package-url/purl-spec for more information)
    # Default: ""
     allow-dependencies-licenses: "pkg:maven/com.github.spotbugs/spotbugs-annotations, pkg:maven/com.h3xstream.findsecbugs:findsecbugs-plugin"
```

### action-deploy-docs

Action to deploy a docs artifact to a web page.

Executes the following steps:

1. Deploy docs to a web page

Workflows using that action need the following permissions:

| Permission        | Purpose                                                     | Required |
| ----------------- | ----------------------------------------------------------- | :------: |
| `pages: write`    | Deploy to GitHub Pages                                      |   Yes    |
| `id-token: write` | Verify the deployment originates from an appropriate source |   Yes    |

<!-- prettier-ignore -->
```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-deploy-docs
  with:
    # Name of the artifact to deploy
    # Default: github-pages
    artifact_name: "github-pages"

    # Branch to deploy docs from
    # Default: main
    deploy-branch: "docs"
```

### action-maven-build

Action to build a Maven project.

Executes the following steps:

1. Checkout repository
2. Setup Java version
3. Execute Maven build
4. Upload build artifact

Output parameters:

1. `artifact-name`: Name of the uploaded artifact

Workflows using that action need the following permissions:

| Permission       | Purpose                      | Required |
| ---------------- | ---------------------------- | :------: |
| `contents: read` | Checkout repository contents |   Yes    |

<!-- prettier-ignore -->
```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-maven-build
  with:
    # Java Version to use
    # Default: 21
    java-version: "21"

    # Path to pom.xml
    app-path: "."
```

### action-maven-release

Action to create a Maven release and deploy it to Maven Central.

Executes the following steps:

1. Checkout repository
2. Setup Java version
3. Execute Maven release and deploy it to Maven Central
4. Upload release artifact
5. Create PR for version bump (if enabled) using [peter-evans/create-pull-request](https://github.com/peter-evans/create-pull-request)

Output parameters:

1. `MVN_ARTIFACT_ID`: Artifact name of pom.xml
2. `artifact-name`: Name of the uploaded artifact

Workflows using that action need the following permissions:

| Permission             | Purpose                          |                             Required                             |
| ---------------------- | -------------------------------- | :--------------------------------------------------------------: |
| `contents: write`      | Change artifact's version number |                               Yes                                |
| `pull-requests: write` | Create a pull request            | Optional. Necessary if input parameter `use-pr` is set to `true` |

<!-- prettier-ignore -->
```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-maven-release
  with:
    # Java Version to use
    # Default: 21
    java-version: "21"

    # Path to pom.xml
    app-path: "."

    # Version which will be released
    releaseVersion:

    # Next snapshot version
    developmentVersion:

    # Skip deployment to maven central
    # Default: true
    skipDeployment: "false"

    # Use a PR for the version bump instead of directly pushing it
    # default: false
    use-pr: "true"

    # Environment variable for GPG private key passphrase
    SIGN_KEY_PASS: ${{ secrets.gpg_passphrase }}

    # Environment variable for Maven central username
    CENTRAL_USERNAME: ${{ secrets.sonatype_username }}

    # Env variable for Maven central token
    CENTRAL_PASSWORD: ${{ secrets.sonatype_password }}

    # Value of the GPG private key to import
    GPG_PRIVATE_KEY: ${{ secrets.gpg_private_key }}
```

### action-npm-build

Action to build an npm project.

Executes the following steps:

1. Checkout repository
2. Setup Node.js
3. Install dependencies
4. Run lint
5. Run test
6. Run build
7. Upload artifact

Output parameters:

1. `artifact-name`: Name of the uploaded artifact

Workflows using that action need the following permissions:

| Permission       | Purpose                      | Required |
| ---------------- | ---------------------------- | :------: |
| `contents: read` | Checkout repository contents |   Yes    |

<!-- prettier-ignore -->
```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-npm-build
  with:
    # Node Version to use
    # Default: 22.11.0
    node-version: "22.11.0"

    # Path to package.json
    app-path: "."

    # Controls the execution of the 'npm run lint' script
    # Default: true
    run-lint: "true"

    # Controls the execution of the 'npm run test' script
    # Default: true
    run-test: "true"
```

### action-npm-release

Action to create an npm release and deploy it to Node.js.

Executes the following steps:

1. Checkout repository
2. Setup Node.js version
3. Bump version and create Git tag
4. Create PR for version bump (if enabled) using [peter-evans/create-pull-request](https://github.com/peter-evans/create-pull-request)
5. Run npm build
6. Deploy npm artifact to Node.js

Output parameters:

1. `ARTIFACT_NAME`: Name of artifact
2. `ARTIFACT_VERSION`: Version of the uploaded artifact

Workflows using that action need the following permissions:

| Permission             | Purpose                          |                             Required                             |
| ---------------------- | -------------------------------- | :--------------------------------------------------------------: |
| `contents: write`      | Change artifact's version number |                               Yes                                |
| `pull-requests: write` | Create a pull request            | Optional. Necessary if input parameter `use-pr` is set to `true` |

<!-- prettier-ignore -->
```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-npm-release
  with:
    # Node Version to use
    # Default: see action-npm-build
    node-version: "22"

    # Path to package.json
    # "" would be equal to "./package.json" and "test-frontend" to "./test-frontend/package.json"
    # Required
    app-path: ""

    # Level of release
    # Specifies how the version is increased
    # Required, options: patch, minor, major
    releaseVersion: "patch"

    # Use a PR for the version bump instead of directly pushing it
    # default: false
    use-pr: "true"
```

### action-pr-checklist

Action to enforce ticking of all checklist items inside a PR (useful for PR templates)

Workflows using that action do not require any explicit additional permissions.

<!-- prettier-ignore -->
```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-pr-checklist
  with:
    # Whether the action should fail if the PR contains no checklist
    # Default: false
    fail-missing: "false"
```

### action-pr-labeler

Action to automatically label pull requests using the configuration file in `.github/labeler.yml` of the repositories.
More information about the configuration of the `labeler.yml` file can be found in [official documentation](https://github.com/actions/labeler)

Workflows using that action need the following permissions:

| Permission             | Purpose                      | Required |
| ---------------------- | ---------------------------- | :------: |
| `contents: read`       | Checkout repository contents |   Yes    |
| `pull-requests: write` | Add labels to pull requests  |   Yes    |
| `issues: write`        | Create labels if not exist   |   Yes    |

<!-- prettier-ignore -->
```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-pr-labeler
  with:
    # Path to the configuration file inside the repository
    # Default: .github/labeler.yml
    configuration-path: ".github/labeler.yml"
    
    # Optional repository to checkout to reference external configuration file
    # Default: ""
    repository: ""
```

### action-trivy

Action to run a security check with [Trivy](https://trivy.dev/latest/) on the Code.

Workflows using that action need the following permissions:

| Permission               | Purpose                                                   | Required |
| ------------------------ | --------------------------------------------------------- | :------: |
| `contents: read`         | Checkout repository contents                              |   Yes    |
| `security-events: write` | Upload SARIF results to GitHub "Security and quality" tab |   Yes    |

```yaml
- uses: it-at-m/lhm_actions/action-templates/actions/action-trivy
  with:
    # Relative Path to the trivyignore files
    # Default: ".trivyignore"
    trivyignore-files: ".trivyignore"
```
