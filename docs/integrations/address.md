# Address Integration

Integration for reading, searching and validating addresses via the LHM address service.

## Modules

The modules follow the [default naming convention](./index.md#naming-conventions).

## Usage

Add the following dependency for using the address integration.

```xml
<dependencies>
    <dependency>
        <groupId>de.muenchen.oss.refarch</groupId>
        <artifactId>refarch-address-integration-starter</artifactId>
        <version>...</version>
    </dependency>
</dependencies>
```

After that the [AddressOutPort](https://github.com/it-at-m/refarch/blob/main/refarch-integrations/refarch-address-integration/refarch-address-integration-core/src/main/java/de/muenchen/oss/refarch/integration/address/application/port/out/AddressOutPort.java) can be used for accessing the address service (e.g. via autowiring, as port is available as bean).

## Configuration

### refarch-address-integration-starter

| Property              | Description                |
| --------------------- | -------------------------- |
| `refarch.address.url` | URL of the address service |
