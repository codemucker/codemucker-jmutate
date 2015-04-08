package org.codemucker.jmutate.generate;

import org.codemucker.jmatch.Expect;
import org.codemucker.jmutate.generate.util.PluralToSingularConverter;
import org.junit.Test;

public class PluralToSingularConverterTest {

	@Test
	public void convertsBackAndForthCorrectly() {

		test("child","children");
		test("sheep","sheep");
		test("man","men");
		test("foo","foos");
		test("company","companies");
		test("index","indexes");
		test("city","cities");
		test("address","addresses");
		
		test("life","lives");
		test("wolf","wolves");
		
		test("apple","apples");
		test("use","uses");
		test("one","ones");
		test("two","twos");
		test("person","people");
		test("leaf","leaves");
		
	}
	
	private void test(String single,String plural){
		
		String pluralActual = PluralToSingularConverter.INSTANCE.singleToPlural(single);
		String singleActual = PluralToSingularConverter.INSTANCE.pluralToSingle(plural);
		
		
		Expect.that(pluralActual).isEqualTo(plural);
		Expect.that(singleActual).isEqualTo(single);
		
	}
}
