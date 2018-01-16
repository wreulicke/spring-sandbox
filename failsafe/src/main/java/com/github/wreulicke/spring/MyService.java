package com.github.wreulicke.spring;

import java.util.Map;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MyService {
	
	@GET("/backend/{number}")
	Single<Map<String, String>> call(@Path("number") int number);
}
