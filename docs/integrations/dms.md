# DMS Integration

Integration for CRUD operations on a dms system in specific fabasoft. Uses
s3-integration for file handling.

## Usage

```xml

<dependencies>
    <dependency>
        <groupId>de.muenchen.refarch</groupId>
        <artifactId>refarch-dms-integration-starter</artifactId>
        <version>...</version>
    </dependency>
</dependencies>
```

and a [s3-integration starter](../refarch-s3-integration/README.md#usage).

## Configuration

### refarch-dms-integration-starter

| Property                                  | Description                                                                                                    |
|-------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| `refarch.dms.fabasoft.url`                | Url to fabasoft endpoint                                                                                       |
| `refarch.dms.fabasoft.username`           | Technical fabasoft dms user                                                                                    |
| `refarch.dms.fabasoft.password`           | Technical fabasoft dms password                                                                                |
| `refarch.dms.fabasoft.businessapp`        | App registered in the DMS                                                                                      |
| `refarch.dms.fabasoft.ui-url`             | Url to fabasoft web interface used for ui link in object metadata                                              |
| `refarch.dms.fabasoft.enable-mtom`        | Flag to enable SOAP message transmission optimization                                                          |
| `refarch.dms.supported-file-extensions.*` | Map of allowed file extensions in the format "extension (key): mime-type (value)". I.e. "pdf: application/pdf" |
