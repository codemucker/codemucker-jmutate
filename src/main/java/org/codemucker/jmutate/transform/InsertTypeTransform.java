package org.codemucker.jmutate.transform;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.PlacementStrategy;
import org.codemucker.jmutate.ast.ContextNames;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class InsertTypeTransform extends AbstractNodeInsertTransform<ASTNode,InsertTypeTransform>{
	
	private JType type;
	
	public static InsertTypeTransform newTransform(){
		return new InsertTypeTransform();
	}
	
	@Override
	public void transform() {
		checkFieldsSet();
		checkState(type != null, "missing type");
		
		JType target = getTarget();
		
		List<JType> found = target.findNestedTypesMatching(AJType.with().fullName(type.getSimpleName())).toList();
		if( !found.isEmpty()){
			JType existingType = found.get(0);
			switch(getClashStrategyResolver().resolveClash(existingType.getAstNode(),type.getAstNode())){
			case REPLACE:
				insert(type.getAstNode(),existingType.getAstNode());
				existingType.getAstNode().delete();
				return;
			case SKIP:
				return;
			case ERROR:
				throw new JMutateException("Existing type %s, not replacing with %s", existingType.getAstNode(), type);
			default:
				throw new JMutateException("Existing type %s, unsupported clash strategy %s", existingType.getAstNode(), getClashStrategyResolver());
			}
		}
		insert(type.getAstNode(),null);
	}
	
	private void insert(ASTNode type, ASTNode beforeNode){
		PlacementStrategy placement = new PlacementStrategySameLocation(getPlacementStrategy(),beforeNode);
		
		new NodeInserter()
			.nodeToInsert(type)
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
	 * The type to transform
	 * @param type
	 * @return
	 */
	public InsertTypeTransform setType(AbstractTypeDeclaration type) {
    	setType(JType.from(type));
    	return this;
	}
	
	/**
	 * The type to transform
	 * @param type
	 * @return
	 */
	public InsertTypeTransform setType(JType type) {
    	this.type = type;
    	return this;
	}

	
}
