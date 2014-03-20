package org.rakshak.core.internal.impl;

import org.rakshak.core.api.IDGenerator;

import com.google.common.base.CharMatcher;

public class URLFriendlyIDGenerator implements IDGenerator{

	public String generateID(String str) {
		str = str.toLowerCase();
		str = CharMatcher.WHITESPACE.or(CharMatcher.JAVA_LETTER_OR_DIGIT).or(CharMatcher.is('-')).retainFrom(str);
		str = CharMatcher.WHITESPACE.replaceFrom(str, "-");
		return str;
	}

}
