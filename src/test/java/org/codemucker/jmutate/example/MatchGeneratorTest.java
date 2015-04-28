package org.codemucker.jmutate.example;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.StringTemplate;
import org.codemucker.jtest.MavenProjectLayout;
import org.codemucker.jtest.ProjectLayout;
import org.codemucker.jtest.ProjectLayouts;
import org.codemucker.testfirst.Scenario;
import org.codemucker.testfirst.Scenario.Inserter;
import org.codemucker.testfirst.Scenario.Invoker;
import org.codemucker.testfirst.inject.TestInjector;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.google.inject.Inject;

public class MatchGeneratorTest {

	private String testMethodName;

	@Rule
	public TestRule watcher = new TestWatcher() {
		protected void starting(Description description) {
			testMethodName = description.getMethodName();
		}
	};

	@Test
	public void smokeTest() {
		MavenTestProject project;
		
		scenario()
				.given(a(project = MavenTestProject.with()
						.plugin(MavenTestPlugin.with().id("org.codemucker.jmutate:jmutate-maven-plugin:1.0-SNAPSHOT"))))
				.when(MavenIsRun.with().project(project).args("clean generate-sources"))
				.thenNothing();
	}

	private <T> T a(T obj) {
		return obj;
	}

	private Scenario scenario() {
		return new Scenario(testMethodName, newInjector());
	}
	
	private TestInjector newInjector(){
		File tmpDir = new MavenProjectLayout().getTmpDir();
		
		TestInjector injector = new MyInjector();
		injector.provide(new TestDirectory(tmpDir));
		injector.provide(MavenProjectLayout.createUsingBaseDir(tmpDir));
		injector.provide(MavenLocalRepo.with());
		return injector;
	}

	private static class MyInjector extends TestInjector {
		@Override
		public <T> T afterInject(T obj) {
			if (obj instanceof MavenTestProject) {
				provide(MavenTestProject.class,(MavenTestProject) obj);
			}
			if (obj instanceof MavenLocalRepo) {
				provide(MavenLocalRepo.class,(MavenLocalRepo) obj);
			}
			if (obj instanceof ProjectLayout) {
				provide(ProjectLayout.class,(ProjectLayout) obj);
			}			
			return obj;
		}
	}

	public class TestDirectory {
		private final File dir;

		public TestDirectory(File dir) {
			super();
			this.dir = dir;
		}

		public File getDir() {
			return dir;
		}
	}
	
	public static class MavenLocalRepo {

		@Inject
		private TestDirectory testDir;
		
		public static MavenLocalRepo with(){
			return new MavenLocalRepo();
		}
		
		
	}

	public static class MavenTestProject implements Inserter {

		@Inject
		ProjectLayout projectLayout;
		
		@Inject
		MavenLocalRepo repo;
		
		private String packaging = "jar";
		
		private MavenArtifact projectVersion =  MavenArtifact.createFromId("cxm.localhost.mytestproj:my-test-artifact:1.0-SNAPSHOT");
		
		private List<MavenDependency> dependencies = new ArrayList<>();
		private List<MavenTestPlugin> plugins = new ArrayList<>();
		private Map<String, Object> properties = new HashMap<String, Object>();
		
		public static MavenTestProject with() {
			return new MavenTestProject();
		}

		public MavenTestProject id(String id) {
			projectVersion = MavenArtifact.createFromId(id);
			return this;
		}

		public MavenTestProject dependency(String id) {
			dependency(MavenDependency.createFromId(id));
			return this;
		}

		public MavenTestProject dependency(MavenDependency dep) {
			dependencies.add(dep);
			return this;
		}

		public MavenTestProject plugin(MavenTestPlugin plugin) {
			plugins.add(plugin);
			return this;
		}

		@Override
		public void insert() throws Exception {
			// TODO Auto-generated method stub
			StringTemplate t = new StringTemplate();
			t.var("project.version", projectVersion.version);
			t.var("project.groupId", projectVersion.groupId);
			t.var("project.artifactId", projectVersion.artifactId);
			t.var("project.packaging", packaging);
			
			t.pl("<?xml version='1.0' encoding='UTF-8'?>");
			
			t.pl("<project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'");
			t.pl("xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd'>");
			t.pl("<modelVersion>4.0.0</modelVersion>");
/*				<parent>
					<groupId>org.codemucker</groupId>
					<artifactId>codemucker-parent</artifactId>
					<version>1.0-SNAPSHOT</version>
					<relativePath>../codemucker-parent/pom.xml</relativePath>
				</parent>*/
			t.pl("<groupId>${project.groupId}</groupId>");
			t.pl("<artifactId>${project.artifactId}</artifactId>");
			t.pl("<version>${project.version}</version>");
			t.pl("<packaging>${project.packaging}</packaging>");
			t.pl("<name>" + MavenTestProject.class.getName() + "-Internal-TestProject</name>");
			t.pl("<description>UnitTestProject</description>");
			if(properties.size() > 0){
				t.pl("<properties>");
				for (Entry<String, Object> entry : properties.entrySet()) {
					t.pl("<" + entry.getKey() + ">" + entry.getValue()+ "</" + entry.getKey() + ">");
				}
				t.pl("</properties>");
			}
			/*//TODO:repo!!!
			
			//add back to point to local repo? as settings changed it?
			t.pl("<repository>");
			t.pl("  <id>testrepo</id>");
			t.pl("  <name>testrepo</name>"); 
			t.pl("  <releases>");
			t.pl("    <enabled>true</enabled>");
			t.pl("    <checksumPolicy>ignore</checksumPolicy>");
			t.pl("  </releases>");
			t.pl("  <snapshots>");
			t.pl("    <enabled>true</enabled>");
			t.pl("    <checksumPolicy>ignore</checksumPolicy>");
			t.pl("  </snapshots>");
			t.pl("  <url>${project.baseUri}repo</url>");
			t.pl("</repository>");
			*/
			if(dependencies.size() > 0){
				t.pl("<dependencies>");
				for(MavenDependency dep:dependencies){
					t.pl("<dependency>");
					t.pl("<groupId>" + dep.groupId + "</groupId>");
					t.pl("<artifactId>" + dep.artifactId + "</artifactId>");
					if (dep.version != null) {
						t.pl("<version>" + dep.version + "</version>");
					}
					if (dep.scope != null) {
						t.pl("<scope>" + dep.scope + "</scope>");
					}
					t.pl("</dependency>");
				}
				t.pl("</dependencies>");
			}
			t.pl("<build>");
			if(plugins.size() > 0){
				t.pl("<plugins>");
				for(MavenTestPlugin plugin:plugins){
					t.pl("<plugin>");
					t.pl("<groupId>" + plugin.artifact.groupId + "</groupId>");
					t.pl("<artifactId>" + plugin.artifact.artifactId + "</artifactId>");
					if (plugin.artifact.version != null) {
						t.pl("<version>" + plugin.artifact.version + "</version>");
					}
					if(plugin.configurationXml!= null){
						t.pl("<configuration>");
						t.pl(plugin.configurationXml);
						t.pl("</configuration>");
					}
					t.pl("</plugin>");
				}
				t.pl("</plugins>");
			}
			t.pl("</build>");			
			t.pl("</project>");
			
			String pomXml = singleToDoubleQuotes(t.interpolateTemplate());
			File pom = new File(projectLayout.getBaseDir(),"pom.xml");
			//in case we've stuffed up and try overwriting our real project's pom.xml
			if(pom.exists()){
				throw new IllegalArgumentException("pom file " + pom.getAbsolutePath() + " already exists. Not overwriting");
			}
			try(FileOutputStream fos= new FileOutputStream(pom)){
				IOUtils.write(pomXml,fos);
			}
		}
		
		public File getBaseDir(){
			return projectLayout.getBaseDir();
		}
		
		public File getPomFile(){
			return new File(projectLayout.getBaseDir(),"pom.xml");
		}
		
		
	}
	
	private static String singleToDoubleQuotes(CharSequence s){
		return new StringBuffer(s).toString().replace("'", "\"");
	}

	public static class MavenIsRun implements Runnable {

		@Inject
		private MavenTestProject project;

		@Inject
		private MavenLocalRepo localRepo;
		
		private List<String> args = new ArrayList<>();

		public static MavenIsRun with() {
			return new MavenIsRun();
		}

		public MavenIsRun project(MavenTestProject project) {
			this.project = project;
			return this;
		}

		public MavenIsRun args(String... args) {
			this.args = Arrays.asList(args);
			return this;
		}

		@Override
		public void run() {

			//grab directory to run from
			project.getBaseDir();
			
			//generate pom
			//generate src dir's
			//add source files?
			
		}

	}

	public static class MavenTestPlugin {

		private MavenArtifact artifact;
		public CharSequence configurationXml;

		public static MavenTestPlugin with() {
			return new MavenTestPlugin();
		}

		public MavenTestPlugin id(String id) {
			artifact = MavenArtifact.createFromId(id);
			return this;
		}
		
		public MavenTestPlugin configurationXml(String xml) {
			this.configurationXml = xml;
			return this;
		}
	}

	public static class MavenDependency extends MavenArtifact {

		public String scope;

		public static MavenDependency createFromId(String id) {
			String[] parts = id.split(":");
			MavenDependency d = new MavenDependency();
			// TODO:check all valid
			d.groupId = parts[0];
			d.artifactId = parts[1];
			d.version = parts[2];
			d.scope = parts[3];
			return d;
		}

	}

	public static class MavenArtifact {
		public String groupId;
		public String artifactId;
		public String version;

		public static MavenArtifact createFromId(String id) {
			String[] parts = id.split(":");
			MavenArtifact artifact = new MavenArtifact();
			// TODO:check all valid
			artifact.groupId = parts[0];
			artifact.artifactId = parts[1];
			artifact.version = parts[2];
			return artifact;
		}

	}
}
