# Email Integration

Integration for sending text and HTML mails with attachments.

## Modules

The modules follow the [default naming convention](./index.md#naming-conventions).

## Usage

Add the following dependency for using the email integration.

```xml
<dependencies>
    <dependency>
        <groupId>de.muenchen.oss.refarch</groupId>
        <artifactId>refarch-email-integration-starter</artifactId>
        <version>...</version>
    </dependency>
</dependencies>
```

After that the [MailOutPort](https://github.com/it-at-m/refarch/blob/main/refarch-integrations/refarch-email-integration/refarch-email-integration-core/src/main/java/de/muenchen/refarch/integration/email/application/port/out/MailOutPort.java) can be used for sending mails (e.g. via autowiring, as port is available as bean).

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
