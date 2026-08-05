# ADR002 - No additional auth sources besides OpenID Connect

State: accepted

## Context

The RefArch uses OAuth 2.0 / OpenID Connect for authentication and authorization and therefore all projects require an
according identity provider which supports the protocol.

As not everyone (especially small companies and municipalities) have such a service in their infrastructure, this
collides with the target of the underlying OpenSource strategy to support broad re-usage. Some alternative
authentication and authorization techniques would be LDAP and sign-in via User and Password.

This ADR describes the decision if the support of more auth providers should be implemented in the RefArch.

## Decision

As nearly any software requires additional services to work (e.g. DB) the support of other auth providers than
OAuth 2.0 / OpenID Connect lies outside the scope of the RefArch and would introduce a huge overhead for maintaining,
testing and documenting the additional auth options. Beside that the setup of an identity provider like Keycloak is very
easy and supports a broader range of auth options as ever could be implemented in the RefArch.

To still following the target of easy re-usage the target is to provide additional documentation on how to easily setup
and fully running RefArch application without any auth provider requisites.

## Consequences

No additional auth options are implemented into the RefArch. The RefArch documentation should document the re-usage of
applications including an exemplary auth provider setup.
