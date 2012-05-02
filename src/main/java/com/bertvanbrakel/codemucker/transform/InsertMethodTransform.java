package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkState;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JMethodMatchers;

public final class InsertMethodTransform extends AbstractNodeInsertTransform<InsertMethodTransform>{

	private JMethod method;

	public static InsertMethodTransform newTransform(){
		return new InsertMethodTransform();
	}
	
	public void apply() {
		checkFieldsSet();
		checkState(method != null, "missing method");
		
	    //TODO:detect if it exists?
		boolean insert = true;
		FindResult<JMethod> found = getTarget().findMethodsMatching(JMethodMatchers.withNameAndArgSignature(method));
		if(!found.isEmpty()){
			insert = false;
			JMethod existingMethod = found.getFirst();
			switch(getClashStrategy()){
			case REPLACE:
				existingMethod.getAstNode().delete();
				insert = true;
				break;
			case IGNORE:
				break;
			case ERROR:
				throw new CodemuckerException("Existing method %s, not replacing with %s", existingMethod.getAstNode(), method);
			default:
				throw new CodemuckerException("Existing method %s, unsupported clash strategy %s", existingMethod.getAstNode(), getClashStrategy());
			}
		}
		if(insert){
			new NodeInserter()
				.setNodeToInsert(method.getAstNode())
				.setTarget(getTarget())
				.setStrategy(getPlacementStrategy())
				.insert();
		
		}
    }

	public InsertMethodTransform setMethod(MethodDeclaration method) {
    	setMethod(new JMethod(method));
    	return this;
	}
	
	public InsertMethodTransform setMethod(JMethod newMethod) {
    	this.method = newMethod;
    	return this;
	}
}
