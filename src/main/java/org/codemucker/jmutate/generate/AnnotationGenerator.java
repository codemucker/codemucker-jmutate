package org.codemucker.jmutate.generate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.codemucker.jmutate.JMutateCompileException;
import org.codemucker.jmutate.generate.DefaultAnnotationCompiler.CompiledAnnotationExtractor;
import org.codemucker.jmutate.util.IdUtils;
import org.codemucker.jmutate.util.NameUtil;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.IntegerMemberValue;

public class AnnotationGenerator {

	private static String pkg = AnnotationGenerator.class.getPackage().getName() + ".runtimeGeneratedClass";

	public <T extends Annotation> T createAnnonation(Class<T> annotationClassToGenerate) throws CannotCompileException{
		ClassPool pool = ClassPool.getDefault();

		// create the class
		String className = pkg + ".TmpClass" + IdUtils.nextTimeBasedId();
		CtClass newClass = pool.makeClass(className);


		ClassFile classFile = newClass.getClassFile();
		ConstPool constpool = classFile.getConstPool();

		// create the annotation
		AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
		Annotation annot = new Annotation(NameUtil.compiledNameToSourceName(annotationClassToGenerate.getName()), constpool);
		
		for(Method m:annotationClassToGenerate.getDeclaredMethods()){
			if(!m.isAccessible() || !Modifier.isPublic(m.getModifiers()) || Modifier.isFinal(m.getModifiers())){
				continue;
			}
			String name = m.getName();
			
			//TODO:wrap type
			annot.addMemberValue(name, new IntegerMemberValue(classFile.getConstPool(), 0));
		}
		attr.addAnnotation(annot);
		
		java.lang.annotation.Annotation a;
		Target target = annotationClassToGenerate.getAnnotation(Target.class);
		AnnotationExtractor extractor;
		
		int typeNum = -1;//declared on top level type if -1, else a sub class
		int fieldNum = 0;
		int methodNum = 0;
		int ctorNum = 0;
		
		if(target != null){
			for(ElementType ele:target.value()){
				switch(ele){
				case ANNOTATION_TYPE:
					break;
				case CONSTRUCTOR:
					CtConstructor ctor= CtNewConstructor.make("public " + className + "() {}", newClass);
					newClass.addConstructor(ctor);
					
					extractor = AnnotationExtractor.constructor(typeNum, methodNum);
					break;
				case FIELD:
					CtField f = new CtField(CtClass.intType, "f" + fieldNum, newClass);
					newClass.addField(f);
					
					extractor = AnnotationExtractor.field(typeNum, fieldNum);
					fieldNum++;
					break;
				case LOCAL_VARIABLE:
					break;
				case METHOD:
					CtMethod method = CtNewMethod.make("public Object m" + methodNum + "() { return null; }", newClass);
					newClass.addMethod(method);
					
					extractor = AnnotationExtractor.method(typeNum, methodNum);
					methodNum++;
					break;
				case PACKAGE:
					break;
				case PARAMETER:
					break;
				case TYPE:
					extractor = AnnotationExtractor.type(typeNum);
					
					classFile.addAttribute(attr);
					Class<?> clazz = newClass.toClass();
					a = clazz.getAnnotations()[0];
				//	typeNum++;
					break;
				case TYPE_PARAMETER:
					break;
				case TYPE_USE:
					break;
				}
			}
		}
		return null;
		
	}
	
    /**
     * Extracts the compiled annotation from a class
     */
    private static class AnnotationExtractor {
        /**
         * The index of the type holding the annotation. If -1 is the top level class, else a sub class
         */
        final int typeIndex;
        /**
         * The index of the method with the annotation, or -1 if not a method annotation.
         */
        final int methodIndex;
        /**
         * The index of the ctor with the annotation, or -1 if not a ctor annotation.
         */
        final int ctorIndex;

        /**
         * The index of the field or constructor/method parameter with the annotation. Is a field if the method and constructor index is -1, else a method param
         */
        final int fieldIndex;

        public static AnnotationExtractor type(int typeNum) {
            return new AnnotationExtractor(typeNum, -1, -1, -1);
        }

        public static AnnotationExtractor constructor(int typeNum, int ctorNum) {
            return new AnnotationExtractor(typeNum, ctorNum, -1, -1);
        }
        
        public static AnnotationExtractor method(int typeNum, int methodNum) {
            return new AnnotationExtractor(typeNum, -1, methodNum, -1);
        }

        public static AnnotationExtractor field(int typeNum, int fieldNum) {
            return new AnnotationExtractor(typeNum, -1, 0, fieldNum);
        }

        public static AnnotationExtractor methodParam(int typeNum, int methodNum, int paramNum) {
            return new AnnotationExtractor(typeNum, -1, 0, paramNum);
        }

        private AnnotationExtractor(int typeNum, int ctorNum, int methodNum, int fieldNum) {
            super();
            this.typeIndex = typeNum;
            this.ctorIndex = ctorNum;
            this.methodIndex = methodNum;
            this.fieldIndex = fieldNum;
        }

        private void extractAndCacheAnnotation(Class<?> tmpClass) {
            java.lang.annotation.Annotation anon = extractAnnotation(tmpClass);
        }

        private java.lang.annotation.Annotation extractAnnotation(Class<?> klass) {
            try {
                Class<?> type =  typeIndex<0?klass:klass.getDeclaredClasses()[typeIndex];
                if (ctorIndex != -1) {
                    Constructor<?> c = type.getDeclaredConstructors()[ctorIndex];
                    if (fieldIndex != -1) {
                        return c.getParameterAnnotations()[fieldIndex][0];
                    }
                    return c.getDeclaredAnnotations()[0];
                }
                if (methodIndex != -1) {
                    Method m = type.getDeclaredMethods()[methodIndex];
                    if (fieldIndex != -1) {
                        return m.getParameterAnnotations()[fieldIndex][0];
                    }
                    return m.getDeclaredAnnotations()[0];
                }
                if (fieldIndex != -1) {
                    return type.getDeclaredFields()[fieldIndex].getAnnotations()[0];
                }
                return type.getDeclaredAnnotations()[0];

            } catch (ArrayIndexOutOfBoundsException e) {
                StringBuilder msg = new StringBuilder();
                msg.append("Couldn't find the annotation to extract using :");
                msg.append(toString());
                extractInfo(klass,msg,0);
                
                throw new JMutateCompileException(msg.toString());
            }
        }
        
        private static void extractInfo(Class<?> klass, StringBuilder msg, int depth) {
            newlineAndPad(msg,depth);
            msg.append("for type " + klass.getSimpleName());

            newlineAndPad(msg,depth);
            java.lang.annotation.Annotation[] annons = klass.getAnnotations();
            msg.append("found " + annons.length + " annotations");
            
            newlineAndPad(msg,depth);
            java.lang.annotation.Annotation[] annonsDeclared = klass.getDeclaredAnnotations();
            msg.append("found " + annonsDeclared.length + " annotations (declared)");

            newlineAndPad(msg,depth);
            Field[] fields = klass.getDeclaredFields();
            msg.append("found " + fields.length + " fields");
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                newlineAndPad(msg,depth +1);
                msg.append("field " + i + " '" + f.getName() + "' has " + f.getDeclaredAnnotations().length + " annotations");
            }

            newlineAndPad(msg,depth);
            Constructor<?>[] ctors = klass.getDeclaredConstructors();
            msg.append("found " + ctors.length + " constructors");
            for (int i = 0; i < ctors.length; i++) {
                Constructor<?> c = ctors[i];
                newlineAndPad(msg,depth + 1);
                msg.append("constructor " + i + " '" + c.getName() + "' has " + c.getDeclaredAnnotations().length + " annotations");
            }

            newlineAndPad(msg,depth);
            Method[] methods = klass.getDeclaredMethods();
            msg.append("found " + methods.length + " methods");
            for (int i = 0; i < methods.length; i++) {
                Method m = methods[i];
                newlineAndPad(msg,depth + 1);
                msg.append("method " + i + " '" + m.getName() + "' has " + m.getDeclaredAnnotations().length + " annotations");
            }

            newlineAndPad(msg,depth);
            Class<?>[] types = klass.getDeclaredClasses();
            msg.append("found " + types.length + " embedded classes");
            for (int i = 0; i < types.length; i++) {
                Class<?> embeddedClass = types[i];
                extractInfo(embeddedClass, msg, depth + 1);
            }
        }
        
        private static void newlineAndPad(StringBuilder sb, int pad){
            sb.append("\n");
            for(int i=0;i<pad;i++){
                sb.append("..");
            }
        }
        
        @Override
        public String toString() {
            String s = super.toString() + "{";
            s += "type[" + typeIndex + "]";
            if (ctorIndex != -1) {
                s += ",ctor[" + ctorIndex+ "]";
                if (fieldIndex != -1) {
                    s += ",ctorParam[" + fieldIndex + "]";
                }
            } else if (methodIndex != -1) {
                s += ",method[" + methodIndex + "]";
                if (fieldIndex != -1) {
                    s += ",methodParam[" + fieldIndex + "]";
                }
            } else if (fieldIndex != -1) {
                s += ",field[" + fieldIndex + "]";
            }
            s += ",annotation[0]";
            s += "}";
            return s;
        }
    }
}
