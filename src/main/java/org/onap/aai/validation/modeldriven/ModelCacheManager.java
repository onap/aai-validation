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
package org.onap.aai.validation.modeldriven;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.apache.http.client.utils.URIBuilder;
import org.dom4j.Element;
import org.onap.aai.validation.config.ModelConfig;
import org.onap.aai.validation.config.RestConfig;
import org.onap.aai.validation.data.client.RestClient;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.modeldriven.parser.XMLModelParser;

/**
 * A cache of model UUID against the parsed model Element, using Guava's CacheBuilder.
 */
public class ModelCacheManager {

	public static final String FILE_MODEL_PROTOCOL = "file";

	private LoadingCache<ModelId, Element> modelCache;
	private RestConfig restConfig;

	/**
	 * Initialises the instance by loading validator properties from config.
	 *
	 * @param modelConfig
	 * @param restConfig
	 * @throws ValidationServiceException
	 */
	@Inject
	public ModelCacheManager(ModelConfig modelConfig, RestConfig restConfig) {
		this.restConfig = restConfig;

		// Create an expiring cache with a load implementation which is executed when a key value is not cached.
		modelCache = CacheBuilder.newBuilder().maximumSize(1000)
				.expireAfterWrite(modelConfig.getModelCacheExpirySeconds(), TimeUnit.SECONDS)
				.build(new CacheLoader<ModelId, Element>() {
					@Override
					public Element load(ModelId key) throws ValidationServiceException {
						return retrieveModelElement(key);
					}
				});
	}

	/**
	 * Gets the model with specified uuid from the model cache. If model is not cached, retrieve the model from the
	 * external system.
	 *
	 * @param uuid
	 *            The model UUID.
	 * @return The DOM Element representing the model's root element.
	 * @throws ValidationServiceException
	 */
	public Element get(ModelId uuid) throws ValidationServiceException {
		if (uuid == null || uuid.isEmpty()) {
			return null;
		}

		return getCachedModel(uuid);
	}

	private Element getCachedModel(ModelId uuid) throws ValidationServiceException {
		Element element = null;
		try {
			element = modelCache.get(uuid);
		} catch (ExecutionException e) {
			// If the wrapped exception is a model validation error, return null.
			Throwable cause = e.getCause();
			if (cause != null && cause.getClass().equals(ValidationServiceException.class)
					&& (((ValidationServiceException) cause).getId().equals(ValidationServiceError.MODEL_NOT_FOUND.getId())
							|| ((ValidationServiceException) cause).getId().equals(ValidationServiceError.REST_CLIENT_RESPONSE_NOT_FOUND.getId()))) {
				return null;
			}
			throw new ValidationServiceException(ValidationServiceError.MODEL_CACHE_ERROR, e, "");
		}
		return element;
	}

	/**
	 * Puts an item into the model cache.
	 *
	 * @param uuid
	 *            The key.
	 * @param modelElement
	 *            The value.
	 */
	public void put(ModelId uuid, Element modelElement) {
		modelCache.put(uuid, modelElement);
	}

	/**
	 * Constructs and invokes the configured REST URL. The XML payload is then parsed into a model Element.
	 *
	 * @param uuid
	 *            The model UUID to retrieve.
	 * @return The payload of the REST URL call as a model Element.
	 * @throws ValidationServiceException
	 *             if the payload is null
	 */
	private Element retrieveModelElement(ModelId uuid) throws ValidationServiceException {
		String protocol = restConfig.getProtocol();
		Element modelElement;
		if (FILE_MODEL_PROTOCOL.equals(protocol)) {
			File modelFile = new File(restConfig.getBaseModelURI());
			modelElement = retrieveModelElementFromFile(modelFile, uuid);
		} else {
			modelElement = retrieveModelElementFromREST(restConfig, uuid);
		}

		// Do not store a null value in the CacheBuilder
		if (modelElement != null) {
			return modelElement;
		} else {
			throw new ValidationServiceException(ValidationServiceError.MODEL_NOT_FOUND, uuid);
		}
	}

	/**
	 * Constructs and invokes the configured REST URL to retrieve the model for the supplied UUID. The XML payload is
	 * then parsed into a model Element.
	 *
	 * @param uuid
	 *            The model UUID to retrieve.
	 * @return The payload of the REST URL call as a model Element, or null of no model could be found.
	 * @throws ValidationServiceException
	 */
	private Element retrieveModelElementFromREST(RestConfig restConfig, ModelId uuid)
			throws ValidationServiceException {
		try {
			URI restURI = new URIBuilder(restConfig.getBaseModelURI())
					.addParameter(uuid.getModelIdAttribute(), uuid.getModelId()).build();
			String restPayload = new RestClient(restConfig).get(restURI.toString(), MediaType.APPLICATION_XML);
			return XMLModelParser.parse(restPayload, true);
		} catch (URISyntaxException e) {
			throw new ValidationServiceException(ValidationServiceError.MODEL_RETRIEVAL_ERROR, e);
		}
	}

	/**
	 * Retrieves the model with supplied model UUID from the supplied model file. The XML payload is then parsed into a
	 * model Element.
	 *
	 * @param uuid
	 *            The model UUID to retrieve.
	 * @return The model Element, or null if no model could be found.
	 * @throws ValidationServiceException
	 */
	private Element retrieveModelElementFromFile(File modelFile, ModelId uuid) throws ValidationServiceException {
		Element rootElement = XMLModelParser.parse(modelFile, false);
		// Check that the root element of the model file is correct.
		if (!XMLModelParser.MODELS_ROOT_ELEMENT.equals(rootElement.getName())) {
			return null;
		}
		return XMLModelParser.getModelElementWithId(rootElement, uuid.getModelIdAttribute(), uuid.getModelId());
	}
}