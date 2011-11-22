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
package com.bertvanbrakel.test.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 * Convenience helper class to make accessing test resources easier
 */
public class TestHelper {

	static {
		// custom user defined system properties
		Properties customSystemProps = loadResourceProperties("system.properties");
		for (Enumeration<?> keys = customSystemProps.propertyNames(); keys.hasMoreElements();) {
			String key = (String) keys.nextElement();
			System.setProperty(key, customSystemProps.getProperty(key));
		}

		Properties log4jProps = loadResourceProperties("log4j.properties");
		if (log4jProps.size() > 0) {
			PropertyConfigurator.configure(log4jProps);
		}
		// logback...?
	}

	protected static final Logger LOG = Logger.getLogger(TestHelper.class);

	private Properties testProperties = null;

	public TestHelper() {
		super();
	}

	public Properties getTestProperties() {
		if (testProperties == null) {
			testProperties = loadResourceProperties("test.properties");
		}
		return testProperties;
	}

	private static Properties loadResourceProperties(String fileName) {
		return loadResourceProperties(TestHelper.class, fileName);
	}

	private static Properties loadResourceProperties(Class<?> relativeToClass, String fileName) {
		Properties props = new Properties();
		InputStream is = null;
		try {
			is = relativeToClass.getResourceAsStream(fileName);
			if (is != null) {
				props.load(is);
			}
		} catch (IOException e) {
			// ignore, fail silently
		} finally {
			IOUtils.closeQuietly(is);
		}
		return props;
	}

	public Logger getLog() {
		return LOG;
	}

	public String createTempFilePath() {
		return createTempFile().getAbsolutePath();
	}

	public String createTempFilePathWithPostfix(String fileName) {
		return createTempFileWithName(fileName).getAbsolutePath();
	}

	public File createTempFile() {
		return createTempFileWithName(null);
	}

	public File createTempFileWithName(String fileName) {
		if (fileName == null) {
			fileName = "temp.txt";
		}
		return new File(createTempDirWithPostfix(null), fileName);
	}

	public File createTempDir() {
		return createTempDirWithPostfix(null);
	}

	/**
	 * Create a new temporary time stamped directory for test purposes. Each
	 * call will generate a new empty directory.
	 */
	public File createTempDirWithPostfix(String namePostfix) {
		namePostfix = namePostfix == null ? "" : ("-" + namePostfix);
		File baseDir = new File(getMavenTargetDir().asFile(), "junit-tmp-dirs/");
		String namePrefix = "tmp-" + new SimpleDateFormat("yyyy.MM.dd_HHmm.ss_S").format(new Date());
		File dir = generateUniqueDirNameIn(baseDir, namePrefix, namePostfix);
		return dir;
	}

	public Resource getTestJavaSourceDir() {
		return new Resource(ProjectFinder.findDefaultMavenTestDir());
	}

	public Resource getTestResourceDir() {
		return new Resource(ProjectFinder.findDefaultMavenCompileTestDir());
	}

	public Resource getMavenTargetDir() {
		return new Resource(ProjectFinder.findTargetDir());
	}

	public Resource getProjectDir() {
		return new Resource(ProjectFinder.findProjectDir());
	}

	private File generateUniqueDirNameIn(File baseDir, String namePrefix, String namePostfix) {
		File dir = new File(baseDir, namePrefix + "-0" + namePostfix);
		if (dir.exists()) {
			for (long i = 1; dir.exists(); i++) {
				if (i == Long.MAX_VALUE - 1) {
					throw new RuntimeException("couldn't generate a unique tmp directory name");
				}
				dir = new File(baseDir, namePrefix + "-" + i + namePostfix);
			}
		}
		if (!dir.mkdirs()) {
			throw new RuntimeException("Couldn't create temp directory '" + dir.getAbsolutePath() + "'");
		}
		return dir;
	}

	public static class Resource {
		private final File dir;

		Resource(File dir) {
			this.dir = dir;
		}

		public File asFile() {
			return dir;
		}

		public String asPath() {
			return dir.getAbsolutePath();
		}

		public String readAsString() throws IOException{
			InputStream is = asStream();
			if( is == null ){
				throw new IOException("No resource with path " + dir.getAbsolutePath());
			}
			try {
				return IOUtils.toString(is);
			} finally {
				IOUtils.closeQuietly(is);
			}
		}

		public InputStream asStream() throws IOException{
			return getFileInputSteam(dir);
		}
		
		public InputStream childStream(String relativePath) throws IOException {
			return childResource(relativePath).asStream();
		}

		public InputStream childStream(Class<?> relativeToClass, String relativePath) throws IOException {
			return childResource(relativeToClass, relativePath).asStream();
		}
		
		public Resource childResource(String relativePath) throws IOException {
			return new Resource(new File(dir, relativePath));
		}

		public Resource childResource(Class<?> relativeToClass, String relativePath) throws IOException {
			String basePath = relativeToClass.getPackage().getName().replace('.', '/');
			File baseDir = new File(this.dir, basePath);
			File f = new File(baseDir, relativePath);
			return new Resource(f);
		}
		
		private static FileInputStream getFileInputSteam(File f) throws IOException {
			try {
				return new FileInputStream(f);
			} catch (FileNotFoundException e) {
				throw new IOException("Could not find file '" + f.getAbsolutePath() + "'", e);
			}
		}
	}
}
