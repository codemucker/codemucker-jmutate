package org.codemucker.jmutate.generate.log;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.generate.AbstractGenerator;
import org.codemucker.jmutate.generate.GenerateOptions;
import org.codemucker.jmutate.generate.SmartConfig;
import org.codemucker.jpattern.generate.ClashStrategy;
import org.codemucker.jpattern.generate.GenerateLog;

import com.google.common.base.Strings;
import com.google.inject.Inject;

/**
 * Generates the 'LOG' field
 */
public class LogGenerator extends AbstractGenerator<GenerateLog> {

	private static final Logger LOG = LogManager.getLogger(LogGenerator.class);

	@Inject
	public LogGenerator(JMutateContext ctxt) {
		super(ctxt);
	}
	
	protected void generate(JType type, SmartConfig config) {
		
		LogOptions options = config.mapFromTo(GenerateLog.class, LogOptions.class);
		
		if(options.isEnabled() && !type.isInterface() && !type.isEnum()){
			String fieldName = options.fieldName;
			if(Strings.isNullOrEmpty(fieldName)){
				fieldName = "LOG";
			}
			
			LOG.debug("adding field '" + fieldName + "'");

			String topic;
			if(Strings.isNullOrEmpty(options.topic)){
				topic = type.getSimpleName() + ".class" + (options.logger.isTakesClass()?"":".getName()");
			} else {
				topic = '"' + options.topic + '"';
			}
			SourceTemplate logField = newSourceTemplate()
				.var("field.name", fieldName)
				.var("topic", topic)
				.var("logger.type", options.logger.getLoggerType())
				.var("logger.manager", options.logger.getLogManagerExpression())
				
				
				.pl("private static final ${logger.type} ${field.name} = ${logger.manager}(${topic});");
			
			addField(type, logField.asFieldNodeSnippet(),options.isMarkGenerated());
			
			writeToDiskIfChanged(type.getCompilationUnit().getSource());
		}
	}

	public static class LogOptions extends GenerateOptions<GenerateLog> {

		public String fieldName;
		public String topic;
		public GenerateLog.Type logger;
		public ClashStrategy clashStrategy;
		
		
	}
}