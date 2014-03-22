package org.codemucker.jmutate;

import java.lang.annotation.Annotation;

import com.google.inject.name.Named;

public class NamedAnnotation implements Named {
	final String value;

	public NamedAnnotation(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

	public int hashCode() {
		// This is specified in java.lang.Annotation.
		return 127 * "value".hashCode() ^ value.hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof Named))
			return false;
		Named other = (Named) o;
		return value.equals(other.value());
	}

	public String toString() {
		return "@" + Named.class.getName() + "(value=" + value + ")";
	}

	public Class<? extends Annotation> annotationType() {
		return Named.class;
	}
}
