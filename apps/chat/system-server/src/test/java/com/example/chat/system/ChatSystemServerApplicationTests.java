package com.example.chat.system;

import com.example.chat.system.infrastructure.config.QuartzConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = QuartzConfig.class))
class ChatSystemServerApplicationTests {

	@Test
	void contextLoads() {
		// Context loading test
	}

}