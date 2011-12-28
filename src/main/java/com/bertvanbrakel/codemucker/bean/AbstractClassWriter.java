package com.bertvanbrakel.codemucker.bean;


import com.bertvanbrakel.codemucker.ast.JContext;
import com.bertvanbrakel.codemucker.ast.JTypeMutator;
import com.bertvanbrakel.codemucker.util.SourceUtil;
import com.bertvanbrakel.lang.MapBuilder;
import com.bertvanbrakel.lang.annotation.NotThreadSafe;
import com.bertvanbrakel.lang.interpolator.Interpolator;

@NotThreadSafe
public abstract class AbstractClassWriter {
	
	private StringBuilder sb = new StringBuilder();
	
	private JTypeMutator mutator;

//	AbstractClassWriter(JContext context,String className){
//		context.getAstCreator().parseCompilationUnit(String.format("public class %s{}", className));
//	}
//	
	protected void addImport(Class<?> type){
		println( "import " + type.getName() + ";" );
		this.mutator.newImport(type.getName());
	}
	
	protected void addStaticImport(Class<?> type, String method){
		println( "import static " + type.getName() + "." + method + ";" );
		this.mutator.newImport(type.getName()).setStatic(true);
	}

	public void println(String s){
		println(s, map());
	}
	
	public void println(String s, MapBuilder<String, Object> map){
		CharSequence out = Interpolator.interpolate(s, map.create());
		append(out);
	}
	
	private void append(CharSequence s){
		sb.append(s);
		sb.append("\n");
	}
	
	protected JTypeMutator getMutator(){
		return mutator;
	}
	
	public String toJavaClassString(){
		return sb.toString();
	}
	
	public abstract MapBuilder<String, Object> map();
	
	protected static MapBuilder<String, Object> emptyMap(){
		return new MapBuilder<String, Object>();
	}
	
}