# ADR003 - Clientside routing via HTML5 history mode

State: accepted

## Context

In RefArch frontend applications, client-side routing is implemented using Vue Router.
The previously used mode is hash mode, which uses the `#` character in the URL to manage routing.
However, this approach has several drawbacks, including less clean URLs and issues with deep linking, where users may face problems when trying to access specific routes directly or when refreshing the page.
Besides, the HTML5 mode is the [official recommended mode](https://router.vuejs.org/guide/essentials/history-mode.html#HTML5-Mode) to use.

This ADR describes why the HTML5 mode is used.

## Decision

To improve the user experience, the HTML5 history mode is used.
This mode allows clean URLs without the hash (`#`) and improves the handling of deep links.

## Consequences

RefArch frontends use the HTML5 history mode by default and keeping this configuration is highly recommended.
HTML5 mode requires additional server configuration to ensure that all requests are correctly routed to our application’s entry point (`index.html`).
