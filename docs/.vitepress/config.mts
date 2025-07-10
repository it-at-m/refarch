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
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: "Home", link: "/" },
      {
        text: "Docs",
        items: [
          { text: "Overview", link: "/overview" },
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
        text: "Components",
        items: [{ text: "API Gateway", link: "/gateway" }],
      },
      {
        text: "Templates",
        link: "/templates/",
        items: [
          { text: "Getting Started", link: "/templates/getting-started" },
          { text: "Develop", link: "/templates/develop" },
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
            text: "CI/CD",
            link: "https://github.com/it-at-m/lhm_actions/blob/main/docs/actions.md",
          },
          { text: "Security", link: "/cross-cutting-concepts/security" },
          { text: "Tools", link: "/cross-cutting-concepts/tools" },
          { text: "OpenApi", link: "/cross-cutting-concepts/openapi" },
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
