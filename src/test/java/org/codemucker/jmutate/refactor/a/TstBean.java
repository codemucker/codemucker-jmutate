package org.codemucker.jmutate.refactor.a;

import java.util.Collection;

import org.codemucker.jpattern.TransferBean;
import org.codemucker.jpattern.GenerateBuilder;


@TransferBean
@GenerateBuilder(ctor=true)
public class TstBean {

	private String myFeildString;
	private int myFieldInt;
	private boolean myFieldBool;
	private Collection<String> myFieldCol;
	
	
}
