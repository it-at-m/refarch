module.exports = {
  root: true,
  env: {
    node: true,
  },
  extends: [
    // JavaScript
    "eslint:recommended",
    "plugin:@typescript-eslint/recommended",
  ],
  rules: {
    "no-console": "error",
  },
};
