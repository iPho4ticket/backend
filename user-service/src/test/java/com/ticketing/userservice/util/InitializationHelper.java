package com.ticketing.userservice.util;

import java.util.function.Function;

public class InitializationHelper {

	public static <T> Function<? super T, ? extends T> noInit() {
		return (arg) -> arg;
	}
}