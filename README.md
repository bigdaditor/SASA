# SASA - Spring API Spec Analyzer

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
[![Spring](https://img.shields.io/badge/Spring-6.0+-green.svg)](https://spring.io/)

SASA is a Gradle library that automatically extracts API specifications from Spring Boot applications through **runtime introspection**. Instead of parsing source code, it directly reads Spring MVC's runtime mapping information to generate accurate API documentation in JSON and HTML formats.

## Why SASA?

**Tired of Swagger annotation hell?** SASA takes a different approach.

| Traditional Approach | SASA |
|---------------------|------|
| Requires `@Operation`, `@ApiResponse`, `@Schema` annotations everywhere | Zero annotations needed |
| Documentation can drift from actual implementation | Always accurate - reads actual runtime mappings |
| Clutters your controller code | Clean controllers, documentation generated separately |
| Manual maintenance burden | Fully automatic extraction |

## Features

* 🚀 **Runtime Introspection**: Directly extracts Spring MVC's actual mapping information
* 📝 **Auto-generated Documentation**: Outputs API docs in both JSON and HTML formats
* 🎨 **Clean UI**: Interactive HTML documentation with light/dark themes
* 🔍 **Comprehensive Details**: Extracts parameters, request bodies, response types, and schemas
* ⚙️ **Flexible Filtering**: Filter by path patterns or HTTP methods
* 📦 **Lightweight**: Minimal dependencies for easy integration

## Installation

### Gradle

```groovy
repositories {
    mavenLocal()
    // or mavenCentral() when published
}

dependencies {
    implementation 'com.example:sasa:0.0.1-SNAPSHOT'
}
```

### Maven

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>sasa</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Quick Start

### Basic Usage

Call SASA at your Spring Boot application startup:

```java
@SpringBootApplication
public class YourApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(YourApplication.class, args);

        // Generate API spec
        RequestMappingHandlerMapping mapping = context.getBean(RequestMappingHandlerMapping.class);
        SasaApplication.generateApiSpec(mapping, context);
    }
}
```

After running, the following files are generated:

* `build/api-spec.json` - API specification in JSON format
* `build/api-spec.html` - Interactive HTML documentation

### Custom Configuration

```java
SasaConfig config = SasaConfig.builder()
    .applicationName("My API")
    .outputFilePath("docs/api-spec.json")
    .excludeActuator()
    .excludeError()
    .includePath("/api/**")
    .build();

SasaApplication.generateApiSpec(mapping, context, config);
```

## Configuration Options

### Output Settings

```java
SasaConfig.builder()
    .enableConsoleOutput(true)           // Print JSON to console (default: false)
    .enableFileOutput(true)              // Save to file (default: true)
    .outputFilePath("custom/path.json")  // Output path (default: build/api-spec.json)
    .applicationName("My Service")       // App name (default: SASA)
    .build();
```

### Path Filtering

```java
SasaConfig.builder()
    .includePath("/api/**")              // Include only paths starting with /api
    .includePath("/user/*")              // Include /user/* pattern
    .excludePath("/actuator/**")         // Exclude /actuator paths
    .excludePath("/error")               // Exclude /error path
    .excludeActuator()                   // Exclude Spring Actuator endpoints
    .excludeError()                      // Exclude Spring Error endpoints
    .build();
```

### HTTP Method Filtering

```java
SasaConfig.builder()
    .includeHttpMethod("GET")            // Include only GET methods
    .includeHttpMethod("POST")           // Include POST methods
    .excludeHttpMethod("DELETE")         // Exclude DELETE methods
    .onlyGetMethods()                    // Include GET only
    .onlyReadMethods()                   // Include GET, HEAD, OPTIONS only
    .build();
```

## Output Format

### JSON Output

```json
{
  "applicationName": "My API",
  "version": "0.0.1-SNAPSHOT",
  "generatedAt": "2025-01-04T10:30:00",
  "endpoints": [
    {
      "paths": ["/api/users"],
      "methods": ["GET"],
      "handler": "UserController#getUsers",
      "parameters": [
        {
          "name": "page",
          "type": "Integer",
          "parameterType": "QUERY_PARAM",
          "required": false
        }
      ],
      "response": {
        "type": "List",
        "elementType": "UserDto",
        "schema": {
          "fields": [
            {
              "name": "id",
              "type": "Long"
            },
            {
              "name": "username",
              "type": "String",
              "validations": {
                "notBlank": true,
                "size": { "min": 3, "max": 20 }
              }
            }
          ]
        }
      }
    }
  ],
  "exceptionHandlers": [...]
}
```

### HTML Output

SASA automatically generates interactive HTML documentation featuring:

* 📊 **Overview**: Statistics for endpoints and exception handlers
* 🔍 **Endpoints**: Detailed info for each endpoint (click to expand/collapse)
* 🎨 **HTTP Method Colors**: GET (green), POST (blue), PUT (orange), DELETE (red)
* 📱 **Responsive Design**: Optimized view across all devices

## Extracted Information

SASA extracts the following information:

### Endpoint Information

* HTTP methods (GET, POST, PUT, DELETE, PATCH, etc.)
* Paths
* Handler methods (Controller and method names)
* Content Types (Consumes/Produces)

### Parameter Information

* Path Variables
* Query Parameters
* Request Headers
* Request Body (including DTO schema)
* Parameter types and required status

### Response Information

* Return types
* Generic types (List, Map, etc.)
* Element types
* DTO field schemas
* Validation annotation info

### Exception Handlers

* Handled exception types
* Handler methods
* Advice types (ControllerAdvice, etc.)

## Examples

### Example 1: Public API Only

```java
SasaConfig config = SasaConfig.builder()
    .applicationName("Public API")
    .includePath("/api/v1/**")
    .excludeActuator()
    .excludeError()
    .build();

SasaApplication.generateApiSpec(mapping, context, config);
```

### Example 2: Read-Only Endpoints

```java
SasaConfig config = SasaConfig.builder()
    .applicationName("Read-Only API")
    .onlyReadMethods()  // GET, HEAD, OPTIONS only
    .build();

SasaApplication.generateApiSpec(mapping, context, config);
```

### Example 3: Custom Output Path

```java
SasaConfig config = SasaConfig.builder()
    .outputFilePath("docs/openapi/api-spec.json")
    .enableConsoleOutput(true)
    .build();

SasaApplication.generateApiSpec(mapping, context, config);
```

## Architecture

Here's how SASA works:

```
Spring Boot Application
    ↓
RequestMappingHandlerMapping (Runtime mapping info)
    ↓
SASA Extractors
    ├── EndpointExtractor (Extracts endpoints)
    ├── ParameterExtractor (Extracts parameters)
    ├── ResponseExtractor (Extracts response types)
    └── ExceptionHandlerExtractor (Extracts exception handlers)
    ↓
API Spec (Map)
    ↓
Generators
    ├── JSON Generator (Jackson)
    └── HTML Generator (Template)
    ↓
Output Files
    ├── api-spec.json
    └── api-spec.html
```

## Why Runtime Introspection?

Here's why we chose runtime introspection over source code parsing:

✅ **Accuracy**: Reads the actual mapping information as interpreted by Spring  
✅ **Simplicity**: No AST parsing or annotation processing required  
✅ **Completeness**: Reflects all Spring configurations and conditions  
✅ **Maintainability**: Stable even when upgrading Spring versions

## Requirements

* Java 17 or higher
* Spring Framework 6.0+
* Spring Boot 3.0+ (recommended)

## Dependencies

SASA requires only these libraries:

* `spring-webmvc` - Spring MVC core
* `spring-context` - Spring context
* `jackson-databind` - JSON serialization
* `jackson-datatype-jsr310` - Java 8 date/time support
* `jakarta.validation-api` (optional) - Validation annotation support

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Roadmap

- [ ] OpenAPI 3.0 spec output support
- [ ] Swagger UI integration
- [ ] WebFlux support
- [ ] Markdown documentation generation
- [ ] REST API endpoint for serving specs
- [ ] Postman Collection generation

## Author

Created by [@bigdaditor](https://github.com/bigdaditor)

## Acknowledgments

* Spring Framework team for the amazing framework
* All contributors who help improve this project
