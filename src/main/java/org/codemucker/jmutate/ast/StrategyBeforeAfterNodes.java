package org.codemucker.jmutate.ast;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.PlacementStrategy;
import org.eclipse.jdt.core.dom.ASTNode;


public class StrategyBeforeAfterNodes implements PlacementStrategy {

	private final Matcher<ASTNode> afterNodes;
	private final Matcher<ASTNode> beforeNodes;
	private final Insert defaultLocation;
	
	private static enum Insert {
		FIRST,LAST
	}
	
	private StrategyBeforeAfterNodes(Matcher<ASTNode> afterNodes, Matcher<ASTNode> beforeNodes,Insert defaultLocation) {
	    super();
	    this.afterNodes = afterNodes;
	    this.beforeNodes = beforeNodes;
	    this.defaultLocation = defaultLocation;
    }

	public static Builder with(){
		return new Builder();
	}
	
	@Override
    public int findIndexToPlaceInto(ASTNode nodeToInsert, List<ASTNode> nodes) {
		int index = findFirstIndexAfter(afterNodes, nodes);
		if (index != PlacementStrategy.INDEX_NOT_FOUND) {
			index++;
		} else {
			index = findFirstIndexBefore(beforeNodes, nodes);
		}
		if (index == PlacementStrategy.INDEX_NOT_FOUND) {
			switch (defaultLocation) {
			case LAST:
				index = nodes.size();
				break;
			case FIRST:
			default:
				index = 0;
				break;
			}
		}
		return index;
	}

	private int findFirstIndexAfter(Matcher<ASTNode> matcher,Collection<ASTNode> nodes) {
		int last = PlacementStrategy.INDEX_NOT_FOUND;
		int idx = 0;
		for (ASTNode node : nodes) {
			if (matcher.matches(node)) {
				if(last == PlacementStrategy.INDEX_NOT_FOUND || idx > last){
					last = idx;
				}
			}
			idx++;
		}
		return last;
	}

	private int findFirstIndexBefore(Matcher<ASTNode> matcher,Collection<ASTNode> nodes) {
		int first = PlacementStrategy.INDEX_NOT_FOUND; 
		int idx = 0;
		for (ASTNode node : nodes) {
			if (matcher.matches(node)) {
				if(first == PlacementStrategy.INDEX_NOT_FOUND || idx < first){
					first =idx;
				}
			}
			idx++;
		}
		return first;
	}
	
	public static class Builder {
		
		private final Collection<Matcher<ASTNode>> afterNodes = newArrayList();
		private final Collection<Matcher<ASTNode>> beforeNodes = newArrayList();
		private Insert defaultLocation = Insert.FIRST;
		
		
		public StrategyBeforeAfterNodes build(){
			return new StrategyBeforeAfterNodes(Logical.any(afterNodes),Logical.any(beforeNodes),defaultLocation);
		}

		public Builder defaultFirstNode() {
			defaultLocation = Insert.FIRST;
			return this;
		}

		public Builder defaultLastNode() {
			defaultLocation = Insert.LAST;
			return this;
		}

		public Builder beforeTypes(Class<?>... nodeTypes){
			for(Class<?> type:nodeTypes){
				before(matchesType(type));
			}
			return this;
		}

		public Builder afterTypes(Class<?>... nodeTypes){
			for(Class<?> type:nodeTypes){
				after(matchesType(type));
			}
			return this;
		}
		
		private Matcher<ASTNode> matchesType(final Class<?> expectNodeType ) {
			return new AbstractNotNullMatcher<ASTNode>() {
				@Override
				protected boolean matchesSafely(ASTNode actual,MatchDiagnostics diag) {
					return expectNodeType.equals(actual.getClass());
				}
			};
		}
		
		public Builder before(Matcher<ASTNode>... matchers){
			beforeNodes.addAll(Arrays.asList(matchers));
			return this;
		}
		
		public Builder before(Matcher<ASTNode> matcher){
			beforeNodes.add(matcher);
			return this;
		}
		
		public Builder after(Matcher<ASTNode>... matchers){
			afterNodes.addAll(Arrays.asList(matchers));
			return this;
		}
		
		public Builder after(Matcher<ASTNode> matcher){
			afterNodes.add(matcher);
			return this;
		}
	}

}
