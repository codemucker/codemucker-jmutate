package org.codemucker.jmutate;

import java.io.File;
import java.io.IOException;

import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jfind.Roots;
import org.codemucker.jmutate.ast.ContextNames;
import org.codemucker.jmutate.ast.DefaultToSourceConverter;
import org.codemucker.jmutate.ast.JAstFlattener;
import org.codemucker.jmutate.ast.JAstParser;
import org.codemucker.jmutate.ast.ToSourceConverter;
import org.codemucker.jtest.MavenLayoutProjectResolver;
import org.codemucker.jtest.ProjectResolver;
import org.codemucker.lang.annotation.Optional;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.name.Named;

@Singleton
public class DefaultMutateContext implements JMutateContext {

    private final ProjectResolver projectResolver;
    private final ResourceLoader resourceLoader;
    
    private final PlacementStrategies strategyProvider;
	/**
	 * Where by default any generated code is output to
	 */
	private final Root generationRoot;
	private final JAstParser parser;
	/**
	 * If set, generated classes will be marked as such
	 */
	private final boolean markGenerated;
	
	//internally created
    private final Injector injector;
    private int tmpCount;
    
	public static Builder with(){
		return new Builder();
	}
	
	private DefaultMutateContext(ProjectResolver projectResolver, Root generationRoot, JAstParser parser, boolean markGenerated, DefaultCodeFormatterOptions formatterOptions,PlacementStrategies strategyProvider) {
        super();
        this.projectResolver = projectResolver;
        this.generationRoot = generationRoot;
        this.parser = parser;
        this.markGenerated = markGenerated;
        this.strategyProvider = strategyProvider;
        this.resourceLoader = DefaultResourceLoader.with().parentLoader(parser.getResourceLoader()).addRoot(generationRoot).build();
        
        injector = Guice.createInjector(Stage.PRODUCTION, new DefaultMutationModule(formatterOptions));
    }

	@Override
    public SourceTemplate newSourceTemplate(){
    	return obtain(SourceTemplate.class);
    }

	@Override
    public SourceTemplate newTempSourceTemplate(){
        return new SourceTemplate(getParser(), newTmpSnippetRoot());
    }
	
	 private Root newTmpSnippetRoot(){
         tmpCount++;
         File tmpDir = new File(projectResolver.getTmpDir(),"SnippetRoot" + tmpCount + "/" );
         if(!tmpDir.mkdirs()){
             throw new JMutateException("Couldn't create a tmp root '" + tmpDir.getAbsolutePath() + "'");     
         }
         if(!tmpDir.isDirectory()){
             throw new JMutateException("Couldn't create a tmp root '" + tmpDir.getAbsolutePath() + "', seems it's not a directory");     
         }
         return new DirectoryRoot(tmpDir,RootType.GENERATED,RootContentType.SRC);
     }
	
	@Override
    public JAstParser getParser() {
	    return obtain(JAstParser.class);
    }
 
	@Override
    public JCompiler getCompiler() {
        return obtain(JCompiler.class);
    }
	
	@Override
    public ToSourceConverter getNodeToSourceConverter() {
	    return obtain(ToSourceConverter.class);
	};
	
	@Override
	public Root getDefaultGenerationRoot() {
	    return generationRoot;	    
	};
	
	@Override
	public ResourceLoader getResourceLoader() {
	    return resourceLoader;
	}
	
	@Override
	public <T> T obtain(Class<T> type){
		return injector.getInstance(type);
	}
	
    private class DefaultMutationModule extends AbstractModule {

        private final DefaultCodeFormatterOptions options;

        public DefaultMutationModule(DefaultCodeFormatterOptions options) {
            this.options = options;
        }

        @Override
        protected void configure() {
            bind(JCompiler.class).to(DefaultCompiler.class).in(Singleton.class);;
        }

        @Provides
        public CodeFormatter provideCodeFormatter() {
            return new DefaultCodeFormatter(getFormattingOptions());
        }

        private DefaultCodeFormatterOptions getFormattingOptions() {
            return options;
        }

        @Provides
        public ClashStrategy provideDefaultClashStrategy() {
            return ClashStrategy.ERROR;
        }

        @Named(ContextNames.MARK_GENERATED)
        @Provides
        public boolean provideDefaultMarkGenerated() {
            return markGenerated;
        }

        @Provides
        public ToSourceConverter provideFlattener() {
            return obtain(DefaultToSourceConverter.class);
        }
        
        @Provides
        public JAstFlattener provideToSourceVisitor() {
            return obtain(JAstFlattener.class);
        }

        @Provides
        @Singleton
        public JMutateContext provideContext() {
            return DefaultMutateContext.this;
        }

        @Provides
        @Singleton
        public PlacementStrategies provideDefaultStrategies() {
            return strategyProvider;
        }

        @Provides
        @Singleton
        @Named(ContextNames.FIELD)
        public PlacementStrategy provideDefaultFieldPlacement() {
            return strategyProvider.getFieldStrategy();
        }

        @Provides
        @Singleton
        @Named(ContextNames.CTOR)
        public PlacementStrategy provideDefaultCtorPlacement() {
            return strategyProvider.getCtorStrategy();
        }

        @Provides
        @Singleton
        @Named(ContextNames.METHOD)
        public PlacementStrategy provideDefaultMethodPlacement() {
            return strategyProvider.getMethodStrategy();
        }

        @Provides
        @Singleton
        @Named(ContextNames.TYPE)
        public PlacementStrategy provideDefaultTypePlacement() {
            return strategyProvider.getTypeStrategy();
        }

        @Provides
        @Singleton
        public JAstParser provideParser() {
            return parser;
        }

        @Provides
        public SourceTemplate provideSourceTemplate() {
            return new SourceTemplate(provideParser(), generationRoot);
        }
        
        @Provides
        @Singleton
        public ResourceLoader provideResourceLoader() {
            return resourceLoader;
        }
        
        @Provides
        @Singleton
        public ProjectResolver provideProjectResolver() {
            return projectResolver;
        }
    }
	
	public static class Builder {
	
		private boolean markGenerated = false;
		private JAstParser parser;
		private DefaultCodeFormatterOptions formattingOptions;
		private Root generateRoot;
		private ProjectResolver projectResolver;
        
		private PlacementStrategies placementStrategy;
		
		private Builder(){
		}

		public Builder defaults() {
        	return this;
		}

        public DefaultMutateContext build() {
            ProjectResolver project = getProjectResolverOrDefault();
            Root generateTo = getGenerationRootOrDefault(project);
            JAstParser parser = getParserOrDefault(project, generateTo);
            DefaultCodeFormatterOptions formatter = getFormatterOptionsOrDefault();
            PlacementStrategies strategy = getPlacementStrategyOrDefault();

            return new DefaultMutateContext(project, generateTo, parser, markGenerated, formatter,strategy);
        }
		
        private JAstParser getParserOrDefault(ProjectResolver project,Root contextRoot) {
            return parser == null ? newDefaultAstParser(project,contextRoot) : parser;
        }

		private JAstParser newDefaultAstParser(ProjectResolver project,Root contextGenerationRoot){
            return JAstParser.with()
                    .defaults()
                    .roots(Roots.with()
                        .projectResolver(project)
                        .classpath(true)
                        .testSrcDir(true)
                        .mainSrcDir(true)
                        .root(contextGenerationRoot)
                        .build()    
                    )
                    .build();
        }
		
		private DefaultCodeFormatterOptions getFormatterOptionsOrDefault(){
		    return formattingOptions==null?newDefaultFormatter():formattingOptions;
		}
		
		private DefaultCodeFormatterOptions newDefaultFormatter(){
            return new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getJavaConventionsSettings());
        }
		
		private Root getGenerationRootOrDefault(ProjectResolver project){
		    return generateRoot==null?newTmpSnippetRoot(project):generateRoot;
		}
		
	    private Root newTmpSnippetRoot(ProjectResolver project){
	            File tmpDir = new File(project.getTmpDir(),"TmpGenRoot");
	            if(!tmpDir.mkdirs()){
	                throw new JMutateException("Couldn't generate mutate context tmp generation root '" + tmpDir.getAbsolutePath() + "'");
	            }
	            
	            return new DirectoryRoot(tmpDir,RootType.GENERATED,RootContentType.SRC);
	    }
	    
	    private PlacementStrategies getPlacementStrategyOrDefault(){
	        return placementStrategy==null?newDefaultPlacementStrategy():placementStrategy;
	    }
	    
	    private PlacementStrategies newDefaultPlacementStrategy(){
            return PlacementStrategies.with().defaults().build();
        }
        
	    private ProjectResolver getProjectResolverOrDefault(){
	        return projectResolver==null?newDefaultProjectResolver():projectResolver;
	    }
	    
	    private ProjectResolver newDefaultProjectResolver(){
            return new MavenLayoutProjectResolver();
        }
        
	    @Optional
		public Builder markGenerated(boolean markGenerated) {
        	this.markGenerated = markGenerated;
        	return this;
		}

		@Optional
        public Builder parser(JAstParser parser) {
            this.parser = parser;
            return this;
        }

        @Optional
        public Builder formattingOptions(DefaultCodeFormatterOptions formattingOptions) {
            this.formattingOptions = formattingOptions;
            return this;
        }

        @Optional
        public Builder root(Root root) {
            this.generateRoot = root;
            return this;
        }

        @Optional
        public Builder placementStrategy(PlacementStrategies strategy) {
            this.placementStrategy = strategy;
            return this;
        }
        
        @Optional
        public Builder projectResolver(ProjectResolver project) {
            this.projectResolver = project;
            return this;
        }
	}

}
