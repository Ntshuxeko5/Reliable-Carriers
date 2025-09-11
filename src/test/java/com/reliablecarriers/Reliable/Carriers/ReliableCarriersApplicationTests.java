package com.reliablecarriers.Reliable.Carriers;

import com.reliablecarriers.Reliable.Carriers.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ReliableCarriersApplicationTests {

	@Test
	void contextLoads() {
	}

}