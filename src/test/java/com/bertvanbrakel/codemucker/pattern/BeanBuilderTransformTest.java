package com.bertvanbrakel.codemucker.pattern;


import java.util.Collection;

import org.junit.Test;

import com.bertvanbrakel.codemucker.annotation.GenerateBuilder;
import com.bertvanbrakel.codemucker.ast.JType;
import com.bertvanbrakel.codemucker.ast.SimpleMutationContext;
import com.bertvanbrakel.codemucker.ast.finder.FilterBuilder;
import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.JSourceFinder;
import com.bertvanbrakel.codemucker.ast.finder.SearchPathsBuilder;
import com.bertvanbrakel.codemucker.ast.matcher.JTypeMatchers;
import com.bertvanbrakel.codemucker.pattern.BeanBuilderTransform;
import com.bertvanbrakel.codemucker.transform.MutationContext;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;
import com.bertvanbrakel.codemucker.util.SourceAsserts;

public class BeanBuilderTransformTest {

	MutationContext ctxt = new SimpleMutationContext();	
	
	@Test
	public void test_apply_pattern(){
		FindResult<JType> types = findTypesToTransform();
		JType type = types.getFirst();
		
		whenTransformAppliedTo(type);
		
	    JType expected = generateExpect(ctxt);
	    SourceAsserts.assertAstsMatch(expected, type);
	}

	private void whenTransformAppliedTo(JType type) {
		ctxt.obtain(BeanBuilderTransform.class)
			.setTarget(type)
			.transform();
	}
	
	public JType generateExpect(MutationContext ctxt){
		SourceTemplate t=ctxt.newSourceTemplate();
		t.pl("@GenerateBuilder");
		t.pl("public static class TestBuilderBean {");
		t.pl("	private String myString;");
		t.pl("	private int myInt;");
		t.pl("	private Collection<String> col;");
		
		t.pl("	public TestBuilderBean(String myString,int myInt,Collection<String> col){this.myString = myString;this.myInt=myInt;this.col=col;}");
		t.pl("	public static class Builder {");
		t.pl("		private String myString;");
		t.pl("		private int myInt;");
		t.pl("		private Collection<String> col;");
		
		t.pl("		public Builder setMyString(String myString){");
		t.pl("			this.myString = myString;");
		t.pl("			return this;");
		t.pl("		}");
		t.pl("		public Builder setMyInt(int myInt){");
		t.pl("			this.myInt = myInt;");
		t.pl("			return this;");
		t.pl("		}");
		t.pl("		public Builder setCol(Collection<String> col){");
		t.pl("			this.col = col;");
		t.pl("			return this;");
		t.pl("		}");
		t.pl("		public TestBuilderBean build(){");
		t.pl("			return new TestBuilderBean(myString,myInt,col);");
		t.pl("		}");
		t.pl("	}");
		t.pl("}");
		return t.asJType();	
	}

	private FindResult<JType> findTypesToTransform() {
	    FindResult<JType> found = JSourceFinder.newBuilder()
			.setSearchPaths(SearchPathsBuilder.newBuilder()
				.setIncludeClassesDir(false)
				.setIncludeTestDir(true)
			)
			.setFilter(FilterBuilder.newBuilder()
				//.addIncludeTypes(JTypeMatchers.withAnnotation(GenerateBuilder.class))
				.addIncludeTypes(JTypeMatchers.withFullName(TestBuilderBean.class))
			)	
			.build()
			.findTypes();
	    return found;
    }

	@GenerateBuilder
	public static class TestBuilderBean {
		private String myString;
		private int myInt;
		private Collection<String> col;
	}
}
