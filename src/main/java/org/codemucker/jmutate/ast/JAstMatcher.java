package org.codemucker.jmutate.ast;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Javadoc;

public class JAstMatcher extends ASTMatcher {

	public static Builder with(){
		return new Builder();
	}

	/**
	 * Creates a new AST matcher instance.
	 *
	 * @param matchDocTags <code>true</code> if doc comment tags are
	 * to be compared by default, and <code>false</code> otherwise
	 * @see #match(Javadoc,Object)
	 * @since 3.0
	 * @See {@link #with()}
	 */
	private JAstMatcher(boolean matchDocTags) {
		super(matchDocTags);
	}
/*	private <T extends ASTNode> T safeCast(Class<T> expectType, Object actual){
		if( !expectType.isInstance(actual)){
			fail("Expected type %s but was %s", expectType.getName(), actual.getClass().getName());
		}
		return (T) actual;
	}

	   private static void fail(String msg, Object... args){
	        Assert.fail(String.format(msg, args));
	    }
	    
*/
	public static class Builder {
		private boolean matchDocTags = false;

		public JAstMatcher build(){
			return new JAstMatcher(matchDocTags);
		}
		
		public ASTMatcher buildNonAsserting(){
			return new ASTMatcher(matchDocTags);
		}
		
		public Builder matchDocTags(boolean matchDocTags) {
        	this.matchDocTags = matchDocTags;
        	return this;
        }
				
	}
}
