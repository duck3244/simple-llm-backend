package com.example.simple;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "llm.vllm.enabled=false",
    "llm.sglang.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SimpleApplicationTests {

    @Test
    void contextLoads() {
        // Spring Boot 애플리케이션 컨텍스트가 정상적으로 로드되는지 확인
        // 이 테스트가 통과하면 모든 Bean이 정상적으로 생성됨을 의미
    }

    @Test
    void applicationStartsSuccessfully() {
        // 애플리케이션이 성공적으로 시작되는지 확인
        // 추가적인 검증 로직이 필요한 경우 여기에 작성
    }
}