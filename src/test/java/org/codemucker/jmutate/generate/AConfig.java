package org.codemucker.jmutate.generate;

import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.codemucker.jmatch.AbstractNotNullMatcher;
import org.codemucker.jmatch.AnInstance;
import org.codemucker.jmatch.AnInt;
import org.codemucker.jmatch.Description;
import org.codemucker.jmatch.MatchDiagnostics;
import org.codemucker.jmatch.Matcher;
import org.codemucker.jmatch.ObjectMatcher;

public class AConfig extends ObjectMatcher<Configuration>{

	public AConfig() {
		super(Configuration.class);
	}

	public static AConfig with(){
		return new AConfig();
	}

	public AConfig numEntries(int num){
		numEntries(AnInt.equalTo(num));
		return this;
	}
	
	public AConfig numEntries(final Matcher<Integer> numMatcher){
		addMatcher(new AbstractNotNullMatcher<Configuration>() {
			@Override
			protected boolean matchesSafely(Configuration actual,MatchDiagnostics diag) {
				int count = 0;
				for(Iterator<?> keys = actual.getKeys();keys.hasNext();keys.next()){
					count++;
				}
				return diag.tryMatch(this, count, numMatcher);	
			}
			
			@Override
			public void describeTo(Description desc) {
				desc.value("number of entries", numMatcher);
			}
		});
		return this;
	}
	
	public AConfig entry(final String key,final Object value){
		entry(key,AnInstance.equalTo(value));
		return this;
	}
	
	public <V> AConfig entry(final String key,final Matcher<V> valueMatcher){
		addMatcher(new AbstractNotNullMatcher<Configuration>() {
			@Override
			protected boolean matchesSafely(Configuration actual,MatchDiagnostics diag) {	
				Object val = actual.getProperty(key);
				if(val == null){
					return diag.tryMatch(this, (V)val, valueMatcher);	
				} else {
					V actualVal = safeCast(val);
					if(actualVal != null){
						return diag.tryMatch(this, actualVal, valueMatcher);
					}
				}
				return false;
			}
			
			@Override
			public void describeTo(Description desc) {
				desc.value("key", key);
				desc.value("value", valueMatcher);
			}
		});
		return this;
	}
	
	public AConfig entry(final Matcher<String> keyMatcher,final Matcher<Object> valueMatcher){
		addMatcher(new AbstractNotNullMatcher<Configuration>() {
			@Override
			@SuppressWarnings("unchecked")
			protected boolean matchesSafely(Configuration actual,MatchDiagnostics diag) {	
				boolean matched = false;
				for(Iterator<String> keys = actual.getKeys();keys.hasNext();){
					String actualKey = keys.next();
					
					if(diag.tryMatch(this, actualKey,keyMatcher)){
						Object actualVal = actual.getProperty(actualKey);
						if(diag.tryMatch(this, actualVal, valueMatcher)){
							matched = true;
							break;
						}
					}
				}
				return matched;
			}
			
			@Override
			public void describeTo(Description desc) {
				desc.value("key", keyMatcher);
				desc.value("value", valueMatcher);
			}
	
		});
		return this;
	}
	
	private static <T> T safeCast(Object val){
		try{
			return (T)val;
		}catch(ClassCastException e){
			return null;
		}
	}
	
}
