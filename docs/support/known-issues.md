# Known Issues

This page informs about known issues or bugs in the different artifacts and templates we provide.

::: info Information
If you found an issue not mentioned here, please head over to our GitHub repositories and check if a corresponding bug ticket exists.
If not feel free to open a new one.
:::

## refarch-backend

### Timeout on SSO misconfiguration or audience claim mismatch

When the configured SSO URLs in the `refarch-backend` and [API gateway](/gateway#security) do not match, accessing a secured endpoint in the backend via the API gateway results in a timeout.

The same behavior occurs when the names of the expected [audience claims](/cross-cutting-concepts/security#client-validation) do not match.

In both cases this is a configuration-error and thus should not occur in a working real-case scenario.
