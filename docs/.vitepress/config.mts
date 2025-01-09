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
          { text: "Integrations", link: "/integrations" },
          { text: "Development", link: "/dev" },
        ],
      },
    ],
    sidebar: [
      { text: "Overview", link: "/overview" },
      { text: "API Gateway", link: "/gateway" },
      {
        text: "Integrations",
        link: "/integrations",
        items: [{ text: "S3", link: "/integrations/s3" }],
      },
      {
        text: "Development",
        link: "/dev",
        items: [
          { text: "Tools", link: "/dev/tools" },
          { text: "Stack", link: "/dev/stack" },
        ],
      },
    ],
    outline: {
      level: "deep",
    },
    socialLinks: [
      { icon: "github", link: "https://github.com/it-at-m/refarch" },
    ],
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
