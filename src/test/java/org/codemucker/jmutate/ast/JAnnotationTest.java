package org.codemucker.jmutate.ast;

import static org.codemucker.jmatch.Assert.assertThat;
import static org.codemucker.jmatch.Assert.isEqualTo;

import java.util.List;

import org.codemucker.jmutate.TestSourceHelper;
import org.codemucker.jpattern.IsGenerated;
import org.junit.Test;

public class JAnnotationTest {

    @Test
	public void testResolveSimpleName(){
		JType type = TestSourceHelper.findSourceForClass(JAnnotationTest.class).getTypeWithName(TestBean.class);
		List<JAnnotation> annons = type.getAnnotations().getAllIncludeNested();
		
		assertThat(annons.size(),isEqualTo(1));
	
		JAnnotation anon = annons.iterator().next();
		
		assertThat(anon.getQualifiedName(),isEqualTo(IsGenerated.class.getName()));
		assertThat(anon.isOfType(IsGenerated.class));
	}
	
	public static class TestBean {
		
		@IsGenerated
		public void myMethod(){
			
		}
	}
}
