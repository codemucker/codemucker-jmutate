/*
 * Copyright 2011 Bert van Brakel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bertvanbrakel.test.finder;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

import com.bertvanbrakel.test.util.ProjectFinder;
import com.bertvanbrakel.test.util.TestUtils;

public class ClassFinderOptions {
	
	private final Collection<String> projectFiles = new ArrayList<String>(ProjectFinder.DEF_PROJECT_FILES);
	
	public static final ClassMatcher MATCHER_ANONYMOUS = new ClassMatcher() {
		@Override
		public boolean matchClass(Class found) {
			return found.isAnonymousClass();
		}
	};
	
	public static final ClassMatcher MATCHER_ENUM = new ClassMatcher() {
		@Override
		public boolean matchClass(Class found) {
			return found.isEnum();
		}
	};
	
	public static final ClassMatcher MATCHER_INNER_CLASS = new ClassMatcher() {
		@Override
		public boolean matchClass(Class found) {
			return found.isMemberClass();
		}
	};

	public static final ClassMatcher MATCHER_INTERFACE = new ClassMatcher() {
		@Override
		public boolean matchClass(Class found) {
			return found.isInterface();
		}
	};

	private final Collection<File> classPathsDir = new HashSet<File>();
	private boolean includeClassesDir = true;
	private boolean includeTestDir = false;

	private final Collection<FileMatcher> excludeFileNameMatchers = new ArrayList<FileMatcher>();
	private final Collection<FileMatcher> includeFileNameMatchers = new ArrayList<FileMatcher>();
	
	private final Collection<ClassMatcher> includeClassMatchers = new ArrayList<ClassMatcher>();
	private final Collection<ClassMatcher> excludeClassMatchers = new ArrayList<ClassMatcher>();
	
	private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	
	public Collection<String> getProjectFiles() {
    	return projectFiles;
    }

	public ClassLoader getClassLoader() {
    	return classLoader;
    }

	public ClassFinderOptions classLoader(ClassLoader classLoader) {
    	this.classLoader = classLoader;
    	return this;
    }

	public ClassFinderOptions includeClassesDir(boolean b) {
		this.includeClassesDir = b;
		return this;
	}

	public ClassFinderOptions includeTestDir(boolean b) {
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

	public ClassFinderOptions addClassPath(File dir) {
		classPathsDir.add(dir);
		return this;
	}

	public ClassFinderOptions excludeFileName(String path) {
		String regExp = TestUtils.antExpToPatternExp(path);
		excludeFileName(Pattern.compile(regExp));
		return this;
	}
	
	public ClassFinderOptions excludeFileName(Pattern pattern) {
		excludeFileName(new RegExpPatternFileNameMatcher(pattern));
		return this;
	}

	public ClassFinderOptions excludeFileName(FileMatcher matcher) {
		this.excludeFileNameMatchers.add(matcher);
		return this;
	}

	public ClassFinderOptions includeFileName(String pattern) {
		String regExp = TestUtils.antExpToPatternExp(pattern);
		includeFileName(Pattern.compile(regExp));
		return this;
	}

	public ClassFinderOptions includeFileName(Pattern pattern) {
		includeFileName(new RegExpPatternFileNameMatcher(pattern));
		return this;
	}
	
	public ClassFinderOptions includeFileName(FileMatcher matcher) {
		this.includeFileNameMatchers.add(matcher);
		return this;
	}
	
	public ClassFinderOptions assignableTo(Class<?>... superclass) {
		includeClassMatching(new ClassAssignableMatcher(superclass));
		return this;
	}
	
	public <T extends Annotation> ClassFinderOptions withAnnotation(Class<T>... annotations){
		includeClassMatching(new ContainsAnnotationsMatcher(annotations));
		return this;
	}
	
	public ClassFinderOptions includeClassMatching(ClassMatcher matcher) {
		this.includeClassMatchers.add(matcher);
		return this;
	}
	
	public ClassFinderOptions excludeEnum() {
		excludeClassMatching(MATCHER_ENUM);
		return this;
	}

	public ClassFinderOptions excludeAnonymous() {
		excludeClassMatching(MATCHER_ANONYMOUS);
		return this;
	}

	public ClassFinderOptions excludeInner() {
		excludeClassMatching(MATCHER_INNER_CLASS);
		return this;
	}

	public ClassFinderOptions excludeInterfaces() {
		excludeClassMatching(MATCHER_INTERFACE);
		return this;
	}

	public ClassFinderOptions excludeClassMatching(ClassMatcher matcher) {
		this.excludeClassMatchers.add(matcher);
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
	
	public ClassMatcher toClassMatcher() {
		return new ClassMatcher() {
			@Override
			public boolean matchClass(Class klass) {
				boolean include = true;
				if (includeClassMatchers != null && includeClassMatchers.size() > 0) {
					include = false;
					for (ClassMatcher matcher : includeClassMatchers) {
						if (matcher.matchClass(klass)) {
							include = true;
							break;
						}
					}
				}
				if (excludeClassMatchers != null && excludeClassMatchers.size() > 0) {
					for (ClassMatcher matcher : excludeClassMatchers) {
						if (matcher.matchClass(klass)) {
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
	
	protected static class ClassAssignableMatcher implements ClassMatcher {
		private final Class<?>[] superclass;

		public ClassAssignableMatcher(Class<?>... superclass) {
	        super();
	        this.superclass = superclass;
        }

		@Override
		public boolean matchClass(Class found) {
			for (Class<?> require : superclass) {
				if (!require.isAssignableFrom(found)) {
					return false;
				}
			}
			return true;
		}
	}
	
	protected static class ContainsAnnotationsMatcher implements ClassMatcher {
		private final Class<? extends Annotation>[] annotations;

		public ContainsAnnotationsMatcher(Class<? extends Annotation>... annotations) {
	        super();
	        this.annotations = annotations;
        }

		@Override
		public boolean matchClass(Class found) {
			for (Class<?> anon : annotations) {
				if (found.getAnnotation(anon) == null) {
					return false;
				}
			}
			return true;
		}
	}
}