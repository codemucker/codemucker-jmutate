package org.codemucker.jmutate.ast;

public class SearchDepth {
    
    public final int max;
    
    public static final SearchDepth DIRECT = new SearchDepth(0);
    public static final SearchDepth DIRECT_CHILDREN = new SearchDepth(1);
    public static final SearchDepth ANY = new SearchDepth(-1);
    
    private SearchDepth(int max){
        this.max = max;
    }
    
    public static SearchDepth max(int max){
        return new SearchDepth(max);
    }
}
