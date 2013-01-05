package com.bertvanbrakel.codemucker.ast.finder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;

import com.bertvanbrakel.codemucker.ast.JAstParser;
import com.bertvanbrakel.codemucker.ast.JFindVisitor;
import com.bertvanbrakel.codemucker.ast.JSourceFile;
import com.bertvanbrakel.lang.IsBuilder;
import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.Root;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

/**
 * Utility class to find source files, java types, and methods.
 * 
 * Usage:
 * 
 * 
 */
public class JFinder {

	private static final String JAVA_EXTENSION = "java";
	private static final JFindListener NULL_LISTENER = new JFindListener() {
		@Override
		public void onMatched(Object obj) {
		}
		
		@Override
		public void onIgnored(Object obj) {
		}
	};
	
	private final Collection<Root> roots;

	private final JFindListener listener;
	
	@Inject
	private final ASTParser parser;

	public static interface JFindListener extends FindResult.MatchListener<Object> {
	}
	
	public static Builder newBuilder(){
		return new Builder();
	}

	@Inject
	public JFinder(
			ASTParser parser
			, Iterable<Root> roots
			, JFindListener listener
			) {
		
		checkNotNull(roots, "expect class path roots");
		
		this.parser = checkNotNull(parser, "expect parser");
		this.roots = ImmutableList.<Root>builder().addAll(roots).build();
		this.listener = checkNotNull(listener, "expect find listener");
	}
	
	public void accept(JFindVisitor visitor) {
		Preconditions.checkNotNull(visitor,"visitor");
		
		for(Root root:roots){
			if( visitor.visit(root)){
				visit(visitor, root);
			}
			visitor.endVisit(root);
		}
	}
	
	private void visit(final JFindVisitor visitor,Root root){
		Function<ClassPathResource, Boolean> collector = new Function<ClassPathResource, Boolean>() {
			@Override
            public Boolean apply(ClassPathResource resource) {
				if( visitor.visit(resource)){
					visit(visitor, resource);
				}
				visitor.endVisit(resource);
				return true;
            }
		};
		root.walkResources(collector);	
	}
	
	private void visit(final JFindVisitor visitor,ClassPathResource resource){
		if( resource.hasExtension(JAVA_EXTENSION)){
			//TODO:turn into classname
			String className = resource.getRelPath();
			if( visitor.visitClass(className)){
				//TODO:catch error here and callback onerror?
				visit( visitor, JSourceFile.fromResource(resource, parser));
			}
			visitor.endVisitClass(className);
		}
	}
	
	private void visit(final JFindVisitor visitor,JSourceFile source){
		source.visit(visitor);
	}
	
	public static class Builder {
		private ASTParser parser;
		private List<Root> roots = newArrayList();
		private JFindListener listener;
		
		public JFinder build(){			
			return new JFinder(
				toParser()
				, roots
				, listener==null?NULL_LISTENER:listener
			);
		}
		
		private ASTParser toParser(){
			return parser != null ? parser : JAstParser.newDefaultParser();
		}

		public Builder setSearchRoots(SearchRoots.Builder searchRoots) {
        	setSearchRoots(searchRoots.build());
        	return this;
        }
		
	 	public Builder setSearchRoots(IsBuilder<? extends Iterable<Root>> rootsBuilder) {
        	setSearchRoots(rootsBuilder.build());
        	return this;
        }
	 	
	 	public Builder setSearchRoots(Iterable<Root> roots) {
        	this.roots = nullSafeList(roots);
        	return this;
        }
	 	
	 	private static <T> List<T> nullSafeList(Iterable<T> iter){
	 		if( iter == null){
	 			return newArrayList();
	 		}
	 		return newArrayList(iter);
	 	}

	 	public Builder setListener(JFindListener listener) {
        	this.listener = listener;
        	return this;
		}

		public Builder setParser(ASTParser parser) {
        	this.parser = parser;
        	return this;
        }
	}
}
