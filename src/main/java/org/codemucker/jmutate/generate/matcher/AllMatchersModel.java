package org.codemucker.jmutate.generate.matcher;

import java.util.ArrayList;
import java.util.List;

import org.codemucker.jmutate.ast.JType;
import org.codemucker.jpattern.generate.GenerateMatchers;

import com.google.common.base.Strings;

/**
 * Global details about what is being generated
 */
public class AllMatchersModel {
    private final GenerateMatchers options;
    private final String defaultPackage;

    private final List<MatcherModel> matchers = new ArrayList<>();
    
    public AllMatchersModel(JType declaredInNode, GenerateMatchers options) {
        this.options = options;
        this.defaultPackage = Strings.emptyToNull(options.generateToPackage());
    }

    void addMatcher(MatcherModel model){
        this.matchers.add(model);
    }

	public GenerateMatchers getOptions() {
		return options;
	}

	public String getDefaultPackage() {
		return defaultPackage;
	}

	List<MatcherModel> getMatchers() {
		return matchers;
	}
    
}