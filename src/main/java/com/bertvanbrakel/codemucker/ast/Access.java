package com.bertvanbrakel.codemucker.ast;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

public enum Access {
	PRIVATE(ModifierKeyword.PRIVATE_KEYWORD, ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.PROTECTED_KEYWORD), 
	PACKAGE(null, ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.PROTECTED_KEYWORD, ModifierKeyword.PRIVATE_KEYWORD),
	PROTECTED(ModifierKeyword.PROTECTED_KEYWORD, ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.PRIVATE_KEYWORD), 
	PUBLIC(ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.PROTECTED_KEYWORD, ModifierKeyword.PRIVATE_KEYWORD);
	
	private final ModifierKeyword keyword;
	private final Collection<ModifierKeyword> keywordsToUnset;
	
	private Access(ModifierKeyword keyword,ModifierKeyword... keywordsToUnset){
		this.keyword = keyword;
		this.keywordsToUnset = Collections.unmodifiableCollection(Arrays.asList(keywordsToUnset));
	}
	
	public ModifierKeyword getKeyword(){
		return keyword;
	}
	
	public Collection<ModifierKeyword> getIncompatibleKeywords(){
		return keywordsToUnset;
	}
}
