package com.bertvanbrakel.test.bean.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

import com.bertvanbrakel.lang.annotation.NotThreadSafe;
import com.bertvanbrakel.test.bean.BeanDefinition;
import com.bertvanbrakel.test.bean.PropertiesExtractor;

/**
 * Generate bean builders from found classes.
 * 
 * Modify src dir? specify src dir? aka src/main/generated...
 * 
 * 
 * fail if builder/factory not found? fail if builder does not cover all bean
 * aspects? fail if builder does not correctly create beans? fail if builder
 * does not use ctor generation correctly?
 * 
 * provide option to run from ide/main to generate missing (main(..))
 * 
 * todo:find not property methods (fooSOmething(args) with non self return type
 * or void), akak all other non property methods)
 */
@NotThreadSafe
public class BeanBuilderGenerator {

	private static final Logger LOG =  Logger.getLogger(BeanBuilderGenerator.class);
	
	private final GenerationContext generationCtxt;

	public BeanBuilderGenerator() {
		this(new GenerationContext());
	}

	public BeanBuilderGenerator(GenerationContext ctxt) {
		this.generationCtxt = ctxt;
	}

	public void generateBuilder(Class<?> beanClass, BuilderOptions options) {
		BeanDefinition def = new PropertiesExtractor(options).extractBeanDef(beanClass);
		generateBuilder(def, options);
	}

	public void generateBuilder(BeanDefinition def, BuilderOptions options) {
		// TODDO:use a proper AST library, Rescriptor etc....
		// for now a simple string builder
		BeanBuilderWriter w = new BeanBuilderWriter(options, def.getBeanType().getName());
		w.generate(def);
		String relPath = w.getSourceFilePath();
		File dest = new File(generationCtxt.getGenerationMainDir(), relPath);
		String src = w.toJavaClassString();
		writeTo(dest, src);
	}

	private void writeTo(File dest, String src) {
		FileOutputStream fos = null;
		try {
			if( LOG.isDebugEnabled()){
				LOG.debug("writing source to " + dest.getAbsolutePath());
			}
			dest.getParentFile().mkdirs();
			fos = new FileOutputStream(dest);
			IOUtils.write(src, fos);
		} catch (IOException e) {
			throw new BeanGenerationException("Could not write source destintion file %s", e, dest);
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}
}
