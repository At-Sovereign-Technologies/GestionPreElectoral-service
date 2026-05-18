package com.selloLegitimo.GestionPreElectoral;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public abstract class AbstractIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("gestion_pre_electoral")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("DB_URL", postgreSQLContainer::getJdbcUrl);
        registry.add("DB_USER", postgreSQLContainer::getUsername);
        registry.add("DB_PASSWORD", postgreSQLContainer::getPassword);
    }
}
