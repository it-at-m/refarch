# Email Integration

Integration for sending text and HTML mails with attachments. Uses [s3-integration](./s3.md) for file handling.

## Modules

The modules follow the [default naming convention](./index.md#naming-conventions).

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

## Configuration

### refarch-email-integration-starter

| Property                                | Description                                       | Example                |
| --------------------------------------- | ------------------------------------------------- | ---------------------- |
| `spring.mail.host`                      | Host of smtp server used for sending mails.       | `mail.example.com`     |
| `spring.mail.port`                      | Host of smtp server used for sending mails.       | `1025`                 |
| `spring.mail.username`                  | Username of smtp server.                          |                        |
| `spring.mail.password`                  | Password of smtp server.                          |                        |
| `refarch.mail.from-address`             | Default from address used when sending mails.     | `test@example.com`     |
| `refarch.mail.default-reply-to-address` | Default reply to address used when sending mails. | `no_reply@example.com` |
