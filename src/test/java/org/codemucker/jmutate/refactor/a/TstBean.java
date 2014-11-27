package org.codemucker.jmutate.refactor.a;

import java.util.Collection;

import org.codemucker.jpattern.bean.TransferBean;
import org.codemucker.jpattern.builder.GenerateBuilder;


@TransferBean
@GenerateBuilder(generateNoArgCtor=true)
public class TstBean {

	private String myFeildString;
	private int myFieldInt;
	private boolean myFieldBool;
	private Collection<String> myFieldCol;
	
	
}
