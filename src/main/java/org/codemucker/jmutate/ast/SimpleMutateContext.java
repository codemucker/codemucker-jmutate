package org.codemucker.jmutate.ast;

import java.io.File;
import java.io.IOException;

import org.codemucker.jfind.DirectoryRoot;
import org.codemucker.jfind.Root;
import org.codemucker.jfind.Root.RootContentType;
import org.codemucker.jfind.Root.RootType;
import org.codemucker.jfind.Roots;
import org.codemucker.jmutate.NamedAnnotation;
import org.codemucker.jmutate.transform.ClashStrategy;
import org.codemucker.jmutate.transform.MutateContext;
import org.codemucker.jmutate.transform.PlacementStrategies;
import org.codemucker.jmutate.transform.PlacementStrategy;
import org.codemucker.jmutate.transform.SourceTemplate;
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
public class SimpleMutateContext implements MutateContext {

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
	
	public SimpleMutateContext(){
		this(false);
	}
	
	public SimpleMutateContext(boolean markGenerated){
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
			throw new MutateException("Couldn't create a tmp root");
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
		public MutateContext provideContext(){
			return SimpleMutateContext.this;
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

		public SimpleMutateContext build(){
			return new SimpleMutateContext(markGenerated);
		}
		
		public Builder setMarkGenerated(boolean markGenerated) {
        	this.markGenerated = markGenerated;
        	return this;
		}
		
	}
}
