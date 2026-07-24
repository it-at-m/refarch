# Architecture

This section describes the architectural building blocks, runtime structure and interaction patterns of the RefArch.
It complements the component-specific pages such as [API Gateway](../gateway.md), [Security](../cross-cutting-concepts/security.md) and [Integrations](../integrations/index.md).

Architectural explanations and architectural decisions are intentionally separated:

- This section explains how the architecture is structured and how the main building blocks interact.
- The [Architecture Decision Records](../cross-cutting-concepts/adr/index.md) document why specific decisions were made.

## Pages in this section

- [Application Basics](./application-basics.md): Shared structural principles for RefArch applications.
- [Microservice Applications](./microservice-applications.md): Service boundaries, responsibilities and communication patterns.
- [Infrastructure](./infrastructure.md): Container platform, scaling and runtime topology.
- [Frontend Architecture](./frontend-architecture.md): Centralized and distributed UI patterns.
- [External Access](./external-access.md): Typical patterns for access from outside the internal network.
- [Security Flows](./security-flows.md): Authentication, backend access, logout and authorization checks.
- [Integration Patterns](./integration-patterns.md): Communication between RefArch applications and external systems.
