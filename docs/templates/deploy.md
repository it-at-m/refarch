# Deploy

The templates include configuration files to simplify deployment of applications. Those are further explained below.

## Docker Images

All templates include a simple `Dockerfile` to build Docker images. The following base images are used:

- Java-based templates (`backend` and `eai`): [RedHat UBI OpenJDK Runtime](https://rh-openjdk.github.io/redhat-openjdk-containers/)
- JavaScript-based templates (`frontend` and `webcomponent`): RedHat UBI Nginx (e.g. [`ubi10/nginx-126`](https://catalog.redhat.com/en/software/containers/ubi10/nginx-126/677d3735607921b4d7503cf3))

::: danger Important
To ensure broad hardware compatibility of built container images, it is strongly recommended to use a multi-architecture base image whenever possible. In such cases, an architecture-agnostic SHA should be used.
Depending on the image source, the correct SHA can be found for:

- **RedHat Image Catalog**: Check "Get this image -> Image identifiers -> Manifest List Digest" on the image page
- **GitHub**: Use SHA at the top directly underneath the tag name, not the SHA listed in "OS/Arch"
- **DockerHub**: Use "Index Digest", not "Manifest Digest"
  :::

By default, applications based on the RefArch Templates are built for **AMD64** and **ARM64** hardware architectures.

## Deployment Overview

![architecture-overview](../assets/ci_cd_github_big_picture_public.drawio.png)
The diagram shows an step by step overview of how applications are delivered at it@M/LHM, from code changes to deployment in environments:

1. _GitHub Project `it-at-m/foo`_: Add code changes, compile and build code and images
2. _GitHub Project `it-at-m/helm-charts`_: Provide helm charts for project deployment
3. _Image Registry `Quay`_: Internal image registry as pull through cache for the project images
4. _GitLab `git.muenchen.de`_: Internal Git repository to run deployment pipelines
5. _OpenShift `Container Application Platform`_: Internal kubernetes plattform to run applications

You can find further details in the following chapters.

### Helm Chart

[Helm](https://helm.sh/) allows easy deployment of multi-container applications to a Kubernetes cluster using charts.

For RefArch-based applications, there are multiple ways to use Helm charts with increasing customizability and manual effort.

#### Variant 1: Direct use of `refarch-templates` chart (recommended)

The reference architecture provides a [Helm chart](https://github.com/it-at-m/helm-charts/tree/main/charts/refarch-templates) to easily deploy RefArch-based multi-container applications by just providing a configuration file (`values.yaml`).
Each application container is called a "module" in the `refarch-templates` chart.
Additionally, a [RefArch API Gateway](../gateway.md) can be deployed as well.
An example `values.yaml` file can be found in the [Helm chart sources](https://github.com/it-at-m/helm-charts/blob/main/charts/refarch-templates/values-example.yaml).

Splitting into multiple files (e.g a common `values.yaml` and environment specific configuration in `values-<ENV>.yaml`) is also possible.

::: warning Warning
When using multiple files, array-based configuration options are not merged.
:::

The release notes of this chart can be found in the [GitHub releases](https://github.com/it-at-m/helm-charts/releases?q="refarch-templates") of the it@M Helm Charts repository.

::: info Information
Detailed information about all configuration options for the `refarch-templates` Helm chart can be found in its [README](https://github.com/it-at-m/helm-charts/tree/main/charts/refarch-templates#refarch).
Available options for the RefArch Gateway can be found in the [configuration documentation](../gateway.md#configuration).
:::

The configuration file (or multiple) can then be used to install the chart to your cluster with the following commands:

```bash
helm repo add it-at-m https://it-at-m.github.io/helm-charts
helm install <HELM_RELEASE_NAME> it-at-m/refarch-templates --version <HELM_CHART_VERSION> --values values.yaml --values values-<ENV>.yaml
```

::: details it@M internal configuration
An [internal IaC example repository](https://git.muenchen.de/ccse/refarch/refarch-iac) is provided, which implements this variant.
:::

#### Variant 2: `refarch-templates` chart as dependency for an application-specific chart

The `refarch-templates` chart can be used as a dependency for application-specific charts through [Helm dependencies](https://helm.sh/docs/helm/helm_dependency/).
This allows reuse of the mechanisms provided by the `refarch-templates` chart,
while the application-specific chart can be further enhanced with custom configurations and Kubernetes resources.

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

#### Variant 3: Application-specific chart only

Creating a custom Helm chart allows for the manual definition of all required Kubernetes resources. This approach provides complete control over the configuration but requires a high level of effort.

:::danger Important
Using this variant is not recommended. It is advisable to explore variants 1 or 2 first. If any features are found to be lacking, an issue can be opened in the [it@M Helm Charts](https://github.com/it-at-m/helm-charts) repository.
:::

### Internal Deployment (Image Repository `Quay`, Git Repository GitLab `git.muenchen.de`, Kubernetes platform `OpenShift`)

::: details it@M internal configuration

#### What are we using internally

- Image Registry [Red Hat Quay](https://docs.redhat.com/de/documentation/red_hat_quay) as pull through cache for docker images
- [GitLab](https://docs.gitlab.com/) to execute internal infrastructure pipelines (IaC) and configuration of the applications
- [OpenShift](https://docs.redhat.com/en/documentation/openshift_container_platform) as Kubernetes platform to host the applications

#### How it works together

##### Autorollout: For the dev environment (only not productive enviornment)

An automatic rollout is implemented, through an image stream that links to the internal image registry (Quay). The deployment includes a special annotation that prompts the internal Kubernetes platform (OpenShift) to automatically trigger a new rollout when a new image becomes available. For more details, see [this documentation](https://docs.redhat.com/en/documentation/openshift_container_platform/4.17/html/images/triggering-updates-on-imagestream-changes#triggering-updates-on-imagestream-changes).

##### manual rollout for the deployment in productive and close to productive environments

For other environments (stage, prod), a manual rollout is implemented. The image version must be specified for each service in the Helm configuration file `values.yml`.
:::
