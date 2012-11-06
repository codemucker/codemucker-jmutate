package com.bertvanbrakel.codemucker.transform;

import static com.google.common.base.Preconditions.checkState;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.bertvanbrakel.codemucker.ast.CodemuckerException;
import com.bertvanbrakel.codemucker.ast.ContextNames;
import com.bertvanbrakel.codemucker.ast.JMethod;
import com.bertvanbrakel.codemucker.ast.finder.FindResult;
import com.bertvanbrakel.codemucker.ast.matcher.AMethod;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public final class InsertCtorTransform extends AbstractNodeInsertTransform<InsertCtorTransform> {
	
	private JMethod ctor;

	public static InsertCtorTransform newTransform(){
		return new InsertCtorTransform();
	}
	
	@Override
	public void transform(){
		checkFieldsSet();
		checkState(ctor != null, "missing constructor");
		
		boolean insert = true;
		if( !ctor.isConstructor()){
			throw new CodemuckerException("Constructor method is not a constructor. Method is %s",ctor);
			
		}
		if( !getTarget().getSimpleName().equals(ctor.getName())){
			throw new CodemuckerException("Constructor method name should be the same as the target type. Expected name to be %s but was %s",getTarget().getSimpleName(),ctor.getName());
		}
		
		FindResult<JMethod> found = getTarget().findMethodsMatching(AMethod.withNameAndArgSignature(ctor));
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
                .setTargetToInsertInto(getTarget())
                .setNodeToInsert(ctor.getAstNode())
                .setStrategy(getPlacementStrategy())
                .insert();
    	}
    }

	@Inject
    public void injectPlacementStrategy(@Named(ContextNames.CTOR) PlacementStrategy strategy) {
	    setPlacementStrategy(strategy);
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
