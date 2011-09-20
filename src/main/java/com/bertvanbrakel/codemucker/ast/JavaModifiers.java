package com.bertvanbrakel.codemucker.ast;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

public class JavaModifiers {
	private final AST ast;
	private final List<IExtendedModifier> modifiers;

	public JavaModifiers(AST ast, List<IExtendedModifier> modifiers) {
		super();
		this.modifiers = modifiers;
		this.ast = ast;
	}

	public void setAccess(Access access) {
		setModifier(access.getKeyword(), access.getIncompatibleKeywords());
	}
	
	public boolean isPublic() {
		return isAccess(Access.PUBLIC);
	}

	public boolean isProtected() {
		return isAccess(Access.PROTECTED);
	}

	public boolean isPrivate() {
		return isAccess(Access.PRIVATE);
	}
	
	public boolean isPackagePrivate() {
		return isAccess(Access.PACKAGE);
	}
	
	public boolean isAccess(Access access) {
		return asAccess().equals(access);
	}
	
	public Access asAccess() {
		for (Modifier m : getModifiers()) {
			if (ModifierKeyword.PUBLIC_KEYWORD.equals(m.getKeyword())) {
				return Access.PUBLIC;
			}
			if (ModifierKeyword.PRIVATE_KEYWORD.equals(m.getKeyword())) {
				return Access.PRIVATE;
			}
			if (ModifierKeyword.PROTECTED_KEYWORD.equals(m.getKeyword())) {
				return Access.PROTECTED;
			}
		}
		return Access.PACKAGE;
	}

	public void setModifierKeywordIfNotSet(ModifierKeyword keywordToSet) {
		setModifier(ast.newModifier(keywordToSet), null);
	}
	
	private void setModifier(ModifierKeyword keywordToSet, Collection<ModifierKeyword> removeKeywords) {
		setModifier(keywordToSet==null?null:ast.newModifier(keywordToSet), removeKeywords);
	}

	private void setModifier(Modifier modifierToSet, Collection<ModifierKeyword> removeKeywords) {
		ModifierKeyword keywordToSet = modifierToSet==null?null:modifierToSet.getKeyword();
		boolean exists = false;
		for (Iterator<Modifier> modifiers = getModifiers().iterator(); modifiers.hasNext();) {
			Modifier m = modifiers.next();
			if (removeKeywords.contains(m.getKeyword())
			        && (keywordToSet == null || !keywordToSet.equals(m.getKeyword()))) {
				modifiers.remove();
			}

			if (!exists && keywordToSet != null && keywordToSet.equals(m.getKeyword())) {
				exists = true;
			}
		}
		if (!exists && keywordToSet != null) {
			this.modifiers.add(modifierToSet);
		}
	}

	public boolean isStatic() {
		return containsKeyword(ModifierKeyword.STATIC_KEYWORD);
	}

	public boolean isFinal() {
		return containsKeyword(ModifierKeyword.FINAL_KEYWORD);
	}

	public boolean isAbstract() {
		return containsKeyword(ModifierKeyword.ABSTRACT_KEYWORD);
	}

	public boolean isTransient() {
		return containsKeyword(ModifierKeyword.TRANSIENT_KEYWORD);
	}

	public boolean isVolatile() {
		return containsKeyword(ModifierKeyword.VOLATILE_KEYWORD);
	}

	public boolean isNative() {
		return containsKeyword(ModifierKeyword.NATIVE_KEYWORD);
	}
	
	public boolean isSynchronized() {
		return containsKeyword(ModifierKeyword.SYNCHRONIZED_KEYWORD);
	}
	
	public boolean isStrictFp() {
		return containsKeyword(ModifierKeyword.STRICTFP_KEYWORD);
	}

	private boolean containsKeyword(ModifierKeyword keyword) {
		for (Modifier m : getModifiers()) {
			if (keyword.equals(m.getKeyword())) {
				return true;
			}
		}
		return false;
	}

	private Iterable<Modifier> getModifiers() {
		return new ModifierIterable(modifiers);
	}

	static class ModifierIterable implements Iterable<Modifier> {
		private final List<? extends IExtendedModifier> modifiers;

		public ModifierIterable(List<? extends IExtendedModifier> modifiers) {
			super();
			this.modifiers = modifiers;
		}

		@Override
		public Iterator<Modifier> iterator() {
			return new ModifierIterator(modifiers);
		}
	}

	static class ModifierIterator implements Iterator<Modifier> {
		private static final int UNSET = -1;
		private static final int REMOVE_CALLED = -2;
		
		private final List<? extends IExtendedModifier> modifiers;
		private Modifier nextModifier;
		private int currentIndex = UNSET;
		private int nextIndex = -1;
		
		public ModifierIterator(List<? extends IExtendedModifier> modifiers) {
			super();
			this.modifiers = modifiers;
			this.nextModifier = nextModifier();
		}

		@Override
		public boolean hasNext() {
			return nextModifier != null;
		}

		@Override
		public Modifier next() {
			if( !hasNext()){
				throw new NoSuchElementException();
			}
			Modifier returnModifier = nextModifier;
			currentIndex = nextIndex;
			nextModifier = nextModifier();
			return returnModifier;
		}
		
		@Override
		public void remove() {
			if( currentIndex < 0){
				if( currentIndex == REMOVE_CALLED ){
					throw new IllegalStateException("'remove' has already been called");
				}
				if( currentIndex == UNSET){
					throw new IllegalStateException("have to call 'next' first");
				}
				throw new IllegalStateException("Can't call 'remove' at this stage");
			}
			modifiers.remove(currentIndex);
			currentIndex = REMOVE_CALLED;
			nextIndex--;
		}

		private Modifier nextModifier() {
			for( nextIndex++;nextIndex < modifiers.size(); nextIndex++){
				IExtendedModifier next = modifiers.get(nextIndex);
				if (next instanceof Modifier) {
					return (Modifier) next;
				}
			}
			return null;
		}
	};
}
