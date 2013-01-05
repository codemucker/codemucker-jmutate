package com.bertvanbrakel.codemucker.ast.finder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;

import com.bertvanbrakel.codemucker.ast.JAstParser;
import com.bertvanbrakel.lang.IsBuilder;
import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.Root;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

public class JSearchScope {

	private final Collection<Root> roots;

	@Inject
	private final ASTParser parser;

	public static Builder newBuilder(){
		return new Builder();
	}

	@Inject
	public JSearchScope(
			ASTParser parser
			, Iterable<Root> roots
			) {
		
		checkNotNull(roots, "expect class path roots");
		
		this.parser = checkNotNull(parser, "expect parser");
		this.roots = ImmutableList.<Root>builder().addAll(roots).build();
	}
	
	public ASTParser getParser(){
		return parser;
	}
	
	
	public void accept(JSearchScopeVisitor visitor) {
		Preconditions.checkNotNull(visitor,"visitor");
		for(Root root:roots){
			if( visitor.visit(root)){
				visit(visitor, root);
			}
			visitor.endVisit(root);
		}
	}
	
	private void visit(final JSearchScopeVisitor visitor,Root root){
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
	
	private void visit(final JSearchScopeVisitor visitor,ClassPathResource resource){
		visitor.visit(resource);
		visitor.endVisit(resource);
	}

	public static class Builder {
		private ASTParser parser;
		private List<Root> roots = newArrayList();
		
		public JSearchScope build(){			
			return new JSearchScope(
				toParser()
				, roots
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

		public Builder setParser(ASTParser parser) {
        	this.parser = parser;
        	return this;
        }
	}
}
