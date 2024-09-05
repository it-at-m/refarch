# RefArch CoSys integration

Integration for creating documents with cosys. Uses s3-integration for file handling.

## Usage

```xml

<dependencies>
    <dependency>
        <groupId>de.muenchen.refarch</groupId>
        <artifactId>refarch-cosys-integration-starter</artifactId>
        <version>...</version>
    </dependency>
</dependencies>
```

and a [s3-integration starter](../refarch-s3-integration/README.md#usage).

## Configuration

Following are the properties to configure the different modules. Some of them are custom defined and others are synonyms
for spring package properties.
Whether a property is an alias can be checked in the corresponding `application.yml` of each module.

### refarch-cosys-integration-starter

| Property                             | Description                                                                   |
|--------------------------------------|-------------------------------------------------------------------------------|
| `refarch.cosys.url`                  | Url of the CoSys service                                                      |
| `refarch.cosys.merge.datafile`       |                                                                               |
| `refarch.cosys.merge.inputLanguage`  | incoming language                                                             |
| `refarch.cosys.merge.outputLanguage` | outgoing language                                                             |
| `refarch.cosys.merge.keepFields`     |                                                                               |
| `SSO_COSYS_ISSUER_URL`               | Issuer url of oAuth2 service to use for authentication against cosys service. |
| `SSO_COSYS_CLIENT_ID`                | Client id to be used for authentication against cosys.                        |
| `SSO_COSYS_CLIENT_SECRET`            | Client secret to be used for gathering client service account token.          |
