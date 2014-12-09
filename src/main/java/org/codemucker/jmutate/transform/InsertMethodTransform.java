package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.codemucker.jfind.FindResult;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.PlacementStrategy;
import org.codemucker.jmutate.ast.ContextNames;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.matcher.AJMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public final class InsertMethodTransform extends AbstractNodeInsertTransform<InsertMethodTransform>{

	private JMethod method;

	public static InsertMethodTransform newTransform(){
		return new InsertMethodTransform();
	}
	
	@Override
	public void transform() {
		checkFieldsSet();
		checkState(method != null, "missing method");
		
	    FindResult<JMethod> found = getTarget().findMethodsMatching(AJMethod.with().nameAndArgSignature(method));
		if(!found.isEmpty()){
			JMethod existingMethod = found.getFirst();
			switch(getClashStrategy()){
			case REPLACE:
			    //put in same location as existing
				insert(method.getAstNode(), existingMethod.getAstNode());
				existingMethod.getAstNode().delete();
				return;
			case SKIP:
				return;
			case ERROR:
				throw new JMutateException("Existing method:\n %s\n, not replacing with %s", existingMethod.getAstNode(), method);
			default:
				throw new JMutateException("Existing method:\n %s\n, unsupported clash strategy %s", existingMethod.getAstNode(), getClashStrategy());
			}
		}
		
		insert(method.getAstNode(), null);
	}

	private void insert(MethodDeclaration m, ASTNode beforeNode){

		PlacementStrategy placement = new InsertSameLocationPlacementStrategy(getPlacementStrategy(),beforeNode);
		
		new NodeInserter()
			.nodeToInsert(method.getAstNode())
			.target(getTarget())
			.placementStrategy(placement)
			.insert();
		
	}

	/**
	 * Used by the DI container to set the default
	 */
	@Inject
    public void injectPlacementStrategy(@Named(ContextNames.METHOD) PlacementStrategy strategy) {
	    placementStrategy(strategy);
    }

	
	/**
	 * The method to transform
	 * @param method
	 * @return
	 */
	public InsertMethodTransform method(MethodDeclaration method) {
    	method(JMethod.from(method));
    	return this;
	}
	
	/**
	 * The method to transform
	 * @param newMethod
	 * @return
	 */
	public InsertMethodTransform method(JMethod newMethod) {
    	this.method = newMethod;
    	return this;
	}
	
	private static class InsertSameLocationPlacementStrategy implements PlacementStrategy {

		private final PlacementStrategy fallbackStrategy;
		private final ASTNode afterNode;
		
		public InsertSameLocationPlacementStrategy(PlacementStrategy fallbackStrategy,
				ASTNode afterNode) {
			super();
			this.fallbackStrategy = fallbackStrategy;
			this.afterNode = afterNode;
		}

		@Override
		public int findIndexToPlaceInto(ASTNode nodeToInsert,
				List<ASTNode> nodes) {
			if(afterNode != null){
				for(int i = 0; i < nodes.size();i++){
					if( nodes.get(i)== afterNode){
						return i+1;
					}
				}
			}
			return fallbackStrategy.findIndexToPlaceInto(nodeToInsert, nodes);
		}
	}
}
