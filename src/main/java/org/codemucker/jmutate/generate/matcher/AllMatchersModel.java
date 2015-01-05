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
    public final GenerateMatchers options;
    public final String defaultPackage;

    final List<MatcherModel> matchers = new ArrayList<>();
    
    public AllMatchersModel(JType declaredInNode, GenerateMatchers options) {
        this.options = options;
        this.defaultPackage = Strings.emptyToNull(options.generateToPackage());
    }

    void addMatcher(MatcherModel model){
        this.matchers.add(model);
    }
    
}