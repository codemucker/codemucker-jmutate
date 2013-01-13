package org.codemucker.jmutate.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.IOUtils;
import org.codemucker.jmutate.bean.BeanGenerationException;
import org.codemucker.jmutate.transform.Template;

import com.bertvanbrakel.test.finder.DirectoryRoot;
import com.bertvanbrakel.test.finder.Root;
import com.bertvanbrakel.test.finder.RootResource;
import com.bertvanbrakel.test.util.ProjectFinder;

public class ResourceWriter {

	private static AtomicLong uniqueIdCounter = new AtomicLong();

	public static RootResource writeResource(Template template) {
		return writeResource(template,newResourceName());
	}
	
	private static RootResource writeResource(Template template, String relPath) {
		return writeResource(template, findRootDir(), relPath);
	}

	private static File findRootDir() {
	    File dir = new File(ProjectFinder.findTargetDir(), "junit-test-generate/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
	    return dir;
	}
	
	private static RootResource writeResource(Template template, File rootDir, String relPath) {
		Root root = new DirectoryRoot(rootDir);
		RootResource resource = new RootResource(root, relPath);
		writeResource(template, resource);
		return resource;
	}

	private static RootResource writeResource(Template template, RootResource resource) {
		OutputStream os = null;
		try {
			os = resource.getOutputStream();
			IOUtils.write(template.interpolateTemplate(), os);
		} catch (FileNotFoundException e) {
	        throw new BeanGenerationException("Can't find resource " + resource,e);
        } catch (IOException e) {
	        throw new BeanGenerationException("Error writing resource " + resource,e);
        } finally {
			IOUtils.closeQuietly(os);
		}
		
		return resource;
	}
	
	private static String newResourceName(){
		return "com/bertvanbrakel/codemucker/randomjunit/Resource" + uniqueIdCounter.incrementAndGet() + ".text";
	}
}