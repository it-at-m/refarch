import commonjs from "@rollup/plugin-commonjs";
import json from "@rollup/plugin-json";
import nodeResolve from "@rollup/plugin-node-resolve";
import typescript from "@rollup/plugin-typescript";

export default {
  input: "index.ts",
  output: {
    dir: "dist",
    format: "esm",
    entryFileNames: "[name].mjs",
  },
  plugins: [
    typescript(),
    nodeResolve({ exportConditions: ["node"] }),
    commonjs(),
    json(),
  ],
};
