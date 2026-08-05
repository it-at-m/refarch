# ADR001 - Separate Gateway and Frontend

State: accepted

## Context

The RefArch consists of two service types the API gateway (with included frontend) and backend services.
This requires each project to maintain their own gateway although most of them don't modify anything and use the default
functionality. This leads to multiple LCMs efforts and inconsistent solutions / implementations for the same problem.

This ADR describes the decision if the gateway and frontend should be separated.

## Decision

The API gateway and the frontend should be separated. This brings multiple advantages like central LCM, consistent
implementations and frontend-independent usage (e.g. web components instead of frontend).

The frontend will be deployed as additional service, while using static building and nginx.

## Consequences

- The gateway and frontend will be deployed and built as two different images.
- The gateway is developed and maintained by the RefArch team.
