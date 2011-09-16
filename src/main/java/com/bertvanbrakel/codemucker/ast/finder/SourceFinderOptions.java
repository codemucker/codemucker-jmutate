package com.bertvanbrakel.codemucker.ast.finder;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

import com.bertvanbrakel.codemucker.ast.JavaSourceFile;
import com.bertvanbrakel.codemucker.ast.JavaType;
import com.bertvanbrakel.test.finder.FileMatcher;
import com.bertvanbrakel.test.util.ProjectFinder;

public class SourceFinderOptions {
	
	private final Collection<String> projectFiles = new ArrayList<String>(ProjectFinder.DEF_PROJECT_FILES);
	
	public static final JavaTypeMatcher MATCHER_ANONYMOUS = new JavaTypeMatcher() {
		@Override
		public boolean matchType(JavaType found) {
			return found.isAnonymousClass();
		}
	};
	
	public static final JavaTypeMatcher MATCHER_ENUM = new JavaTypeMatcher() {
		@Override
		public boolean matchType(JavaType found) {
			return found.isEnum();
		}
	};
	
	public static final JavaTypeMatcher MATCHER_INNER_CLASS = new JavaTypeMatcher() {
		@Override
		public boolean matchType(JavaType found) {
			return found.isInnerClass();
		}
	};

	public static final JavaTypeMatcher MATCHER_INTERFACE = new JavaTypeMatcher() {
		@Override
		public boolean matchType(JavaType found) {
			return found.isInterface();
		}
	};

	private final Collection<File> classPathsDir = new HashSet<File>();
	private boolean includeClassesDir = true;
	private boolean includeTestDir = false;

	private final Collection<FileMatcher> excludeFileNameMatchers = new ArrayList<FileMatcher>();
	private final Collection<FileMatcher> includeFileNameMatchers = new ArrayList<FileMatcher>();
	
	private final Collection<JavaTypeMatcher> includeTypeMatchers = new ArrayList<JavaTypeMatcher>();
	private final Collection<JavaTypeMatcher> excludeTypeMatchers = new ArrayList<JavaTypeMatcher>();
	
	public Collection<String> getProjectFiles() {
    	return projectFiles;
    }

	public SourceFinderOptions includeClassesDir(boolean b) {
		this.includeClassesDir = b;
		return this;
	}

	public SourceFinderOptions includeTestDir(boolean b) {
		this.includeTestDir = b;
		return this;
	}

	public Collection<File> getClassPathsDir() {
		return classPathsDir;
	}

	public boolean isIncludeClassesDir() {
		return includeClassesDir;
	}

	public boolean isIncludeTestDir() {
		return includeTestDir;
	}

	public SourceFinderOptions addClassPath(File dir) {
		classPathsDir.add(dir);
		return this;
	}

	public SourceFinderOptions excludeFileName(String path) {
		String regExp = antToRegExp(path);
		excludeFileName(Pattern.compile(regExp));
		return this;
	}
	
	public SourceFinderOptions excludeFileName(Pattern pattern) {
		excludeFileName(new RegExpPatternFileNameMatcher(pattern));
		return this;
	}

	public SourceFinderOptions excludeFileName(FileMatcher matcher) {
		this.excludeFileNameMatchers.add(matcher);
		return this;
	}

	public SourceFinderOptions includeFileName(String pattern) {
		String regExp = antToRegExp(pattern);
		includeFileName(Pattern.compile(regExp));
		return this;
	}

	public SourceFinderOptions includeFileName(Pattern pattern) {
		includeFileName(new RegExpPatternFileNameMatcher(pattern));
		return this;
	}
	
	public SourceFinderOptions includeFileName(FileMatcher matcher) {
		this.includeFileNameMatchers.add(matcher);
		return this;
	}
	
	private String antToRegExp(String antPattern) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < antPattern.length(); i++) {
			char c = antPattern.charAt(i);
			if (c == '.') {
				sb.append("\\.");
			} else if (c == '*') {
				sb.append(".*");
			} else if (c == '?') {
				sb.append(".?");
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public SourceFinderOptions assignableTo(Class<?>... superclass) {
		includeClassMatching(new TypeAssignableMatcher(superclass));
		return this;
	}
	
	public <T extends Annotation> SourceFinderOptions withAnnotation(Class<T>... annotations){
		includeClassMatching(new ContainsAnnotationsMatcher(annotations));
		return this;
	}
	
	public SourceFinderOptions includeClassMatching(JavaTypeMatcher matcher) {
		this.includeTypeMatchers.add(matcher);
		return this;
	}
	
	public SourceFinderOptions excludeEnum() {
		excludeClassMatching(MATCHER_ENUM);
		return this;
	}

	public SourceFinderOptions excludeAnonymous() {
		excludeClassMatching(MATCHER_ANONYMOUS);
		return this;
	}

	public SourceFinderOptions excludeInner() {
		excludeClassMatching(MATCHER_INNER_CLASS);
		return this;
	}

	public SourceFinderOptions excludeInterfaces() {
		excludeClassMatching(MATCHER_INTERFACE);
		return this;
	}

	public SourceFinderOptions excludeClassMatching(JavaTypeMatcher matcher) {
		this.excludeTypeMatchers.add(matcher);
		return this;
	}
	
	public FileMatcher toFileMatcher() {
		return new FileMatcher() {
			@Override
			public boolean matchFile(File file, String relPath) {
				boolean include = true;
				if (includeFileNameMatchers != null && includeFileNameMatchers.size() > 0) {
					include = false;// by default if we have includes we exclude
									// all except matches
					for (FileMatcher matcher : includeFileNameMatchers) {
						if (matcher.matchFile(null, relPath)) {
							include = true;
							break;
						}
					}
				}
				if (include && (excludeFileNameMatchers != null && excludeFileNameMatchers.size() > 0)) {
					for (FileMatcher matcher : excludeFileNameMatchers) {
						if (matcher.matchFile(null, relPath)) {
							include = false;
						}
					}
				}
				return include;
			}
		};
	}
	
	public JavaSourceFileMatcher toJavaSourceMatcher() {
		final JavaTypeMatcher typeMatcher = toJavaTypeMatcher();
		return new JavaSourceFileMatcher() {
			@Override
			public boolean matchSource(JavaSourceFile file) {
				for( JavaType type :file.getJavaTypes()){
					if( typeMatcher.matchType(type)){
						return true;
					}
				}
				return false;
			}
		};
	}
	
	public JavaTypeMatcher toJavaTypeMatcher() {
		return new JavaTypeMatcher() {
			@Override
			public boolean matchType(JavaType type) {
				boolean include = true;
				if (includeTypeMatchers != null && includeTypeMatchers.size() > 0) {
					include = false;
					for (JavaTypeMatcher matcher : includeTypeMatchers) {
						if (matcher.matchType(type)) {
							include = true;
							break;
						}
					}
				}
				if (excludeTypeMatchers != null && excludeTypeMatchers.size() > 0) {
					for (JavaTypeMatcher matcher : excludeTypeMatchers) {
						if (matcher.matchType(type)) {
							include = false;
							break;
						}
					}
				}
				return include;
			}
		};
	}

	protected static class RegExpPatternFileNameMatcher implements FileMatcher {
		private final Pattern pattern;
		
		RegExpPatternFileNameMatcher(Pattern pattern) {
			this.pattern = pattern;
		}

		@Override
		public boolean matchFile(File file, String path) {
			return pattern.matcher(path).matches();
		}
	}
	
	protected static class TypeAssignableMatcher implements JavaTypeMatcher {
		private final Class<?>[] superclass;

		public TypeAssignableMatcher(Class<?>... superclass) {
	        super();
	        this.superclass = superclass;
        }

		@Override
		public boolean matchType(JavaType found) {
			for (Class<?> require : superclass) {
				if (!found.isImplementing(require)) {
					return false;
				}
			}
			return true;
		}
	}
	
	protected static class ContainsAnnotationsMatcher implements JavaTypeMatcher {
		private final Class<? extends Annotation>[] annotations;

		public ContainsAnnotationsMatcher(Class<? extends Annotation>... annotations) {
	        super();
	        this.annotations = annotations;
        }

		@Override
		public boolean matchType(JavaType found) {
			for (Class<? extends Annotation> anon : annotations) {
				if (!found.hasAnnotation(anon)) {
					return false;
				}
			}
			return true;
		}
	}
}