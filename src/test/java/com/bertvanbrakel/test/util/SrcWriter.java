package com.bertvanbrakel.test.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SrcWriter extends PrintWriter {
	
	private final StringWriter sw;
	
	public SrcWriter(){
		this(new StringWriter());
	}
	
	private SrcWriter(StringWriter sw){
		super(sw);
		this.sw = sw;
	}
	
	public String getSource(){
		return sw.toString();
	}
	
}