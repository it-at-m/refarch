# RefArch S3 integration

Integration for CRUD operations on a s3 storage. Also used for file handling in other integrations.

## Modules

The S3 integration follows the [default naming conventions](../README.md#naming-conventions).

Beside the default integration it contains different client libraries for accessing the integration. The client
libraries are especially provided for usage in other integrations.

- REST: The rest client uses the rest endpoints of the s3-rest-service to manage data in s3.
- Java: The Java client directly uses the in ports of the s3-core.

## Usage

```xml

<dependencies>
    <!-- REST -->
    <!-- requires refarch-s3-integration-rest-service -->
    <dependency>
        <groupId>de.muenchen.refarch</groupId>
        <artifactId>refarch-s3-integration-rest-starter</artifactId>
        <version>...</version>
    </dependency>
    <!-- or Java -->
    <dependency>
        <groupId>de.muenchen.refarch</groupId>
        <artifactId>refarch-s3-integration-java-starter</artifactId>
        <version>...</version>
    </dependency>
</dependencies>
```

## Configuration

Following are the properties to configure the different modules. Some of them are custom defined and others are synonyms
for spring package properties.
Whether a property is an alias can be checked in the corresponding `application.yml` of each module.

### s3-integration-rest-service

| Property                 | Description                                                    | Example                                       |
|--------------------------|----------------------------------------------------------------|-----------------------------------------------|
| `refarch.s3.url`         | Url of s3 endpoint to connect to.                              | `s3.example.com`                              |
| `refarch.s3.bucket-name` | Name of the bucket to connect to.                              | `refarch-bucket`                              |
| `refarch.s3.access-key`  | Access key to use for connection.                              |                                               |
| `refarch.s3.secret-key`  | Secret key to use for connection.                              |                                               |
| `SSO_ISSUER_URL`         | Issuer url of oAuth2 service used for securing rest endpoints. | `https://sso.example.com/auth/realms/refarch` |

### s3-integration-java-client-starter

| Property                                                   | Description                                           | Example                  |
|------------------------------------------------------------|-------------------------------------------------------|--------------------------|
| `refarch.s3.client.max-file-size` (optional)               | Single file limit for up- or downloading in byte.     | `10MB`                   |
| `refarch.s3.client.max-batch-size` (optional)              | Limit for up- or downloading a list of files in byte. | `100MB`                  |
| `refarch.s3.client.supported-file-extensions.*` (optional) | Map of allowed file extensions for up- and download.  | `pdf: "application/pdf"` |

### s3-integration-rest-client-starter

All properties of [s3-integration-java-client-starter](#s3-integration-rest-client-starter) and following:

| Property                                 | Description                                                                | Example                                       |
|------------------------------------------|----------------------------------------------------------------------------|-----------------------------------------------|
| `refarch.s3.client.document-storage-url` | Url to the RefArch S3 integration service.                                 | `http://s3-integration-service:8080`          |
| `refarch.s3.client.enable-security`      | Switch to enable or disable oAuth2 authentication against s3 service.      | `true`                                        |
| `SSO_ISSUER_URL`                         | Issuer url of oAuth2 service to use for authentication against s3 service. | `https://sso.example.com/auth/realms/refarch` |
| `SSO_S3_CLIENT_ID`                       | Client id to be used for authentication.                                   | `refarch_client`                              |
| `SSO_S3_CLIENT_SECRET`                   | Client secret to be used for gathering client service account token.       |                                               |
