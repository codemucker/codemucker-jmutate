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
package org.codemucker.jmutate.ast.a;

import com.bertvanbrakel.codemucker.annotation.BeanProperty;
import com.bertvanbrakel.codemucker.annotation.Pattern;
import com.bertvanbrakel.codemucker.annotation.PatternType;

@Pattern(type=PatternType.Builder)
public class TestBean {

	@BeanProperty(name = "one")
	private String fieldOne;
	private String fieldTwo;

	public void methodOne() {

	}

	public void methodTwo() {

	}

	private static class StaticInnerClass {

		private String staticInnerClassFieldA;

		public void staticInnerDoIt() {

		}
	}

	private class InnerClass {
		private String innerClassFieldA;

		public void innerDoIt() {

		}
	}

}
