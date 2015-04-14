package org.codemucker.jmutate.generate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jfind.RootResource;
import org.codemucker.jfind.matcher.AnAnnotation;
import org.codemucker.jmatch.Logical;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.ResourceLoader;
import org.codemucker.jmutate.ast.JAnnotation;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.JSourceFile;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.ast.matcher.AJAnnotation;
import org.codemucker.jmutate.ast.matcher.AJType;
import org.codemucker.jmutate.util.NameUtil;
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
	
	private final ResourceLoader resourceLoader;
	private final JAstParser parser;
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
	public ConfigExtractor(ResourceLoader resourceLoader,JAstParser parser) {
		super();
		this.resourceLoader = resourceLoader;
		this.parser = parser;
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
		     ASTNode attachedToNode = getAttachedNodeFor(sourceAnnotation);
		     //TODO:only non template nodes!
		     if(JType.is(attachedToNode)){
		    	 if(!JType.from(attachedToNode).getAnnotations().contains(SOURCE_IS_TEMPLATE_OR_CONFIG_ANNOTATION)){
		    		 nodes.add(attachedToNode);
		    	 }
		     } else {
		    	 nodes.add(attachedToNode);
		     }
		     extractOptions(attachedToNode, sourceAnnotation);
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
    
    /**
     * If the provided annotation is a generator annotation (else a normal annotation which got caught up in the scan)
     */
    private void extractOptions(ASTNode node, Annotation annotationDeclaration){
		String annotationName = JAnnotation.from(annotationDeclaration).getQualifiedName();
		//skip if we've already rules out this annotation type
		if(isIgnoreAnnotation(annotationName)){
			return;
		}
		SmartConfig sc = SmartConfig.get(node);
		if(sc == null){
			sc = new SmartConfig();
			SmartConfig.set(node, sc);
		}
		//direct settings for this node
		sc.addNodeConfigFor(annotationName,new AnnotationConfiguration(annotationDeclaration));
		
		//use the templates options if it's already been loaded
		SmartConfig templateConfig = templateOptions.get(annotationName);
		if(templateConfig != null){
			sc.addParentConfigsFrom(templateConfig);
			return;
		}
		
		//haven't come across this annotation yet, let's see if we can load it and decide whether it's a standard generation annotation, or a template
		// look up the source first. This may have changed and not yet compiled
		// (freshest)
		//load source code declaration of annotation

		if(extractConfiguration(sc, loadTypeForOrNull(annotationName))){
			return;
		}

		// try the compiled version
		if(extractConfiguration(sc, (Class<java.lang.annotation.Annotation>) resourceLoader.loadClassOrNull(annotationName))){
			return;
		}

		LOG.warn("Can't load annotation as class or resource '" + annotationName + "'. Ignoring as a generator");
    }

	private boolean extractConfiguration(SmartConfig sc, JType annotation) {
		if(annotation == null){
			return false;
		}
		String anonName = annotation.getFullName();
		
		if(isIgnoreAnnotation(anonName)){
			return true;
		}
		for(JAnnotation markedWithAnnotation:annotation.getAnnotations().getAllDirect()){
			String markedWithName = markedWithAnnotation.getQualifiedName();
			if(isIgnoreAnnotation(markedWithName)){
				continue;
			}
			if(generateAnnotations.contains(markedWithName)){
				sc.addNodeConfigFor(markedWithName, new AnnotationConfiguration(markedWithAnnotation));
				continue;
			}
			if(AST_CONTAINS_TEMPLATE_ANNOTATION.matches(markedWithAnnotation)){					
				SmartConfig templateConfig = templateOptions.get(markedWithName);
				if(templateConfig == null){
					templateConfig = new SmartConfig();
					JType templateClass = loadTypeForOrNull(markedWithName);
					if(!extractConfiguration(templateConfig,templateClass)){
						Class<java.lang.annotation.Annotation> annoationClass = (Class<java.lang.annotation.Annotation>) resourceLoader.loadClassOrNull(markedWithName);
						
						extractConfiguration(sc, annoationClass);
					}
				}
				sc.addParentConfigsFrom(templateConfig);
			} else if(AST_CONTAINS_GENERATOR_CONFIG_ANNOTATION.matches(markedWithAnnotation)){
				sc.addNodeConfigFor(markedWithName, new AnnotationConfiguration(markedWithAnnotation));
				generateAnnotations.add(markedWithName);
			} else {//ensure we ignore so we don't try to scan again
				normalAnnotationsToIgnore.add(markedWithName);
			}
		}
		return true;
	}
	
	private boolean extractConfiguration(SmartConfig sc,Class<java.lang.annotation.Annotation> annotationClass) {
		if(annotationClass==null){
			return false;
		}
		if(!isIgnoreAnnotation(NameUtil.compiledNameToSourceName(annotationClass))){
			for(java.lang.annotation.Annotation a:annotationClass.getDeclaredAnnotations()){
				extractConfiguration(sc, a);
			}
		}
		return true;
	}
	
	private void extractConfiguration(SmartConfig sc,java.lang.annotation.Annotation annotation) {
		String anonName= NameUtil.compiledNameToSourceName(annotation.annotationType().getName());
		
		if(isIgnoreAnnotation(anonName)){
			return;
		}
		if(generateAnnotations.contains(anonName)){
			sc.addNodeConfigFor(anonName, new AnnotationConfiguration(annotation));
			return;
		}
		if (annotation.annotationType().isAnnotationPresent(IsGeneratorTemplate.class)) {
			SmartConfig templateConfig = templateOptions.get(anonName);
			if(templateConfig == null){
				templateConfig = new SmartConfig();
				//rip off all the annotations from this template
				extractConfiguration(templateConfig,(Class<java.lang.annotation.Annotation>)annotation.getClass());
				templateOptions.put(anonName,templateConfig);
			}
			sc.addParentConfigsFrom(templateConfig);
		} else if (annotation.annotationType().isAnnotationPresent(IsGeneratorConfig.class)) {
			sc.addNodeConfigFor(anonName,new AnnotationConfiguration(annotation));
			generateAnnotations.add(anonName);
		} else {
			normalAnnotationsToIgnore.add(anonName);
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
	
	private JType loadTypeForOrNull(String fullName){
		JType type = null;
		RootResource resource = resourceLoader.getResourceOrNullFromClassName(fullName);
		if (resource != null && resource.exists()) {
			JSourceFile source = JSourceFile.fromResource(resource,parser);
			type = source.findTypesMatching(AJType.with().fullName(fullName)).getFirstOrNull();		
			if(type == null){
				LOG.warn("Can't find type '" + fullName + "' in " + source.getResource().getFullPath());
			}
		}
		return type;
	}

}