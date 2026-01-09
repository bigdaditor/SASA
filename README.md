# SASA - Spring API Spec Analyzer

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
[![Spring](https://img.shields.io/badge/Spring-6.0+-green.svg)](https://spring.io/)

SASA는 **Spring Boot 애플리케이션의 API 메타데이터를 런타임 기준으로 수집**하여  
API 스펙을 **JSON 및 HTML 문서 형태로 자동 생성**하는 라이브러리입니다.

Spring Context와 Handler Mapping 정보를 기반으로 실제 실행 환경과 일치하는 API 정보를 추출합니다.
## Summary

- **문제**: API 문서는 코드/문서 간 싱크가 쉽게 깨지고 유지보수 비용이 높음
- **해결**: Spring 런타임 매핑 정보를 직접 읽어 “실제 동작” 기준의 문서를 자동 생성
- **결과물**: `build/api-spec.json`, `build/api-spec.html` 자동 생성

## Key Features

- 🚀 **런타임 인트로스펙션** 기반 정확한 매핑 추출
- 📝 **JSON/HTML 자동 생성** 및 인터랙티브 문서 제공
- 🔍 **상세 스키마**: 파라미터, Request Body, Response 타입/스키마
- ⚙️ **유연한 필터링**: 경로/HTTP 메서드 단위 포함/제외
- 📦 **경량 통합**: 최소 의존성으로 손쉬운 적용

## Tech Stack

- Java 17, Spring Boot 3 / Spring Framework 6
- Gradle, Jackson
- Optional: `jakarta.validation-api`

## How It Works

```
Spring Boot Application
    ↓
RequestMappingHandlerMapping (런타임 매핑)
    ↓
Extractors (Endpoint/Parameter/Response/Exception)
    ↓
API Spec (Map)
    ↓
Generators (JSON/HTML)
    ↓
Output Files (api-spec.json / api-spec.html)
```

## Quick Start

### Basic Usage

```java
@SpringBootApplication
public class YourApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(YourApplication.class, args);

        RequestMappingHandlerMapping mapping = context.getBean(RequestMappingHandlerMapping.class);
        SasaApplication.generateApiSpec(mapping, context);
    }
}
```

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

## Configuration Highlights

```java
SasaConfig.builder()
    .enableConsoleOutput(true)
    .enableFileOutput(true)
    .outputFilePath("custom/path.json")
    .applicationName("My Service")
    .includePath("/api/**")
    .excludePath("/actuator/**")
    .onlyReadMethods()
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
            { "name": "id", "type": "Long" },
            {
              "name": "username",
              "type": "String",
              "validations": { "notBlank": true, "size": { "min": 3, "max": 20 } }
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

- 📊 **Overview**: 엔드포인트 및 예외 핸들러 통계
- 🔍 **Endpoints**: 상세 정보 아코디언 UI
- 🎨 **HTTP Method 색상 구분**
- 📱 **반응형 디자인**

## Installation

### Gradle

```gradle
repositories {
    mavenLocal()
    // or mavenCentral() when published
}

dependencies {
    implementation 'io.github.bigdaditor:sasa:0.0.1-SNAPSHOT'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.bigdaditor</groupId>
    <artifactId>sasa</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Why Runtime Introspection?

- **정확성**: Spring이 해석한 실제 매핑 정보를 사용
- **단순성**: AST 파싱/어노테이션 프로세싱 불필요
- **완전성**: 런타임 설정까지 반영
- **유지보수성**: 프레임워크 업그레이드에 강함

## Requirements

- Java 17+
- Spring Framework 6.0+
- Spring Boot 3.0+ (recommended)

## Roadmap

- [ ] OpenAPI 3.0 스펙 출력 지원
- [ ] Swagger UI 통합
- [ ] WebFlux 지원
- [ ] Markdown 문서 생성
- [ ] REST API 엔드포인트로 스펙 제공
- [ ] Postman Collection 생성

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
