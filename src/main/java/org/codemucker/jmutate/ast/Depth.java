package org.codemucker.jmutate.ast;

public class Depth {
    
    public final int max;
    
    public static final Depth  DIRECT = new Depth(0);
    public static final Depth DIRECT_CHILDREN = new Depth(1);
    public static final Depth ANY = new Depth(-1);
    
    private Depth(int max){
        this.max = max;
    }
    
    public static Depth max(int max){
        return new Depth(max);
    }
}
