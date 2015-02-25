package org.codemucker.jmutate.ast;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

public enum JAccess {
	PRIVATE(0,ModifierKeyword.PRIVATE_KEYWORD, ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.PROTECTED_KEYWORD), 
	PACKAGE(1,null, ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.PROTECTED_KEYWORD, ModifierKeyword.PRIVATE_KEYWORD),
	PROTECTED(2,ModifierKeyword.PROTECTED_KEYWORD, ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.PRIVATE_KEYWORD), 
	PUBLIC(3,ModifierKeyword.PUBLIC_KEYWORD, ModifierKeyword.PROTECTED_KEYWORD, ModifierKeyword.PRIVATE_KEYWORD);
	
	private final int accessLevel;
	private final ModifierKeyword keyword;
	private final Collection<ModifierKeyword> keywordsToUnset;
	
	private JAccess(int accessLevel,ModifierKeyword keyword,ModifierKeyword... keywordsToUnset){
		this.keyword = keyword;
		this.keywordsToUnset = Collections.unmodifiableCollection(Arrays.asList(keywordsToUnset));
		this.accessLevel = accessLevel;
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
	
	public boolean isMoreAccessibleThan(JAccess access){
		return compare(access) > 0;
	}
	
	public boolean isLessAccessibleThan(JAccess access){
		return compare(access) < 0;
	}
	
	/**
	 * Return whether this access level is less than, equal to, or greater than the given access.
	 * 
	 * @param access
	 * @return 1 if this access is greater, 0 if equal, or -1 if less
	 */
	public int compare(JAccess access){
		int i = accessLevel - access.accessLevel;
		return i == 0?0:(i>0?1:-1);
	}
	
	public Collection<ModifierKeyword> getIncompatibleKeywords(){
		return keywordsToUnset;
	}
}
