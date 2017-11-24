/**
 * MIT License
 *
 * Copyright (c) 2017 Wreulicke
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.wreulicke.spring;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import rx.Single;

@RequestMapping("/user")
@RestController
@Slf4j
public class SampleController {
	
	@Autowired
	UnstableService service;
	
	@Autowired
	RestTemplate template;
	
	
	@GetMapping
	public User user() {
		return new User("test");
	}
	
	@GetMapping("/_test")
	public ResponseEntity<User> getUser() {
		log.info("test");
		return template.getForEntity("/user", User.class);
	}
	
	@GetMapping("/_unstable")
	public ResponseEntity<User> unstableGetUser() {
		log.info("test");
		return service.unstableGetUser();
	}
	
	@GetMapping("/_unstable_command")
	public Single<ResponseEntity<User>> unstable_commandGetUser() {
		log.info("test");
		return new UnstableCommand(template).toObservable().toSingle();
	}
	
	
	@Value
	public static class User {
		private final String name;
	}
}
