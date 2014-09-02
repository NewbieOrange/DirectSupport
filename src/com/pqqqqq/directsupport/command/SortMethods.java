package com.pqqqqq.directsupport.command;

import java.lang.reflect.Method;
import java.util.Comparator;

public class SortMethods implements Comparator<Method> {

	@Override
	public int compare(Method m1, Method m2) {
		Command cm1 = m1.getAnnotation(Command.class);
		Command cm2 = m2.getAnnotation(Command.class);
		
		if (cm1 == null || cm2 == null)
			return 0;
		
		String a1 = cm1.aliases()[0];
		String a2 = cm2.aliases()[0];
		
		return a1.compareToIgnoreCase(a2);
	}
}
