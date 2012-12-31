package com.bertvanbrakel.codemucker.ast;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * An Ast visitor which ignores child type declarations
 */
public class IgnoreableChildTypesVisitor extends BaseASTVisitor {

	// a little bit of funkyness to ignore child classes
	final Collection<Class<? extends ASTNode>> childNodeTypes = Arrays.asList(
//			TypeDeclaration.class,
//	        AnonymousClassDeclaration.class, 
//	        EnumDeclaration.class, 
//	        ClassInstanceCreation.class
	);
	
	// TODO:skip method bodies? or we want to include anonymous inner types?

	private int typeDepth = 0;

	private final int maxTypeDepth;
	
	private final boolean ignoreAnonymousInnerTypes = true;

	public IgnoreableChildTypesVisitor() {
		this(-1, false);
	}

	/**
	 * 
	 * @param maxDepth 0 = look only in current node directly. -1 means any depth
	 */
	public IgnoreableChildTypesVisitor(int maxDepth) {
		this(maxDepth, false);
	}

	public IgnoreableChildTypesVisitor(int maxDepth, boolean visitDocTags) {
		super(visitDocTags);
		this.maxTypeDepth = maxDepth;
	}

	@Override
	protected boolean visitNode(ASTNode node) {
		super.visitNode(node);
		if (matchIgnore(node)) {
			if (maxTypeDepth >= 0 && typeDepth > maxTypeDepth) {
				return false;
			}
			incr();
		}
		return true;
	}

	@Override
	protected void endVisitNode(ASTNode node) {
		super.endVisitNode(node);
		if (matchIgnore(node)) {
			decr();
		}
	}

	private boolean matchIgnore(ASTNode node) {
		return childNodeTypes.contains(node.getClass());
	}

	private void incr() {
		typeDepth++;
	}

	private void decr() {
		typeDepth--;
	}
}
