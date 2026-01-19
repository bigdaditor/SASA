package io.github.bigdaditor.sasa.annotation;

import java.lang.annotation.*;

/**
 * API 엔드포인트에 설명을 추가하는 어노테이션.
 *
 * <p>메서드 레벨 또는 클래스 레벨에서 사용할 수 있으며,
 * 메서드 레벨 어노테이션이 클래스 레벨보다 우선순위가 높습니다.</p>
 *
 * <h2>사용 예시</h2>
 *
 * <h3>메서드 레벨</h3>
 * <pre>{@code
 * @GetMapping("/users")
 * @ApiDescription("모든 사용자 목록을 조회합니다. 페이징을 지원합니다.")
 * public List<User> getUsers() { ... }
 * }</pre>
 *
 * <h3>클래스 레벨</h3>
 * <pre>{@code
 * @RestController
 * @ApiDescription("사용자 관리 API")
 * public class UserController { ... }
 * }</pre>
 *
 * <h3>요약과 설명 분리</h3>
 * <pre>{@code
 * @ApiDescription(
 *     value = "사용자를 생성합니다. 이메일 중복 체크를 수행하며, 비밀번호는 암호화되어 저장됩니다.",
 *     summary = "사용자 생성"
 * )
 * public User createUser(@RequestBody UserDto dto) { ... }
 * }</pre>
 *
 * <h2>자동 요약 생성</h2>
 * <p>{@code summary}를 지정하지 않으면 {@code value}의 첫 문장이 자동으로 요약으로 사용됩니다.
 * 문장 구분은 마침표(.), 물음표(?), 느낌표(!)로 판단합니다.</p>
 *
 * @author bigdaditor
 * @since 0.0.1
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiDescription {

    /**
     * API 엔드포인트의 전체 설명.
     *
     * <p>여러 문장으로 구성할 수 있으며, 첫 문장은 요약으로 자동 추출됩니다.</p>
     *
     * @return 설명 문자열 (기본값: 빈 문자열)
     */
    String value() default "";

    /**
     * API 엔드포인트의 짧은 요약 (한 줄).
     *
     * <p>지정하지 않으면 {@link #value()}의 첫 문장이 요약으로 사용됩니다.
     * 명시적으로 지정하면 해당 값이 우선 적용됩니다.</p>
     *
     * @return 요약 문자열 (기본값: 빈 문자열, value의 첫 문장 자동 사용)
     */
    String summary() default "";
}