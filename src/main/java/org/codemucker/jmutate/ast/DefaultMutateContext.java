package org.codemucker.jmutate.ast;

import java.io.File;
import java.io.IOException;

import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jfind.Roots;
import org.codemucker.jmutate.ClashStrategy;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.JMutateException;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.PlacementStrategies;
import org.codemucker.jmutate.PlacementStrategy;
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

	private final PlacementStrategies strategyProvider;
	private final Root snippetRoot;
	private final JAstParser parser;
	/**
	 * If set, generated classes will be marked as such
	 */
	private final boolean markGenerated;
	private final Injector injector;
	
	public static Builder with(){
		return new Builder();
	}
	
	private DefaultMutateContext(Root snippetRoot, JAstParser parser, boolean markGenerated, DefaultCodeFormatterOptions formatterOptions,PlacementStrategies strategyProvider) {
        super();
        this.snippetRoot = snippetRoot;
        this.parser = parser;
        this.markGenerated = markGenerated;
        this.strategyProvider = strategyProvider;
        injector = Guice.createInjector(Stage.PRODUCTION, new DefaultMutationModule(formatterOptions));
    }

	private class DefaultMutationModule extends AbstractModule {

	    private final DefaultCodeFormatterOptions options;
	    
	    public DefaultMutationModule(DefaultCodeFormatterOptions options){
	        this.options = options;
	    }
	    
		@Override
		protected void configure() {
		}

		@Provides
		public CodeFormatter provideCodeFormatter(){
			return new DefaultCodeFormatter(getFormattingOptions());
		}
		
		private DefaultCodeFormatterOptions getFormattingOptions(){
			return options;
		}
		
		@Provides
		public ClashStrategy provideDefaultClashStrategy(){
			return ClashStrategy.ERROR;
		}
		
		@Named(ContextNames.MARK_GENERATED)
		@Provides
		public boolean provideDefaultMarkGenerated(){
			return markGenerated;
		}
		
		@Provides
		public AstNodeFlattener provideFlattener(){
			return obtain(SimpleFlattener.class);
		}
		
		@Provides
		@Singleton
		public JMutateContext provideContext(){
			return DefaultMutateContext.this;
		}
		
		@Provides
		@Singleton
		public PlacementStrategies provideDefaultStrategies(){
			return strategyProvider;
		}
		
		@Provides
		@Singleton
		@Named(ContextNames.FIELD)
		public PlacementStrategy provideDefaultFieldPlacement(){
			return strategyProvider.getFieldStrategy();
		}
		
		@Provides
		@Singleton
		@Named(ContextNames.CTOR)
		public PlacementStrategy provideDefaultCtorPlacement(){
			return strategyProvider.getCtorStrategy();
		}
		
		@Provides
		@Singleton
		@Named(ContextNames.METHOD)
		public PlacementStrategy provideDefaultMethodPlacement(){
			return strategyProvider.getMethodStrategy();
		}
		
		@Provides
		@Singleton
		@Named(ContextNames.TYPE)
		public PlacementStrategy provideDefaultTypePlacement(){
			return strategyProvider.getTypeStrategy();
		}
		
		@Provides
		@Singleton
		public JAstParser provideParser(){
			return parser;
		}
		
		@Provides
		public SourceTemplate provideSourceTemplate(){
			return new SourceTemplate(provideParser(),snippetRoot);
		}	
	}

	@Override
    public SourceTemplate newSourceTemplate(){
    	return obtain(SourceTemplate.class);
    }

	@Override
	public <T> T obtain(Class<T> type){
		return injector.getInstance(type);
	}
	
	public static class Builder {
	
		private boolean markGenerated = false;
		private JAstParser parser;
		private DefaultCodeFormatterOptions formattingOptions;
		private Root root;
		private PlacementStrategies placementStrategy;
		
		private Builder(){
		}

		public Builder defaults() {
        	return this;
		}

        public DefaultMutateContext build() {
            Root generateTo = getGenerationRootOrDefault();
            JAstParser parser = getParserOrDefault(generateTo);
            DefaultCodeFormatterOptions formatter = getFormatterOptionsOrDefault();
            PlacementStrategies strategy = getPlacementStrategyOrDefault();
            
            return new DefaultMutateContext(generateTo, parser, markGenerated, formatter,strategy);
        }
		
        private JAstParser getParserOrDefault(Root contextRoot) {
            return parser == null ? newDefaultAstParser(contextRoot) : parser;
        }

		private JAstParser newDefaultAstParser(Root contextGenerationRoot){
            return JAstParser.with()
                    .defaults()
                    .roots(Roots.with()
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
		
		private Root getGenerationRootOrDefault(){
		    return root==null?newTmpSnippetRoot():root;
		}
		
	    private static Root newTmpSnippetRoot(){
	        try {
	            File f = File.createTempFile("SimpleCodeMuckContextSnippetRoot","");
	            File tmpDir = new File(f.getAbsolutePath() + ".d/");
	            tmpDir.mkdirs();
	            
	            return new DirectoryRoot(tmpDir,RootType.GENERATED,RootContentType.SRC);
	        } catch (IOException e) {
	            throw new JMutateException("Couldn't create a tmp root");
	        }
	    }
	    
	    private PlacementStrategies getPlacementStrategyOrDefault(){
	        return placementStrategy==null?newDefaultPlacementStrategy():placementStrategy;
	    }
	    
	    private PlacementStrategies newDefaultPlacementStrategy(){
	        return PlacementStrategies.with().defaults().build();
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
            this.root = root;
            return this;
        }

        @Optional
        public Builder placementStrategy(PlacementStrategies strategy) {
            this.placementStrategy = strategy;
            return this;
        }
	}
}
