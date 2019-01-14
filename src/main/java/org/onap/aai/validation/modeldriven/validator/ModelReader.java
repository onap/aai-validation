/**
 * ============LICENSE_START===================================================
 * Copyright (c) 2018 Amdocs
 * ============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=====================================================
 */
package org.onap.aai.validation.modeldriven.validator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.dom4j.Node;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.modeldriven.ModelCacheManager;
import org.onap.aai.validation.modeldriven.ModelId;
import org.onap.aai.validation.modeldriven.configuration.mapping.Filter;
import org.onap.aai.validation.modeldriven.configuration.mapping.ModelInstanceMapper;
import org.onap.aai.validation.modeldriven.configuration.mapping.ModelInstanceMapper.MappingType;
import org.onap.aai.validation.modeldriven.parser.XMLModelParser;

/**
 * Reads values from the model.
 */
public class ModelReader {

    private static final String ATTRIBUTE_MODELTYPE = "model-type";

    /**
     * Do not instantiate an object of this class
     */
    private ModelReader() {
        // Deliberately empty
    }

    /**
     * Gets the values of a model element as defined by the model-instance mapping configuration. When the mapping type
     * is "attribute", the multimap will be returned with a null value.
     *
     * @param modelElement
     *        the model element from which the values will be extracted
     * @param mapping
     *        the model-instance mapping object defining the path to the model values
     * @param modelCacheManager
     *        the model cache manager used to retrieve further models
     * @return a {@link Multimap} of model values.
     * @throws ValidationServiceException
     */
    public static Multimap<String, Node> getValues(Node modelElement, ModelInstanceMapper mapping,
            ModelCacheManager modelCacheManager) throws ValidationServiceException {
        Multimap<String, Node> values = HashMultimap.create();

        if (MappingType.ATTRIBUTE.equals(mapping.getMappingType())) {
            // Get attributes on current model element.
            Multimap<String, Node> modelValues = getModelValues(modelElement, mapping, false);
            if (modelValues.isEmpty()) {
                throw new ValidationServiceException(ValidationServiceError.MODEL_VALUE_ERROR,
                        mapping.getModel().getValue(), modelElement.asXML());
            }
            values.putAll(modelValues);
        } else {
            // Get related objects.
            getValuesAndModels(modelElement, mapping, modelCacheManager, values);
        }


        return values;
    }

    /**
     * Returns the model type property of the current model element.
     *
     * @param model
     *        The current model element.
     * @return the model type of the current element or null if not found.
     */
    public static String getModelType(Node model) {
        String modelType = null;
        List<Node> modelTypeElements = XMLModelParser.getObjectsFromXPath(model, ATTRIBUTE_MODELTYPE);
        if (!modelTypeElements.isEmpty()) {
            modelType = modelTypeElements.iterator().next().getText();
        }
        return modelType;
    }

    /**
     * @param model
     * @param mapping
     * @return True if supplied model is of type widget.
     */
    public static boolean isValidModelType(Node model, ModelInstanceMapper mapping) {
        Collection<String> validTypes = mapping.getModel().getFilter().getValid();
        return !validTypes.isEmpty() && validTypes.contains(getModelType(model));
    }

    /**
     * Populates a Multimap of models. If a root and filter are defined in the mapping it will navigate the model to
     * find a valid models according to the filter. If the root property is not defined a model is not returned.
     *
     * @param model
     *        the model to be inspected
     * @param mapping
     *        the model-instance mapping object defining the root model
     * @param modelCacheManager
     *        the model cache manager used to retrieve further models
     * @param models
     *        a Multimap of models that will be populated with further models
     * @throws ValidationServiceException
     */
    public static void getValuesAndModels(Node model, ModelInstanceMapper mapping, ModelCacheManager modelCacheManager,
            Multimap<String, Node> models) throws ValidationServiceException {
        String root = mapping.getModel().getRoot();

        if (root == null) {
            return;
        }

        List<Node> childModelElements = XMLModelParser.getObjectsFromXPath(model, root);
        for (Node childModel : childModelElements) {
            // If the child element is a leaf, this could either mean the end of the hierarchy, or that we have
            // encountered a resource and need to retrieve a separate model to continue the model traversal.
            List<String> modelNames = getModelValuesList(childModel, mapping);
            if (!hasChildren(childModel, root) && !isValidModel(childModel, mapping.getModel().getFilter())
                    && mapping.getModel().getId() != null) {
                childModel = getChildModelNode(modelCacheManager, childModel, mapping);
            }

            if (isValidModel(childModel, mapping.getModel().getFilter())) {
                for (String modelName : modelNames) {
                    models.put(modelName, childModel);
                }
            } else {
                getValuesAndModels(childModel, mapping, modelCacheManager, models);
            }
        }
    }

    /**
     * Find the next child model given a specific node.
     *
     * @param modelCacheManager
     *        the model cache manager used to retrieve further models
     * @param node
     *        the top-level node under which child model nodes are searched
     * @param rootXPath
     *        the path expression to apply to the node to find child elements
     * @param modelIdPath
     *        the path expression to apply to the node to find the child model IDs
     * @return either or the {@code node} if there were no matches for {@code id}
     * @throws ValidationServiceException
     */
    private static Node getChildModelNode(ModelCacheManager modelCacheManager, Node node, ModelInstanceMapper mapping)
            throws ValidationServiceException {
        Node childModel = node;

        // Get the model for the specified node to check its type.
        // Only one model ID is expected, although the API returns a list.
        List<Node> childModelIds = XMLModelParser.getObjectsFromXPath(node, mapping.getModel().getId());

        if (!childModelIds.isEmpty()) {
            // Found the child model ID, so retrieve the child model from cache.
            ModelId modelId =
                    new ModelId(ModelId.ATTR_MODEL_NAME_VERSION_ID, childModelIds.iterator().next().getText());
            Node fullChildModel = modelCacheManager.get(modelId);

            if (fullChildModel != null && !isValidModelType(fullChildModel, mapping)) {
                // Child model is not a widget so replace current child model with the full child model
                // retrieved from the cache.
                List<Node> fullChildModelElements =
                        XMLModelParser.getObjectsFromXPath(fullChildModel, mapping.getModel().getRoot());
                // Only one crown widget is expected, although the API returns a list.
                childModel = fullChildModelElements.isEmpty() ? node : fullChildModelElements.iterator().next();
            }
        }

        return childModel;
    }

    private static Multimap<String, Node> getModelValues(Node model, ModelInstanceMapper mapping, boolean addModel) {
        Multimap<String, Node> values = HashMultimap.create();
        List<String> valueStrings = getModelValuesList(model, mapping);
        for (String value : valueStrings) {
            values.put(value, addModel ? model : null); // NOSONAR
        }
        return values;
    }

    private static List<String> getModelValuesList(Node model, ModelInstanceMapper mapping) {
        List<String> values = new ArrayList<>();
        List<Node> valueElements = XMLModelParser.getObjectsFromXPath(model, mapping.getModel().getValue());
        for (Node node : valueElements) {
            values.add(node.getText());
        }
        return values;
    }

    private static boolean isValidModel(Node node, Filter filter) {
        if (filter == null) {
            return true;
        }

        final List<String> validValues = filter.getValid();

        if (!validValues.isEmpty() && filter.getPath() != null) {
            for (Node filterNode : XMLModelParser.getObjectsFromXPath(node, filter.getPath())) {
                if (validValues.contains(filterNode.getText())) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean hasChildren(Node parent, String rootXPath) {
        return !XMLModelParser.getObjectsFromXPath(parent, rootXPath).isEmpty();
    }
}
