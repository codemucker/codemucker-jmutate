package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkState;

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

public final class InsertMethodTransform extends AbstractNodeInsertTransform<MethodDeclaration,InsertMethodTransform>{

	private JMethod method;

	public static InsertMethodTransform newTransform(){
		return new InsertMethodTransform();
	}
	
	@Override
	public void transform() {
		checkFieldsSet();
		checkState(method != null, "missing method");
		
	    FindResult<JMethod> foundWithSameSig = getTarget().findMethodsMatching(AJMethod.with().nameAndArgSignature(method));
		if(!foundWithSameSig.isEmpty()){
			JMethod existingMethod = foundWithSameSig.getFirst();
			switch(getClashStrategyResolver().resolveClash(existingMethod.getAstNode(), method.getAstNode())){
			case REPLACE:
			    //put in same location as existing
				insertAfter(method.getAstNode(), existingMethod.getAstNode());
				existingMethod.getAstNode().delete();
				return;
			case SKIP:
				return;
			case ERROR:
				throw new JMutateException("Existing method:\n %s\n, not replacing with %s", existingMethod.getAstNode(), method);
			default:
				throw new JMutateException("Existing method:\n %s\n, unsupported clash strategy %s", existingMethod.getAstNode(), getClashStrategyResolver());
			}
		}
		//find methods with same name and insert afterwards. Handles existing ctors as well as methods
		FindResult<JMethod> foundWithName = getTarget().findMethodsMatching(AJMethod.with().name(method.getName()));
		if(!foundWithName.isEmpty()){
			insertAfter(method.getAstNode(), foundWithName.getFirst().getAstNode());
		} else {
			insertAfter(method.getAstNode(), null);
		}
	}

	private void insertAfter(MethodDeclaration newMethod, ASTNode afterNode){
		PlacementStrategy placement = new PlacementStrategySameLocation(getPlacementStrategy(),afterNode);
		
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
    public void injectPlacementStrategy(PlacementStrategy strategy) {
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
}
