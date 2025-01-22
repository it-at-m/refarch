# Tools

For better developer experience, the RefArch provides configuration presets for different tools.

These different configurations are described in the following.
The tools itself are described in the [Develop](https://refarch-templates.oss.muenchen.de/develop.html) section of the RefArch templates documentation.

## PMD

The `refarch-pmd` Java module provides a default ruleset for [PMD](https://pmd.github.io/) which is used for code linting.
The module code can be found [here](https://github.com/it-at-m/refarch/tree/main/refarch-tools/refarch-java-tools/refarch-pmd) and the specific ruleset configuration [here](https://github.com/it-at-m/refarch/blob/main/refarch-tools/refarch-java-tools/refarch-pmd/src/main/resources/refarch-pmd-ruleset.xml).

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

The provided [Renovate](https://renovatebot.com) configuration [`refarch-renovate-config.json5`](https://github.com/it-at-m/refarch/blob/main/refarch-tools/refarch-renovate/refarch-renovate-config.json5) is based on the [it@M global Renovate configurations](https://github.com/it-at-m/.github/tree/main/renovate-configs).
Besides that there are some presets for tagging and restrictions for specific modules, so that Renovate doesn't suggest updates for dependencies which are pinned from the RefArch side.

The used presets from Renovate are described in the according [Renovate documentation](https://docs.renovatebot.com/presets-default/).

For using the preset a file `renovate.json5` with following content needs to be created in the repository root.
The RefArch templates already include this file.

```json5
{
  $schema: "https://docs.renovatebot.com/renovate-schema.json",
  extends: [
    "github>it-at-m/refarch//refarch-tools/refarch-renovate/refarch-renovate-config.json5",
  ],
}
```
