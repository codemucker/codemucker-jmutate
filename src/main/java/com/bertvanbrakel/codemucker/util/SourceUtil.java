package com.bertvanbrakel.codemucker.util;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.dom.ASTNode;

import com.bertvanbrakel.codemucker.ast.JAstFlattener;
import com.bertvanbrakel.codemucker.bean.BeanGenerationException;
import com.bertvanbrakel.codemucker.transform.Template;
import com.bertvanbrakel.test.finder.ClassPathResource;
import com.bertvanbrakel.test.finder.DirectoryRoot;
import com.bertvanbrakel.test.finder.Root;
import com.bertvanbrakel.test.util.ProjectFinder;

public class SourceUtil {

	private static AtomicLong uniqueIdCounter = new AtomicLong();

	public static ClassPathResource writeResource(Template template) {
		return writeResource(template,newResourceName());
	}
	
	public static ClassPathResource writeResource(Template template, String relPath) {
		return writeResource(template, findRootDir(), relPath);
	}

	private static File findRootDir() {
	    File dir = new File(ProjectFinder.findTargetDir(), "junit-test-generate/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
	    return dir;
	}
	
	public static ClassPathResource writeResource(Template template, File rootDir, String relPath) {
		Root root = new DirectoryRoot(rootDir);
		ClassPathResource resource = new ClassPathResource(root, relPath);
		writeResource(template, resource);
		return resource;
	}

	public static ClassPathResource writeResource(Template template, ClassPathResource resource) {
		OutputStream os = null;
		try {
			os = resource.getOutputStream();
			IOUtils.write(template.interpolate(), os);
		} catch (FileNotFoundException e) {
	        throw new BeanGenerationException("Can't find resource " + resource,e);
        } catch (IOException e) {
	        throw new BeanGenerationException("Error writing resource " + resource,e);
        } finally {
			IOUtils.closeQuietly(os);
		}
		
		return resource;
	}
	
	public static String nodeToString(ASTNode node){
		return JAstFlattener.asString(node);
	}

	private static String newResourceName(){
		return "com/bertvanbrakel/codemucker/randomjunit/Resource" + uniqueIdCounter.incrementAndGet() + ".text";
	}
}