package com.bertvanbrakel.codemucker.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.bertvanbrakel.codemucker.transform.Template;

//use SOurceTemplate instead, or StringTemplate

@Deprecated
public class SrcWriter extends PrintWriter implements Template {
	
	private final StringWriter sw;
	
	public SrcWriter(){
		this(new StringWriter());
	}
	
	private SrcWriter(StringWriter sw){
		super(sw);
		this.sw = sw;
	}
	
	@Override
    public CharSequence interpolate() {
	    return sw.toString();
    }
	
	public String getSource(){
		return sw.toString();
	}
}