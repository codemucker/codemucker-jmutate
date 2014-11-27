package org.codemucker.jmutate;

import com.google.inject.ImplementedBy;


@ImplementedBy(DefaultProjectOptions.class)
public interface ProjectOptions {

    String getSourceVersion();

    String getTargetVersion();

}
