package org.codemucker.jmutate;

public class DefaultProjectOptions implements ProjectOptions {
    private String sourceVersion;
    private String targetVersion;

    public DefaultProjectOptions(){
        setSourceVersion("1.7");
        setTargetVersion("1.7");
    }
    
    @Override
    public String getSourceVersion() {
        return sourceVersion;
    }

    public void setSourceVersion(String sourceVersion) {
        this.sourceVersion = sourceVersion;
    }

    @Override
    public String getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(String targetVersion) {
        this.targetVersion = targetVersion;
    }

}
