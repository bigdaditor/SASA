# SASA - Spring API Spec Analyzer

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
[![Spring](https://img.shields.io/badge/Spring-6.0+-green.svg)](https://spring.io/)

SASA는 **런타임 인트로스펙션**을 통해 Spring Boot 애플리케이션의 API 스펙을 자동으로 추출하는 Gradle 라이브러리입니다. 소스 코드 파싱이 아닌 Spring MVC의 런타임 매핑 정보를 직접 읽어 정확한 API 문서를 JSON 및 HTML 형식으로 생성합니다.

## Why SASA?

**Swagger 어노테이션 지옥에서 벗어나세요.** SASA는 다른 접근 방식을 제안합니다.

| 기존 방식 | SASA |
|-----------|------|
| `@Operation`, `@ApiResponse`, `@Schema` 어노테이션 필수 | 어노테이션 불필요 (선택적 `@ApiDescription` 제공) |
| 문서가 실제 구현과 달라질 수 있음 | 항상 정확 - 실제 런타임 매핑 정보를 읽음 |
| 컨트롤러 코드가 지저분해짐 | 깔끔한 컨트롤러, 문서는 별도 생성 |
| 수동 유지보수 부담 | 완전 자동 추출 |

## Features

* 🚀 **런타임 인트로스펙션**: Spring MVC의 실제 매핑 정보를 직접 추출
* 📝 **자동 문서 생성**: JSON 및 HTML 형식의 API 문서 출력
* 🎨 **깔끔한 UI**: 라이트/다크 테마를 지원하는 인터랙티브 HTML 문서
* 🔍 **상세 정보 추출**: 파라미터, Request Body, Response 타입, 스키마 추출
* ✅ **Validation 지원**: Bean Validation 어노테이션 자동 추출 (`@NotNull`, `@Size` 등)
* 📖 **선택적 설명**: `@ApiDescription` 어노테이션으로 커스텀 설명 추가
* ⚙️ **유연한 필터링**: 경로 패턴 또는 HTTP 메서드별 필터링
* 🔌 **확장 가능한 아키텍처**: 인터페이스 기반 설계로 쉬운 커스터마이징
* 📦 **경량**: 최소한의 의존성으로 쉬운 통합

## Installation

### Gradle

```groovy
repositories {
    mavenLocal()
    // or mavenCentral() when published
}

dependencies {
    implementation 'io.github.bigdaditor:SASA:0.0.1-SNAPSHOT'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.bigdaditor</groupId>
    <artifactId>SASA</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Quick Start

### 기본 사용법

Spring Boot 애플리케이션 시작 시 SASA를 호출하세요:

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

실행 후 다음 파일이 생성됩니다:

* `build/api-spec.json` - JSON 형식의 API 스펙
* `build/api-spec.html` - 인터랙티브 HTML 문서

### 커스텀 설정

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

## API 설명 추가하기

`@ApiDescription` 어노테이션을 사용하여 엔드포인트에 설명을 추가할 수 있습니다:

```java
@RestController
@ApiDescription("사용자 관리 엔드포인트")
public class UserController {

    @GetMapping("/users")
    @ApiDescription("페이지네이션을 지원하는 전체 사용자 조회. 사용자 객체 목록을 반환합니다.")
    public List<User> getUsers(@RequestParam(defaultValue = "0") int page) {
        // ...
    }

    @PostMapping("/users")
    @ApiDescription(value = "시스템에 새로운 사용자를 생성합니다", summary = "사용자 생성")
    public User createUser(@RequestBody @Valid UserDto dto) {
        // ...
    }
}
```

어노테이션 지원 기능:
- `value`: 전체 설명 텍스트
- `summary`: 짧은 요약 (미지정 시 첫 문장에서 자동 추출)
- 메서드 레벨 어노테이션이 클래스 레벨 어노테이션보다 우선

## 설정 옵션

### 출력 설정

```java
SasaConfig.builder()
    .enableConsoleOutput(true)           // 콘솔에 JSON 출력 (기본값: false)
    .enableFileOutput(true)              // 파일 저장 (기본값: true)
    .outputFilePath("custom/path.json")  // 출력 경로 (기본값: build/api-spec.json)
    .applicationName("My Service")       // 앱 이름 (기본값: SASA)
    .build();
```

### 경로 필터링

```java
SasaConfig.builder()
    .includePath("/api/**")              // /api로 시작하는 경로만 포함
    .includePath("/user/*")              // /user/* 패턴 포함
    .excludePath("/actuator/**")         // /actuator 경로 제외
    .excludePath("/error")               // /error 경로 제외
    .excludeActuator()                   // Spring Actuator 엔드포인트 제외
    .excludeError()                      // Spring Error 엔드포인트 제외
    .build();
```

### HTTP 메서드 필터링

```java
SasaConfig.builder()
    .includeHttpMethod("GET")            // GET 메서드만 포함
    .includeHttpMethod("POST")           // POST 메서드 포함
    .excludeHttpMethod("DELETE")         // DELETE 메서드 제외
    .onlyGetMethods()                    // GET만 포함
    .onlyReadMethods()                   // GET, HEAD, OPTIONS만 포함
    .build();
```

## 출력 형식

### JSON 출력

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
      "description": "페이지네이션을 지원하는 전체 사용자 조회",
      "summary": "페이지네이션을 지원하는 전체 사용자 조회",
      "parameters": [
        {
          "name": "page",
          "type": "Integer",
          "parameterType": "REQUEST_PARAM",
          "required": false,
          "defaultValue": "0"
        }
      ],
      "response": {
        "type": "List",
        "elementType": "User",
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

### HTML 출력

SASA는 다음 기능을 포함한 인터랙티브 HTML 문서를 자동 생성합니다:

* 📊 **Overview**: 엔드포인트 및 예외 핸들러 통계
* 🔍 **Endpoints**: 각 엔드포인트의 상세 정보 (클릭하여 펼치기/접기)
* 📖 **Descriptions**: 문서화된 엔드포인트의 요약 및 전체 설명 표시
* 🎨 **HTTP 메서드 색상**: GET (녹색), POST (파랑), PUT (주황), DELETE (빨강)
* 📱 **반응형 디자인**: 모든 기기에서 최적화된 뷰

## 추출 정보

SASA는 다음 정보를 추출합니다:

### 엔드포인트 정보

* HTTP 메서드 (GET, POST, PUT, DELETE, PATCH 등)
* 경로
* 핸들러 메서드 (컨트롤러 및 메서드 이름)
* Content Type (Consumes/Produces)
* 설명 (`@ApiDescription`에서)

### 파라미터 정보

* Path Variable (`@PathVariable`)
* Query Parameter (`@RequestParam`)
* Request Header (`@RequestHeader`)
* Request Body (`@RequestBody` - DTO 스키마 포함)
* 파라미터 타입 및 필수 여부
* 기본값

### 응답 정보

* 반환 타입
* 제네릭 타입 (List, Map, ResponseEntity 등)
* 요소 타입
* DTO 필드 스키마

### Validation 정보

Bean Validation 어노테이션 자동 추출:
* `@NotNull`, `@NotEmpty`, `@NotBlank`
* `@Size(min, max)`
* `@Min`, `@Max`
* `@Email`, `@Pattern`
* `@Positive`, `@Negative`, `@PositiveOrZero`, `@NegativeOrZero`
* `@Past`, `@Future`, `@PastOrPresent`, `@FutureOrPresent`
* `@Digits`, `@DecimalMin`, `@DecimalMax`

### 예외 핸들러

* 처리되는 예외 타입
* 핸들러 메서드
* Advice 타입 (ControllerAdvice 등)

## Architecture

SASA는 모듈화된 인터페이스 기반 아키텍처를 사용합니다:

```
Spring Boot Application
    ↓
RequestMappingHandlerMapping (런타임 매핑 정보)
    ↓
SASA Extractors (인터페이스 기반)
    ├── EndpointExtractor      → DefaultEndpointExtractor
    ├── ParameterExtractor     → DefaultParameterExtractor
    ├── ResponseExtractor      → DefaultResponseExtractor
    ├── TypeSchemaExtractor    → DefaultTypeSchemaExtractor
    ├── ValidationExtractor    → DefaultValidationExtractor
    ├── DescriptionExtractor   → DefaultDescriptionExtractor
    └── ExceptionHandlerExtractor → DefaultExceptionHandlerExtractor
    ↓
API Spec (Map)
    ↓
Output Generators
    ├── JsonOutputGenerator
    └── HtmlGenerator
    ↓
Output Writers
    └── FileOutputWriter
    ↓
Output Files
    ├── api-spec.json
    └── api-spec.html
```

### SASA 확장하기

인터페이스를 구현하여 커스텀 추출기를 만들 수 있습니다:

```java
public class CustomEndpointExtractor implements EndpointExtractor {
    @Override
    public List<Map<String, Object>> extract(RequestMappingHandlerMapping mapping, SasaConfig config) {
        // 커스텀 추출 로직
    }
}
```

## 사용 예시

### 예시 1: Public API만

```java
SasaConfig config = SasaConfig.builder()
    .applicationName("Public API")
    .includePath("/api/v1/**")
    .excludeActuator()
    .excludeError()
    .build();

SasaApplication.generateApiSpec(mapping, context, config);
```

### 예시 2: 읽기 전용 엔드포인트

```java
SasaConfig config = SasaConfig.builder()
    .applicationName("Read-Only API")
    .onlyReadMethods()  // GET, HEAD, OPTIONS만
    .build();

SasaApplication.generateApiSpec(mapping, context, config);
```

### 예시 3: API 설명 포함

```java
@RestController
@RequestMapping("/api/products")
@ApiDescription("상품 카탈로그 관리")
public class ProductController {

    @GetMapping
    @ApiDescription("전체 상품 목록을 조회합니다. 카테고리 필터링과 페이지네이션을 지원합니다.")
    public Page<Product> listProducts(
            @RequestParam(required = false) String category,
            Pageable pageable) {
        // ...
    }

    @GetMapping("/{id}")
    @ApiDescription(value = "ID로 단일 상품을 조회합니다", summary = "상품 조회")
    public Product getProduct(@PathVariable Long id) {
        // ...
    }
}
```

## 왜 런타임 인트로스펙션인가?

소스 코드 파싱 대신 런타임 인트로스펙션을 선택한 이유:

✅ **정확성**: Spring이 해석한 실제 매핑 정보를 읽음
✅ **단순성**: AST 파싱이나 어노테이션 프로세싱 불필요
✅ **완전성**: 모든 Spring 설정과 조건 반영
✅ **유지보수성**: Spring 버전 업그레이드에도 안정적

## Requirements

* Java 17 이상
* Spring Framework 6.0+
* Spring Boot 3.0+ (권장)

## Dependencies

SASA는 다음 라이브러리만 필요합니다:

* `spring-webmvc` - Spring MVC 코어
* `spring-context` - Spring 컨텍스트
* `jackson-databind` - JSON 직렬화
* `jackson-datatype-jsr310` - Java 8 날짜/시간 지원
* `jakarta.validation-api` (선택) - Validation 어노테이션 지원

## License

이 프로젝트는 Apache License 2.0 하에 라이선스가 부여됩니다 - 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## Contributing

기여를 환영합니다! Pull Request를 자유롭게 제출해 주세요.

1. 저장소를 Fork 합니다
2. Feature 브랜치를 생성합니다 (`git checkout -b feature/AmazingFeature`)
3. 변경 사항을 커밋합니다 (`git commit -m 'Add some AmazingFeature'`)
4. 브랜치에 Push 합니다 (`git push origin feature/AmazingFeature`)
5. Pull Request를 엽니다

## Roadmap

- [ ] OpenAPI 3.0 스펙 출력 지원
- [ ] Swagger UI 통합
- [ ] WebFlux 지원
- [ ] Markdown 문서 생성
- [ ] REST API 엔드포인트로 스펙 제공
- [ ] Postman Collection 생성

## Author

Created by [@bigdaditor](https://github.com/bigdaditor)

## Acknowledgments

* Spring Framework 팀의 훌륭한 프레임워크에 감사드립니다
* 이 프로젝트를 개선하는 데 도움을 주신 모든 기여자분들께 감사드립니다