/*
 * Copyright 2011 Bert van Brakel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codemucker.jmutate.generate.matcher;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codemucker.jmatch.ObjectMatcher;
import org.codemucker.jmutate.ClashStrategy;
import org.codemucker.jpattern.DefaultGenerator;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
@DefaultGenerator("org.codemucker.jmutate.generate.matcher.MatcherGenerator")
public @interface GenerateMatchers {

    /**
     * If enabled keep this pattern in sync with changes after the initial
     * generation. Defaults to true.
     */
    boolean keepInSync() default true;
    
    /**
     * The package to generate the matchers in. If empty use the same package as the pojo one
     */
    String generateToPackage() default "";
    
    /**
     * The class to extend the matchers from. Default is empty to use the default.
     */
    Class<?> matcherBaseClass() default ObjectMatcher.class;

    /**
     * The matcher prefix to add to the class found. Default is empty to use the auto generated one (which either prepends 'A' or 'An')
     */
    String matcherPrefix() default "";

    /**
     * The static builder create methods to add. The 'with' as in 'AFoo.with()...' is always added. This provides additional methods to add. By default add nothing more.
     * @return
     */
    String[] builderMethodNames() default {};

    /**
     * If set filter the dependencies scanned for pojos
     */
    String pojoDependencies() default "";

    String pojoTypes() default "PublicConcreteClass";
    
    /**
     * The pattern for finding the pojos. By default matches everything (in the packages and dependencies set to be scanned). Logical ant expression pattern matching.
     */
    String pojoNames() default "!(java.* || javax.* || *Abstract*)";
    
    /**
     * If true also scan source files (not just compiled dependencies). Default is true.
     */
    boolean scanSources() default true;
    
    /**
     * If true also scan binary dependencies for pojos to generate matchers for. Default is true.
     */
    boolean scanDependencies() default true;
    
    boolean debugShowMatches() default false;
    boolean debugShowIgnores() default false;
    
    ClashStrategy clashStrategy() default ClashStrategy.SKIP;
    
}
