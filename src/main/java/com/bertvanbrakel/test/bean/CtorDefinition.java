/*
 * Copyright 2011 Bert van Brakel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bertvanbrakel.test.bean;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class CtorDefinition {

	private final Constructor<?> ctor;
	private final List<CtorArgDefinition> args;
	private boolean makeAccessible = false;

	public CtorDefinition(Constructor<?> ctor) {
		this.ctor = ctor;
		args = new ArrayList<CtorArgDefinition>(ctor.getParameterTypes().length);
	}

	public boolean isMakeAccessible() {
		return makeAccessible;
	}

	public void setMakeAccessible(boolean makeAccessible) {
		this.makeAccessible = makeAccessible;
	}

	public Constructor<?> getCtor() {
		return ctor;
	}

	public void addArg(CtorArgDefinition arg) {
		args.add(arg);
	}
	
	public int getNumArgs(){
		return args.size();
	}

	public List<CtorArgDefinition> getArgs() {
		return args;
	}

	public CtorArgDefinition getArg(int position) {
		return args.get(position);
	}

	public CtorArgDefinition getArgNamed(String name) {
		for (CtorArgDefinition arg : args) {
			if (name.equals(arg.getName())) {
				return arg;
			}
		}
		return null;
	}

}
