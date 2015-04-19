package org.codemucker.jmutate.generate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.SourceLoader;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJAnnotation;
import org.codemucker.jpattern.generate.IsGeneratorConfig;
import org.codemucker.jpattern.generate.IsGeneratorTemplate;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.inject.Inject;

class ConfigExtractor {

	private static final Logger LOG = LogManager.getLogger(ConfigExtractor.class);
	
	private final SourceLoader sourceLoader;
	
	/**
	 * Used to prevent constant scanning of already found template nodes
	 */
	private final Map<String, SmartConfig> templateOptions = new HashMap<>();
	/**
	 * Used to prevent constant scanning of already found annotations
	 */
	private final Set<String> normalAnnotationsToIgnore = new HashSet<>();
	private final Set<String> generateAnnotations = new HashSet<>();
	
	private final Matcher<JAnnotation> SOURCE_IS_TEMPLATE_OR_CONFIG_ANNOTATION = Logical.any(
			AJAnnotation.with().fullName(IsGeneratorTemplate.class),
			AJAnnotation.with().fullName(IsGeneratorConfig.class));
	
	private final Matcher<JAnnotation> AST_CONTAINS_TEMPLATE_ANNOTATION = AJAnnotation.with().annotationPresent(IsGeneratorTemplate.class);
	private final Matcher<JAnnotation> AST_CONTAINS_GENERATOR_CONFIG_ANNOTATION = AJAnnotation.with().annotationPresent(IsGeneratorConfig.class);
	
	@Inject
	public ConfigExtractor(SourceLoader sourceLoader) {
		super();
		this.sourceLoader = sourceLoader;
	}

	/**
     * Given a list of source annotations, find non template nodes and attach calculated config.
     * 
     * @param annotations
     * @return
     */
    public Set<ASTNode> processAnnotations(List<Annotation> annotations) {
        //collect all the annotations of a given type to be passed to a generator at once (and so we can apply generator ordering)        
    	Set<ASTNode> nodes = new HashSet<>();
		for(Annotation sourceAnnotation:annotations){
			JAnnotation janon = JAnnotation.from(sourceAnnotation);
			ASTNode node = getAttachedNodeFor(sourceAnnotation);
			
			extractIt(janon,getOrSetSmartConfig(node));
			
			//then find generators
			
			//remaining are real
		     ASTNode attachedToNode = getAttachedNodeFor(sourceAnnotation);
		     //TODO:only non template nodes!
		     if(JType.is(attachedToNode)){
		    	 if(!JType.from(attachedToNode).getAnnotations().contains(SOURCE_IS_TEMPLATE_OR_CONFIG_ANNOTATION)){
		    		 nodes.add(attachedToNode);
		    	 }
		     } else {
		    	 nodes.add(attachedToNode);
		     }
		//     extractOptions(attachedToNode, sourceAnnotation);
		}
        return nodes;
    }
    
    
    private ASTNode getAttachedNodeFor(Annotation annotation) {
        ASTNode parent = annotation.getParent();
        while (parent != null) {
            if (parent instanceof FieldDeclaration || parent instanceof MethodDeclaration || parent instanceof TypeDeclaration
                    || parent instanceof AnnotationTypeDeclaration || parent instanceof SingleVariableDeclaration) {
                return parent;
            }
            parent = parent.getParent();
        }
        throw new JMutateException("Currently can't figure out correct parent for annotation:" + annotation);
    }
    
    
    private SmartConfig getOrSetSmartConfig(ASTNode node){
    	SmartConfig sc = SmartConfig.get(node);
		if(sc == null){
			sc = new SmartConfig();
			SmartConfig.set(node, sc);
		}
		return sc;
    }
    
    private void extractTemplateOptions(JAnnotation templateAnnon, SmartConfig templateConfig){
    	
    	String templateFullName = templateAnnon.getFullName();
    	
    	JType templateType = sourceLoader.loadTypeForClass(templateFullName);
    	if(templateType != null){
    		for(JAnnotation a:templateType.getAnnotations().getAllDirect()){
    			extractIt(a,templateConfig);
    		}
    	} else {
    		//try class loader?
    	}
    }
    
    private void extractIt(JAnnotation janon,SmartConfig smart){
    	String fullName = janon.getFullName();
		if(isIgnoreAnnotation(fullName)){
			return;
		}
		//is a template and we already loaded it
		if(templateOptions.containsKey(fullName)){
			smart.addParentConfigsFrom(templateOptions.get(fullName));
			return;
		}
		//we know it's a magic generator annotation
		if(generateAnnotations.contains(fullName)){
			smart.addNodeConfigFor(fullName, new AnnotationConfiguration(janon));
			return;
		}
		//okay, haven't come across this annotation before
		
		
		//first find templates
		if(AST_CONTAINS_TEMPLATE_ANNOTATION.matches(janon)){
			//grab templates options
			SmartConfig templateConfig = new SmartConfig();
			templateOptions.put(fullName, templateConfig);
			extractTemplateOptions(janon,templateConfig);
			smart.addParentConfigsFrom(templateConfig);
			return;
		} else if(AST_CONTAINS_GENERATOR_CONFIG_ANNOTATION.matches(janon)){
			//add as is
			smart.addNodeConfigFor(fullName, new AnnotationConfiguration(janon));
			generateAnnotations.add(fullName);
			return;
		} else {
			//ignore
			normalAnnotationsToIgnore.add(fullName);
			return;
		}
    }
   
	private boolean isIgnoreAnnotation(String anonName){
		if(normalAnnotationsToIgnore.contains(anonName)){
			return true;
		}
		if(anonName.startsWith("java.") || anonName.startsWith("javax.")){
			normalAnnotationsToIgnore.add(anonName);
			return true;
		}
		return false;
	}

}