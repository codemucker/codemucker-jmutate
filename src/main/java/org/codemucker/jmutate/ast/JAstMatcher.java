package org.codemucker.jmutate.ast;

import java.util.Iterator;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;
import org.junit.Assert;

/**
 * A Matcher which throws {@link AssertionFailedError} when nodes don't match. This is so useful error messages are generated
 * on non matching Ast's. Copied and modified from IBM's ASTMatcher. 
 */
public class JAstMatcher extends ASTMatcher {

	/**
	 * Indicates whether doc tags should be matched.
	 * @since 3.0
	 */
	private boolean matchDocTags;

	public static Builder builder(){
		return new Builder();
	}

	/**
	 * Creates a new AST matcher instance.
	 *
	 * @param matchDocTags <code>true</code> if doc comment tags are
	 * to be compared by default, and <code>false</code> otherwise
	 * @see #match(Javadoc,Object)
	 * @since 3.0
	 * @See {@link #builder()}
	 */
	private JAstMatcher(boolean matchDocTags) {
		this.matchDocTags = matchDocTags;
	}

	/**
	 * Returns whether the given objects are equal according to
	 * <code>equals</code>. Returns <code>false</code> if either
	 * node is <code>null</code>.
	 *
	 * @param o1 the first object, or <code>null</code>
	 * @param o2 the second object, or <code>null</code>
	 * @return <code>true</code> if the nodes are equal according to
	 *    <code>equals</code> or both <code>null</code>, and
	 *    <code>false</code> otherwise
	 */
	public static boolean safeEquals(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		return o1.equals(o2);
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.1
	 */
	public boolean match(AnnotationTypeDeclaration node, Object other) {
		AnnotationTypeDeclaration o = safeCast(AnnotationTypeDeclaration.class, other);
		// node type added in JLS3 - ignore old JLS2-style modifiers
		return (assertSubtreeMatch(node.getJavadoc(), o.getJavadoc())
				&& assertSubtreeListMatch(node.modifiers(), o.modifiers())
				&& assertSubtreeMatch(node.getName(), o.getName())
				&& assertSubtreeListMatch(node.bodyDeclarations(), o.bodyDeclarations()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.1
	 */
	public boolean match(AnnotationTypeMemberDeclaration node, Object other) {
		AnnotationTypeMemberDeclaration o = safeCast(AnnotationTypeMemberDeclaration.class, other);
		// node type added in JLS3 - ignore old JLS2-style modifiers
		return (assertSubtreeMatch(node.getJavadoc(), o.getJavadoc())
				&& assertSubtreeListMatch(node.modifiers(), o.modifiers())
				&& assertSubtreeMatch(node.getType(), o.getType())
				&& assertSubtreeMatch(node.getName(), o.getName())
				&& assertSubtreeMatch(node.getDefault(), o.getDefault()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(AnonymousClassDeclaration node, Object other) {
		AnonymousClassDeclaration o = safeCast(AnonymousClassDeclaration.class, other);
		return assertSubtreeListMatch(node.bodyDeclarations(), o.bodyDeclarations());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ArrayAccess node, Object other) {
		ArrayAccess o = safeCast(ArrayAccess.class, other);
		return (
			assertSubtreeMatch(node.getArray(), o.getArray())
				&& assertSubtreeMatch(node.getIndex(), o.getIndex()));
	}

	/**
	 * Returns whether the given node and the other object object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ArrayCreation node, Object other) {
		ArrayCreation o = safeCast(ArrayCreation.class, other);
		return (
			assertSubtreeMatch(node.getType(), o.getType())
				&& assertSubtreeListMatch(node.dimensions(), o.dimensions())
				&& assertSubtreeMatch(node.getInitializer(), o.getInitializer()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ArrayInitializer node, Object other) {
		ArrayInitializer o = safeCast(ArrayInitializer.class, other);
		return assertSubtreeListMatch(node.expressions(), o.expressions());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ArrayType node, Object other) {
		ArrayType o = safeCast(ArrayType.class, other);;
		return assertSubtreeMatch(node.getComponentType(), o.getComponentType());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(AssertStatement node, Object other) {
		AssertStatement o = safeCast(AssertStatement.class, other);
		return (
			assertSubtreeMatch(node.getExpression(), o.getExpression())
				&& assertSubtreeMatch(node.getMessage(), o.getMessage()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(Assignment node, Object other) {
		Assignment o = safeCast(Assignment.class, other);
		return (
			node.getOperator().equals(o.getOperator())
				&& assertSubtreeMatch(node.getLeftHandSide(), o.getLeftHandSide())
				&& assertSubtreeMatch(node.getRightHandSide(), o.getRightHandSide()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(Block node, Object other) {
		Block o = safeCast(Block.class, other);
		return assertSubtreeListMatch(node.statements(), o.statements());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type. Subclasses may override
	 * this method as needed.
	 * </p>
	 * <p>Note: {@link LineComment} and {@link BlockComment} nodes are
	 * not considered part of main structure of the AST. This method will
	 * only be called if a client goes out of their way to visit this
	 * kind of node explicitly.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.0
	 */
	public boolean match(BlockComment node, Object other) {
		safeCast(BlockComment.class, other);
		return true;
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(BooleanLiteral node, Object other) {
		BooleanLiteral o = safeCast(BooleanLiteral.class, other);
		return node.booleanValue() == o.booleanValue();
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(BreakStatement node, Object other) {
		BreakStatement o = safeCast(BreakStatement.class, other);
		return assertSubtreeMatch(node.getLabel(), o.getLabel());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(CastExpression node, Object other) {
		CastExpression o = safeCast(CastExpression.class, other);
		return (
			assertSubtreeMatch(node.getType(), o.getType())
				&& assertSubtreeMatch(node.getExpression(), o.getExpression()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(CatchClause node, Object other) {
		CatchClause o = safeCast(CatchClause.class, other);
		return (
			assertSubtreeMatch(node.getException(), o.getException())
				&& assertSubtreeMatch(node.getBody(), o.getBody()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(CharacterLiteral node, Object other) {
		CharacterLiteral o = safeCast(CharacterLiteral.class, other);
		return safeEquals(node.getEscapedValue(), o.getEscapedValue());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
//	public boolean match(ClassInstanceCreation node, Object other) {
//		if (!(other instanceof ClassInstanceCreation)) {
//			return false;
//		}
//		ClassInstanceCreation o = (ClassInstanceCreation) other;
//		int level = node.getAST().apiLevel();
//		if (level == AST.JLS2) {
//			if (!assertSubtreeMatch(node.internalGetName(), o.internalGetName())) {
//				return false;
//			}
//		}
//		if (level >= AST.JLS3) {
//			if (!assertSubtreeListMatch(node.typeArguments(), o.typeArguments())) {
//				return false;
//			}
//			if (!assertSubtreeMatch(node.getType(), o.getType())) {
//				return false;
//			}
//		}
//		return
//			assertSubtreeMatch(node.getExpression(), o.getExpression())
//				&& assertSubtreeListMatch(node.arguments(), o.arguments())
//				&& assertSubtreeMatch(
//					node.getAnonymousClassDeclaration(),
//					o.getAnonymousClassDeclaration());
//	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(CompilationUnit node, Object other) {
		CompilationUnit o = safeCast(CompilationUnit.class, other);
		return (
			assertSubtreeMatch(node.getPackage(), o.getPackage())
				&& assertSubtreeListMatch(node.imports(), o.imports())
				&& assertSubtreeListMatch(node.types(), o.types()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ConditionalExpression node, Object other) {
		ConditionalExpression o = safeCast(ConditionalExpression.class, other);
		return (
			assertSubtreeMatch(node.getExpression(), o.getExpression())
				&& assertSubtreeMatch(node.getThenExpression(), o.getThenExpression())
				&& assertSubtreeMatch(node.getElseExpression(), o.getElseExpression()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ConstructorInvocation node, Object other) {
		ConstructorInvocation o = safeCast(ConstructorInvocation.class, other);
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!assertSubtreeListMatch(node.typeArguments(), o.typeArguments())) {
				return false;
			}
		}
		return assertSubtreeListMatch(node.arguments(), o.arguments());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ContinueStatement node, Object other) {
		ContinueStatement o = safeCast(ContinueStatement.class, other);
		return assertSubtreeMatch(node.getLabel(), o.getLabel());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.7.1
	 */
//	public boolean match(UnionType node, Object other) {
//		if (!(other instanceof UnionType)) {
//			return false;
//		}
//		UnionType o = (UnionType) other;
//		return
//			assertSubtreeListMatch(
//				node.types(),
//				o.types());
//	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(DoStatement node, Object other) {
		DoStatement o = safeCast(DoStatement.class, other);
		return (
			assertSubtreeMatch(node.getExpression(), o.getExpression())
				&& assertSubtreeMatch(node.getBody(), o.getBody()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(EmptyStatement node, Object other) {
		safeCast(EmptyStatement.class, other);
		return true;
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.1
	 */
	public boolean match(EnhancedForStatement node, Object other) {
		EnhancedForStatement o = safeCast(EnhancedForStatement.class, other);
		return (
			assertSubtreeMatch(node.getParameter(), o.getParameter())
				&& assertSubtreeMatch(node.getExpression(), o.getExpression())
				&& assertSubtreeMatch(node.getBody(), o.getBody()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.1
	 */
	public boolean match(EnumConstantDeclaration node, Object other) {
		EnumConstantDeclaration o = safeCast(EnumConstantDeclaration.class, other);
		return (
			assertSubtreeMatch(node.getJavadoc(), o.getJavadoc())
				&& assertSubtreeListMatch(node.modifiers(), o.modifiers())
				&& assertSubtreeMatch(node.getName(), o.getName())
				&& assertSubtreeListMatch(node.arguments(), o.arguments())
				&& assertSubtreeMatch(
					node.getAnonymousClassDeclaration(),
					o.getAnonymousClassDeclaration()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.1
	 */
	public boolean match(EnumDeclaration node, Object other) {
		EnumDeclaration o = safeCast(EnumDeclaration.class, other);
		return (
			assertSubtreeMatch(node.getJavadoc(), o.getJavadoc())
				&& assertSubtreeListMatch(node.modifiers(), o.modifiers())
				&& assertSubtreeMatch(node.getName(), o.getName())
				&& assertSubtreeListMatch(node.superInterfaceTypes(), o.superInterfaceTypes())
				&& assertSubtreeListMatch(node.enumConstants(), o.enumConstants())
				&& assertSubtreeListMatch(
					node.bodyDeclarations(),
					o.bodyDeclarations()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ExpressionStatement node, Object other) {
		ExpressionStatement o = safeCast(ExpressionStatement.class, other);
		return assertSubtreeMatch(node.getExpression(), o.getExpression());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(FieldAccess node, Object other) {
		FieldAccess o = safeCast(FieldAccess.class, other);
		return (
			assertSubtreeMatch(node.getExpression(), o.getExpression())
				&& assertSubtreeMatch(node.getName(), o.getName()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(FieldDeclaration node, Object other) {
		FieldDeclaration o = safeCast(FieldDeclaration.class, other);
		int level = node.getAST().apiLevel();
		if (level == AST.JLS2) {
			if (node.getModifiers() != o.getModifiers()) {
				return false;
			}
		}
		if (level >= AST.JLS3) {
			if (!assertSubtreeListMatch(node.modifiers(), o.modifiers())) {
				return false;
			}
		}
		return
			assertSubtreeMatch(node.getJavadoc(), o.getJavadoc())
			&& assertSubtreeMatch(node.getType(), o.getType())
			&& assertSubtreeListMatch(node.fragments(), o.fragments());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ForStatement node, Object other) {
		ForStatement o = safeCast(ForStatement.class, other);
		return (
			assertSubtreeListMatch(node.initializers(), o.initializers())
				&& assertSubtreeMatch(node.getExpression(), o.getExpression())
				&& assertSubtreeListMatch(node.updaters(), o.updaters())
				&& assertSubtreeMatch(node.getBody(), o.getBody()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(IfStatement node, Object other) {
		IfStatement o = safeCast(IfStatement.class, other);
		return (
			assertSubtreeMatch(node.getExpression(), o.getExpression())
				&& assertSubtreeMatch(node.getThenStatement(), o.getThenStatement())
				&& assertSubtreeMatch(node.getElseStatement(), o.getElseStatement()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ImportDeclaration node, Object other) {
		ImportDeclaration o = safeCast(ImportDeclaration.class, other);
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (node.isStatic() != o.isStatic()) {
				return false;
			}
		}
		return (
			assertSubtreeMatch(node.getName(), o.getName())
				&& node.isOnDemand() == o.isOnDemand());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(InfixExpression node, Object other) {
		InfixExpression o = safeCast(InfixExpression.class, other);
		// be careful not to trigger lazy creation of extended operand lists
		if (node.hasExtendedOperands() && o.hasExtendedOperands()) {
			if (!assertSubtreeListMatch(node.extendedOperands(), o.extendedOperands())) {
				return false;
			}
		}
		if (node.hasExtendedOperands() != o.hasExtendedOperands()) {
			return false;
		}
		return (
			node.getOperator().equals(o.getOperator())
				&& assertSubtreeMatch(node.getLeftOperand(), o.getLeftOperand())
				&& assertSubtreeMatch(node.getRightOperand(), o.getRightOperand()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(InstanceofExpression node, Object other) {
		InstanceofExpression o = safeCast(InstanceofExpression.class, other);
		return (
				assertSubtreeMatch(node.getLeftOperand(), o.getLeftOperand())
				&& assertSubtreeMatch(node.getRightOperand(), o.getRightOperand()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(Initializer node, Object other) {
		Initializer o = safeCast(Initializer.class, other);
		int level = node.getAST().apiLevel();
		if (level == AST.JLS2) {
			if (node.getModifiers() != o.getModifiers()) {
				return false;
			}
		}
		if (level >= AST.JLS3) {
			if (!assertSubtreeListMatch(node.modifiers(), o.modifiers())) {
				return false;
			}
		}
		return (
				assertSubtreeMatch(node.getJavadoc(), o.getJavadoc())
				&& assertSubtreeMatch(node.getBody(), o.getBody()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * Unlike other node types, the behavior of the default
	 * implementation is controlled by a constructor-supplied
	 * parameter  {@link #ASTMatcher(boolean) ASTMatcher(boolean)}
	 * which is <code>false</code> if not specified.
	 * When this parameter is <code>true</code>, the implementation
	 * tests whether the other object is also a <code>Javadoc</code>
	 * with structurally isomorphic child subtrees; the comment string
	 * (<code>Javadoc.getComment()</code>) is ignored.
	 * Conversely, when the parameter is <code>false</code>, the
	 * implementation tests whether the other object is also a
	 * <code>Javadoc</code> with exactly the same comment string;
	 * the tag elements ({@link Javadoc#tags() Javadoc.tags} are
	 * ignored. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @see #ASTMatcher()
	 * @see #ASTMatcher(boolean)
	 */
	public boolean match(Javadoc node, Object other) {
		Javadoc o = safeCast(Javadoc.class, other);
		if (this.matchDocTags) {
			return assertSubtreeListMatch(node.tags(), o.tags());
		} else {
			return compareDeprecatedComment(node, o);
		}
	}

	/**
	 * Return whether the deprecated comment strings of the given java doc are equals.
	 * <p>
	 * Note the only purpose of this method is to hide deprecated warnings.
	 * @deprecated mark deprecated to hide deprecated usage
	 */
	private boolean compareDeprecatedComment(Javadoc first, Javadoc second) {
		if (first.getAST().apiLevel() == AST.JLS2) {
			return safeEquals(first.getComment(), second.getComment());
		} else {
			return true;
		}
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(LabeledStatement node, Object other) {
		LabeledStatement o = safeCast(LabeledStatement.class, other);
		return (
			assertSubtreeMatch(node.getLabel(), o.getLabel())
				&& assertSubtreeMatch(node.getBody(), o.getBody()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type. Subclasses may override
	 * this method as needed.
	 * </p>
	 * <p>Note: {@link LineComment} and {@link BlockComment} nodes are
	 * not considered part of main structure of the AST. This method will
	 * only be called if a client goes out of their way to visit this
	 * kind of node explicitly.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.0
	 */
	public boolean match(LineComment node, Object other) {
		safeCast(LineComment.class, other);
		return true;
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.1
	 */
	public boolean match(MarkerAnnotation node, Object other) {
		MarkerAnnotation o = safeCast(MarkerAnnotation.class, other);
		return assertSubtreeMatch(node.getTypeName(), o.getTypeName());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.0
	 */
	public boolean match(MemberRef node, Object other) {
		MemberRef o = safeCast(MemberRef.class, other);
		return (
				assertSubtreeMatch(node.getQualifier(), o.getQualifier())
				&& assertSubtreeMatch(node.getName(), o.getName()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.1
	 */
	public boolean match(MemberValuePair node, Object other) {
		MemberValuePair o = safeCast(MemberValuePair.class, other);
		return (assertSubtreeMatch(node.getName(), o.getName())
				&& assertSubtreeMatch(node.getValue(), o.getValue()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.0
	 */
	public boolean match(MethodRef node, Object other) {
		MethodRef o = safeCast(MethodRef.class, other);
		return (
				assertSubtreeMatch(node.getQualifier(), o.getQualifier())
				&& assertSubtreeMatch(node.getName(), o.getName())
		        && assertSubtreeListMatch(node.parameters(), o.parameters()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.0
	 */
	public boolean match(MethodRefParameter node, Object other) {
		MethodRefParameter o = safeCast(MethodRefParameter.class, other);
		int level = node.getAST().apiLevel();
		if (level >= AST.JLS3) {
			if (node.isVarargs() != o.isVarargs()) {
				return false;
			}
		}
		return (
				assertSubtreeMatch(node.getType(), o.getType())
				&& assertSubtreeMatch(node.getName(), o.getName()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * <p>
	 * Note that extra array dimensions are compared since they are an
	 * important part of the method declaration.
	 * </p>
	 * <p>
	 * Note that the method return types are compared even for constructor
	 * declarations.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(MethodDeclaration node, Object other) {
		MethodDeclaration o = safeCast(MethodDeclaration.class, other);
		
		checkJslLevel(node);
		if (!assertSubtreeListMatch(node.modifiers(), o.modifiers())) {
			return false;
		}
		if (!assertSubtreeMatch(node.getReturnType2(), o.getReturnType2())) {
			return false;
		}
		// n.b. compare type parameters even for constructors
		if (!assertSubtreeListMatch(node.typeParameters(), o.typeParameters())) {
			return false;
		}
	
		return ((node.isConstructor() == o.isConstructor())
				&& assertSubtreeMatch(node.getJavadoc(), o.getJavadoc())
				&& assertSubtreeMatch(node.getName(), o.getName())
				// n.b. compare return type even for constructors
				&& assertSubtreeListMatch(node.parameters(), o.parameters())
	 			&& node.getExtraDimensions() == o.getExtraDimensions()
				&& assertSubtreeListMatch(node.thrownExceptions(), o.thrownExceptions())
				&& assertSubtreeMatch(node.getBody(), o.getBody()));
	}

	private void checkJslLevel(ASTNode node){
		int level = node.getAST().apiLevel();
		if (level == AST.JLS2) {
			throw new UnsupportedOperationException("Don't support JSL2 or lower");
		}
	}
	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(MethodInvocation node, Object other) {
		MethodInvocation o = safeCast(MethodInvocation.class, other);
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!assertSubtreeListMatch(node.typeArguments(), o.typeArguments())) {
				return false;
			}
		}
		return (
			assertSubtreeMatch(node.getExpression(), o.getExpression())
				&& assertSubtreeMatch(node.getName(), o.getName())
				&& assertSubtreeListMatch(node.arguments(), o.arguments()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.1
	 */
	public boolean match(Modifier node, Object other) {
		Modifier o = safeCast(Modifier.class, other);
		return (node.getKeyword() == o.getKeyword());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.1
	 */
	public boolean match(NormalAnnotation node, Object other) {
		NormalAnnotation o = safeCast(NormalAnnotation.class, other);
		return (assertSubtreeMatch(node.getTypeName(), o.getTypeName())
					&& assertSubtreeListMatch(node.values(), o.values()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(NullLiteral node, Object other) {
		safeCast(NullLiteral.class, other);
		return true;
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(NumberLiteral node, Object other) {
		NumberLiteral o = safeCast(NumberLiteral.class, other);
		return safeEquals(node.getToken(), o.getToken());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(PackageDeclaration node, Object other) {
		PackageDeclaration o = safeCast(PackageDeclaration.class, other);
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!assertSubtreeMatch(node.getJavadoc(), o.getJavadoc())) {
				return false;
			}
			if (!assertSubtreeListMatch(node.annotations(), o.annotations())) {
				return false;
			}
		}
		return assertSubtreeMatch(node.getName(), o.getName());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.1
	 */
	public boolean match(ParameterizedType node, Object other) {
		ParameterizedType o = safeCast(ParameterizedType.class, other);
		return assertSubtreeMatch(node.getType(), o.getType())
				&& assertSubtreeListMatch(node.typeArguments(), o.typeArguments());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ParenthesizedExpression node, Object other) {
		ParenthesizedExpression o = safeCast(ParenthesizedExpression.class, other);
		return assertSubtreeMatch(node.getExpression(), o.getExpression());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(PostfixExpression node, Object other) {
		PostfixExpression o = safeCast(PostfixExpression.class, other);
		return (
			node.getOperator().equals(o.getOperator())
				&& assertSubtreeMatch(node.getOperand(), o.getOperand()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(PrefixExpression node, Object other) {
		PrefixExpression o = safeCast(PrefixExpression.class, other);
		return (
			node.getOperator().equals(o.getOperator())
				&& assertSubtreeMatch(node.getOperand(), o.getOperand()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(PrimitiveType node, Object other) {
		PrimitiveType o = safeCast(PrimitiveType.class, other);
		return (node.getPrimitiveTypeCode() == o.getPrimitiveTypeCode());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(QualifiedName node, Object other) {
		QualifiedName o = safeCast(QualifiedName.class, other);
		return (
			assertSubtreeMatch(node.getQualifier(), o.getQualifier())
				&& assertSubtreeMatch(node.getName(), o.getName()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.1
	 */
	public boolean match(QualifiedType node, Object other) {
		QualifiedType o = safeCast(QualifiedType.class, other);
		return (
			assertSubtreeMatch(node.getQualifier(), o.getQualifier())
				&& assertSubtreeMatch(node.getName(), o.getName()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ReturnStatement node, Object other) {
		ReturnStatement o = safeCast(ReturnStatement.class, other);
		return assertSubtreeMatch(node.getExpression(), o.getExpression());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SimpleName node, Object other) {
		SimpleName o = safeCast(SimpleName.class, other);
		return node.getIdentifier().equals(o.getIdentifier());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SimpleType node, Object other) {
		SimpleType o = safeCast(SimpleType.class, other);
		return assertSubtreeMatch(node.getName(), o.getName());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.1
	 */
	public boolean match(SingleMemberAnnotation node, Object other) {
		SingleMemberAnnotation o = safeCast(SingleMemberAnnotation.class, other);
		return (assertSubtreeMatch(node.getTypeName(), o.getTypeName())
				&& assertSubtreeMatch(node.getValue(), o.getValue()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * <p>
	 * Note that extra array dimensions and the variable arity flag
	 * are compared since they are both important parts of the declaration.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SingleVariableDeclaration node, Object other) {
		SingleVariableDeclaration o = safeCast(SingleVariableDeclaration.class, other);
		int level = node.getAST().apiLevel();
		if (level == AST.JLS2) {
			if (node.getModifiers() != o.getModifiers()) {
				return false;
			}
		}
		if (level >= AST.JLS3) {
			if (!assertSubtreeListMatch(node.modifiers(), o.modifiers())) {
				return false;
			}
			if (node.isVarargs() != o.isVarargs()) {
				return false;
			}
		}
		return
		    assertSubtreeMatch(node.getType(), o.getType())
				&& assertSubtreeMatch(node.getName(), o.getName())
	 			&& node.getExtraDimensions() == o.getExtraDimensions()
				&& assertSubtreeMatch(node.getInitializer(), o.getInitializer());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(StringLiteral node, Object other) {
		StringLiteral o = safeCast(StringLiteral.class, other);
		return safeEquals(node.getEscapedValue(), o.getEscapedValue());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SuperConstructorInvocation node, Object other) {
		SuperConstructorInvocation o = safeCast(SuperConstructorInvocation.class, other);
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!assertSubtreeListMatch(node.typeArguments(), o.typeArguments())) {
				return false;
			}
		}
		return (
			assertSubtreeMatch(node.getExpression(), o.getExpression())
				&& assertSubtreeListMatch(node.arguments(), o.arguments()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SuperFieldAccess node, Object other) {
		SuperFieldAccess o = safeCast(SuperFieldAccess.class, other);
		return (
			assertSubtreeMatch(node.getName(), o.getName())
				&& assertSubtreeMatch(node.getQualifier(), o.getQualifier()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SuperMethodInvocation node, Object other) {
		SuperMethodInvocation o = safeCast(SuperMethodInvocation.class, other);
		if (node.getAST().apiLevel() >= AST.JLS3) {
			if (!assertSubtreeListMatch(node.typeArguments(), o.typeArguments())) {
				return false;
			}
		}
		return (
			assertSubtreeMatch(node.getQualifier(), o.getQualifier())
				&& assertSubtreeMatch(node.getName(), o.getName())
				&& assertSubtreeListMatch(node.arguments(), o.arguments()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SwitchCase node, Object other) {
		SwitchCase o = safeCast(SwitchCase.class, other);
		return assertSubtreeMatch(node.getExpression(), o.getExpression());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SwitchStatement node, Object other) {
		SwitchStatement o = safeCast(SwitchStatement.class, other);
		return (
			assertSubtreeMatch(node.getExpression(), o.getExpression())
				&& assertSubtreeListMatch(node.statements(), o.statements()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(SynchronizedStatement node, Object other) {
		SynchronizedStatement o = safeCast(SynchronizedStatement.class, other);
		return (
			assertSubtreeMatch(node.getExpression(), o.getExpression())
				&& assertSubtreeMatch(node.getBody(), o.getBody()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.0
	 */
	public boolean match(TagElement node, Object other) {
		TagElement o = safeCast(TagElement.class, other);
		return (
				safeEquals(node.getTagName(), o.getTagName())
				&& assertSubtreeListMatch(node.fragments(), o.fragments()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.0
	 */
	public boolean match(TextElement node, Object other) {
		TextElement o = safeCast(TextElement.class, other);
		return safeEquals(node.getText(), o.getText());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ThisExpression node, Object other) {
		ThisExpression o = safeCast(ThisExpression.class, other);
		return assertSubtreeMatch(node.getQualifier(), o.getQualifier());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(ThrowStatement node, Object other) {
		ThrowStatement o = safeCast(ThrowStatement.class, other);
		return assertSubtreeMatch(node.getExpression(), o.getExpression());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(TryStatement node, Object other) {
		TryStatement o = safeCast(TryStatement.class, other);
		switch(node.getAST().apiLevel()) {
			case AST.JLS2 :
			case AST.JLS3 :
			case AST.JLS4 :
				return (
						assertSubtreeMatch(node.getBody(), o.getBody())
							&& assertSubtreeListMatch(node.catchClauses(), o.catchClauses())
							&& assertSubtreeMatch(node.getFinally(), o.getFinally()));
		}
		return (
			//TODO:put resource check back in
			//assertSubtreeListMatch(node.resources(), o.resources())
			assertSubtreeMatch(node.getBody(), o.getBody())
			&& assertSubtreeListMatch(node.catchClauses(), o.catchClauses())
			&& assertSubtreeMatch(node.getFinally(), o.getFinally()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(TypeDeclaration node, Object other) {
		TypeDeclaration o = safeCast(TypeDeclaration.class, other);
		checkJslLevel(node);
		if (!assertSubtreeListMatch(node.modifiers(), o.modifiers())) {
			return false;
		}
		if (!assertSubtreeListMatch(node.typeParameters(), o.typeParameters())) {
			return false;
		}
		if (!assertSubtreeMatch(node.getSuperclassType(), o.getSuperclassType())) {
			return false;
		}
		if (!assertSubtreeListMatch(node.superInterfaceTypes(), o.superInterfaceTypes())) {
			return false;
		}
	
		return (
				(node.isInterface() == o.isInterface())
				&& assertSubtreeMatch(node.getJavadoc(), o.getJavadoc())
				&& assertSubtreeMatch(node.getName(), o.getName())
				&& assertSubtreeListMatch(node.bodyDeclarations(), o.bodyDeclarations()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(TypeDeclarationStatement node, Object other) {
		TypeDeclarationStatement o = safeCast(TypeDeclarationStatement.class, other);
		return assertSubtreeMatch(node.getDeclaration(), o.getDeclaration());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(TypeLiteral node, Object other) {
		TypeLiteral o = safeCast(TypeLiteral.class, other);
		return assertSubtreeMatch(node.getType(), o.getType());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.1
	 */
	public boolean match(TypeParameter node, Object other) {
		TypeParameter o = safeCast(TypeParameter.class, other);
		return assertSubtreeMatch(node.getName(), o.getName())
				&& assertSubtreeListMatch(node.typeBounds(), o.typeBounds());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(VariableDeclarationExpression node, Object other) {
		VariableDeclarationExpression o = safeCast(VariableDeclarationExpression.class, other);
		int level = node.getAST().apiLevel();
		if (level == AST.JLS2) {
			if (node.getModifiers() != o.getModifiers()) {
				return false;
			}
		}
		if (level >= AST.JLS3) {
			if (!assertSubtreeListMatch(node.modifiers(), o.modifiers())) {
				return false;
			}
		}
		return assertSubtreeMatch(node.getType(), o.getType())
			&& assertSubtreeListMatch(node.fragments(), o.fragments());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 * <p>
	 * Note that extra array dimensions are compared since they are an
	 * important part of the type of the variable.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(VariableDeclarationFragment node, Object other) {
		VariableDeclarationFragment o = safeCast(VariableDeclarationFragment.class, other);
		return assertSubtreeMatch(node.getName(), o.getName())
			&& node.getExtraDimensions() == o.getExtraDimensions()
			&& assertSubtreeMatch(node.getInitializer(), o.getInitializer());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(VariableDeclarationStatement node, Object other) {
		VariableDeclarationStatement o = safeCast(VariableDeclarationStatement.class, other);
		checkJslLevel(node);
		int level = node.getAST().apiLevel();
		if (level >= AST.JLS3) {
			if (!assertSubtreeListMatch(node.modifiers(), o.modifiers())) {
				return false;
			}
		}
		return assertSubtreeMatch(node.getType(), o.getType())
			&& assertSubtreeListMatch(node.fragments(), o.fragments());
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 */
	public boolean match(WhileStatement node, Object other) {
		WhileStatement o = safeCast(WhileStatement.class, other);
		return (
			assertSubtreeMatch(node.getExpression(), o.getExpression())
				&& assertSubtreeMatch(node.getBody(), o.getBody()));
	}

	/**
	 * Returns whether the given node and the other object match.
	 * <p>
	 * The default implementation provided by this class tests whether the
	 * other object is a node of the same type with structurally isomorphic
	 * child subtrees. Subclasses may override this method as needed.
	 * </p>
	 *
	 * @param node the node
	 * @param other the other object, or <code>null</code>
	 * @return <code>true</code> if the subtree matches, or
	 *   <code>false</code> if they do not match or the other object has a
	 *   different node type or is <code>null</code>
	 * @since 3.1
	 */
	public boolean match(WildcardType node, Object other) {
		WildcardType o = safeCast(WildcardType.class, other);
		if( node.isUpperBound() != o.isUpperBound()){
			return false;
		}
		return assertSubtreeMatch(node.getBound(), o.getBound());
	}

	private boolean assertSubtreeMatch(Object node1, Object node2){
		return super.safeSubtreeMatch(node1, node2);
	}
	
	private boolean assertSubtreeListMatch(List list1, List list2){
		@SuppressWarnings("unchecked")
        Iterator<ASTNode> it1 = list1.iterator();
		@SuppressWarnings("unchecked")
        Iterator<ASTNode> it2 = list2.iterator();
		for (; it1.hasNext() && it2.hasNext();) {
			ASTNode n1 = it1.next();
			ASTNode n2 = it2.next();
			if (!n1.subtreeMatch(this, n2)) {
				return false;
			}
		}
		if( it1.hasNext()){
			return false;
		}
		if( it2.hasNext()){
			return false;
		}
		return true;
	}
	
	private static void fail(String msg, Object... args){
		Assert.fail(String.format(msg, args));
	}
	
	private <T extends ASTNode> T safeCast(Class<T> expectType, Object actual){
		if( !expectType.isInstance(actual)){
			fail("Expected type %s but was %s", expectType.getName(), actual.getClass().getName());
		}
		return (T) actual;
	}
	
	public static class Builder {
		private boolean matchDocTags = false;

		public JAstMatcher build(){
			return new JAstMatcher(matchDocTags);
		}
		
		public ASTMatcher buildNonAsserting(){
			return new ASTMatcher(matchDocTags);
		}
		
		public Builder copyOf(){
			Builder copy = new Builder();
			copy.matchDocTags = matchDocTags;
			return copy;
		}
		
		public Builder setMatchDocTags(boolean matchDocTags) {
        	this.matchDocTags = matchDocTags;
        	return this;
        }
				
	}
}
