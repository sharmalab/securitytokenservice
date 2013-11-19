package edu.emory.cci.bindaas.sts.internal.impl;

import com.google.common.base.CharMatcher;

import edu.emory.cci.bindaas.sts.api.IDGenerator;

public class URLFriendlyIDGenerator implements IDGenerator{

	public String generateID(String str) {
		str = str.toLowerCase();
		str = CharMatcher.WHITESPACE.or(CharMatcher.JAVA_LETTER_OR_DIGIT).or(CharMatcher.is('-')).retainFrom(str);
		str = CharMatcher.WHITESPACE.replaceFrom(str, "-");
		return str;
	}

}
