package com.bertvanbrakel.codemucker.pattern;


import org.junit.Test;

import com.bertvanbrakel.codemucker.annotation.GenerateBuilder;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.SimpleMutationContext;
import com.bertvanbrakel.codemucker.ast.finder.FilterBuilder;
import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.SearchPathsBuilder;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JTypeMatchers;
import com.bertvanbrakel.codemucker.pattern.BeanBuilderPattern;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;
import com.bertvanbrakel.codemucker.util.SourceAsserts;

public class BeanBuilderPatternTest {

	MutationContext ctxt = new SimpleMutationContext();	
	
	@Test
	public void test_apply_pattern(){
		FindResult<JType> types = findMatchingTypes();
		JType type = types.getFirst();
		
		ctxt.obtain(BeanBuilderPattern.class)
			.setTarget(type)
			.apply();
		
	    JType expected = generateExpect(ctxt);
	    SourceAsserts.assertAstsMatch(expected, type);
	}
	
	public JType generateExpect(MutationContext ctxt){
		SourceTemplate t=ctxt.newSourceTemplate();
		t.pl("@GenerateBuilder");
		t.pl("public static class TestBuilderBean {");
		t.pl("	private String foo;");
		t.pl("	private String bar;");
		t.pl("	public static class Builder {");
		t.pl("		private java.lang.String foo;");
		t.pl("		private java.lang.String bar;");
		t.pl("		public Builder setFoo(java.lang.String foo){");
		t.pl("			this.foo = foo;");
		t.pl("			return this;");
		t.pl("		}");
		t.pl("		public Builder setBar(java.lang.String bar){");
		t.pl("			this.bar = bar;");
		t.pl("			return this;");
		t.pl("		}");
		t.pl("	}");
		t.pl("}");
		return t.asJType();	
	}

	private FindResult<JType> findMatchingTypes() {
	    FindResult<JType> found = JSourceFinder.newBuilder()
			.setSearchPaths(SearchPathsBuilder.newBuilder()
				.setIncludeClassesDir(false)
				.setIncludeTestDir(true)
			)
			.setFilter(FilterBuilder.newBuilder()
				//.addIncludeTypes(JTypeMatchers.withAnnotation(GenerateBuilder.class))
				.addIncludeTypes(JTypeMatchers.withFQN(TestBuilderBean.class))
			)	
			.build()
			.findTypes();
	    return found;
    }

	@GenerateBuilder
	public static class TestBuilderBean {
		private String foo;
		private String bar;
		
	}
}
