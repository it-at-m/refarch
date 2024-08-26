#!/usr/bin/env node
import * as fs from "fs";
import {replaceInFile} from "replace-in-file";
import {checkbox, input, select} from "@inquirer/prompts";


async function startpoint() {
    const FRONTEND = "frontend";
    const BACKEND = "backend";
    const EAI = "eai";
    await select({
        message: 'Select Project/s you want to generate with space',
        choices: [
            {name: FRONTEND, value: FRONTEND, description: ""},
            {name: BACKEND, value: BACKEND, description: ""},
            {name: EAI, value: EAI, description: ""},
            {name: "exit", value: "EXIT"}
        ]
    }).then(async (result: string) => {
        if (result == BACKEND) {
            await generateBackend();
        }
        if (result == FRONTEND) {
            await generateFrontend();
        }
        if (result == EAI) {
            await generateEAI();
        }
        if (result != "EXIT") {
            await startpoint();
        }
    });
}

async function generateBackend() {
    fs.cpSync("../refarch-backend", "../refarch-backend-copy", {recursive: true});
    const groupId = await input({
        message: "Define value for property groupId (should match expression '^de\\.muenchen\\.[a-z0-9]+(\\.[a-z0-9]+)*$'): ",
        validate(value) {
            const pass = value.match(/^de\.muenchen\.[a-z0-9]+(\.[a-z0-9]+)*$/g);
            return pass ? true : "GroupId name not valid";
        },
        required: true,
    });
    const artifactId = await input({message: "Define value for property artifactId:",  required: true,});
    const packageName = await input({message: "Define value for property package:",
        default: groupId,
        validate(value) {
            const pass = value.match(/^de\.muenchen\.[a-z0-9]+(\.[a-z0-9]+)*$/g);
            return pass ? true : "Package name not valid";
        },
        required: true,
    });
    const projectName = await input({message: "Define value for Project Name:",  required: true});
    const replacements = [{
        files: "../refarch-backend-copy/src/main/java/de/muenchen/refarch/**/*.java",
        from: [/package de.muenchen.refarch/g, /import de.muenchen.refarch/g],
        to: [`package ${packageName}`, `import ${packageName}`],
        dry: true,
        countMatches: true,
    }, {
        files: "../refarch-backend-copy/pom.xml",
        from: ["<groupId>de.muenchen.refarch</groupId>", "<artifactId>refarch-backend</artifactId>", "<name>refarch_backend</name>"],
        to: [`<groupId>${groupId}</groupId>`, `<artifactId>${artifactId}</artifactId>`, `<name>${artifactId}</name>`],
        dry: true,
        countMatches: true,
    }]
    Promise.all(
        replacements.map(options => replaceInFile(options))
    ).then(result => {
        console.log(result)
        fs.rename("../refarch-backend-copy", `../${projectName}`, () => {});
    });
    return Promise<void>;
}

async function generateFrontend() {
    const name = await input({message: "Define value for property name:",  required: true,});
    const replacements = {
        files: ["../refarch-frontend/package.json", "../refarch-frontend/package-lock.json"],
        from: [/refarch-frontend/g],
        to: [`package ${name}`],
        dry: true,
        countMatches: true,
    }
    const result = await replaceInFile(replacements)
    console.log(result);
}

async function generateEAI() {
    const groupId = await input({
        message: "Define value for property groupId (should match expression '^de\\.muenchen\\.[a-z0-9]+(\\.[a-z0-9]+)*$'): ",
        validate(value) {
            const pass = value.match(/^de\.muenchen\.[a-z0-9]+(\.[a-z0-9]+)*$/g);
            return pass ? true : "GroupId name not valid";
        },
        required: true,
    });
    const artifactId = await input({message: "Define value for property artifactId:",  required: true,});
    const packageName = await input({message: "Define value for property package:",
        default: groupId,
        validate(value) {
            const pass = value.match(/^de\.muenchen\.[a-z0-9]+(\.[a-z0-9]+)*$/g);
            return pass ? true : "Package name not valid";
        },
        required: true,
    });
    const replacements = [{
        files: "../refarch-eai/src/main/java/de/muenchen/refarch/**/*.java",
        from: [/package de.muenchen.refarch/g, /import de.muenchen.refarch/g],
        to: [`package ${packageName}`, `import ${packageName}`],
        dry: true,
        countMatches: true,
    }, {
        files: "../refarch-eai/pom.xml",
        from: ["<groupId>de.muenchen.refarch</groupId>", "<artifactId>refarch-eai</artifactId>", "<name>refarch_eai</name>"],
        to: [`<groupId>${groupId}</groupId>`, `<artifactId>${artifactId}</artifactId>`, `<name>${artifactId}</name>`],
        dry: true,
        countMatches: true,
    }]
    Promise.all(
        replacements.map(options => replaceInFile(options))
    ).then(result => {
        console.log(result)
       // fs.rename("../refarch-eai", `../${projectName}`, () => {});
    });
}

module.exports = startpoint();