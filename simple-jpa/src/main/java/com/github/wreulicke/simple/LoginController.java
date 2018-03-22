package com.github.wreulicke.simple;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class LoginController {
	
	@GetMapping("/login")
	public String login(Authentication authentication) {
		log.info("test {}", authentication);
		return "login";
	}
	
}
