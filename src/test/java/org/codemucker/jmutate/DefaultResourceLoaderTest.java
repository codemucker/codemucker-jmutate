package org.codemucker.jmutate;

import org.junit.Test;

public class DefaultResourceLoaderTest {

    //why is Nonnull not working?
    @Test
    public void canLoadAnnotation() throws Exception{
        getClass().getClassLoader().loadClass("java.lang.annotation.Documented");
    }
    
}
