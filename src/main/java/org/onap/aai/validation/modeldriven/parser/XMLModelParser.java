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
package org.onap.aai.validation.modeldriven.parser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;

/**
 * Read models from XML file
 *
 */
public class XMLModelParser {

	public static final String MODEL_ROOT_ELEMENT = "model";
	public static final String MODELS_ROOT_ELEMENT = "models";

	private XMLModelParser() {
		// Do nothing
	}

	/**
	 * Parses an xml file and returns the root Element.
	 *
	 * @param modelFile
	 *            The XML File.
	 * @param validateModel
	 *            If true the model will be validated.
	 * @return The root Element of the document.
	 * @throws DocumentException
	 * @throws ValidationServiceException
	 */
	public static Element parse(File modelFile, boolean validateModel) throws ValidationServiceException {
		try {
			byte[] encoded = Files.readAllBytes(modelFile.toPath());
			String modelString = new String(encoded, Charset.defaultCharset());

			return parse(modelString, validateModel);
		} catch (IOException e) {
			throw new ValidationServiceException(ValidationServiceError.MODEL_PARSE_ERROR, e, modelFile.toString());
		}
	}

	/**
	 * Parses an xml String and returns the root Element.
	 *
	 * @param modelString
	 *            The XML String.
	 * @param validateModel
	 *            If true the model will be validated.
	 * @return The root Element of the document, or null if no valid model can be parsed.
	 * @throws DocumentException
	 */
	public static Element parse(String modelString, boolean validateModel) throws ValidationServiceException {
		// Strip namespace information first.
		String model = removeXmlStringNamespaceAndPreamble(modelString);

		try {
			Element modelElement = DocumentHelper.parseText(model).getRootElement();
			if (validateModel && (modelElement == null || !isValidModel(modelElement))) {
				return null;
			}
			return modelElement;
		} catch (DocumentException e) {
			throw new ValidationServiceException(ValidationServiceError.MODEL_PARSE_ERROR, e, model);
		}
	}

	/**
	 * Returns the result of running the XPath expression on the DOM Node.
	 *
	 * @param currentNode
	 *            The current Node being processed.
	 * @param xPath
	 *            The XPath expression to run on the Node.
	 * @return A List of Nodes representing the result of the XPath expression.
	 */
	public static List<Node> getObjectsFromXPath(Node currentNode, String xPath) {
		return currentNode.selectNodes(xPath);
	}

	/**
	 * Returns the child model Element that corresponds to the provided root Element and model ID.
	 *
	 * @param rootElement
	 *            The root Element to search.
	 * @param attributeName
	 *            The name of the attribute that holds the model ID.
	 * @param modelId
	 *            The model ID to search for.
	 * @return The model Element that matches the model ID supplied.
	 */
	public static Element getModelElementWithId(Element rootElement, String attributeName, String modelId) {
		Element modelElement = null;
		List<Node> modelsList = getObjectsFromXPath(rootElement, MODEL_ROOT_ELEMENT + "/" + attributeName);
		for (Node model : modelsList) {
			if (model.getText().equals(modelId)) {
				modelElement = model.getParent();
			}
		}
		return modelElement;
	}

	/**
	 * Determines the validity of the supplied model element by checking if the name of the root element is correct.
	 *
	 * @param modelElement
	 *            The model element to check.
	 * @return True if the root element matches the expected name.
	 */
	private static boolean isValidModel(Element modelElement) {
		return MODEL_ROOT_ELEMENT.equals(modelElement.getName());
	}

	/**
	 * Strips all namespace information from the model XML.
	 *
	 * @param modelXML
	 *            The model XML as a String.
	 * @return The model XML String minus the namespace information.
	 */
	private static String removeXmlStringNamespaceAndPreamble(String xmlString) {
		return xmlString.replaceAll("(<\\?[^<]*\\?>)?", "") /* remove preamble */
				.replaceAll("xmlns.*?(\"|\').*?(\"|\')", "") /* remove xmlns declaration */
				.replaceAll("(<)(\\w+:)(.*?>)", "$1$3") /* remove opening tag prefix */
				.replaceAll("(</)(\\w+:)(.*?>)", "$1$3"); /* remove closing tags prefix */
	}
}
