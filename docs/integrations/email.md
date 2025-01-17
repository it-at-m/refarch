# Email Integration

Integration for sending text and HTML mails with attachments. Uses [s3-integration](./s3.md) for file handling.

## Modules

The modules follow the [default naming convention](./index.md#naming-conventions).

Besides the default modules we provide the additional ones:

- `email-api`: Legacy module containing core email functionality. This module will be merged into the core module in future releases and should not be used in new implementations.
- `rest-example`: Example which uses s3-rest-client.
- `java-example`: Example which uses s3-java-client.

### Dependency graph

The following graph shows the relationships between the various modules and how they interact and rely on each other.

```mermaid
flowchart RL
    starter --> core --> api-starter --> api
    core --> s3-client
    rest-example --> starter
    rest-example --> s3-rest-client-starter
    java-example --> starter
    java-example --> s3-java-client-starter
```

## Usage

```xml

<dependencies>
    <dependency>
        <groupId>de.muenchen.refarch</groupId>
        <artifactId>refarch-email-integration-starter</artifactId>
        <version>...</version>
    </dependency>
</dependencies>
```

Additionally, a specific `s3-integration-*-client-starter` is required as dependency, because S3 is used for file handling.
See [according documentation](./s3.md#usage).

## Configuration

### refarch-email-integration-starter

| Property                                | Description                                       | Example                |
|-----------------------------------------|---------------------------------------------------|------------------------|
| `spring.mail.host`                      | Host of smtp server used for sending mails.       | `mail.example.com`     |
| `spring.mail.port`                      | Host of smtp server used for sending mails.       | `1025`                 |
| `spring.mail.username`                  | Username of smtp server.                          |                        |
| `spring.mail.password`                  | Password of smtp server.                          |                        |
| `refarch.mail.from-address`             | Default from address used when sending mails.     | `test@example.com`     |
| `refarch.mail.default-reply-to-address` | Default reply to address used when sending mails. | `no_reply@example.com` |
