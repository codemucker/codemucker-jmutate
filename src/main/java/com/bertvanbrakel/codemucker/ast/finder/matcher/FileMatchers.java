package com.bertvanbrakel.codemucker.ast.finder.matcher;

import java.util.regex.Pattern;

import com.bertvanbrakel.test.finder.FileMatcher;
import com.bertvanbrakel.test.util.TestUtils;

public class FileMatchers {

	public FileMatcher withPackage(String packageName) {
		String regExp = "/" + packageName.replace('.', '/') + "/.*";
		return withPath(Pattern.compile(regExp));
	}

	public static FileMatcher withExtension(String extension) {
		return withPath("*." + extension);
	}

	public static FileMatcher withName(Class<?> classToMatch) {
		String path = '/' + classToMatch.getSimpleName() + "\\.java";
		Package pkg = classToMatch.getPackage();
		if (pkg != null) {
			path = '/' + pkg.getName().replace('.', '/') + path;
		}
		return withPath(Pattern.compile(path));
	}
	
	public static FileMatcher withName(String antPattern) {
		return withPath("*/" + antPattern);
	}

	public static FileMatcher inPackage(Class<?> classWithPkg) {
		return inPackage(classWithPkg.getPackage());
	}

	public static FileMatcher inPackage(Package pkg) {
		return withPath(pkg.toString().replace('.', '/'));
	}

	public static FileMatcher withPath(String antPattern) {
		return withPath(TestUtils.antExpToPattern(antPattern));
	}

	public static FileMatcher withPath(Pattern pattern) {
		return new RegExpPatternFileNameMatcher(pattern);
	}


		
}
