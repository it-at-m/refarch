# Templates

The Refarch templates provide starter files for web application projects based on the reference architecture of it@M.

The templates allow us to:

1. 🚀 Accelerate project setup: With repository templates, new projects are kick-started in no time! Pre-configured with essential files and folder structures, these templates eliminate the repetitive setup tasks, allowing developers to focus on business logic.
2. 💎 Achieve consistent code quality: By establishing best practices for a unified structure and including helpful development tools, code consistency and quality is maintained across the organization as best as possible and collaboration is improved.
3. 👥 Simplify onboarding: With clear documentation and predefined development configurations, new developers can quickly get up to speed, reducing the learning curve and increasing productivity from day one.

The code for the templates is maintained under a different repository than the rest of the RefArch code. This is because the templates are used as a template repository in GitHub and thus need to be in its separate location.
You can find the code for the templates in the [refarch-templates repository](https://github.com/it-at-m/refarch-templates).

::: info Information
All path references you find in the following documentation for the templates are always in relation to the [refarch-templates repository](https://github.com/it-at-m/refarch-templates) (if not stated differently).
:::

## Available Templates

### Frontend

The frontend provides the application UI and is developed with Vue.js. It calls the backend (via the API gateway) for reading and modifying data.
It's developed separate for each application and therefore a default template is provided.

Language: [TypeScript](<[TypeScript](https://www.typescriptlang.org/)>)  
Technologies: Vue.js, Vuetify

### Backend

The backend provides a REST-API for reading and modifying application data, handles business logic and access to databases.
Like the frontend the backend is also provided as a template.

Language: Java  
Technologies: [Spring Boot](https://spring.io/projects/spring-boot)

### Web Component

The web components template enables development of modular frontend components which can be integrated into other applications.
This concept is also known as micro frontends. By default, most applications won't require this template.

Language: [TypeScript](https://www.typescriptlang.org/)  
Technologies: [Vue.js](https://vuejs.org/), [muc-patternlab-vue](https://github.com/it-at-m/muc-patternlab-vue)

### Enterprise Application Integration (EAI)

Beside the development of web applications, the RefArch also provides a template for enterprise application integration (EAI).
This enables integration between existing applications. By default, most applications won't require this template.

Language: Java  
Technologies: [Apache Camel](https://camel.apache.org/), [Spring Boot](https://spring.io/projects/spring-boot)

:::info Plain Spring EAI
As an alternative to the `eai` template (which uses Apache Camel and Spring Boot) the `backend` template can be used
for starting a plain Spring Boot EAI.
:::
