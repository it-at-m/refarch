# Known Issues

This page informs about known issues or bugs in the different artifacts and templates we provide.

::: info Information
If you found an issue not mentioned here, please head over to our GitHub repositories and check if a corresponding bug ticket exists.
If not feel free to open a new one.
:::

## refarch-gateway

### "Origin not allowed" but configured

By providing default CORS configuration for `/public/**` and `/clients/**` routes (to support configuration via environment variables),
this default one can take precedence over custom configuration.
This leads to deployment specific configuration (setting e.g. `/**`) not being applied for `/public/**` and `/clients/**` routes.

To fix that, set CORS for the keys `/public/**` and/or `/clients/**` or use the according environment variables.
See [gateway configuration section](../gateway.md#configuration).

### Duplicate CORS headers

If an underlying service (e.g. frontend) already sends an `Access-Control-Allow-Origin` header and the gateway is also
configured to set one (e.g. via `ALLOWED_ORIGINS_PUBLIC` see [Gateway Configuration](../gateway.md#configuration)) the browser
blocks the according request with `CORS Error: The 'Access-Control-Allow-Origin' header contains multiple values '...', but only one is allowed`.

The easiest and best fix is to not set the header in the underlying service and leave that up to the gateway.

Besides that this is also a known "issue" of the gateway which can
be fixed by adding the `DedupeResponseHeader` filter (see [according docs](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webflux/gatewayfilter-factories/deduperesponseheader-factory.html)).

## refarch-backend

### Timeout on SSO misconfiguration or audience claim mismatch

When the configured SSO URLs in the `refarch-backend` and [API gateway](../gateway.md#security) do not match, accessing a secured endpoint in the backend via the API gateway results in a timeout.

The same behavior occurs when the names of the expected [audience claims](../cross-cutting-concepts/security.md#client-validation) do not match.

In both cases this is a configuration-error and thus should not occur in a working real-case scenario.

## refarch-frontend

### Refresh loop (local only)

If the frontend is showing a white page and reloading infinitely, this is because the session expired but the frontend 
is called directly instead through the gateway. To fix this behaviour the frontend needs to be called through the 
gateway to allow the re-authentication to take place.
See [according code](https://github.com/it-at-m/refarch-templates/blob/main/refarch-frontend/src/api/fetch-utils.ts#L87-L116)
