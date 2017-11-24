package com.github.wreulicke.configprops;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Qualifier;

@Target({
	ElementType.METHOD,
	ElementType.FIELD,
	ElementType.TYPE,
	ElementType.TYPE_PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface Hoge {
	
}
