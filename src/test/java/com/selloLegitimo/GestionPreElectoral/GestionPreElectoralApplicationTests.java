package com.selloLegitimo.GestionPreElectoral;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "grpc.elecciones.host=localhost",
        "grpc.elecciones.port=9090"
})
class GestionPreElectoralApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }

}
