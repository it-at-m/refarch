# OpenAPI Integration

The documentation of the api (at least the backend component for now) follows the OpenApi specification.
The OpenApi definition ist generated from the controllers inside the backend-component, which serves as the single source of truth.
Therefor the documentation of the api is not saved anywhere permanent inside the codebase.

The next chapters provide an overview how the documentation is integrated into the component and explains the usage.

## Available Documentation Endpoints

The backend component supplies the OpenApi documentation over two endpoints explained below.
Both endpoints will be dynamically updated based on the current annotations and JavaDoc present in the controller code.

### Swagger UI

The Swagger UI provides a web-based interface for exploring and testing the documented API. 
It is automatically available when using springdoc-openapi-starter-webmvc-ui and can be accessed on the following path:

```http request
/swagger-ui/index.html
```

This endpoint renders the OpenAPI specification in a user-friendly format and allows direct interaction with all available operations.

### OpenAPI

The raw OpenAPI specification is served as machine-readable JSON and YAML. 
It can be retrieved locally from the following standard paths:

```http request
/v3/api-docs (JSON)

/v3/api-docs.yaml (YAML)
```


These endpoints are normally used for client generation, validation or exporting of the documentation as this is standardized format for any OpenAPI generator.

## Generated Local Export for Client Generation

To enable OpenAPI-based client generation or external documentation processing, the current API definition can be exported locally as a YAML file using the Maven plugin provided by springdoc.
This export is performed via the following command:

```shell
mvn springdoc-openapi:generate
```
> **Note:** The backend component must be running during this process. The plugin fetches the OpenAPI definition from the running instance via its /v3/api-docs endpoint.

The OpenAPI YAML file will be saved to the project's target directory with the name of the maven project itself (`{project-name}.yaml`).
> **Note:** It is recommended not to change the output file location or name, as other processes (e.g., client generation) might depend on the default.

## Documenting controller endpoints

Currently, there are two ways to add documentation to an endpoint.
The default case (currently used inside the backend component) is the usage of javadoc which is automagically used to enrich die OpenAPI documentation.
Another possibility is the usage of annotations. Both are explained below.

### JavaDoc

Documenting the various endpoints via JavaDoc is possibly the more intuitive way.
By documentation a method with basic JavaDoc will supply information directly to the OpenAPI-Documentation.

```java
/**
 * This text is used as a description for the following request.
 * @param someId describes the path variable used for this request.
 * @return explains the return value of the request.f
 */
@GetMapping("{someId}")
@ResponseStatus(HttpStatus.OK)
public SomeEntity getEndpoint(@PathVariable("someId") final UUID someId) {
    return service.doSth(someId);
}

```

Using JavaDoc only allows the documentation of a description, the variables used for the request and the return value.
Examples for a request are automatically created with default values. 

### SpringDoc Annotation
In addition to JavaDoc, it's possible to document endpoints using OpenAPI-specific annotations. 
This approach offers much finer control over the generated documentation, allowing for custom examples, detailed descriptions, response codes, parameter specifications, and security requirements.

Annotation-based documentation is especially useful when more structured metadata is needed, such as for standardized response objects or complex request examples.
The annotations are provided by springdoc-openapi and come from the `io.swagger.v3.oas.annotations.*` package.

```java
@Operation(
    summary = "Your summary here",
    description = "This text is used as a description for the following request.",
    parameters = {
        @Parameter(
            name = "someId",
            description = "someId for an entity",
            required = true,
            in = ParameterIn.PATH,
            example = "a81bc81b-dead-4e5d-abff-90865d1e13b1"
        )
    }
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Entity for someId was found"
    ),
    @ApiResponse(responseCode = "404", description = "SomeEntity not found"),
})
@GetMapping("{someId}")
@ResponseStatus(HttpStatus.OK)
public SomeEntity getEndpoint(@PathVariable("someId") final UUID someId) {
  return service.doSth(someId);
}
```

This example using the annotation is exaggerated but shows the possibilities. Further information can be found on the official [springdoc webpage](https://springdoc.org/).
