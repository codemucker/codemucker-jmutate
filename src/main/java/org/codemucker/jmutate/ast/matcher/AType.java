package org.codemucker.jmutate.ast.matcher;

import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.util.NameUtil;
import org.eclipse.jdt.core.dom.Type;

public class AType extends ObjectMatcher<Type>{

	public static final AType STRING = AType.with().fullName("java.lang.String");
	public static final AType BOOL_PRIMITIVE = AType.with().fullName("boolean");
	public static final AType VOID = AType.with().fullName("void");
	
	public static AType that(){
		return with();
	}
	
	public static AType with(){
		return new AType();
	}
	
	public AType(){
	    super(Type.class);
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
				return diag.tryMatch(this,NameUtil.resolveQualifiedName(found),fullNameMatcher);
			}
			
			@Override
			public void describeTo(Description desc) {
			    desc.value("with fullname",fullNameMatcher);
			}
		});
		return this;
	}
	
}
