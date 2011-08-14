package com.bertvanbrakel.test.bean;

import com.bertvanbrakel.test.bean.annotation.BeanProperty;

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
