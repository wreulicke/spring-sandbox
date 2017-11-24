package com.github.wreulicke.spring;

import org.ff4j.aop.Flip;

@Flip(name = "sample-feature", alterBean = "another")
public interface SampleService {
	String provide();
}
