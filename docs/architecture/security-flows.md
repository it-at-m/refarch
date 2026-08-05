# Security Flows

This page describes the main authentication and authorization flows at an architectural level. Concrete implementation details and configuration options are documented on the [Security](../cross-cutting-concepts/security.md) page.

## Authentication and authorization overview

Authentication and authorization of user requests are split between the gateway and the backend services.

Authentication and authorization flow:

![Authentication and authorization flow](../assets/Anwendung_Security_TokenAutorisierung.png)

The gateway handles the user-facing login flow and forwards authenticated requests to backend services. Backend services validate the incoming token and enforce authorization for protected operations.

## Login

The typical login flow is:

1. A user opens the application through the gateway.
2. If no authenticated session exists, the gateway redirects the user to the configured OpenID Connect provider.
3. After successful authentication, the gateway stores the resulting session information and delivers the frontend.

## Backend access

After login, frontend requests reach backend services through the gateway.

The architectural principles are:

- backend calls are routed through the gateway
- the gateway forwards authenticated requests (Session) with a token usable by the backend
- each backend service validates the token before executing business logic (signature, audience, expiry)
- authorization is enforced in the backend, not only in the UI (roles & authorities in token or user-info-endpoint)

## Logout behavior

The reference architecture does not implement a dedicated application-level logout flow.

Effective logout depends on two independent session lifetimes:

- the gateway web session, which can expire locally after its configured idle timeout
- the identity-provider session, which is managed by the OpenID Connect provider

The resulting behavior is:

- While the gateway session is valid, the application remains authenticated without a new login flow.
- If the gateway session expires but the identity-provider session is still valid, the next access through the gateway starts a new login roundtrip and transparently creates a new authenticated gateway session.
- If the identity-provider session expires or is ended at the identity provider, the next attempt to create or refresh the gateway session requires a new login.

Separate local (application-only) or global (identity-provider-wide) logout variants are not supported, to avoid immediate re-login in the local case and unintended logout from other applications in the global case.

## Authorization checks in services

Authorization checks belong in the backend services because UI-level checks are only a usability feature.

Depending on the chosen authorization model, services either:

- derive roles directly from the token
- resolve additional permissions from the identity provider

If additional permissions are resolved remotely, they should be cached for the token lifetime or another short bounded period to avoid unnecessary calls to the identity provider.
