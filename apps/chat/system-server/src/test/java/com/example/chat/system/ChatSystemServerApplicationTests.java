package com.example.chat.system;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Disabled("Requires Docker/external services - run manually")
@SpringBootTest
@ActiveProfiles("test")
class ChatSystemServerApplicationTests {

	@Test
	void contextLoads() {
		// Context loading test
	}

}
