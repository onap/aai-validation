/*
 * ============LICENSE_START===================================================
 * Copyright (c) 2018 Amdocs
 * ============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=====================================================
 */
package org.onap.aai.validation.ruledriven.configuration.build;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;


/**
 * Helper class for building internal configuration settings.
 */
public class ContentBuilder {

	private final String contentName;
	protected List<ContentBuilder> items = new ArrayList<>();
	private StringBuilder content = new StringBuilder();
	protected String indent = "";

	/**
	 * Construct an empty section.
	 */
	public ContentBuilder() {
		this.contentName = null;
	}

	/**
	 * Instantiates a new content builder.
	 *
	 * @param contentName
	 *            the content name
	 */
	public ContentBuilder(String contentName) {
		this.contentName = contentName;
	}

	@Override
	public String toString() {
		return build();
	}

	/**
	 * Builds the configuration section
	 *
	 * @return the configuration as a string
	 */
	public String build() {
		StringBuilder sb = new StringBuilder();
		appendPrefix(sb);
		// Build child items
		for (ContentBuilder item : items) {
			sb.append(item.build());
		}
		// Any ad-hoc content goes here
		sb.append(content);
		appendSuffix(sb);
		return sb.toString();
	}

	/**
	 * Adds the content.
	 *
	 * @param item
	 *            the item
	 */
	public void addContent(ContentBuilder item) {
		items.add(item);
	}

	/**
	 * Adds the content.
	 *
	 * @param stringContent
	 *            the string content
	 */
	public void addContent(String stringContent) {
		content.append(stringContent);
	}

	/**
	 * Append content as a new line.
	 *
	 * @param stringContent
	 *            the string
	 */
	public void appendLine(String stringContent) {
		content.append(indent).append(stringContent).append(System.lineSeparator());
	}

	/**
	 * Append properties.
	 *
	 * @param props
	 *            the props
	 */
	public void appendProperties(Properties props) {
		@SuppressWarnings("unchecked")
		Enumeration<String> e = (Enumeration<String>) props.propertyNames();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			appendValue(key, props.get(key));
		}
	}

	/**
	 * Append value.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return
	 */
	public ContentBuilder appendValue(String key, Object value) {
		if (value == null) {
			return this;
		}

		addContent(indent + "\t" + key + " ");

		if (value instanceof String) {
			addStringValue((String) value);
		} else if (value instanceof Number) {
			addStringValue(value.toString());
		} else if (value instanceof List<?>) {
			boolean first = true;
			for (Object element : (List<?>) value) {
				if (!first) {
					addContent(", ");
				}
				addStringValue((String) element);
				first = false;
			}
		} else if (value instanceof Properties) {
			appendLine("{");
			appendProperties((Properties) value);
			appendLine("}");
		} else {
			throw new IllegalArgumentException(key);
		}
		addContent(System.lineSeparator());

		return this;
	}

	/**
	 * Adds the string value.
	 *
	 * @param value
	 *            the value
	 */
	private void addStringValue(String value) {
		addContent("'" + value + "'");
	}

	/**
	 * Append suffix.
	 *
	 * @param sb
	 *            the sb
	 */
	private void appendSuffix(StringBuilder sb) {
		if (contentName != null) {
			sb.append(indent).append("}").append(System.lineSeparator());
		}
	}

	/**
	 * Append prefix.
	 *
	 * @param sb
	 *            the sb
	 */
	private void appendPrefix(StringBuilder sb) {
		if (contentName != null) {
			sb.append(indent);
			sb.append(contentName);
			sb.append(" {").append(System.lineSeparator());
		}
	}

}