package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkState;

import org.codemucker.jfind.FindResult;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.PlacementStrategy;
import org.codemucker.jmutate.ast.ContextNames;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.matcher.AJMethod;
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
		
	    //TODO:detect if it exists?
		boolean insert = true;
		FindResult<JMethod> found = getTarget().findMethodsMatching(AJMethod.with().nameAndArgSignature(method));
		if(!found.isEmpty()){
			insert = false;
			JMethod existingMethod = found.getFirst();
			switch(getClashStrategy()){
			case REPLACE:
			    //todo:remember where this method is
				existingMethod.getAstNode().delete();
				insert = true;
				break;
			case IGNORE:
				break;
			case ERROR:
				throw new JMutateException("Existing method:\n %s\n, not replacing with %s", existingMethod.getAstNode(), method);
			default:
				throw new JMutateException("Existing method:\n %s\n, unsupported clash strategy %s", existingMethod.getAstNode(), getClashStrategy());
			}
		}
		if(insert){
			new NodeInserter()
				.nodeToInsert(method.getAstNode())
				.target(getTarget())
				.placementStrategy(getPlacementStrategy())
				.insert();
		}
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
}
