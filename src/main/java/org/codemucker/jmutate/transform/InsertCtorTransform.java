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

public final class InsertCtorTransform extends AbstractNodeInsertTransform<InsertCtorTransform> {
	
	private JMethod contructorToAdd;

	public static InsertCtorTransform newTransform(){
		return new InsertCtorTransform();
	}
	
	@Override
	public void transform(){
		checkFieldsSet();
		checkState(contructorToAdd != null, "missing constructor to add");
		
		boolean insert = true;
		if( !contructorToAdd.isConstructor()){
			throw new JMutateException("Constructor method is not a constructor. Method is %s",contructorToAdd);
			
		}
		if( !getTarget().getSimpleName().equals(contructorToAdd.getName())){
			throw new JMutateException("Constructor method name should be the same as the target type. Expected name to be %s but was %s",getTarget().getSimpleName(),contructorToAdd.getName());
		}
		
		FindResult<JMethod> found = getTarget().findMethodsMatching(AJMethod.with().nameAndArgSignature(contructorToAdd));
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
				throw new JMutateException("Existing ctor %s, not replacing with %s", existingCtor.getAstNode(), contructorToAdd);
			default:
				throw new JMutateException("Existing ctor method %s, unsupported clash strategy %s", existingCtor.getAstNode(), getClashStrategy());
			}
    	}
    	if(insert){
    		new NodeInserter()
                .target(getTarget())
                .nodeToInsert(contructorToAdd.getAstNode())
                .placementStrategy(getPlacementStrategy())
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
    	this.contructorToAdd = constructor;
    	return this;
	}
}
