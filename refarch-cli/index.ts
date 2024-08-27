#!/usr/bin/env node
import * as fs from "fs";
import {replaceInFile, replaceInFileSync} from "replace-in-file";
import {input, select, Separator} from "@inquirer/prompts";

const FRONTEND = "frontend";
const BACKEND = "backend";
const EAI = "eai";
const EXIT = "exit"

async function projectConfiguration() {

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
            case EAI:
            case BACKEND:
                await generateJavaInteractiveCli(EAI);
                break;
            case FRONTEND:
                await generateFrontendInteractiveCli();
                break;
        }
        if (result != EXIT) {
            await projectConfiguration();
        }
    });
}

async function generateJavaInteractiveCli(application: string) {
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
    if(application == BACKEND) {
        generateBackend(packageName, groupId, artifactId);
    } else if (application == EAI){
        generateEAI(packageName, groupId, artifactId)
    }
}

function generateBackend(packageName, groupId, artifactId) {
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
        replacements.map(options => replaceInFileSync(options))
    ).then(result => {
        fs.renameSync("../refarch-backend-copy", `../${artifactId}`);
    });
}

async function generateFrontendInteractiveCli() {
    const name = await input({message: "Define value for property name:", required: true,});
    generateFrontend(name)
}

function generateFrontend(name: string) {
    fs.cpSync("../refarch-frontend", "../refarch-frontend-copy", {recursive: true});
    const replacements = {
        files: ["../refarch-frontend-copy/package.json", "../refarch-frontend-copy/package-lock.json"],
        from: [/refarch-frontend/g],
        to: [`package ${name}`],
        dry: true,
        countMatches: true,
    }
    replaceInFileSync(replacements)
}


function generateEAI(packageName, groupId, artifactId,) {
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
        replacements.map(options => replaceInFileSync(options))
    ).then(result => {
        fs.renameSync("../refarch-eai-copy", `../${artifactId}`);
    });
}

function cleanup() {
    fs.rmSync("../refarch-eai", {recursive: true})
    fs.rmSync("../refarch-backend", {recursive: true})
    fs.rmSync("../refarch-frontend", {recursive: true})
}

module.exports = projectConfiguration();