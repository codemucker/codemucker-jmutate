package com.bertvanbrakel.codemucker.ast;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.bertvanbrakel.codemucker.util.TypeUtil;
import com.google.common.base.Function;

public class JField implements JAnnotatable, AstNodeProvider<FieldDeclaration> {

	public static final Function<FieldDeclaration, JField> TRANSFORMER = new Function<FieldDeclaration, JField>() {
		public JField apply(FieldDeclaration node){
			return JField.from(node);
		}
	};
	
	private final FieldDeclaration fieldNode;

	public static JField from(FieldDeclaration node){
		return new JField(node);
	}
	
	private JField(final FieldDeclaration fieldNode) {
		checkNotNull(fieldNode, "expect field declaration");
		this.fieldNode = fieldNode;
	}

	@Override
	public FieldDeclaration getAstNode(){
		return fieldNode;
	}

	public boolean hasName(final String name){
		return getNames().contains(name);
	}

	public List<SingleJField> asSingleFields(){
		final List<SingleJField> singles = newArrayList();
		final BaseASTVisitor visitor = new BaseASTVisitor(){
			@Override
            public boolean visit(final VariableDeclarationFragment node) {
				singles.add(new SingleJField(JField.this,node));
				return false;
            }
		};
		fieldNode.accept(visitor);
		return singles;
	}

	public static class SingleJField {
		private final JField parent;
		private final VariableDeclarationFragment frag;

		public SingleJField(final JField parent, final VariableDeclarationFragment frag) {
	        super();
	        this.parent = parent;
	        this.frag = frag;
        }

		public String getName() {
			return frag.getName().getIdentifier();
		}

		public Expression getInitilizer() {
			return frag.getInitializer();
		}

		public Type getType() {
			return parent.getType();
		}
	}


	public JAccess getAccess(){
		return getJavaModifiers().asAccess();
	}

	public boolean isType(final JField field){
		return isType(field.getAstNode().getType());
	}

	public boolean isType(final Type type){
		return fieldNode.getType().equals(type) ;
	}

	public Type getType(){
		return fieldNode.getType();
	}

	/**
	 * Returns the full type signature
	 * @return
	 */
	public String getTypeSignature(){
		return TypeUtil.toTypeSignature(fieldNode.getType());
	}

	/**
	 * Get the name of this field. If there are multiple names throw an error
	 */
	public String getName() {
		final List<String> names = getNames();
		checkState(names.size() == 1, "expect only a single name");
		return names.get(0);
	}

	public boolean isMultiNamed(){
		return getNames().size() > 1;
	}

	public List<String> getNames(){
		final List<String> names = newArrayList();
		final BaseASTVisitor visitor = new BaseASTVisitor(){
			@Override
            public boolean visit(final VariableDeclarationFragment node) {
				names.add(node.getName().getIdentifier());
				return false;
            }
		};
		fieldNode.accept(visitor);
		return names;
	}

	public boolean isAccess(final JAccess access) {
		return getJavaModifiers().asAccess().equals(access);
	}

	@SuppressWarnings("unchecked")
    public JModifiers getJavaModifiers(){
		return new JModifiers(fieldNode.getAST(),fieldNode.modifiers());
	}

	@SuppressWarnings("unchecked")
	@Override
    public <A extends Annotation> boolean hasAnnotationOfType(final Class<A> annotationClass) {
		return JAnnotation.hasAnnotation(annotationClass, fieldNode.modifiers());
	}

	@Override
	public <A extends Annotation> JAnnotation getAnnotationOfType(final Class<A> annotationClass) {
		return JAnnotation.getAnnotationOfType(fieldNode, JAnnotation.ANY_DEPTH, annotationClass);
	}

	@Override
	public Collection<org.eclipse.jdt.core.dom.Annotation> getAnnotations(){
		return JAnnotation.findAnnotations(fieldNode);
	}
}
