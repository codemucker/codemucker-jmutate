package org.codemucker.jmutate.generate.model;

import java.util.NoSuchElementException;

public interface IModel {

	void set(Object key, Object val);

	boolean has(Object key);

	/**
	 * @See {@link #getOrFail(Object)}
	 * @param key
	 * @return
	 */
	<T> T getOrFail(Class<T> key);

	/**
	 * Get the object associated with the given key or fail if no such value with the given key or the value is null
	 * @param key
	 * @return
	 * @throws NoSuchElementException if no element with the given key exists
	 */
	Object getOrFail(Object key) throws NoSuchElementException;

	<T> T getOrNull(Class<T> key);

	Object getOrNull(Object key);

}