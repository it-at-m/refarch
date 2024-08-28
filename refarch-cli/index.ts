#!/usr/bin/env node
import { cpSync, renameSync, rmSync } from "fs";

import { confirm, input, select, Separator } from "@inquirer/prompts";
import { replaceInFileSync } from "replace-in-file";

enum applications {
  FRONTEND = "frontend",
  BACKEND = "backend",
  EAI = "eai",
}
const EXIT = "exit";
let keepStack = true;
let keepDocs = true;
let hasJavaApplicationBeenGenerated = false;

/**
 * Starting-point for the Projekt configuration.
 * Depending on the value of the select, an interactive CLI will be shown
 */
async function projectConfiguration() {
  await select({
    message: "Select Project/s you want to generate with space",
    choices: [
      { name: applications.FRONTEND, value: applications.FRONTEND },
      { name: applications.BACKEND, value: applications.BACKEND },
      { name: applications.EAI, value: applications.EAI },
      { name: EXIT, value: EXIT },
      new Separator(),
    ],
  }).then(async (result: string) => {
    switch (result) {
      case applications.EAI:
      case applications.BACKEND:
        await generateJavaInteractiveCli(applications.EAI);
        break;
      case applications.FRONTEND:
        await generateFrontendInteractiveCli();
        break;
    }
    if (result != EXIT) {
      await projectConfiguration();
    }
  });
}

/**
 *  interactive CLI for the inputs, needed to generated the backend or eai
 * @param application - java application that should be generated
 */
async function generateJavaInteractiveCli(application: string) {
  hasJavaApplicationBeenGenerated = true;
  const groupId = await input({
    message:
      "Define value for property groupId (should match expression '^de\\.muenchen\\.[a-z0-9]+(\\.[a-z0-9]+)*$'): ",
    validate(value) {
      const pass = value.match(/^de\.muenchen\.[a-z0-9]+(\.[a-z0-9]+)*$/g);
      return pass ? true : "GroupId name not valid";
    },
    required: true,
  });
  const artifactId = await input({
    message: "Define value for property artifactId:",
    required: true,
  });
  const packageName = await input({
    message: "Define value for property package:",
    default: groupId,
    validate(value) {
      const pass = value.match(/^de\.muenchen\.[a-z0-9]+(\.[a-z0-9]+)*$/g);
      return pass ? true : "Package name not valid";
    },
    required: true,
  });
  if (application == applications.BACKEND) {
    generateBackend(packageName, groupId, artifactId);
  } else if (application == applications.EAI) {
    generateEAI(packageName, groupId, artifactId);
  }
}

/**
 * Generates a new BACKEND package with the provided package name, groupId and artifactId.
 * The function copies the existing `../refarch-backend` folder to a new folder named `../<artifactId>`.
 * It then performs string replacements on the Java files and pom.xml file within the new folder
 * to update the package name, groupId, and artifactId.
 *
 * @param packageName - The new package name to use for the Java files.
 * @param groupId - The new groupId to use for the pom.xml file.
 * @param artifactId - The new artifactId to use for the pom.xml file and the name of the new folder
 */
function generateBackend(packageName, groupId, artifactId) {
  cpSync("../refarch-backend", "../refarch-backend-copy", {
    recursive: true,
  });
  const replacements = [
    {
      files:
        "../refarch-backend-copy/src/main/java/de/muenchen/refarch/**/*.java",
      from: [/package de.muenchen.refarch/g, /import de.muenchen.refarch/g],
      to: [`package ${packageName}`, `import ${packageName}`],
      dry: true,
      countMatches: true,
    },
    {
      files: "../refarch-backend-copy/pom.xml",
      from: [
        "<groupId>de.muenchen.refarch</groupId>",
        "<artifactId>refarch-backend</artifactId>",
        "<name>refarch_backend</name>",
      ],
      to: [
        `<groupId>${groupId}</groupId>`,
        `<artifactId>${artifactId}</artifactId>`,
        `<name>${artifactId}</name>`,
      ],
      dry: true,
      countMatches: true,
    },
  ];
  Promise.all(replacements.map((options) => replaceInFileSync(options))).then(
    () => {
      renameSync("../refarch-backend-copy", `../${artifactId}`);
    }
  );
}

async function generateFrontendInteractiveCli() {
  const name = await input({
    message: "Define value for property name:",
    required: true,
  });
  generateFrontend(name);
}

/**
 * Generates a new FRONTEND directory with the provided name
 * The function copies the existing `../refarch-frontend` folder to a new folder named `../<name>`.
 * It then performs string replacements in the package.json and package-lock.json to update the name
 *
 * @param name - The new name to use for the application
 */
function generateFrontend(name: string) {
  cpSync("../refarch-frontend", "../refarch-frontend-copy", {
    recursive: true,
  });
  const replacements = {
    files: [
      "../refarch-frontend-copy/package.json",
      "../refarch-frontend-copy/package-lock.json",
    ],
    from: [/refarch-frontend/g],
    to: [`package ${name}`],
    dry: true,
    countMatches: true,
  };
  replaceInFileSync(replacements);
}

/**
 * Generates a new EAI package with the provided package name, groupId and artifactId.
 * The function copies the existing `../refarch-eai` folder to a new folder named `../<artifactId>`.
 * It then performs string replacements on the Java files and pom.xml file within the new folder
 * to update the package name, groupId, and artifactId.
 *
 * @param  packageName - The new package name to use for the Java files.
 * @param  groupId - The new groupId to use for the pom.xml file.
 * @param  artifactId - The new artifactId to use for the pom.xml file and the name of the new folder
 */
function generateEAI(packageName: string, groupId: string, artifactId: string) {
  cpSync("../refarch-eai", "../refarch-eai-copy", { recursive: true });
  const replacements = [
    {
      files: "../refarch-eai-copy/src/main/java/de/muenchen/refarch/**/*.java",
      from: [/package de.muenchen.refarch/g, /import de.muenchen.refarch/g],
      to: [`package ${packageName}`, `import ${packageName}`],
      dry: true,
      countMatches: true,
    },
    {
      files: "../refarch-eai-copy/pom.xml",
      from: [
        "<groupId>de.muenchen.refarch</groupId>",
        "<artifactId>refarch-eai</artifactId>",
        "<name>refarch_eai</name>",
      ],
      to: [
        `<groupId>${groupId}</groupId>`,
        `<artifactId>${artifactId}</artifactId>`,
        `<name>${artifactId}</name>`,
      ],
      dry: true,
      countMatches: true,
    },
  ];
  Promise.all(replacements.map((options) => replaceInFileSync(options))).then(
    () => {
      renameSync("../refarch-eai-copy", `../${artifactId}`);
    }
  );
}

/**
 *  generated the interactive CLI for choosing, if the Docs and Stack folder should be kept
 */
async function generateDefaultCliForDocsAndStack() {
  keepDocs = await confirm({ message: "Want to keep the Docs folder" });
  keepStack = await confirm({ message: "Want to keep the Stack folder" });
  //cleanup()
}

/**
 * remove the templates for the frontend, backend and EAI.
 * remove the shared files if not java-application got generated
 * remove the docs and stack depending on the user input
 */
function cleanup() {
  if (!keepDocs) {
    rmSync("../docs", { recursive: true });
  }
  if (!keepStack) {
    rmSync("../stack", { recursive: true });
  }
  if (!hasJavaApplicationBeenGenerated) {
    rmSync("../shared-files", { recursive: true });
  }
  rmSync("../refarch-eai", { recursive: true });
  rmSync("../refarch-backend", { recursive: true });
  rmSync("../refarch-frontend", { recursive: true });
}

module.exports = projectConfiguration();
