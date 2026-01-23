# Document

The templates include various tools and best practices to help you with documenting your software project.
Those are further explained below.

::: danger IMPORTANT
Please make sure you worked through the corresponding [Getting Started](./getting-started.md#documentation) instructions before proceeding.
:::

## Code-focused documentation

To support code understandability for developers it's useful to document the code using mechanisms like [Javadoc](https://www.oracle.com/de/technical-resources/articles/java/javadoc-tool.html), [TSDoc](https://tsdoc.org/) or respective tools for other programming languages.
This also improves developer experience when using tools like IDEs.

Additionally, it's recommended to document developed APIs using standards like [OpenAPI](https://www.openapis.org/).
We provide OpenAPI support for documenting APIs out of the box. This is further described in [our OpenAPI documentation page](../cross-cutting-concepts/openapi.md).

The rest of this page will focus on code-decoupled documentation.

## README.md

The `README.md` acts as an entrypoint to your GitHub repository and thus should have the most important information about your repository.
We highly suggest you only include the most important stuff and use a dedicated documentation page (see more in the sections down below) for the actual content.
But if you have a small repository and don't need a dedicated documentation page, you can use the `README.md` file for that purpose as well.

Nonetheless, we provide a template for your `README.md` file inside the `.github` folder. This file needs to be adjusted to your specific project.
Alter the project name, description, used technologies and remove sections like "Roadmap" if you don't need them. Also update the project shields and links at the top of the file to your needs.
Please check out the [shields.io documentation](https://shields.io/) for further information.

When you solely use the `README.md` as your documentation also make sure that any links to externally hosted documentation are reachable or removed if not required.
Also in this case, the sections below are irrelevant to you.

## Writing the documentation

We encourage you to write your software documentation using Markdown (`.md`). This format is broadly supported, standard on GitHub and has good support in IDEs.
If you need further information about Markdown itself, please check out this [guide](https://www.markdownguide.org/).

By combining the use of Markdown with the Node-based tool [Vitepress](https://vitepress.dev/), we enable easy conversion of raw markdown files into aesthetically pleasing and easy to use static web pages.

In the [templates repository](https://github.com/it-at-m/refarch-templates), you will find a `docs` folder. This folder holds your markdown files and other configuration files for Vitepress itself.
For more information on Vitepress and its configuration options, please check out its [Getting Started Guide](https://vitepress.dev/guide/getting-started).

If you want a preview of the documentation while writing, use the `npm run dev` script to spin up a web server serving your documentation.

## Checking for errors

To maintain high quality in your documentation, we provide a script to run various checking tools.

By executing `npm run lint`, the tool [markdownlint-cli](https://github.com/igorshubovych/markdownlint-cli) will be used to check validity of your markdown files.
The configuration file we provide (`.markdownlint.jsonc`) is almost default, but feel free to adjust it to your own needs. Please check out the official documentation of [markdownlint](https://github.com/DavidAnson/markdownlint#optionsconfig) in this case.
For all other files (e.g. the Vitepress configuration file) [Prettier](https://prettier.io/) and its respective configuration file (`.prettierrc`) is used.

A lot of the reported errors can be automatically fixed by running `npm run fix`.
It's recommended to ensure your documentation is error-free before committing to version control.

## Deploying to the web

Deploying the documentation to the web requires the markdown files to be converted into static `.html` files. You can use `npm run build` to build the documentation and check the final result on your local machine inside the generated `dist` folder.
You can then move this folder onto a web server manually.

For ease of use, we prefer the use of GitHubs CI/CD capabilities using workflows and actions. That's why the templates provide a workflow file to deploy your application to the web. This file can be found in the directory `.github/workflows/deploy-docs.yml`.
By default, the documentation is deployed whenever a push to the `main` branch occurs, but you can adjust the workflow file to your needs.
For further information about CI/CD-related topics, please check out the documentation for our [custom actions and workflows](https://github.com/it-at-m/.github).

::: danger IMPORTANT
To run this workflow, you must enable GitHub Pages for your repository and set the source to "GitHub Actions." For step-by-step instructions, see the official [GitHub documentation](https://docs.github.com/en/pages/getting-started-with-github-pages/configuring-a-publishing-source-for-your-github-pages-site#publishing-with-a-custom-github-actions-workflow).
:::

::: info Information
If your documentation is hosted under a sub path (e.g. on GitHub Pages `https://username.github.io/my-repo/`), make sure to set the `base` property in your VitePress `config.mts`. Without this, asset URLs may point to the root path and result in 404 errors.
:::
