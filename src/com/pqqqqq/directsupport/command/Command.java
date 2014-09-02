package com.pqqqqq.directsupport.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

	public String[] permissions();

	public String[] aliases();

	public String description();
	
	public String usage();
	
	public String example() default "";

}
