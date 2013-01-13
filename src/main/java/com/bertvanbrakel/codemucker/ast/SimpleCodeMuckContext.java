package com.bertvanbrakel.codemucker.ast;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;

import com.bertvanbrakel.codemucker.NamedAnnotation;
import com.bertvanbrakel.codemucker.transform.ClashStrategy;
import com.bertvanbrakel.codemucker.transform.CodeMuckContext;
import com.bertvanbrakel.codemucker.transform.PlacementStrategies;
import com.bertvanbrakel.codemucker.transform.PlacementStrategy;
import com.bertvanbrakel.codemucker.transform.SourceTemplate;
import com.bertvanbrakel.test.finder.DirectoryRoot;
import com.bertvanbrakel.test.finder.Root;
import com.bertvanbrakel.test.finder.Root.RootContentType;
import com.bertvanbrakel.test.finder.Root.RootType;
import com.bertvanbrakel.test.finder.Roots;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.name.Named;

@Singleton
public class SimpleCodeMuckContext implements CodeMuckContext {

	private final PlacementStrategies strategyProvider  = PlacementStrategies.builder().setDefaults().build();
	private final JAstParser parser = JAstParser.builder()
		.setDefaults()
		.setResolveRoots(Roots.builder()
			.setIncludeClasspath(true)
			.setIncludeTestSrcDir(true)
			.setIncludeMainSrcDir(true)
			.build()	
		)
		.build();
	private final Root snippetRoot = newTmpSnippetRoot();
	/**
	 * If set, generated classes will be marked as such
	 */
	private final boolean markGenerated;
	private final Injector injector;
	
	public static Builder builder(){
		return new Builder();
	}
	
	public SimpleCodeMuckContext(){
		this(false);
	}
	
	public SimpleCodeMuckContext(boolean markGenerated){
		injector = Guice.createInjector(Stage.PRODUCTION, new MutationModule());
		this.markGenerated = markGenerated;
	}
	
	private static Root newTmpSnippetRoot(){
		try {
			File f = File.createTempFile("SimpleCodeMuckContextSnippetRoot","");
			File tmpDir = new File(f.getAbsolutePath() + ".d/");
			tmpDir.mkdirs();
			
			return new DirectoryRoot(tmpDir,RootType.GENERATED,RootContentType.SRC);
		} catch (IOException e) {
			throw new CodemuckerException("Couldn't create a tmp root");
		}
	}
	
	private class MutationModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(Object.class).annotatedWith(new NamedAnnotation(ContextNames.FIELD)).toInstance(provideDefaultFieldPlacement());
			bind(Object.class).annotatedWith(new NamedAnnotation(ContextNames.CTOR)).toInstance(provideDefaultCtorPlacement());
			bind(Object.class).annotatedWith(new NamedAnnotation(ContextNames.METHOD)).toInstance(provideDefaultMethodPlacement());
			bind(Object.class).annotatedWith(new NamedAnnotation(ContextNames.TYPE)).toInstance(provideDefaultTypePlacement());
		}

		@Provides
		public CodeFormatter provideCodeFormatter(){
			return new DefaultCodeFormatter(getFormattingOptions());
		}
		
		private DefaultCodeFormatterOptions getFormattingOptions(){
			DefaultCodeFormatterOptions opts = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getJavaConventionsSettings());
			//TODO:add custom options from format file
			return opts;
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
		public CodeMuckContext provideContext(){
			return SimpleCodeMuckContext.this;
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

		private Builder(){
		}

		public Builder setDefaults() {
        	return this;
		}

		public SimpleCodeMuckContext build(){
			return new SimpleCodeMuckContext(markGenerated);
		}
		
		public Builder setMarkGenerated(boolean markGenerated) {
        	this.markGenerated = markGenerated;
        	return this;
		}
		
	}
}
