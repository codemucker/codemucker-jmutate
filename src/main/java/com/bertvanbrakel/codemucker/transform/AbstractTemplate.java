package com.bertvanbrakel.codemucker.transform;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import com.bertvanbrakel.lang.annotation.NotThreadSafe;
import com.bertvanbrakel.lang.interpolator.Interpolator;
import com.google.common.base.Objects;

@NotThreadSafe
public abstract class AbstractTemplate<S extends AbstractTemplate<S>> implements Template {
	private Map<String,Object> vars = newHashMap();
	private StringBuilder buffer = new StringBuilder();

	@SuppressWarnings("unchecked")
    private S self(){
		return(S)this;
	}

	public S setVars(Map<String,?> vars){
		this.vars.clear();
		this.vars.putAll(vars);
		return self();
	}
	
	public S v(String name,Object val){
		setVar(name,val);
		return self();
	}
	
	public S setVar(String name,Object val){
		this.vars.put(name,val);
		return self();
	}
	
	public S setTemplate(CharSequence template){
		//or throw NPE?
		this.buffer.setLength(0);
		if( template != null){
			this.buffer.append(template);
		}
		return self();
	}

	public S pl(char c){
		println(c);
		return self();
	}
	
	public S println(char c){
		print(c);
		println();
		return self();
	}	
	

	public S pl(CharSequence template){
		println(template);
		return self();
	}
	
	public S println(CharSequence template){
		print(template);
		println();
		return self();
	}	

	public S nl(){
		println();
		return self();
	}
	
	public S println(){
		this.buffer.append("\n");
		return self();
	}
	
	public S p(char c){
		print(c);
		return self();
	}
	
	public S p(CharSequence c){
		print(c);
		return self();
	}
	
	public S print(char c){
		this.buffer.append(c);
		return self();
	}	
	
	public S print(CharSequence template){
		this.buffer.append(template);
		return self();
	}	

	@Override
	public CharSequence interpolate(){
		return Interpolator.interpolate(buffer, vars);
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
			.add("template", buffer)
			.add("vars", vars)
			.toString();
	}	
}