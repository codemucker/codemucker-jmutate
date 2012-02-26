package com.bertvanbrakel.codemucker.refactor.a;

import java.util.Collection;

import com.bertvanbrakel.codemucker.annotation.GenerateBean;
import com.bertvanbrakel.codemucker.annotation.GenerateBuilder;
import com.bertvanbrakel.codemucker.annotation.Pattern;
import com.bertvanbrakel.codemucker.annotation.PatternType;

@GenerateBean
@GenerateBuilder(ctor=true)
public class TstBean {

	private String myFeildString;
	private int myFieldInt;
	private boolean myFieldBool;
	private Collection<String> myFieldCol;
	
	
}
