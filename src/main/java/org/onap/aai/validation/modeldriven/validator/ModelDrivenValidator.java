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
package org.onap.aai.validation.modeldriven.validator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Node;
import org.onap.aai.validation.Validator;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.modeldriven.ModelCacheManager;
import org.onap.aai.validation.modeldriven.ModelId;
import org.onap.aai.validation.modeldriven.configuration.mapping.ModelInstanceMapper;
import org.onap.aai.validation.modeldriven.configuration.mapping.ModelInstanceMappingReader;
import org.onap.aai.validation.modeldriven.configuration.mapping.ModelInstanceMapper.MappingType;
import org.onap.aai.validation.reader.EntityReader;
import org.onap.aai.validation.reader.EventReader;
import org.onap.aai.validation.reader.InstanceEntityReader;
import org.onap.aai.validation.reader.data.Entity;
import org.onap.aai.validation.reader.data.EntityId;
import org.onap.aai.validation.result.ValidationResult;
import org.onap.aai.validation.result.Violation;
import org.onap.aai.validation.result.Violation.Builder;
import org.onap.aai.validation.result.Violation.ViolationType;

/**
 * Validates object instances against the ONAP model.
 *
 */
public class ModelDrivenValidator implements Validator {

	/**
	 * Types of validation rules.
	 */
	public enum RuleType {
		REL, ATTR
	}

	/**
	 * Types of validation outcomes.
	 */
	public enum ValidationOutcomeType {
		NO_MODEL, MISSING, UNEXPECTED;
	}

	private ModelCacheManager modelCacheManager;
	private List<ModelInstanceMapper> mappings;
	private InstanceReader instanceReader;
	private EventReader eventReader;

	/**
	 * Constructor defining injected dependencies.
	 *
	 * @param modelCacheManager
	 *            a cache manager for the models
	 * @param modelInstanceMappingReader
	 *            a configuration reader to provide model and instance mapping
	 * @param instanceReader
	 *            a reader of A&AI instances
	 * @param eventReader
	 * @throws ValidationServiceException
	 */
	@Inject
	public ModelDrivenValidator(ModelCacheManager modelCacheManager, ModelInstanceMappingReader modelInstanceMappingReader, InstanceReader instanceReader,
			EventReader eventReader) throws ValidationServiceException {
		this.modelCacheManager = modelCacheManager;
		this.mappings = modelInstanceMappingReader.getMappings();
		this.instanceReader = instanceReader;
		this.eventReader = eventReader;
	}

	@Override
	public void initialise() throws ValidationServiceException {
		// Deliberately empty
	}

	/**
	 * Validates the given event instance against the ECOMP model.
	 *
	 * @param eventInstance
	 *            the event instance to be validated
	 * @return {@link Violation} with the results of the object validation
	 * @throws ValidationServiceException
	 */
	@Override
	public List<ValidationResult> validate(String eventInstance) throws ValidationServiceException {
		// Read event json into Entity bean
		Entity eventEntity = eventReader.getEntity(eventInstance);

		EntityReader reader = new InstanceEntityReader(instanceReader);
		String entityJson = instanceReader.getInstance(eventEntity.getJson(), mappings);
		Entity instanceEntity = new Entity(entityJson, instanceReader.getInstanceType(entityJson), eventEntity.getEntityLink(), reader);

		// Get model ID from object instance and retrieve corresponding model.
		ModelId modelId = new ModelId(ModelId.ATTR_MODEL_ID, instanceReader.getModelId(instanceEntity.getJson()));
		Node modelElement = modelCacheManager.get(modelId);

		List<Violation> violations = new ArrayList<>();

		if (modelElement == null) {
			ViolationInfo info = ViolationInfo.valueOf(ModelDrivenValidator.ValidationOutcomeType.NO_MODEL.toString());
			Map<String, Object> details = new HashMap<>();
			details.put("No model ID", modelId.getModelId());
			Violation.Builder builder = new Violation.Builder(instanceEntity).violationType(ViolationType.MODEL);
			builder.category(info.getCategory()).severity(info.getSeverity()).violationDetails(details)
					.errorMessage(info.getErrorMessage(modelId.getModelId()));
			violations.add(builder.build());
		} else {
			// Validate model with instance according to mappings.
			for (ModelInstanceMapper mapping : mappings) {
				List<Violation> currentViolations = new ArrayList<>();

				// If we are validating related objects, find the first valid child object to begin validating from.
				Node validModelElement = modelElement;
				if (MappingType.RELATIONSHIP.equals(mapping.getMappingType()) && !ModelReader.isValidModelType(modelElement, mapping)) {
					Multimap<String, Node> models = HashMultimap.create();
					ModelReader.getValuesAndModels(modelElement, mapping, modelCacheManager, models);
					validModelElement = models.isEmpty() ? modelElement : models.values().iterator().next();
				}

				validateAllRecursive(validModelElement, instanceEntity, mapping, currentViolations, reader);
				violations.addAll(currentViolations);
			}
		}

		ValidationResult validationResult = new ValidationResult(instanceEntity);

		// This is a shortcut to passing the parent model name all the way down.
		populateViolationModelNames(violations, instanceEntity);

		validationResult.addViolations(violations);

		return Arrays.asList(validationResult);
	}

	/*
	 * Recursively validates all child entities starting with the crown widget.
	 */
	private void validateAllRecursive(Node currentModelNode, Entity entity, ModelInstanceMapper mapping, List<Violation> validations, EntityReader reader)
			throws ValidationServiceException {
		String entityLink = null;
		Multimap<String, Node> modelMap = ModelReader.getValues(currentModelNode, mapping, modelCacheManager);
		Multimap<String, String> instanceMap = instanceReader.getValues(entity.getJson(), mapping);

		// Validate model with instance according to mappings.
		// Note: Currently the cardinality of instances are not validated.
		List<Violation> currentViolations = validateModelInstanceValues(modelMap.keySet(), instanceMap.keySet(), entity, mapping);
		validations.addAll(currentViolations);

		// Remove erroring objects from the maps so that we don't validate their children.
		for (Violation currentViolation : currentViolations) {
			String entityType = (String) currentViolation.getViolationDetails().get(Violation.ENTITY_TYPE_PROPERTY);
			if (entityType != null) {
				modelMap.removeAll(entityType);
				instanceMap.removeAll(entityType);
			}
		}

		// Continue down the model hierarchy for objects that did not error in the current layer.
		for (Entry<String, Node> modelEntry : modelMap.entries()) {
			// Get the child model.
			Node childModelNode = modelEntry.getValue();
			if (childModelNode != null) {
				// Validate all child instance objects with current child model.
				Collection<String> childInstanceObjects = instanceMap.get(modelEntry.getKey());
				for (String childInstanceObject : childInstanceObjects) {
					Entity childEntity = new Entity(childInstanceObject, instanceReader.getInstanceType(childInstanceObject), entityLink, reader);
					validateAllRecursive(childModelNode, childEntity, mapping, validations, reader);
				}
			}
		}
	}

	/**
	 * Compares the List of values found in the model with the values found in the Json and generates validation errors
	 * based on the differences.
	 *
	 * @param modelValues
	 *            the values found in the model
	 * @param instanceValues
	 *            the values found in the Json.
	 * @param entity
	 * @param modelInstanceMapper
	 *            the mappings used to to find the model and instance values
	 * @return List of Validation objects representing the errors found during validation.
	 * @throws ValidationServiceException
	 */
	private List<Violation> validateModelInstanceValues(Set<String> modelValues, Set<String> instanceValues, Entity entity,
			ModelInstanceMapper modelInstanceMapper) throws ValidationServiceException {
		List<Violation> violations = new ArrayList<>();

		Collection<?> missingValues = CollectionUtils.subtract(modelValues, instanceValues);
		violations.addAll(setViolationDetails(ValidationOutcomeType.MISSING, missingValues, entity, modelInstanceMapper));

		Collection<?> unexpectedValues = CollectionUtils.subtract(instanceValues, modelValues);
		violations.addAll(setViolationDetails(ValidationOutcomeType.UNEXPECTED, unexpectedValues, entity, modelInstanceMapper));

		return violations;
	}

	/*
	 * Sets the list of {@link Violation} objects with all the violation details.
	 *
	 * @param outcome
	 *
	 * @param values the values that are causing the violation
	 *
	 * @param entity the entity being validated
	 *
	 * @param modelInstanceMapper the mappings used to to find the model and instance values
	 *
	 * @return list of {@link Violation} objects set by this method
	 *
	 * @throws ValidationServiceException
	 */
	private List<Violation> setViolationDetails(ValidationOutcomeType outcome, Collection<?> values, Entity entity, ModelInstanceMapper modelInstanceMapper)
			throws ValidationServiceException {

		List<Violation> violations = new ArrayList<>();
		Builder builder = new Builder(entity).violationType("Model");

		for (Object value : values) {
			RuleType ruleType = modelInstanceMapper.getMappingType().equals(MappingType.RELATIONSHIP) ? RuleType.REL : RuleType.ATTR;
			String category = outcome.toString() + "_" + ruleType;

			ViolationInfo info = ViolationInfo.valueOf(category);
			builder.category(info.getCategory());
			builder.severity(info.getSeverity());

			Map<String, Object> details = new HashMap<>();
			details.put(outcome.toString() + " " + ruleType.toString(), value);

			switch (ruleType) {
			case ATTR:
				builder.errorMessage(info.getErrorMessage(value));
				break;
			case REL:
				buildEntityIdDetails(details, entity);
				builder.errorMessage(info.getErrorMessage(entity.getIds().toString(), entity.getType(), value));
				break;
			default:
				// Do nothing
				break;
			}
			builder.violationDetails(details);
			violations.add(builder.build());
		}

		return violations;
	}

	/*
	 * Add entity IDs to the violation details
	 *
	 * @param details the violation details to populate
	 *
	 * @param entity
	 *
	 * @throws ValidationServiceException
	 */
	private void buildEntityIdDetails(Map<String, Object> details, Entity entity) throws ValidationServiceException {
		JsonObject entityIdsObject = new JsonObject();
		for (EntityId entityId : entity.getIds()) {
			entityIdsObject.addProperty(entityId.getPrimaryKey(), entityId.getValue());
		}
		details.put(Violation.ENTITY_ID_PROPERTY, entityIdsObject);
		details.put(Violation.ENTITY_TYPE_PROPERTY, entity.getType());
		details.put(Violation.ENTITY_MODELNAME_PROPERTY, instanceReader.getModelName(entity.getJson()));
	}

	/*
	 * Sets the model name attribute on all violations in the list. The model name is retrieved from the entity
	 * provided.
	 */
	private void populateViolationModelNames(List<Violation> violations, Entity entity) {
		String modelName = instanceReader.getModelName(entity.getJson());
		for (Violation violation : violations) {
			violation.setModelName(modelName);
		}
	}
}
