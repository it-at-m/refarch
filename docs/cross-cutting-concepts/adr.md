# Architecture Decision Records

Documentation of key architectural decisions made throughout the RefArch.

Each ADR describes the context, decision and consequences to make all decisions comprehensible.

## ADR001 - Separate Gateway and Frontend

State: accepted

### Context

The RefArch consists of two service types the API gateway (with included frontend) and backend services.
This requires each project to maintain their own gateway although most of them don't modify anything and use the default
functionality. This leads to multiple LCMs efforts and inconsistent solutions / implementations for the same problem.

This ADR describes the decision if the gateway and frontend should be seperated.

### Decision

The API gateway and the frontend should be seperated. This brings multiple advantages like central LCM, consistent
implementations and frontend independent usage (e.g. web components instead of frontend).

The frontend will be deployed as additional service, while using static building and nginx.

### Consequences

- The gateway and frontend will be deployed and built as two different images.
- The gateway is developed and maintained by the RefArch team.

## ADR002 - No additional auth sources beside OpenId Connect

State: accepted

### Context

The RefArch uses OAuth 2.0 / OpenID Connect for authentication and authorization and therefore all projects require an
according identity provider which supports the protocol.

As not everyone (especially small companies and municipalities) have such a service in there infrastructure, this
collides with the target of the underlying OpenSource strategy to support broad re-usage. Some alternative
authentication and authorization techniques would be LDAP and signin via User and Password.

This ADR describes the decision if the support of more auth providers should be implemented in the RefArch.

### Decision

As nearly any software requires additional services to work (e.g. DB) the support of other auth providers than
OAuth 2.0 / OpenID Connection lies outside the scope of the RefArch and would introduce a huge overhead for maintaining,
testing and documenting the additional auth options. Beside that the setup of an identity provider like Keycloak is very
easy and supports a border range of auth options as ever could be implemented in the RefArch.

To still following the target of easy re-usage the target is to provide additional documentation on how to easily setup
and fully running RefArch application without any auth provider requisites.

### Consequences

No additional auth options are implemented into the RefArch. The RefArch documentation should document the re-usage of
applications including an exemplary auth provider setup.
