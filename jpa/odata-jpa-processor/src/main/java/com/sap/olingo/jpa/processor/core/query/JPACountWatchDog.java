package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.COUNT_NON_SUPPORTED_COUNT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.CountOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.helper.AbstractWatchDog;

/**
 *
 * @author Oliver Grande
 * @since 1.1.1
 * 12.04.2023
 */

class JPACountWatchDog extends AbstractWatchDog {

  static final String TERM = "CountRestrictions";
  static final String VOCABULARY_ALIAS = "Capabilities";
  static final String COUNTABLE = "Countable";
  static final String NON_COUNTABLE_PROPERTIES = "NonCountableProperties";
  static final String NON_COUNTABLE_NAVIGATION_PROPERTIES = "NonCountableNavigationProperties";

  private final String externalName;
  private final Optional<CsdlAnnotation> annotation;
  private final boolean isCountable;
  private final List<String> nonCountableProperties;
  private final List<String> nonCountableNavigationProperties;

  JPACountWatchDog(final Optional<JPAAnnotatable> annotatable) throws ODataJPAQueryException {

    Map<String, CsdlExpression> properties = Collections.emptyMap();
    if (annotatable.isPresent()) {
      try {
        externalName = annotatable.get().getExternalName();
        annotation = Optional.ofNullable(annotatable.get().getAnnotation(VOCABULARY_ALIAS, TERM));
        if (annotation.isPresent())
          properties = getAnnotationProperties(annotation);
        else
          properties = Collections.emptyMap();
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    } else {
      annotation = Optional.empty();
      externalName = "";
    }
    this.isCountable = determineCountable(properties);
    this.nonCountableProperties = determineNonCountableProperties(properties);
    this.nonCountableNavigationProperties = determineNonCountableNavigationProperties(properties);
  }

  /**
   * Example requests:
   * - /AnnotationsParents?$count=true
   * - /AnnotationsParents/$count
   * - /AdministrativeDivisions(CodePublisher='Eurostat',CodeID='NUTS2',DivisionCode='BE24')/Children/$count
   * - /AdministrativeDivisions(CodePublisher='Eurostat',CodeID='NUTS2',DivisionCode='BE24')/Children?$count=true
   * - /Organizations('1')/Comment/$count
   * - /Organizations('1')/Comment?$count=true
   * - /Collections('501')/Nested/$count
   * @param uriResource
   * @throws ODataJPAQueryException
   * @throws ODataJPAProcessorException
   */
  void watch(final UriInfoResource uriResource) throws ODataJPAProcessorException {
    final boolean count = determineCount(uriResource);
    if (count) {
      if (!isCountable)
        throw new ODataJPAProcessorException(COUNT_NON_SUPPORTED_COUNT, HttpStatusCode.BAD_REQUEST, externalName);
      if (!propertyIsCountable(uriResource))
        throw new ODataJPAProcessorException(COUNT_NON_SUPPORTED_COUNT, HttpStatusCode.BAD_REQUEST, externalName);
      if (!navigationIsCountable(uriResource))
        throw new ODataJPAProcessorException(COUNT_NON_SUPPORTED_COUNT, HttpStatusCode.BAD_REQUEST, externalName);
    }
  }

  private boolean navigationIsCountable(final UriInfoResource uriResource) {
    return !nonCountableNavigationProperties.contains(buildPath(uriResource));
  }

  private boolean propertyIsCountable(final UriInfoResource uriResource) {
    return !nonCountableProperties.contains(buildPath(uriResource));
  }

  private String buildPath(final UriInfoResource uriResource) {
    final List<String> pathItems = new ArrayList<>(uriResource.getUriResourceParts().size());
    for (int i = 1; i < uriResource.getUriResourceParts().size() - 1; i++) {
      final UriResource resourcePart = uriResource.getUriResourceParts().get(i);
      if (resourcePart instanceof UriResourceNavigation)
        pathItems.add(((UriResourceNavigation) resourcePart).getProperty().getName());
      if (resourcePart instanceof UriResourceProperty && ((UriResourceProperty) resourcePart).isCollection())
        pathItems.add(((UriResourceProperty) resourcePart).getProperty().getName());
    }
    return pathItems.stream().collect(Collectors.joining("/"));
  }

  private boolean determineCount(final UriInfoResource uriResource) {
    final List<UriResource> uriResourceParts = uriResource.getUriResourceParts();
    if (uriResourceParts != null
        && !uriResourceParts.isEmpty()
        && uriResourceParts.get(uriResourceParts.size() - 1).getKind() == UriResourceKind.count)
      return true;

    return Optional.ofNullable(uriResource.getCountOption())
        .map(CountOption::getValue)
        .orElse(false);

  }

  String getExternalName() {
    return externalName;
  }

  boolean isCountable() {
    return isCountable;
  }

  List<String> getNonCountableProperties() {
    return nonCountableProperties;
  }

  List<String> getNonCountableNavigationProperties() {
    return nonCountableNavigationProperties;
  }

  private boolean determineCountable(final Map<String, CsdlExpression> properties) {
    return determineConstantExpression(properties, COUNTABLE)
        .map(Boolean::valueOf)
        .orElse(true);
  }

  private List<String> determineNonCountableProperties(final Map<String, CsdlExpression> properties) {
    return getNavigationPathItems(properties, NON_COUNTABLE_PROPERTIES);
  }

  private List<String> determineNonCountableNavigationProperties(final Map<String, CsdlExpression> properties) {
    return getNavigationPathItems(properties, NON_COUNTABLE_NAVIGATION_PROPERTIES);
  }

}
