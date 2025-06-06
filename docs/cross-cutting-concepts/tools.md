# Tools

For better developer experience, the RefArch provides configuration presets for different tools.

These different configurations are described in the following.
The tools itself are described in [Develop](./templates/develop).

## PMD

The `refarch-pmd` Java module provides a default ruleset for [PMD](https://pmd.github.io/) which is used for code linting.
The module code can be found in the [`refarch-pmd` directory](https://github.com/it-at-m/refarch/tree/main/refarch-tools/refarch-java-tools/refarch-pmd) and the specific ruleset configuration inside the [`resources` subdirectory](https://github.com/it-at-m/refarch/blob/main/refarch-tools/refarch-java-tools/refarch-pmd/src/main/resources/refarch-pmd-ruleset.xml).

All available rules can be found in [the according PMD documentation](https://docs.pmd-code.org/latest/pmd_rules_java.html).

For using the ruleset, `de.muenchen.refarch.tools:refarch-pmd` needs to be added as a dependency for the `maven-pmd-plugin` plugin and the `refarch-pmd-ruleset.xml` needs to be added as a ruleset in the plugin configuration.
See following example. The RefArch templates are already configured this way.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-pmd-plugin</artifactId>
            <version>${maven-pmd-plugin.version}</version>
            <configuration>
                ...
                <rulesets>
                    <ruleset>refarch-pmd-ruleset.xml</ruleset>
                </rulesets>
            </configuration>
            <dependencies>
                <dependency>
                    <groupId>de.muenchen.refarch.tools</groupId>
                    <artifactId>refarch-pmd</artifactId>
                    <version>${refarch-tools.version}</version>
                </dependency>
            </dependencies>
            ...
        </plugin>
    </plugins>
</build>
```

## Renovate

The provided [Renovate](https://renovatebot.com) configuration [`refarch-renovate-config.json5`](https://github.com/it-at-m/refarch/blob/main/refarch-tools/refarch-renovate/refarch-renovate-config.json5) extends the [it@M global Renovate configurations](https://github.com/it-at-m/.github/tree/main/renovate-configs).
Additionally, it includes specific presets for tagging and dependency restrictions to prevent Renovate from suggesting updates to dependencies that are pinned by the RefArch.
The configuration also enables auto-merging of patch releases for e.g. GitHub Actions, NPM dependencies, Maven artifacts, image versions in `docker-compose.yml` and Dockerfile base images while keeping pinning of dependencies intact.
This greatly reduces LCM effort.

The used presets from Renovate are described in the according [Renovate documentation](https://docs.renovatebot.com/presets-default/).

To use this configuration, create a `renovate.json5` file with the following content in the repository root.
The file is included by default in the RefArch templates.

```json5
{
  $schema: "https://docs.renovatebot.com/renovate-schema.json",
  extends: [
    "github>it-at-m/refarch//refarch-tools/refarch-renovate/refarch-renovate-config.json5",
  ],
}
```

::: danger IMPORTANT
To make Renovate work, make sure that it has access to your GitHub repository.
For projects in the `it-at-m` organization, Renovate has access by default and is enabled when the configuration file is found in your repository.
However, to finish the onboarding process of Renovate, you need to open a PR for a dependency update found by Renovate through the "Dependency Dashboard" issue.
This PR then has to be merged manually once.
After that's done Renovate will start opening PRs automatically.

If you have configured [CODEOWNERS](./templates/develop#codeowners) for your project and require CODEOWNER approval inside your [ruleset](./templates/develop#github-rulesets),
you need to configure bypass rules to make auto-merging possible.
This can be achieved by adding the Renovate bot account to the [bypass list](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/creating-rulesets-for-a-repository#granting-bypass-permissions-for-your-branch-or-tag-ruleset).
:::

To modify the Renovate settings, the `renovate.json5` file can be edited.
Check the official [Renovate documentation](https://docs.renovatebot.com/configuration-options/) for further information and configuration options.
