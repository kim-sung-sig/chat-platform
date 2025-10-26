package com.example.chat.common.util;

import java.util.Optional;
import java.util.function.Supplier;

public interface ThrowReader {

	default <T> T orThrow(Optional<T> optional, Supplier<? extends RuntimeException> exceptionSupplier) {
		return optional.orElseThrow(exceptionSupplier);
	}

}