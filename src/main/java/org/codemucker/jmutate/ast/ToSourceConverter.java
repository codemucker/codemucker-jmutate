package org.codemucker.jmutate.ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.formatter.CodeFormatter;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultToSourceConverter.class)
public interface ToSourceConverter {
    
    public static enum Kind {
        COMPILATION_UNIT(CodeFormatter.K_COMPILATION_UNIT), 
        EXPRESSION(CodeFormatter.K_EXPRESSION), 
        STATEMENTS(CodeFormatter.K_STATEMENTS), 
        CLASS_BODY(CodeFormatter.K_CLASS_BODY_DECLARATIONS), 
        UNKNOWN(CodeFormatter.K_UNKNOWN);

        private final int kind;

        Kind(int kind) {
            this.kind = kind;
        }

        public int getCodeFormatterKind() {
            return kind;
        }
    }

    public String toSource(ASTNode node);

    public String toFormattedSource(String src, Kind kind);

}
