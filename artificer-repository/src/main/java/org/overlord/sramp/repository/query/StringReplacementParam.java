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

/**
 * A {@link QueryReplacementParam} for quoted string literals.
 *
 * @author eric.wittmann@redhat.com
 */
public class StringReplacementParam extends QueryReplacementParam<String> {

	/**
	 * Constructor.
	 * @param value
	 */
	public StringReplacementParam(String value) {
		super(value);
	}

	/**
	 * @see org.overlord.sramp.common.repository.query.QueryReplacementParam#getFormattedValue()
	 */
	@Override
	public String getFormattedValue() {
		StringBuilder builder = new StringBuilder();
		builder.append("'"); //$NON-NLS-1$
		builder.append(getValue().replace("'", "''")); //$NON-NLS-1$ //$NON-NLS-2$
		builder.append("'"); //$NON-NLS-1$
		return builder.toString();
	}

}
