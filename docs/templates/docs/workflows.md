# GitHub Actions

[[toc]]

We use GitHub Actions to build our software. The costs are minimal because we use free and public repositories.

We designed templates to use GitHub Actions. The GitHub Action needs permission on the repository, for example, to create a release or push a tag. Here you can use automatic token authentication from GitHub.
Therefore, you can use the access token via <span v-pre>`${{ secrets.GITHUB_TOKEN }}`</span>. In contrast to the .gitlab-ci.yml, you can create more workflow files which are independent of each other. The it@M-Templates are flexibly designed to suit your project’s needs. You can create reusable actions for single steps.

The templates can be activated under the "Actions" tab with the "New workflow" button. In the software catalog, the templates can be found under the category "By it@m".

- **maven-node-nuild**: Executes `mvn install` for Maven projects or `npm run build` for Node.js projects. It selects a free version of OpenJDK and Node.js based on the presence of a pom.xml or package.json in the folder.
  Specify your subfolders if needed. After the source code is built, a `docker build` is executed, and the resulting Docker image is pushed to GitHub’s internal registry with the tag `latest`. Ensure that the Dockerfile is available in the folder.
- **maven-release**: This manual workflow requires you to navigate to the `actions` tab on the left and start the `maven-release` workflow using the `Run workflow` button at the top of the table. You can then select the desired version in the x.y.z format, followed by the corresponding SNAPSHOT-x.y.z. Manual configuration of write rights is not required.
  For the maven-release to work, reference the pom.xml as follows. Replace the placeholder variables with the actual values when pushing your artifact to Maven Central.

```xml
<scm>
        <connection>scm:git:${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}.git</connection>
        <developerConnection>scm:git:${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}.git</developerConnection>
      <tag>HEAD</tag>
  </scm>
```

- **npm-release**: This manual workflow is similar to Maven-Release, but for Node.js projects. It allows you to select the desired version, after which an npm release is performed and a Docker image is created.

- **codeql**: Workflow for advanced CodeQL setup used for scanning Java/JavaScript/TypeScript/Vue/Python based source files
- **deploy-docs**: This action publishes VitePress-generated documentation as GitHub Pages.
- **pr-checklist**: Checks if all list items are checked
- **pr-labeler**: This action labels prs

## Maven Central

Maven Central is a central, public repository for the publication from maven artifacts.

For the city of Munich a maven groupId de.muenchen enabled. Artifacts can be published there. There is a central Maven account for the email [opensource@muenchen.de](mailto:opensource@muenchen.de)

Artifacts (without a version) will be linked in maven over a combination out of `groupId:artifactId`. for example `de.muenchen.oss.appswitcher:appswitcher-server`

For the groupId the following rules are applied:

- release is part of a project: `de.muenchen.oss.<project-abbreviation>` for example de.muenchen.oss.appswitcher
- release is not part of a large project: only de.muenchen.oss

Release on Maven Central

Before publishing releases to `maven central` please read the official guide [Sonatyoe-Documentation](https://central.sonatype.org/publish/publish-maven/)

Pom

The pom.xml needs to include special information, for example `<scm>`, project information and the needed plugins.

The following element should be included in the pom:

Project information

```xml
<name>${project-name}</name>
<description>${project-description}</description>
<url>${github-repo-url}</url>

<licenses>
   <license>
      <name>${licence-name}</name>
   </license>
</licenses>

<developers>
   <developer>
      <name>${developer-name}</name>
      <organization>Landeshauptstadt München</organization>
      <url>${developer-github-profile-url}</url>
      <roles>
         <role>${role1}</role>
         <role>${role2}</role>
      </roles>
   </developer>
</developers>
```

Scm is for the communication with the source control of the project.

```xml
<!-- You need to hard code the values otherwise the values are not correct in the registry  -->
<scm>
   <url>${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}</url>
   <connection>scm:git:${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}.git</connection>
   <developerConnection>scm:git:${GITHUB_SERVER_URL}/${GITHUB_REPOSITORY}.git</developerConnection>
   <tag>HEAD</tag>
</scm>
```

Important use scm:git:https – otherwise the maven release will fail.

The following plugins are used with the maven release.

```xml
<build>
   <pluginManagement>
      <plugins>
         <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-release-plugin</artifactId>
            <version>3.0.1</version>
         </plugin>
         <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-gpg-plugin</artifactId>
            <version>3.0.1</version>
         </plugin>
         <plugin>
            <groupId>org.sonatype.central</groupId>
            <artifactId>central-publishing-maven-plugin</artifactId>
            <version>0.4.0</version>
         </plugin>
      </plugins>
   </pluginManagement>
   <plugins>
      <plugin>
         <groupId>org.apache.maven.plugins</groupId>
         <artifactId>maven-release-plugin</artifactId>
         <configuration>
            <autoVersionSubmodules>true</autoVersionSubmodules>
            <useReleaseProfile>false</useReleaseProfile>
            <releaseProfiles>release</releaseProfiles>
            <goals>deploy</goals>
            <tagNameFormat>@{project.version}</tagNameFormat>
         </configuration>
      </plugin>
   </plugins>
```

Over the specific maven profile release the central-publishing-maven-plugin is configured over the GPG Signing for the release process

```xml
<profiles>
   <profile>
      <id>release</id>
      <build>
         <plugins>
            <!-- Central Portal Publishing Plugin -->
            <plugin>
               <groupId>org.sonatype.central</groupId>
               <artifactId>central-publishing-maven-plugin</artifactId>
               <extensions>true</extensions>
               <configuration>
                  <tokenAuth>true</tokenAuth>
                  <autoPublish>true</autoPublish>
                  <deploymentName>${project.groupId}:${project.artifactId}:${project.version}</deploymentName>
               </configuration>
            </plugin>
            <!-- GPG plugin -->
            <plugin>
               <groupId>org.apache.maven.plugins</groupId>
               <artifactId>maven-gpg-plugin</artifactId>
               <configuration>
                  <skip>${skipGpg}</skip>
               </configuration>
               <executions>
                  <execution>
                     <id>sign-artifacts</id>
                     <phase>verify</phase>
                     <goals>
                        <goal>sign</goal>
                     </goals>
                     <configuration>
                        <!-- Prevent `gpg` from using pinentry programs -->
                        <gpgArguments>
                           <arg>--pinentry-mode</arg>
                           <arg>loopback</arg>
                        </gpgArguments>
                     </configuration>
                  </execution>
               </executions>
            </plugin>
         </plugins>
      </build>
   </profile>
</profiles>
```

Release process over GitHub Actions

## NPM

registry.npmjs.org ist he central, public repository for publishing npm packetes

For the city of Munich, the package-scope `@muenchen` is reserved. Therefore, we can publish npm packages. The codes are set in the organizations GitHub settings.

Publish on the npm Registry

Requirements:

The following requirements are needed for npm projects

- The GitHub repository needs to be located in the it-at-m organization public
- The name is in the scope `@muenchen` for example `@muenchen/appswitcher-vue`

The npm-default is that scoped packages are only published private. You need to config it public in the package.json. Moreover we activate the npm provence, for the repository.url.

```json
"repository": {
 "type": "git",
 "url": "https://github.com/it-at-m/meinRepo"
},
"publishConfig": {
 "access": "public",
 "provenance": true
}
```

1. If someone has a new project that they want to publish on npm, an npm admin must enable the trusted publisher, according to the [documentation](https://docs.npmjs.com/trusted-publishers#step-1-add-a-trusted-publisher-on-npmjscom).
1. The GitHub Actions must be granted the appropriate permissions. See the [documentation](https://docs.npmjs.com/trusted-publishers#step-2-configure-your-cicd-workflow).
