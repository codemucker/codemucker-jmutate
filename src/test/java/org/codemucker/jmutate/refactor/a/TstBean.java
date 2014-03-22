package org.codemucker.jmutate.refactor.a;

import java.util.Collection;

import org.codemucker.jpattern.GenerateBean;
import org.codemucker.jpattern.GenerateBuilder;


@GenerateBean
@GenerateBuilder(ctor=true)
public class TstBean {

	private String myFeildString;
	private int myFieldInt;
	private boolean myFieldBool;
	private Collection<String> myFieldCol;
	
	
}
