package org.codemucker.jmutate.generate.bean;

import org.apache.commons.configuration.Configuration;
import org.codemucker.jmutate.JMutateContext;
import org.codemucker.jmutate.SourceTemplate;
import org.codemucker.jmutate.ast.JType;
import org.codemucker.jmutate.generate.SmartConfig;
import org.codemucker.jmutate.generate.bean.HashCodeEqualsGenerator.HashCodeEqualsOptions;
import org.codemucker.jmutate.generate.model.pojo.PojoModel;
import org.codemucker.jmutate.generate.model.pojo.PropertyModel;
import org.codemucker.jpattern.generate.GenerateHashCodeAndEqualsMethod;

import com.google.inject.Inject;

/**
 * Generates the 'hashCode' and 'equals' methods on pojos
 */
public class HashCodeEqualsGenerator extends AbstractBeanGenerator<GenerateHashCodeAndEqualsMethod,HashCodeEqualsOptions> {

	/**
	 * Used to select a reproducible starting prime when generating a hash code. Rather than pick a random one at each generation causing churn in the code, pick out of this array in a deterministic way based
	 * on bean name
	 */
	private static final int[] FIRST_PRIMES = new int[]{31,37,41,43,47,53,59,61,67,71,73,79,83,89,97,101,103,107,109,113,127,131,137,139,149,151,157,163,167,173,179,181,191,193,197,199,211,223,227,229,233,239,241,251,257,263,269,271,277,281,283,293,307,311,313,317,331,337,347,349,353,359,367,373,379,383,389,397,401,409,419,421,431,433,439,443,449,457,461,463,467,479,487,491,499,503,509,521,523,541,547,557,563,569,571,577,587,593,599,601,607,613,617,619,631,641,643,647,653,659,661,673,677,683,691,701,709,719,727,733,739,743,751,757,761,769,773,787,797,809,811,821,823,827,829,839,853,857,859,863,877,881,883,887,907,911,919,929,937,941,947,953,967,971,977,983,991,997,1009,1013};

	@Inject
	public HashCodeEqualsGenerator(JMutateContext ctxt) {
		super(ctxt,GenerateHashCodeAndEqualsMethod.class);
	}
	
	@Override
	protected void generate(JType bean, SmartConfig config,PojoModel model, HashCodeEqualsOptions options) {
		if(options.isEnabled()){
			generateEquals(bean, model, options);
			generateHashCode(bean, model, options);
		}
	}
	
	private void generateEquals(JType bean, PojoModel model, HashCodeEqualsOptions options) {
		if(options.generateEquals && model.hasAnyProperties()){
			
			SourceTemplate equals = newSourceTemplate()
					.var("b.type", options.getType().getSimpleName())
					.pl("@java.lang.Override")
					.pl("public boolean equals(final Object obj){")
					.pl("if (this == obj) return true;")
					.pl("if (!super.equals(obj) || getClass() != obj.getClass()) return false;");
			
			if(model.hasAnyProperties()){
				equals.pl("${b.type} other = (${b.type}) obj;");
				for (PropertyModel property : model.getAllProperties()) {
					if(!((property.isSuperClassProperty() && property.hasGetter()) || (!property.isSuperClassProperty() && property.hasField()))){
						continue;
					}
					SourceTemplate  t = equals
						.child()
						.var("p.accessor",property.getInternalAccessor());
					
					if(property.getType().isPrimitive() && !property.getType().isString()){
						t.pl("if (${p.accessor} != other.${p.accessor}) return false;");
					} else {
						t.pl("if(${p.accessor} == null) {")
						.pl("	if (other.${p.accessor} != null)")
						.pl("		return false;")
						.pl("} else if (!${p.accessor}.equals(other.${p.accessor}))")
						.pl("	return false;");
					}
					equals.add(t);
				}
			}
			equals.pl("	return true;");
			equals.pl("}");
			

			addMethod(bean, equals.asMethodNodeSnippet(),options.isMarkGenerated());
		}
	}

	private void generateHashCode(JType bean, PojoModel model, HashCodeEqualsOptions options) {
		if(options.generateHashCode && model.hasAnyProperties()){
			int startingPrime = pickStartingPrimeForClass(options.getType().getFullName());
			SourceTemplate hashcode = newSourceTemplate()
				.var("prime", startingPrime)
				.pl("@java.lang.Override")
				.pl("public int hashCode(){");
				
			if(!model.hasAnyProperties()){
				hashcode.pl("return super.hashCode();");
			} else {
				hashcode.pl("final int prime = ${prime};");
				hashcode.pl("int result = super.hashCode();");
				for (PropertyModel property : model.getAllProperties()) {
					SourceTemplate t = hashcode
						.child()
						.var("p.accessor",property.getInternalAccessor());
					
					if(property.getType().isPrimitive() && !property.getType().isString()){
						//from the book 'Effective Java'
						if(property.getType().is("boolean")){
							t.pl("result = prime * result + (${p.accessor} ? 1:0);");
						} else if(property.getType().is("byte") || property.getType().is("char") || property.getType().is("int")){
							t.pl("result = prime * result + ${p.accessor};");
						} else if(property.getType().is("long")){
							t.pl("result = prime * result + (int) (${p.accessor} ^ (${p.accessor} >>> 32));");
						} else if(property.getType().is("float")){
							t.pl("result = prime * result + java.lang.Float.floatToIntBits(${p.accessor});");
						} else if(property.getType().is("double")){
							t.pl("result = prime * result + java.lang.Double.doubleToLongBits(${p.accessor});");
						} else  {
							t.pl("result = prime * result + ${p.accessor}.hashCode();");			
						}
					} else {
						t.pl("result = prime * result + ((${p.accessor} == null) ? 0 : ${p.accessor}.hashCode());");
					}
					hashcode.add(t);
				}
				hashcode.pl("return result;");
			}
			
			hashcode.pl("}");
			
			addMethod(bean, hashcode.asMethodNodeSnippet(),options.isMarkGenerated());
		}
	}

	//picks a repeatable but randomish prime for the given type
	private int pickStartingPrimeForClass(String fullName){			
		int hash = fullName.hashCode();
		int index;
		if(hash < 0){
			hash = -hash;
		}
		index = hash % FIRST_PRIMES.length;
		return FIRST_PRIMES[index];
	}
	
	
	@Override
	protected HashCodeEqualsOptions createOptionsFrom(Configuration config, JType type) {
		return new HashCodeEqualsOptions(config,type);
	}
	
	public static class HashCodeEqualsOptions extends AbstractBeanOptions<GenerateHashCodeAndEqualsMethod> {

		public boolean generateHashCode;
		public boolean generateEquals;
		
		public HashCodeEqualsOptions(Configuration config,JType type) {
			super(config,GenerateHashCodeAndEqualsMethod.class,type);
		}
	}

		
}