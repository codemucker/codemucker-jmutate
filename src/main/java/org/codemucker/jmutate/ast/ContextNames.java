package org.codemucker.jmutate.ast;

import com.google.inject.name.Named;

/**
 * Instead of using strings in guice context names ({@link Named}) use this instead

 */
public final class ContextNames {

	public static final String FIELD = "jmutate.field";
	public static final String CTOR = "jmutate.ctor";
	public static final String METHOD = "jmutate.method";
	public static final String TYPE = "jmutate.type";
	public static final String MARK_GENERATED = "jmutate.markeGenerated";
}
