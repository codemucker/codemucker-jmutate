package com.bertvanbrakel.codemucker.ast;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.junit.Test;

public class JavaModifierTest {

	@Test
	public void testIsPublic() throws IOException {
		JavaModifiers mods = newJavaModifiersWithKeywords(ModifierKeyword.ABSTRACT_KEYWORD, ModifierKeyword.NATIVE_KEYWORD, ModifierKeyword.PUBLIC_KEYWORD);
		
		assertTrue(mods.isPublic());
		
		assertFalse(mods.isPackagePrivate());
		assertFalse(mods.isProtected());
		assertFalse(mods.isPrivate());
	}
	
	@Test
	public void testIsPackagePrivate() throws IOException {
		JavaModifiers mods = newJavaModifiersWithKeywords();
		
		assertTrue(mods.isPackagePrivate());
		
		assertFalse(mods.isPublic());
		assertFalse(mods.isProtected());
		assertFalse(mods.isPrivate());
	}
	
	@Test
	public void testIsProtected() throws IOException {
		JavaModifiers mods = newJavaModifiersWithKeywords(ModifierKeyword.ABSTRACT_KEYWORD, ModifierKeyword.NATIVE_KEYWORD, ModifierKeyword.PROTECTED_KEYWORD);
		
		assertTrue(mods.isProtected());
		
		assertFalse(mods.isPublic());
		assertFalse(mods.isPackagePrivate());
		assertFalse(mods.isPrivate());
	}
	
	@Test
	public void testIsPrivate() throws IOException {
		JavaModifiers mods = newJavaModifiersWithKeywords(ModifierKeyword.ABSTRACT_KEYWORD, ModifierKeyword.NATIVE_KEYWORD, ModifierKeyword.PRIVATE_KEYWORD);
		
		assertTrue(mods.isPrivate());
		
		assertFalse(mods.isPublic());
		assertFalse(mods.isPackagePrivate());
		assertFalse(mods.isProtected());
	}
	
	@Test
	public void testIsAbstract() throws IOException {
		JavaModifiers mods1 = newJavaModifiersWithKeywords(ModifierKeyword.ABSTRACT_KEYWORD, ModifierKeyword.FINAL_KEYWORD);
		JavaModifiers mods2 = newJavaModifiersWithKeywords(ModifierKeyword.FINAL_KEYWORD);
		
		assertTrue(mods1.isAbstract());
		assertFalse(mods2.isAbstract());
	}
	
	@Test
	public void testSetAccess() throws IOException {
		for( Access access:new Access[]{ Access.PRIVATE, Access.PROTECTED, Access.PUBLIC } ){
			JavaModifiers mods = newJavaModifiersWithKeywords();
			
			assertFalse(mods.isAccess(access));
			mods.setAccess(access);
			assertTrue(mods.isAccess(access));
		}
		//package
		JavaModifiers mods = newJavaModifiersWithKeywords(ModifierKeyword.PUBLIC_KEYWORD);
		
		assertFalse(mods.isAccess(Access.PACKAGE));
		mods.setAccess(Access.PACKAGE);
		assertTrue(mods.isAccess(Access.PACKAGE));
	}

	private static JavaModifiers newJavaModifiersWithKeywords(ModifierKeyword... keywords){
		AST ast = AST.newAST(AST.JLS3);
		JavaModifiers mods = new JavaModifiers(ast, toModifiers(ast,keywords));
		
		return  mods;
	}
	
	private static List<IExtendedModifier> toModifiers(AST ast, ModifierKeyword... keywords){
		List<IExtendedModifier> mods = new ArrayList<IExtendedModifier>();
		for( ModifierKeyword keyword:keywords){
			mods.add(ast.newModifier(keyword));
		}
		return mods;
	}
}
