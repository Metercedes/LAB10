package com.example.lab10;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class Lab10ApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void contextLoads() {
		// Verify that the application context loads successfully
		assert applicationContext != null;
	}

}
