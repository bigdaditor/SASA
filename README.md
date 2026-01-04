# SASA - Spring API Spec Analyzer

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
[![Spring](https://img.shields.io/badge/Spring-6.0+-green.svg)](https://spring.io/)

SASA는 Spring Boot 애플리케이션의 API 스펙을 **런타임 인트로스펙션**을 통해 자동으로 추출하는 Gradle 라이브러리입니다. 소스 코드 파싱 없이 Spring MVC의 런타임 매핑 정보를 직접 읽어 정확한 API 문서를 JSON과 HTML 형식으로 생성합니다.

## Features

- 🚀 **런타임 인트로스펙션**: Spring MVC의 실제 매핑 정보를 직접 추출
- 📝 **자동 문서 생성**: JSON과 HTML 형식으로 API 문서 자동 생성
- 🎨 **깔끔한 UI**: 화이트/블랙 디자인의 인터랙티브 HTML 문서
- 🔍 **상세 정보**: 파라미터, Request Body, Response 타입 및 스키마 추출
- ⚙️ **유연한 필터링**: 경로, HTTP 메서드별 필터링 지원
- 📦 **경량 라이브러리**: 최소한의 의존성으로 가볍게 통합

## Installation

### Gradle

```gradle
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

Spring Boot 애플리케이션의 시작 시점에 SASA를 호출하세요:

```java
@SpringBootApplication
public class YourApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(YourApplication.class, args);

        // API 스펙 생성
        RequestMappingHandlerMapping mapping = context.getBean(RequestMappingHandlerMapping.class);
        SasaApplication.generateApiSpec(mapping, context);
    }
}
```

실행 후 다음 파일들이 생성됩니다:
- `build/api-spec.json` - JSON 형식의 API 스펙
- `build/api-spec.html` - 인터랙티브 HTML 문서

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
    .enableConsoleOutput(true)           // 콘솔에 JSON 출력 (기본: false)
    .enableFileOutput(true)              // 파일로 저장 (기본: true)
    .outputFilePath("custom/path.json")  // 출력 경로 (기본: build/api-spec.json)
    .applicationName("My Service")       // 앱 이름 (기본: SASA)
    .build();
```

### Path Filtering

```java
SasaConfig.builder()
    .includePath("/api/**")              // /api로 시작하는 경로만 포함
    .includePath("/user/*")              // /user/* 패턴 포함
    .excludePath("/actuator/**")         // /actuator 경로 제외
    .excludePath("/error")               // /error 경로 제외
    .excludeActuator()                   // Spring Actuator 제외
    .excludeError()                      // Spring Error 제외
    .build();
```

### HTTP Method Filtering

```java
SasaConfig.builder()
    .includeHttpMethod("GET")            // GET 메서드만 포함
    .includeHttpMethod("POST")           // POST 메서드 포함
    .excludeHttpMethod("DELETE")         // DELETE 메서드 제외
    .onlyGetMethods()                    // GET만 포함
    .onlyReadMethods()                   // GET, HEAD, OPTIONS만 포함
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

SASA는 자동으로 인터랙티브한 HTML 문서를 생성합니다:

- 📊 **Overview**: 엔드포인트 및 예외 핸들러 통계
- 🔍 **Endpoints**: 각 엔드포인트의 상세 정보 (클릭하여 펼치기/접기)
- 🎨 **HTTP Method 색상**: GET(녹색), POST(파란색), PUT(주황색), DELETE(빨간색)
- 📱 **반응형 디자인**: 모든 디바이스에서 최적화된 뷰

## Extracted Information

SASA는 다음 정보를 추출합니다:

### Endpoint Information
- HTTP 메서드 (GET, POST, PUT, DELETE, PATCH 등)
- 경로 (Path)
- 핸들러 메서드 (Controller와 Method 이름)
- Content Types (Consumes/Produces)

### Parameter Information
- Path Variables
- Query Parameters
- Request Headers
- Request Body (DTO 스키마 포함)
- Parameter 타입 및 required 여부

### Response Information
- 반환 타입
- Generic 타입 (List, Map 등)
- Element 타입
- DTO 필드 스키마
- Validation 어노테이션 정보

### Exception Handlers
- 처리하는 예외 타입
- 핸들러 메서드
- Advice 타입 (ControllerAdvice 등)

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

SASA는 다음과 같은 방식으로 동작합니다:

```
Spring Boot Application
    ↓
RequestMappingHandlerMapping (런타임 매핑 정보)
    ↓
SASA Extractors
    ├── EndpointExtractor (엔드포인트 추출)
    ├── ParameterExtractor (파라미터 추출)
    ├── ResponseExtractor (응답 타입 추출)
    └── ExceptionHandlerExtractor (예외 핸들러 추출)
    ↓
API Spec (Map)
    ↓
Generators
    ├── JSON Generator (Jackson)
    └── HTML Generator (템플릿)
    ↓
Output Files
    ├── api-spec.json
    └── api-spec.html
```

## Why Runtime Introspection?

기존의 소스 코드 파싱 방식 대신 런타임 인트로스펙션을 사용하는 이유:

✅ **정확성**: Spring이 실제로 해석한 매핑 정보를 직접 읽음
✅ **단순성**: AST 파싱이나 어노테이션 프로세싱 불필요
✅ **완전성**: Spring의 모든 설정과 조건을 반영
✅ **유지보수**: Spring 업그레이드 시에도 안정적

## Requirements

- Java 17 or higher
- Spring Framework 6.0+
- Spring Boot 3.0+ (recommended)

## Dependencies

SASA는 다음 라이브러리만 필요합니다:

- `spring-webmvc` - Spring MVC 핵심
- `spring-context` - Spring 컨텍스트
- `jackson-databind` - JSON 직렬화
- `jackson-datatype-jsr310` - Java 8 날짜/시간 지원
- `jakarta.validation-api` (optional) - Validation 어노테이션 지원

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

- [ ] OpenAPI 3.0 스펙 출력 지원
- [ ] Swagger UI 통합
- [ ] WebFlux 지원
- [ ] Markdown 문서 생성
- [ ] REST API 엔드포인트로 스펙 제공
- [ ] Postman Collection 생성

## Author

SASA is created and maintained by the community.

## Acknowledgments

- Spring Framework team for the amazing framework
- All contributors who help improve this project