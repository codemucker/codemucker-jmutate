package org.codemucker.jmutate;

import org.eclipse.jdt.core.dom.ASTNode;

public interface ClashStrategyResolver{

	public ClashStrategy resolveClash(ASTNode existingNode, ASTNode newNode);

	public static class Fixed implements ClashStrategyResolver{

		private final ClashStrategy strategy;

		public Fixed(ClashStrategy strategy) {
			this.strategy = strategy;
		}

		@Override
		public ClashStrategy resolveClash(ASTNode existingNode, ASTNode newNode) {
			return strategy;
		}
	};
}