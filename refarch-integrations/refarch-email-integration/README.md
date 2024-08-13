# RefArch email integration

Integration for sending text and html emails with attachments. Uses [s3-integration](../refarch-s3-integration) for file
handling.

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

and a [s3-integration starter](../refarch-s3-integration/README.md#usage).

## Configuration

### refarch-email-integration-starter

| Property                                | Description                                       | Example |
|-----------------------------------------|---------------------------------------------------|---------|
| `spring.mail.host`                      | Host of smtp server used for sending mails.       |         |
| `spring.mail.username`                  | Username of smtp server.                          |         |
| `spring.mail.password`                  | Password of smtp server.                          |         |
| `refarch.mail.from-address`             | Default from address used when sending mails.     |         |
| `refarch.mail.default-reply-to-address` | Default reply to address used when sending mails. |         |
