package org.codemucker.jmutate.ast;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

public enum JAccess {
	PRIVATE(ModifierKeyword.PRIVATE_KEYWORD, ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.PROTECTED_KEYWORD), 
	PACKAGE(null, ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.PROTECTED_KEYWORD, ModifierKeyword.PRIVATE_KEYWORD),
	PROTECTED(ModifierKeyword.PROTECTED_KEYWORD, ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.PRIVATE_KEYWORD), 
	PUBLIC(ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.PROTECTED_KEYWORD, ModifierKeyword.PRIVATE_KEYWORD);
	
	private final ModifierKeyword keyword;
	private final Collection<ModifierKeyword> keywordsToUnset;
	
	private JAccess(ModifierKeyword keyword,ModifierKeyword... keywordsToUnset){
		this.keyword = keyword;
		this.keywordsToUnset = Collections.unmodifiableCollection(Arrays.asList(keywordsToUnset));
	}
	
	public ModifierKeyword getKeyword(){
		return keyword;
	}
	
	public String toCode(){
		if( this == PACKAGE){
			return "";
		}
		return name().toLowerCase();
	}
	
	
	public Collection<ModifierKeyword> getIncompatibleKeywords(){
		return keywordsToUnset;
	}
}
