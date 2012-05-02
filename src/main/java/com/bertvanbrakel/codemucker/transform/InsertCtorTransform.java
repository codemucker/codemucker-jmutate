package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkState;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JMethodMatchers;

public final class InsertCtorTransform extends AbstractNodeInsertTransform<InsertCtorTransform> {
	
	private JMethod ctor;

	public static InsertCtorTransform newTransform(){
		return new InsertCtorTransform();
	}
	
	public void apply(){
		checkFieldsSet();
		checkState(ctor != null, "missing constructor");
		
		boolean insert = true;
		if( !ctor.isConstructor()){
			throw new CodemuckerException("Constructor method is not a constructor. Method is %s",ctor);
			
		}
		if( !getTarget().getSimpleName().equals(ctor.getName())){
			throw new CodemuckerException("Constructor method name should be the same as the target type. Expected name to be %s but as %s",getTarget().getSimpleName(),ctor.getName());
		}
		
		FindResult<JMethod> found = getTarget().findMethodsMatching(JMethodMatchers.withNameAndArgSignature(ctor));
    	if( !found.isEmpty()){
    		insert = false;
    		JMethod existingCtor = found.getFirst();
    		//modify it!??
    		switch(getClashStrategy()){
			case REPLACE:
				existingCtor.getAstNode().delete();
				insert = true;
				break;
			case IGNORE:
				break;
			case ERROR:
				throw new CodemuckerException("Existing ctor %s, not replacing with %s", existingCtor.getAstNode(), ctor);
			default:
				throw new CodemuckerException("Existing ctor method %s, unsupported clash strategy %s", existingCtor.getAstNode(), getClashStrategy());
			}
    	}
    	if(insert){
    		new NodeInserter()
                .setTarget(getTarget())
                .setNodeToInsert(ctor.getAstNode())
                .setStrategy(getPlacementStrategy())
                .insert();
    	}
	}

	public InsertCtorTransform setCtor(MethodDeclaration constructor) {
    	setCtor(new JMethod(constructor));
    	return this;
	}
	
	public InsertCtorTransform setCtor(JMethod constructor) {
    	this.ctor = constructor;
    	return this;
	}
}
