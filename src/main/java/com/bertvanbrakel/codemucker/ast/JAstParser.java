package com.bertvanbrakel.codemucker.ast;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.bertvanbrakel.lang.IBuilder;
import com.bertvanbrakel.lang.annotation.NotThreadSafe;
import com.bertvanbrakel.test.finder.DirectoryRoot;
import com.bertvanbrakel.test.finder.Root;
import com.bertvanbrakel.test.finder.Root.RootContentType;
import com.bertvanbrakel.test.finder.Root.RootType;
import com.bertvanbrakel.test.finder.RootResource;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Provides some convenience methods around an {@link ASTParser}
 */
@NotThreadSafe
public class JAstParser {
	private static final int LINE_NUM_PADDING = 4; 
	
	private final ASTParser parser;
	private final boolean checkParse;
	private final boolean recordModifications;
	private final Map<?,?> options;
	private final String[] binaryRoots;
	private final String[] sourceRoots;
	
	public static Builder builder(){
		return new Builder();
	}
	
	private final Root snippetRoot;
	
	private static final String[] EMPTY = new String[]{};
	
	private JAstParser(ASTParser parser, boolean checkParse, boolean recordModifications, Map<Object,Object> options, Root snippetRoot, Iterable<Root> resolveRoots) {
	    super();
	    this.parser = checkNotNull(parser,"expect parser");
	    this.checkParse = checkParse;
	    this.recordModifications = recordModifications;
	    this.options = checkNotNull(options,"expect parser options");
		this.snippetRoot = snippetRoot;
		
	    List<String> sources = newArrayList();
	    sources.add(snippetRoot.getPathName());
	    
	    List<String> binaries = newArrayList();
		for (Root root : resolveRoots) {
			if(root.getContentType() == RootContentType.SRC){
				sources.add(root.getPathName());
			}  else {
				binaries.add(root.getPathName());
			}
		}
		
		this.binaryRoots = binaries.toArray(EMPTY);
		this.sourceRoots = sources.toArray(EMPTY);
    }

	/**
	 * Parse the given source as a compilation unit
	 * 
	 * @param src
	 * @return
	 */
	public CompilationUnit parseCompilationUnit(CharSequence src,RootResource resource) {
		CompilationUnit cu = (CompilationUnit) parseNode(src, ASTParser.K_COMPILATION_UNIT, resource);
		if (checkParse){
			IProblem[] problems = cu.getProblems();
			if (problems.length > 0) {
				List<IProblem> errors = extractErrors(problems);
				if( !errors.isEmpty()){		
					String problemString = Joiner.on("\n").join(Lists.transform(errors, problemToStringFunc()));
					fail(String.format("Parsing error for resource %s,  source %s\n,  problems are %s", resource==null?null:resource.getRelPath(), prependLineNumbers(src), problemString));
				}
			}
		}
		if (recordModifications) {
			cu.recordModifications();
		}

		return cu;
	}
	
	private List<IProblem> extractErrors(IProblem[] problems){
		List<IProblem> errors = Lists.newArrayList();
		for(IProblem problem:problems){
			if( problem.isError()){
				errors.add(problem);
			}
		}
		return errors;
	}
	/**
	 * Converts an {@link IProblem} into a human readable string
	 */
	private static Function<IProblem,String> problemToStringFunc(){
		return new Function<IProblem,String>(){
			@Override
            public String apply(IProblem problem) {
				return Objects.toStringHelper("Problem")
					.add("severity", problem.isError()?"error":"warn")
					.add("msg", problem.getMessage())
					.add("line", problem.getSourceLineNumber())
					.add("char", problem.getSourceStart() + LINE_NUM_PADDING)
					//.add("to", problem.getSourceEnd() + PAD_LINE_NUM)
					
//					.add( "id", problem.getID() )
					.toString();
            }
		};
	}
	
	/**
	 * Prepend the line number to each line. Lines are delimited by '\n' or '\r\n'
	 */
	private String prependLineNumbers(CharSequence src){
		StringBuilder sb = new StringBuilder();
		Iterable<String> lines = Splitter.on(Pattern.compile("\r\n|\n")).split(src);
		int lineNum = 1;
		for( String line:lines){
			if( lineNum > 0){
				sb.append('\n');
			}
			//sb.append('[');
			sb.append(Strings.padEnd(Integer.toString(lineNum) + ".", LINE_NUM_PADDING, ' '));
			//sb.append(" ");
			
			sb.append(line);
			lineNum++;
		}
		return sb.toString();
	}

	public ASTNode parseNode(CharSequence src, int kind, RootResource resource) {
		
		parser.setCompilerOptions(options);
		
		if( resource != null ){
			parser.setUnitName(resource.getRelPath());
		}
		if( binaryRoots.length > 0 || sourceRoots.length > 0){
			boolean includeRunningVMBootclasspath = true;//if false can't find all the JDK classes?
	    	parser.setEnvironment(binaryRoots, sourceRoots, null, includeRunningVMBootclasspath);
	    }
		parser.setSource(src.toString().toCharArray());
		parser.setKind(kind);
		ASTNode node = parser.createAST(null);
		//check the parsed type is what was asked for as if there was an error
		//parsing the parser can decide to change it's mind as to what it's returning
				
//		Class<?> expectType = ASTNode.nodeClassForType(kind);
//		if (!expectType.isAssignableFrom(node.getClass())) {
//			throw new CodemuckerException("Expected to parse node of type '%s' but was '%s' for input %s", expectType.getName(), node.getClass().getName(), src);
//		}
		return node;
	}
	
	/**
	 * Return the underlying ASTParser
	 * @return
	 */
	public ASTParser getASTParser(){
		return parser;
	}

	public static JAstParser newDefaultJParser() {
		return builder().build();
	}

	public static ASTParser newDefaultParser() {
		return builder().build().parser;
	}
	
	public static class CompilerOptions {
		
	}
	
	public static class Builder implements IBuilder<JAstParser> {
		private ASTParser parser;
		private boolean checkParse = true;
		private boolean recordModifications = true;
		private boolean resolveBindings = true;
		@SuppressWarnings("unchecked")
        private Map<Object,Object> options = newHashMap(JavaCore.getOptions());
		private List<Root> roots = newArrayList();
		private Root snippetRoot;
		
		public Builder(){
			// In order to parse 1.5 code, some compiler options need to be set
			// to atleast 1.5
			setSourceLevel(JavaCore.VERSION_1_6);
		}
		
		public Builder setDefaults() {
        	setParser(newDefaultAstParser());
        	setSourceLevel(JavaCore.VERSION_1_6);
        	setCompilerOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, "ignore");
        	setCompilerOption(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, "ignore");
        	setCompilerOption(JavaCore.COMPILER_PB_UNUSED_PARAMETER, "ignore");
        	setCompilerOption(JavaCore.COMPILER_PB_UNUSED_TYPE_ARGUMENTS_FOR_METHOD_INVOCATION, "ignore");
         	setCompilerOption(JavaCore.COMPILER_PB_UNUSED_IMPORT, "ignore");
        	return this;
        }

		public JAstParser build(){
			return new JAstParser(
				toParser()
				, checkParse
				, recordModifications
				, newHashMap(options)
				, toSnippetRoot()
				, roots
			);
		}

		private ASTParser toParser() {
			return parser == null ? newDefaultAstParser() : parser;
		}
		
		private Root toSnippetRoot(){
			return snippetRoot==null?newTmpRoot():snippetRoot;
		}
		
		private static Root newTmpRoot(){
			try {
				File f = File.createTempFile("JASTParserRoot","");
				File tmpDir = new File(f.getAbsolutePath() + ".d/");
				tmpDir.mkdirs();
				
				return new DirectoryRoot(tmpDir,RootType.GENERATED,RootContentType.SRC);
			} catch (IOException e) {
				throw new CodemuckerException("Couldn't create a tmp root");
			}
		}
		
		private ASTParser newDefaultAstParser(){
			ASTParser parser = ASTParser.newParser(AST.JLS4);
			parser.setResolveBindings(resolveBindings);
		    parser.setCompilerOptions(options);

			return parser;
		}
		
		public Builder setParser(ASTParser parser) {
        	this.parser = parser;
        	return this;
        }
		
		/**
		 * Add a root to use to use in resolving bindings
		 * @param root
		 * @return
		 */
		public Builder addResolveRoot(Root root){
			this.roots.add(root);
			return this;
		}

		public Builder setResolveRoots(IBuilder<? extends Iterable<Root>> builder){
			setResolveRoots(builder.build());
			return this;
		}
		
		/**
		 * Set the roots to use to resolve bindings
		 * @param roots
		 * @return
		 */
		public Builder setResolveRoots(Iterable<Root> roots){
			this.roots = newArrayList(roots);
			return this;
		}

		public Builder setCheckParse(boolean checkParse) {
			this.checkParse = checkParse;
			return this;
		}

		public Builder setResolveBindings(boolean resolveBindings) {
			this.resolveBindings = resolveBindings;
			return this;
		}
		
		public Builder setRecordModifications(boolean recordModifications) {
			this.recordModifications = recordModifications;
			return this;
		}

		public Builder setSourceLevel(String sourceLevel) {
			setCompilerOption(JavaCore.COMPILER_SOURCE, sourceLevel);
			return this;
		}
		
		public Builder setCompilerOption(String name, String value) {
			options.put(name, value);
			return this;
		}
		
		public Builder setCompilerOptions(Map<String,String> options) {
			options.putAll(options);
			return this;
		}
	}
}
