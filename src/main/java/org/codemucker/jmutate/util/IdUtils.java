package org.codemucker.jmutate.util;

import java.util.concurrent.atomic.AtomicReference;

public class IdUtils {

	private static AtomicReference<Long> currentTime = new AtomicReference<>(System.currentTimeMillis());

	public static long nextTimeBasedId() {
		long prev;
		long next = System.currentTimeMillis();
		do {
			prev = currentTime.get();
			next = next > prev ? next : prev + 1;
		} while (!currentTime.compareAndSet(prev, next));
		return next;
	}

}
