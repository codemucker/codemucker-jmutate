package org.codemucker.jmutate.ast;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.codemucker.jmutate.IProvideCompilationUnit;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.util.NameUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.google.common.base.Function;

public class JField implements AnnotationsProvider, AstNodeProvider<FieldDeclaration> , IProvideCompilationUnit {

	public static final Function<FieldDeclaration, JField> TRANSFORMER = new Function<FieldDeclaration, JField>() {
		@Override
        public JField apply(FieldDeclaration node){
			return JField.from(node);
		}
	};
	
	private final FieldDeclaration fieldNode;

	private final AbstractAnnotations annotable = new AbstractAnnotations(){
        @Override
        public ASTNode getAstNode() {
            return fieldNode;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected List<IExtendedModifier> getModifiers() {
            return fieldNode.modifiers();
        } 
	};
	
    public static JField from(ASTNode node) {
        if (node instanceof FieldDeclaration) {
            return from((FieldDeclaration) node);
        }
        throw new IllegalArgumentException(String.format("Expect a {0} but was {1}", FieldDeclaration.class.getName(), node.getClass().getName()));
    }
	   
	public static JField from(FieldDeclaration node){
		return new JField(node);
	}
	
	public static boolean is(ASTNode node) {
        return node instanceof FieldDeclaration;
    }
    
	private JField(final FieldDeclaration fieldNode) {
		checkNotNull(fieldNode, "expect field declaration");
		this.fieldNode = fieldNode;
	}

	public AbstractTypeDeclaration getEnclosingType(){
		ASTNode node = getAstNode();
		while( node != null ){
			if(node instanceof AbstractTypeDeclaration){
				return (AbstractTypeDeclaration)node;
			}
			node = node.getParent();
		}
		throw new JMutateException("Couldn't find parent type. Unexpected");
	}
	
	@Override
	public FieldDeclaration getAstNode(){
		return fieldNode;
	}

	public boolean hasName(final String name){
		return getNames().contains(name);
	}
	
	public boolean isStatic(){
		return isStatic(true);
	}

	public boolean isStatic(boolean b){
		return getModifiers().isStatic(b);
	}
	
	public boolean isFinal(){
		return isFinal(true);
	}

	public boolean isFinal(boolean b){
		return getModifiers().isFinal(b);
	}
	
	public boolean isTransient(boolean b){
		return getModifiers().isTransient(b);
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

	public JAccess getAccess(){
		return getModifiers().asAccess();
	}

	public boolean isType(final JField field){
		return isType(field.getAstNode().getType());
	}

	public boolean isType(final Type type){
		return fieldNode.getType().equals(type) ;
	}

	public String getFullTypeName(){
        return NameUtil.resolveQualifiedNameElseShort(getType());
    }
    
	public Type getType(){
		return fieldNode.getType();
	}

	/**
	 * Returns the full type signature
	 * @return
	 */
	public String getTypeSignature(){
		return NameUtil.resolveQualifiedName(fieldNode.getType());
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
		return getModifiers().asAccess().equals(access);
	}

	@SuppressWarnings("unchecked")
    public JModifier getModifiers(){
		return new JModifier(fieldNode.getAST(),fieldNode.modifiers());
	}
	
	@Override
	public JCompilationUnit getCompilationUnit(){
		return JCompilationUnit.findCompilationUnit(getAstNode());
	}

	@Override
	public Annotations getAnnotations(){
        return annotable;
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
 
}
