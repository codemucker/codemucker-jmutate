package com.bertvanbrakel.test.generation.a;

import com.bertvanbrakel.test.bean.annotation.BeanProperty;
import com.bertvanbrakel.test.bean.builder.Pattern;
import com.bertvanbrakel.test.bean.builder.PatternType;

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
