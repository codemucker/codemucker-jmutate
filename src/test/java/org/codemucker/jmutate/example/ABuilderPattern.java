package org.codemucker.jmutate.example;

import java.util.ArrayList;
import java.util.List;

import org.codemucker.jfind.FindResult;
import org.codemucker.jmatch.AString;
import org.codemucker.jmatch.AbstractMatcher;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.ast.JMethod;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJMethodNode;
import org.codemucker.jmutate.ast.matcher.AType;
/**
 * Beginnings of pattern enforcement. More cleanup required
 */
class ABuilderPattern extends AbstractMatcher<JType>{

    private Matcher<JMethod> builderMethods;
    private Matcher<JMethod> disallowedBuilderMethods = AJMethodNode.none();
    private final String builderStaticCreateMethod = "with";
    private boolean requireStaticBuilderFactoryMethodOnParent = true;
    private final List<Matcher<JMethod>> ignoreMethods = new ArrayList<>();
    
    public static ABuilderPattern with(){
        return new ABuilderPattern();                
    }

    @Override
    protected boolean matchesSafely(JType builderType, MatchDiagnostics diag) {
        if(builderMethods==null){
            builderMethods = Logical.not(Logical.any(ignoreMethods));
        }
        ABuilderPattern.MatchHolder holder = new MatchHolder(builderType);
        
        boolean passed = validateBuilderClass(holder,diag);
        passed = passed && validateMethods(holder,diag);
        return passed;
    }
    
    private boolean validateBuilderClass(ABuilderPattern.MatchHolder holder,MatchDiagnostics diag){
        if(holder.builderType.isConcreteClass()){//don't expect build methods on abstract builders
            FindResult<JMethod> buildMethod = holder.builderType.findMethodsMatching(AJMethodNode.with().nameMatchingAntPattern("build*").returningSomething());
            
            if(buildMethod.isEmpty()){
                diag.mismatched("expect to find a build*() method on " + holder.builderType.getFullName());
                return false;
            }
            if(requireStaticBuilderFactoryMethodOnParent){
                //ensure has static 'with' on enclosing class
                JType parent;
                if(holder.builderType.isInnerClass()){
                    parent = holder.builderType.getParentJType();
                } else {
                    parent = holder.builderType;
                }
                if(parent != null && !parent.hasMethodsMatching(AJMethodNode.with().name(builderStaticCreateMethod).numArgs(0).isStatic().returning(AType.that().isEqualTo(holder.builderType)))){
                    Description desc = diag.newDescription();
                
                    desc.text("expect to find a static builder factory method");
                    desc.text("expect 'public static %s %s(){...}'",holder.builderType.getFullName(),builderStaticCreateMethod);
                    //desc.value("to return:",holder.builderType.getFullName());
                    
                    desc.value("in builder's enclosing class:", parent.getFullName());
                    desc.value("for builder class:", holder.builderType.getFullName());
                    desc.text("but no method found");
                    
                    diag.mismatched(desc);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateMethods(ABuilderPattern.MatchHolder holder,MatchDiagnostics diag) {
        FindResult<JMethod> foundBuilderSetters = holder.builderType.findMethodsMatching(builderMethods);
        boolean passed = true;
        for(JMethod builderSetterMethod : foundBuilderSetters){
           MatchDiagnostics child = diag.newChild();
            if(child.tryMatch(this, builderSetterMethod, disallowedBuilderMethods)){
                diag.mismatched("builder method is dissallowed as it matches disallowed method");
                diag.matched(child);
                passed = false;
            } else {
                passed = passed && validateMethod(holder,builderSetterMethod,diag);            
            }
        }
        return passed;
    }
    
    private boolean validateMethod(ABuilderPattern.MatchHolder holder,JMethod builderMethod,MatchDiagnostics diag){
        //ensure each builder method return type is the builder. Slightly tricky in that the builder could return 'Self' which could
        //be a generic type so that it can be sub classed
        String returnType = builderMethod.getReturnTypeFullName();
        boolean returnsBuilderType = returnType.equals(holder.builderTypeFullName) || (holder.builderSelfTypeName != null && holder.builderSelfTypeName.equals(returnType));
        if (!returnsBuilderType){
            Description desc = diag.newDescription();
            if(!desc.isNull()){ 
                desc.text("expect builder method to return the builder");
                desc.value("expect return type:",holder.builderTypeFullName);
                desc.value("got return type:",builderMethod.getAstNode().getReturnType2().toString());
                desc.value("builder class:",builderMethod.getEnclosingJType().getFullName());
                desc.value("method:",builderMethod.getFullSignature());
                desc.value("method body:",builderMethod.getAstNode());
                desc.value("enclosing class:",builderMethod.getEnclosingType());
                
                diag.mismatched(desc);
            }
            return false;
        }
        return true;
    }
    
    public ABuilderPattern defaults(){
        ignoreMethods.add(AJMethodNode.that().isConstructor());
        ignoreMethods.add(AJMethodNode.that().isNotPublic());
        ignoreMethods.add(AJMethodNode.with().name(AString.matchingAntPattern("get*")).returningSomething());
        ignoreMethods.add(AJMethodNode.with().name(AString.matchingAntPattern("build*")).returningSomething());
        
        ignoreMethods.add(AJMethodNode.with().name(AString.matchingAntPattern("is??*")).numArgs(0).returning(AType.BOOL_PRIMITIVE));
        ignoreMethods.add(AJMethodNode.with().name(AString.matchingAnyAntPattern("set*","with?*")));
        ignoreMethods.add(AJMethodNode.with().name(AString.equalTo("with")).isNotStatic());
        
        return this;
    }
    
    public ABuilderPattern ignoreMethod(Matcher<JMethod> matcher){
        ignoreMethods.add(matcher);                
        builderMethods = null;
        return this;
    }
    
    public ABuilderPattern builderMethods(Matcher<JMethod> builderMethods) {
        this.builderMethods = builderMethods;
        return this;
    }

    public ABuilderPattern disallowedBuilderMethods(Matcher<JMethod> disallowedBuilderMethods) {
        this.disallowedBuilderMethods = disallowedBuilderMethods;
        return this;
    }
    
    @Override
    public void describeTo(org.codemucker.jmatch.Description desc) {
        if(requireStaticBuilderFactoryMethodOnParent){
            desc.text("static builder create method on parent named '%s()'", builderStaticCreateMethod);
        }
        desc.child("builderMethods", builderMethods);
        desc.child("dissallowedBuilderMethods", disallowedBuilderMethods);
    };

    /**
     * Holds some calculated fields while matching a single builder so we don't have to continuously recalc them.
     */
    private static class MatchHolder {
        private final JType builderType;
        private final String builderTypeName;
        private final String builderTypeFullName;
        private final String builderSelfTypeName;
        
        private MatchHolder(JType builderType){
            this.builderType = builderType;
            this.builderTypeName = builderType.getSimpleName();
            this.builderTypeFullName = builderType.getFullName();
            this.builderSelfTypeName = builderType.getSelfTypeGenericParam();
        }
    }

}