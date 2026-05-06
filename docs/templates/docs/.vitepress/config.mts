import { defineConfig } from "vitepress";

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "lhm-actions",
  base: "/lhm_actions/",
  description: "GitHub Actions for the workflows of the it-at-m organisation",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [{ text: "Home", link: "/" }],

    sidebar: [
      {
        items: [
          { text: "Actions", link: "/actions" },
          { text: "Deployment", link: "/deployment" },
          { text: "Workflows", link: "/workflows" },
        ],
      },
    ],

    socialLinks: [
      { icon: "github", link: "https://github.com/it-at-m/lhm_actions" },
    ],
  },
});
