package org.codemucker.jmutate.ast.matcher;

import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.JavaNameUtil;
import org.eclipse.jdt.core.dom.Type;

public class AType extends ObjectMatcher<Type>{

	public static AType that(){
		return with();
	}
	
	public static AType with(){
		return new AType();
	}
	
	public AType isEqualTo(JType t){
		fullName(t.getFullName());
		return this;
	}

	public AType fullName(String fullName){
		fullName(AString.equalTo(fullName));
		return this;
	}
	
	public AType fullName(final Matcher<String> fullNameMatcher){
		addMatcher(new AbstractNotNullMatcher<Type>() {
			@Override
			public boolean matchesSafely(Type found, MatchDiagnostics diag) {
				return diag.TryMatch(JavaNameUtil.resolveQualifiedName(found),fullNameMatcher);
				
			}
		});
		return this;
	}
	
}
