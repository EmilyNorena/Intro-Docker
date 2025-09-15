package com.mycompany.httpserver;
import java.util.concurrent.atomic.AtomicLong;

import com.mycompany.annotations.*;

@RestController
public class HelloController {
	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	@GetMapping("/greeting")
	public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return "Hola " + name;
	}
}


