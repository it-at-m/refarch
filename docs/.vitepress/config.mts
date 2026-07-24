import { defineConfig } from "vitepress";
import { withMermaid } from "vitepress-plugin-mermaid"; // https://vitepress.dev/reference/site-config

// https://vitepress.dev/reference/site-config
const vitepressConfig = defineConfig({
  title: "RefArch",
  description: "Documentation for the RefArch",
  head: [
    [
      "link",
      {
        rel: "icon",
        href: `https://assets.muenchen.de/logos/lhm/icon-lhm-muenchen-32.png`,
      },
    ],
  ],
  lastUpdated: true,
  ignoreDeadLinks: "localhostLinks",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: "Home", link: "/" },
      {
        text: "Docs",
        items: [
          { text: "Overview", link: "/overview" },
          { text: "Architecture", link: "/architecture/" },
          { text: "API Gateway", link: "/gateway" },
          { text: "Templates", link: "/templates" },
          { text: "Integrations", link: "/integrations" },
          {
            text: "CI/CD",
            link: "https://github.com/it-at-m/lhm_actions/blob/main/docs/actions.md",
          },
          {
            text: "Security",
            link: "/cross-cutting-concepts/security",
          },
          { text: "Tools", link: "/cross-cutting-concepts/tools" },
          { text: "OpenAPI", link: "/cross-cutting-concepts/openapi" },
        ],
      },
      {
        text: "Support",
        items: [
          { text: "Known Issues", link: "/support/known-issues" },
          { text: "Migration", link: "/support/migration" },
          { text: "Release Strategy", link: "/support/release-strategy" },
        ],
      },
      { text: "Contribute", link: "/contribute" },
      {
        text: "GitHub",
        items: [
          { text: "Core", link: "https://github.com/it-at-m/refarch" },
          {
            text: "Templates",
            link: "https://github.com/it-at-m/refarch-templates",
          },
        ],
      },
    ],
    sidebar: [
      { text: "Overview", link: "/overview", items: [] },
      {
        text: "Architecture",
        link: "/architecture/",
        items: [
          {
            text: "Application Basics",
            link: "/architecture/application-basics",
          },
          {
            text: "Microservice Applications",
            link: "/architecture/microservice-applications",
          },
          { text: "Infrastructure", link: "/architecture/infrastructure" },
          {
            text: "Frontend Architecture",
            link: "/architecture/frontend-architecture",
          },
          { text: "External Access", link: "/architecture/external-access" },
          { text: "Security Flows", link: "/architecture/security-flows" },
          {
            text: "Integration Patterns",
            link: "/architecture/integration-patterns",
          },
        ],
      },
      {
        text: "Components",
        items: [{ text: "API Gateway", link: "/gateway" }],
      },
      {
        text: "Templates",
        link: "/templates/",
        items: [
          { text: "Getting Started", link: "/templates/getting-started" },
          { text: "Develop", link: "/templates/develop" },
          { text: "Deploy", link: "/templates/deploy" },
          { text: "Document", link: "/templates/document" },
          { text: "Organize", link: "/templates/organize" },
        ],
      },
      {
        text: "Integrations",
        link: "/integrations/",
        items: [
          { text: "S3", link: "/integrations/s3" },
          { text: "Address", link: "/integrations/address" },
          { text: "CoSys", link: "/integrations/cosys" },
          { text: "DMS", link: "/integrations/dms" },
          { text: "E-Mail", link: "/integrations/email" },
        ],
      },
      {
        text: "Cross-Cutting Concepts",
        items: [
          {
            text: "Architecture Decision Records",
            link: "/cross-cutting-concepts/adr",
          },
          {
            text: "CI/CD",
            link: "https://github.com/it-at-m/lhm_actions/blob/main/docs/actions.md",
          },
          { text: "Security", link: "/cross-cutting-concepts/security" },
          { text: "Tools", link: "/cross-cutting-concepts/tools" },
          { text: "OpenAPI", link: "/cross-cutting-concepts/openapi" },
        ],
      },
      {
        text: "Support",
        items: [
          {
            text: "Known Issues",
            link: "/support/known-issues",
          },
          {
            text: "Migration",
            link: "/support/migration",
          },
          { text: "Release Strategy", link: "/support/release-strategy" },
        ],
      },
    ],
    outline: {
      level: "deep",
    },
    editLink: {
      pattern: "https://github.com/it-at-m/refarch/blob/main/docs/:path",
      text: "View this page on GitHub",
    },
    search: {
      provider: "local",
    },
    footer: {
      message: `<a href="https://opensource.muenchen.de/impress.html">Impress and Contact</a>`,
    },
  },
});

export default withMermaid(vitepressConfig);
