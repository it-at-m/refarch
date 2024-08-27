#!/usr/bin/env node
import * as fs from "fs";
import {replaceInFile} from "replace-in-file";
import {input, select, Separator} from "@inquirer/prompts";

async function startpoint() {
    const FRONTEND = "frontend";
    const BACKEND = "backend";
    const EAI = "eai";
    const EXIT = "exit"
    await select({
        message: 'Select Project/s you want to generate with space',
        choices: [
            {name: FRONTEND, value: FRONTEND},
            {name: BACKEND, value: BACKEND},
            {name: EAI, value: EAI},
            {name: EXIT, value: EXIT},
            new Separator()
        ]
    }).then(async (result: string) => {
        switch (result) {
            case BACKEND:
                await generateBackendInteractiveCli();
                break;
            case FRONTEND:
                await generateFrontendInteractiveCli();
                break;
            case EAI:
                await generateEaiInteractiveCli();
                break;
        }
        if (result != EXIT) {
            await startpoint();
        }
    });
}

async function generateBackendInteractiveCli() {
    const groupId = await input({
        message: "Define value for property groupId (should match expression '^de\\.muenchen\\.[a-z0-9]+(\\.[a-z0-9]+)*$'): ",
        validate(value) {
            const pass = value.match(/^de\.muenchen\.[a-z0-9]+(\.[a-z0-9]+)*$/g);
            return pass ? true : "GroupId name not valid";
        },
        required: true,
    });
    const artifactId = await input({message: "Define value for property artifactId:", required: true,});
    const packageName = await input({
        message: "Define value for property package:",
        default: groupId,
        validate(value) {
            const pass = value.match(/^de\.muenchen\.[a-z0-9]+(\.[a-z0-9]+)*$/g);
            return pass ? true : "Package name not valid";
        },
        required: true,
    });
    const projectName = await input({message: "Define value for Project Name:", required: true});
    generateBackend(packageName, groupId, artifactId, projectName);
}


function generateBackend(packageName, groupId, artifactId, projectName) {
    fs.cpSync("../refarch-backend", "../refarch-backend-copy", {recursive: true});
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
        fs.renameSync("../refarch-backend-copy", `../${projectName}`);
    });
}

async function generateFrontendInteractiveCli() {
    const name = await input({message: "Define value for property name:", required: true,});
    await generateFrontend(name)
}

async function generateFrontend(name: string) {
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

async function generateEaiInteractiveCli() {
    const groupId = await input({
        message: "Define value for property groupId (should match expression '^de\\.muenchen\\.[a-z0-9]+(\\.[a-z0-9]+)*$'): ",
        validate(value) {
            const pass = value.match(/^de\.muenchen\.[a-z0-9]+(\.[a-z0-9]+)*$/g);
            return pass ? true : "GroupId name not valid";
        },
        required: true,
    });
    const artifactId = await input({message: "Define value for property artifactId:", required: true,});
    const packageName = await input({
        message: "Define value for property package:",
        default: groupId,
        validate(value) {
            const pass = value.match(/^de\.muenchen\.[a-z0-9]+(\.[a-z0-9]+)*$/g);
            return pass ? true : "Package name not valid";
        },
        required: true,
    });
    await generateEAI(groupId, artifactId, packageName);
}

async function generateEAI(packageName, groupId, artifactId,) {
    fs.cpSync("../refarch-eai", "../refarch-eai-copy", {recursive: true});
    const replacements = [{
        files: "../refarch-eai-copy/src/main/java/de/muenchen/refarch/**/*.java",
        from: [/package de.muenchen.refarch/g, /import de.muenchen.refarch/g],
        to: [`package ${packageName}`, `import ${packageName}`],
        dry: true,
        countMatches: true,
    }, {
        files: "../refarch-eai-copy/pom.xml",
        from: ["<groupId>de.muenchen.refarch</groupId>", "<artifactId>refarch-eai</artifactId>", "<name>refarch_eai</name>"],
        to: [`<groupId>${groupId}</groupId>`, `<artifactId>${artifactId}</artifactId>`, `<name>${artifactId}</name>`],
        dry: true,
        countMatches: true,
    }]
    Promise.all(
        replacements.map(options => replaceInFile(options))
    ).then(result => {
        fs.renameSync("../refarch-eai-copy", `../${artifactId}`);
    });
}

function cleanup() {
    fs.rmSync("../refarch-eai", {recursive: true})
    fs.rmSync("../refarch-backend", {recursive: true})
    fs.rmSync("../refarch-frontend", {recursive: true})
}

module.exports = startpoint();