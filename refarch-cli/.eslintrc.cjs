module.exports = {
  root: true,
  env: {
    node: true,
  },
  plugins: ["eslint-plugin-tsdoc", "@typescript-eslint/eslint-plugin"],
  extends: [
    "plugin:@typescript-eslint/recommended",
  ],
  rules: {
    "no-console": "error",
    "tsdoc/syntax": "warn",
  },
};
