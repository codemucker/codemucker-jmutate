package com.bertvanbrakel.codemucker.ast;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static junit.framework.Assert.fail;

import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.bertvanbrakel.lang.annotation.NotThreadSafe;
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
	private final ASTParser parser;
	private final boolean checkParse;
	private final boolean recordModifications;
	private final Map<?,?> options;
	
	public static Builder newBuilder(){
		return new Builder();
	}
	
	private JAstParser(ASTParser parser, boolean checkParse, boolean recordModifications, Map<Object,Object> options) {
	    super();
	    this.parser = checkNotNull(parser,"expect parser");
	    this.checkParse = checkParse;
	    this.recordModifications = recordModifications;
	    this.options = checkNotNull(options,"expect parser options");
    }

	/**
	 * Parse the given source as a compilation unit
	 * 
	 * @param src
	 * @return
	 */
	public CompilationUnit parseCompilationUnit(CharSequence src) {
		CompilationUnit cu = (CompilationUnit) parseNode(src, ASTParser.K_COMPILATION_UNIT);
		if (checkParse){
			IProblem[] problems = cu.getProblems();
			if (problems.length > 0) {
				String problemString = Joiner.on("\n").join(Lists.transform(newArrayList(problems), problemToStringFunc()));
				fail(String.format("Parsing error for source %s, problems are %s", prependLineNumbers(src), problemString));
			}
		}
		if (recordModifications) {
			cu.recordModifications();
		}

		return cu;
	}
	
	/**
	 * Converts an {@link IProblem} into a human readable string
	 */
	private static Function<IProblem,String> problemToStringFunc(){
		return new Function<IProblem,String>(){
			@Override
            public String apply(IProblem problem) {
				return Objects.toStringHelper("Problem")
					.add("msg", problem.getMessage())
					.add("line", problem.getSourceLineNumber())
//					.add( "id", problem.getID() )
					.toString();
            }
		};
	}
	
	/**
	 * Prepend the line number to each line. Lines are delimited by '\n' or '\n\r'
	 */
	private String prependLineNumbers(CharSequence src){
		StringBuilder sb = new StringBuilder();
		Iterable<String> lines = Splitter.on(Pattern.compile("\n\r|\n")).split(src);
		int lineNum = 1;
		for( String line:lines){
			if( lineNum > 0){
				sb.append('\n');
			}
			sb.append('[');
			sb.append(Strings.padEnd(Integer.toString(lineNum), 4, ' '));
			sb.append(']');
			
			sb.append(line);
			lineNum++;
		}
		return sb.toString();
	}

	public ASTNode parseNode(CharSequence src, int kind) {
		parser.setCompilerOptions(options);
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
		return newBuilder().build();
	}

	public static ASTParser newDefaultParser() {
		return newBuilder().build().parser;
	}
	
	public static class Builder {
		private ASTParser parser;
		private boolean checkParse = true;
		private boolean recordModifications = true;

		@SuppressWarnings("unchecked")
        private Map<Object,Object> options = newHashMap(JavaCore.getOptions());
		
		public Builder(){
			// In order to parse 1.5 code, some compiler options need to be set
			// to atleast 1.5
			setSourceLevel(JavaCore.VERSION_1_6);
		}
		
		public Builder setParser(ASTParser parser) {
        	this.parser = parser;
        	return this;
        }
		
		public JAstParser build(){
			return new JAstParser(
				toParser()
				, checkParse
				, recordModifications
				, newHashMap(options));
		}
		
		private ASTParser toParser() {
			return parser == null ? newDefaultAstParser() : parser;
		}
		
		private ASTParser newDefaultAstParser(){
			return ASTParser.newParser(AST.JLS3);
		}

		public Builder setCheckParse(boolean checkParse) {
			this.checkParse = checkParse;
			return this;
		}

		public Builder setRecordModifications(boolean recordModifications) {
			this.recordModifications = recordModifications;
			return this;
		}

		public Builder setSourceLevel(String sourceLevel) {
			JavaCore.setComplianceOptions(sourceLevel, options);
			return this;
		}	
	}
}
