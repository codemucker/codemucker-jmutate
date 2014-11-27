package org.codemucker.jmutate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JMutateInfo {

    public static final String name;
    public static final String groupId;
    public static final String artifactId;
    public static final String version;
    public static final String timestamp;
    public static final String all;

    static {
        try (InputStream is = JMutateInfo.class.getResourceAsStream("/application.properties")) {
            if(is == null){
                throw new RuntimeException("could not load app.properties");
            }
            Properties p = new Properties();
            p.load(is);
            name = p.getProperty("build.name");
            groupId = p.getProperty("build.groupId");
            artifactId = p.getProperty("build.artifactId");
            version = p.getProperty("build.version");
            timestamp = p.getProperty("build.timestamp");

            all = name + " (" + groupId + ":" + artifactId + ":" + version + ":" + timestamp + ")";
        } catch (IOException e) {
            throw new RuntimeException("couldn't read application.properties", e);
        }
    }
}
