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

import com.bertvanbrakel.codemucker.annotation.BeanProperty;

public class TstBeanAnnotations {

	private String myField;

	@BeanProperty(name = "noMethods")
	private String myFieldNoMethods;

	@BeanProperty(name = "  ")
	private String myFieldEmptyAnnotationName;

	@BeanProperty(name = "customName")
	public String getMyField() {
		return myField;
	}

	@BeanProperty(name = "customName")
	public void setMyField(String myField) {
		this.myField = myField;
	}

}
