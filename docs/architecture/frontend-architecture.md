# Frontend Architecture

RefArch applications usually provide one or more graphical user interfaces for interacting with backend services. The architecture focuses on one centralized frontend and supports web components as an optional integration style.

## Centralized frontend

In the centralized model, one frontend provides the user-facing application shell and coordinates access to backend capabilities.

Centralized frontend architecture:

![Centralized frontend architecture](./assets/Anwendung_zentraleGUI_PermissionService.png)

This remains the default pattern because it is easier to build, operate and reason about than more fragmented UI compositions. In the current RefArch implementation, the frontend is built and deployed separately from the gateway as described in [ADR001](../cross-cutting-concepts/adr/001-separate_gateway_and_frontend.md), but it still forms one coherent frontend for the application.

## Web components

Web components are an optional addition when parts of the UI should be provided independently from the main frontend.

In RefArch, this is reflected by the dedicated `webcomponent` template and the surrounding tooling:

- a separate JavaScript-based template alongside the regular frontend template
- the same OpenAPI-based client generation workflow as for the frontend
- static build and deployment in the same way as other JavaScript-based UI modules

This makes web components a good fit for reusable UI modules or integrations into an existing host application, while keeping the main RefArch setup centered around one dedicated frontend service. Additional details can be found in:

- [ADR001 - Separate Gateway and Frontend](../cross-cutting-concepts/adr/001-separate_gateway_and_frontend.md)
- [Templates: Getting Started](../templates/getting-started.md#frontend-web-components)
- [Templates: Develop](../templates/develop.md#component-libraries)
- [Cross-Cutting Concepts: OpenAPI](../cross-cutting-concepts/openapi.md#generating-api-client-from-specification)
- [Templates: Deploy](../templates/deploy.md#docker-images)

## Current recommendation

The current default is:

- one dedicated frontend service for the main UI
- one API gateway as the external backend entry point
- optional web components for modular frontend extensions

This keeps the standard setup simple while still allowing independently delivered UI building blocks where they provide clear value.
