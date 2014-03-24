package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkState;

import org.codemucker.jmutate.ast.MutateException;
import org.codemucker.jmutate.ast.ContextNames;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.finder.FindResult;
import org.codemucker.jmutate.ast.matcher.AJMethod;
import org.eclipse.jdt.core.dom.MethodDeclaration;

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
			throw new MutateException("Constructor method is not a constructor. Method is %s",ctor);
			
		}
		if( !getTarget().getSimpleName().equals(ctor.getName())){
			throw new MutateException("Constructor method name should be the same as the target type. Expected name to be %s but was %s",getTarget().getSimpleName(),ctor.getName());
		}
		
		FindResult<JMethod> found = getTarget().findMethodsMatching(AJMethod.withNameAndArgSignature(ctor));
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
				throw new MutateException("Existing ctor %s, not replacing with %s", existingCtor.getAstNode(), ctor);
			default:
				throw new MutateException("Existing ctor method %s, unsupported clash strategy %s", existingCtor.getAstNode(), getClashStrategy());
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

	/**
	 * Used by the DI container to set the default
	 */
	@Inject
    public void injectPlacementStrategy(@Named(ContextNames.CTOR) PlacementStrategy strategy) {
	    setPlacementStrategy(strategy);
    }

	/**
	 * The constructor to transform
	 * 
	 * @param constructor
	 * @return
	 */
	public InsertCtorTransform setCtor(MethodDeclaration constructor) {
    	setCtor(JMethod.from(constructor));
    	return this;
	}
	
	/**
	 * The constructor to transform
	 * 
	 * @param constructor
	 * @return
	 */
	public InsertCtorTransform setCtor(JMethod constructor) {
    	this.ctor = constructor;
    	return this;
	}
}
