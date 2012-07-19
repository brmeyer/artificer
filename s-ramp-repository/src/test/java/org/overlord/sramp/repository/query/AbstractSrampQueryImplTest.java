/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.sramp.repository.query;

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link AbstractSrampQueryImpl} class.
 *
 * @author eric.wittmann@redhat.com
 */
public class AbstractSrampQueryImplTest {

	/**
	 * Test method for {@link org.overlord.sramp.repository.query.AbstractSrampQueryImpl#formatQuery(java.lang.String, java.util.List)}.
	 * @throws InvalidQueryException 
	 */
	@Test
	public void testFormatQuery() throws InvalidQueryException {
		doFormatQueryTest("/s-ramp/xsd/XsdDocument", "/s-ramp/xsd/XsdDocument");
		doFormatQueryTest("/s-ramp/xsd/XsdDocument[@prop = ?]",
				"/s-ramp/xsd/XsdDocument[@prop = 'hello-world']", 
				new StringReplacementParam("hello-world"));
		doFormatQueryTest("/s-ramp/xsd/XsdDocument[@size = ?]",
				"/s-ramp/xsd/XsdDocument[@size = 10]", 
				new NumberReplacementParam(10));
		doFormatQueryTest("/s-ramp/xsd/XsdDocument[@size = ?]",
				"/s-ramp/xsd/XsdDocument[@size = 1.0]", 
				new NumberReplacementParam(1.0));
		doFormatQueryTest("/s-ramp/xsd/XsdDocument[@size = ?]",
				"/s-ramp/xsd/XsdDocument[@size = 12345123451234512345123451234512345]", 
				new NumberReplacementParam(new BigInteger("12345123451234512345123451234512345")));
		doFormatQueryTest("/s-ramp/xsd/XsdDocument[@size = ?]",
				"/s-ramp/xsd/XsdDocument[@size = 123456789012345]", 
				new NumberReplacementParam(123456789012345L));

		doFormatQueryTest("/s-ramp/xsd/XsdDocument[@prop1 = ? and @prop2 = ? and @prop3 = ?]",
				"/s-ramp/xsd/XsdDocument[@prop1 = 'val1' and @prop2 = 'val2' and @prop3 = 17]", 
				new StringReplacementParam("val1"),
				new StringReplacementParam("val2"),
				new NumberReplacementParam(17));
	}

	/**
	 * Test method for {@link org.overlord.sramp.repository.query.AbstractSrampQueryImpl#formatQuery(java.lang.String, java.util.List)}.
	 * @throws InvalidQueryException 
	 */
	@Test(expected=InvalidQueryException.class)
	public void testFormatQuery_tooManyParams() throws InvalidQueryException {
		doFormatQueryTest("/s-ramp/xsd/XsdDocument", null, new StringReplacementParam("val1"));
	}

	/**
	 * Test method for {@link org.overlord.sramp.repository.query.AbstractSrampQueryImpl#formatQuery(java.lang.String, java.util.List)}.
	 * @throws InvalidQueryException 
	 */
	@Test(expected=InvalidQueryException.class)
	public void testFormatQuery_notEnoughParams() throws InvalidQueryException {
		doFormatQueryTest("/s-ramp/xsd/XsdDocument[@prop1 = ? or @prop2 = ?]", null, new StringReplacementParam("val1"));
	}
	
	/**
	 * Does a single formatQuery test case.
	 * @param xpathTemplate
	 * @param expectedXpath
	 * @param params
	 * @throws InvalidQueryException
	 */
	private void doFormatQueryTest(String xpathTemplate, String expectedXpath,
			QueryReplacementParam<?>... params) throws InvalidQueryException {
		String formattedQuery = AbstractSrampQueryImpl.formatQuery(xpathTemplate, Arrays.asList(params));
		Assert.assertEquals(expectedXpath, formattedQuery);
	}

}
