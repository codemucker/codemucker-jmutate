package org.codemucker.jmutate.bean;


import com.bertvanbrakel.lang.MapBuilder;
import com.bertvanbrakel.lang.annotation.NotThreadSafe;
import com.bertvanbrakel.lang.interpolator.Interpolator;

@NotThreadSafe
@Deprecated
public abstract class AbstractClassWriter {
	
	private StringBuilder sb = new StringBuilder();
	
	//private JTypeMutator mutator;
	
	protected void addImport(Class<?> type){
		println( "import " + type.getName() + ";" );
		//getMutator().newImport(type.getName());
	}
	
	protected void addStaticImport(Class<?> type, String method){
		println( "import static " + type.getName() + "." + method + ";" );
		//getMutator().newImport(type.getName()).setStatic(true);
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
	
//	protected JTypeMutator getMutator(){
//		return mutator;
//	}
	
	public String toJavaClassString(){
		return sb.toString();
	}
	
	public abstract MapBuilder<String, Object> map();
	
	protected static MapBuilder<String, Object> emptyMap(){
		return new MapBuilder<String, Object>();
	}
	
}