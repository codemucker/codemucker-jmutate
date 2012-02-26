package com.bertvanbrakel.codemucker.ast;

import java.util.List;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.bertvanbrakel.codemucker.ast.finder.matcher.JFieldMatchers;
import com.bertvanbrakel.codemucker.ast.finder.matcher.JMethodMatchers;

public class Mutations {
	
	public static AbstractMutation2<AbstractTypeDeclaration> fieldChange(JContext context, AbstractTypeDeclaration type, final String fieldSnippet){
		return new AbstractMutation2<AbstractTypeDeclaration>(context,type){
			@Override
			public void apply(final AbstractTypeDeclaration typeNode) {
				FieldDeclaration fieldNode = parseField(getSnippet());
				
				JType javaType = new JType(typeNode);
				JField newField = new JField(javaType, fieldNode);
				
				//TODO:detect if it exists?
				boolean hasFound = false;
				for( String fieldName:newField.getNames()){
					List<JField> found = javaType.findFieldsMatching(JFieldMatchers.withName(fieldName)).asList();
					if( !found.isEmpty()){
						hasFound = true;
						
						JField existingField = found.get(0);
						if( isReplace()){
	    					if( existingField.isType(newField)){
			    				 
							}
	    					existingField.getFieldNode().delete();			
	    					addToBodyUsingStrategy(javaType, newField.getFieldNode(), getInsertionStrategy());
						} else if( isErrorOnExisting()) {
	    					//throw?
	    					throw new CodemuckerException("Existing field %s, not replacing with %s", existingField.getFieldNode(), getSnippet());
	    				} 
					}
				}
				if( !hasFound){
					addToBodyUsingStrategy(javaType, newField.getFieldNode(), getInsertionStrategy());
				}
			}
		}
		.snippet(fieldSnippet)
		.replace()
		.strategyFeild();
	}
	
	public static AbstractMutation2<AbstractTypeDeclaration> constructorChange(JContext context, AbstractTypeDeclaration type,final String ctorSnippet){
		return new AbstractMutation2<AbstractTypeDeclaration>(context,type){
			@Override
			public void apply(final AbstractTypeDeclaration typeNode) {
				MethodDeclaration ctorNode = parseConstructor(getSnippet());
				JType javaType = new JType(typeNode);
				JMethod newCtor = new JMethod(javaType, ctorNode);
				
				//TODO:detect if it exists?
				boolean hasFound = false;
				List<JMethod> found= javaType.findMethodsMatching(JMethodMatchers.withName(newCtor.getName())).asList();
				if( !found.isEmpty()){
					hasFound = true;
					JMethod existingCtor = found.get(0);
    				//modify it!
    				if( isReplace()){
    					
    				} else if( isErrorOnExisting()) {
    					//throw?
    					throw new CodemuckerException("Existing constructor %s, not replacing with %s", existingCtor.getMethodNode(), getSnippet());
    				}	
			
				}
				if( !hasFound){
					addToBodyUsingStrategy(javaType, newCtor.getMethodNode(), getInsertionStrategy());
				}
			}
		}
		.snippet(ctorSnippet)
		.replace()
		.strategyCtor();	
	}
	
	public static AbstractMutation2<AbstractTypeDeclaration> methodChange(JContext context, AbstractTypeDeclaration type, final String methodSnippet){
		return new AbstractMutation2<AbstractTypeDeclaration>(context,type){
			@Override
			public void apply(final AbstractTypeDeclaration typeNode) {
				
				MethodDeclaration methodNode = parseMethod(getSnippet());
				
				JType javaType = new JType(typeNode);
				JMethod newMethod = new JMethod(javaType, methodNode);
				
				//TODO:detect if it exists?
				boolean hasFound = false;
				List<JMethod> found= javaType.findMethodsMatching(JMethodMatchers.withName(newMethod.getName())).asList();
				if( !found.isEmpty()){
					hasFound = true;
					JMethod existingMethod = found.get(0);
    				//modify it!
    				if( isReplace()){
    					
    				} else if( isErrorOnExisting()) {
    					//throw?
    					throw new CodemuckerException("Existing method %s, not replacing with %s", existingMethod.getMethodNode(), getSnippet());
    				}	
			
				}
				if( !hasFound){
					addToBodyUsingStrategy(javaType, newMethod.getMethodNode(), getInsertionStrategy());
				}
			}
		}
		.snippet(methodSnippet)
		.replace()
		.strategyMethod();
	}

}
