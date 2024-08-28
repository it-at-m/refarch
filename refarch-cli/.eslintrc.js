module.exports = {
  root: true,
  env: {
    node: true,
  },
  plugins: ["eslint-plugin-tsdoc", "@typescript-eslint/eslint-plugin"],
  extends: [
    // JavaScript
    "plugin:@typescript-eslint/recommended",
  ],
  rules: {
    "no-console": "error",
    "tsdoc/syntax": "warn",
  },
};
