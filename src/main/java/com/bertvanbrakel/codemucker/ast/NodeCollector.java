package com.bertvanbrakel.codemucker.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class NodeCollector extends BaseASTVisitor {
	
	private final List<ASTNode> matchedNodes = Lists.newArrayList();
	private final Matcher<ASTNode> nodeMatcher;
	private final Map<Class<?>, NodeDepthCounter> nodeCounters = Maps.newHashMap();
	
	private int depth = 0;
			
	public static Builder newBuilder(){
		return new Builder();
	}
	
	private NodeCollector(Collection<NodeCritera> criteria, Matcher<ASTNode> nodeMatcher){
		for (NodeCritera crit : criteria) {
			NodeDepthCounter counter = new NodeDepthCounter();
			counter.maxDepth = crit.max;
			for (Class<?> nodeType : crit.nodeTypes) {
				//todo:detect duplicate
				this.nodeCounters.put(nodeType, counter);
			}
		}
		this.nodeMatcher = nodeMatcher;
	}
	
	@Override
	protected boolean visitNode(ASTNode node) {
		super.visitNode(node);
		boolean visit = true;
		if (depth > 0) {
			NodeDepthCounter counter = getCounterFor(node);
			if (counter != null) {
				visit = counter.visit();
			}
			if (visit && nodeMatcher.matches(node)) {
				matchedNodes.add(node);
			}
		}
		depth++;
		return visit;
	}
	
	@Override
	protected void endVisitNode(ASTNode node) {
		super.endVisitNode(node);
		if (depth > 1) { //always one more than the visit node
			NodeDepthCounter counter = getCounterFor(node);
			if(counter != null){	
				counter.endVisit();
			}
		}
		depth--;
	}
	
	private NodeDepthCounter getCounterFor(ASTNode node){
		return nodeCounters.get(node.getClass());
	}

	public List<ASTNode> getCollected(){
		return matchedNodes;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ASTNode> List<T> getCollectedAs(){
		return  (List<T>) matchedNodes;
	}
	
	static class NodeCritera {
		
		private final Collection<Class<?>> nodeTypes;
		private final int max;
		
		public NodeCritera(int max, Class<?> nodeType) {
			super();
			this.max = max;
			this.nodeTypes = new ArrayList<Class<?>>(1);
			this.nodeTypes.add(nodeType);
		}
		
		public NodeCritera(int max, Class<?>... nodeTypes) {
			super();
			this.max = max;
			this.nodeTypes = Lists.newArrayList(nodeTypes);
		}
	}
	
	private class NodeDepthCounter {
		int maxDepth;
		int currentDepth;
		
		boolean visit(){
			boolean visit = false;
			if( maxDepth > currentDepth){
				visit = true;
			}
			currentDepth++;
			return visit;
		}
		
		void endVisit(){
			if(currentDepth <= 0){
				throw new IllegalStateException("depth can't be negative");
			}
			currentDepth--;
		}
	}
	
	public static class Builder {
		private final Collection<NodeCritera> nodeTypeToMaxDepth = Lists.newArrayList();
		private final Collection<Class<?>> collectNodesOfType = Sets.newHashSet();
		
		public NodeCollector build(){
			return new NodeCollector(nodeTypeToMaxDepth,new NodeTypeMatcher(collectNodesOfType));
		}
		
		public Builder collectType(Class<? extends ASTNode> nodeType){
			collectNodesOfType.add(nodeType);
			return this;
		}
		
		public Builder ignoreChildTypes(){
			maxDepthOn(0,TypeDeclaration.class,AnonymousClassDeclaration.class,EnumDeclaration.class,AnnotationTypeDeclaration.class); 
//	        ClassInstanceCreation.class)
			
			return this;
		}
		
		public Builder ignoreType(Class<? extends ASTNode> nodeType){
			maxDepthOn(0,nodeType);
			return this;
		}
		
		public Builder maxDepthOn(int depth, Class<? extends ASTNode> nodeType){
			nodeTypeToMaxDepth.add(new NodeCritera(depth, nodeType));
			return this;
		}
		
		public Builder maxDepthOn(int depth,Class<? extends ASTNode>... nodeTypesAsGroup){
			nodeTypeToMaxDepth.add(new NodeCritera(depth, nodeTypesAsGroup));
			return this;
		}
		
		private static class NodeTypeMatcher extends BaseMatcher<ASTNode>{
			private final Collection<Class<?>> nodeTypes;
			
			public NodeTypeMatcher(Collection<Class<?>> nodeTypes) {
				super();
				this.nodeTypes = ImmutableSet.copyOf(nodeTypes);
			}

			@Override
			public boolean matches(Object item) {
				return nodeTypes.contains(item.getClass());
			}

			@Override
			public void describeTo(Description description) {
				description.appendValueList("nodes of type[", ",", "]", nodeTypes);
			}
			

			
		}
	}
}