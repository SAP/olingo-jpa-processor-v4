package com.sap.olingo.jpa.processor.core.filter;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException.MessageKeys.FILTERING_MISSING_PROPERTIES;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException.MessageKeys.FILTERING_NOT_SUPPORTED;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException.MessageKeys.FILTERING_REQUIRED;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Expression;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlDynamicExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlPropertyPath;
import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.helper.AbstractWatchDog;

/**
 * In case an annotatable artifact has been annotated with Capabilities#FilterRestrictions, it is checked that the
 * generated WHERE clause fulfills the specified restrictions. See:
 * <a href =
 * "https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Capabilities.V1.md#FilterRestrictionsType"><i>FilterRestrictionsType</i></a>
 *
 * @author Oliver Grande
 * @since 1.1.1
 * 08.05.2023
 */
public class JPAFilterRestrictionsWatchDog extends AbstractWatchDog {

  static final String REQUIRED_PROPERTIES = "RequiredProperties";
  static final String FILTERABLE = "Filterable";
  static final String REQUIRES_FILTER = "RequiresFilter";
  static final String VOCABULARY_ALIAS = "Capabilities";
  static final String TERM = "FilterRestrictions";

  private final Optional<CsdlAnnotation> annotation;
  private final Map<String, CsdlExpression> properties;
  private final String externalName;
  private final Set<String> requiredPropertyPath;
  private boolean singleEntityRequested;

  public JPAFilterRestrictionsWatchDog(final JPAAnnotatable annotatable, final boolean singleEntityRequested)
      throws ODataJPAQueryException {
    try {
      if (annotatable != null) {
        externalName = annotatable.getExternalName();
        annotation = Optional.ofNullable(annotatable.getAnnotation(VOCABULARY_ALIAS, TERM));
        properties = determineProperties();
        requiredPropertyPath = extractRequiredPropertyPath(properties.get(REQUIRED_PROPERTIES));
      } else {
        externalName = "";
        annotation = Optional.empty();
        properties = Collections.emptyMap();
        requiredPropertyPath = Collections.emptySet();
      }
      this.singleEntityRequested = singleEntityRequested;
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  public void watch(final Expression<Boolean> filter) throws ODataJPAFilterException {
    watchFilterable(filter);
    watchRequiresFilter(filter);
    watchRequiredProperties();
  }

  public void watch(final JPAPath attributePath) {
    if (attributePath != null)
      requiredPropertyPath.remove(attributePath.getAlias());
  }

  Set<String> getRequiredPropertyPath() {
    return Collections.unmodifiableSet(requiredPropertyPath);
  }

  private Map<String, CsdlExpression> determineProperties() {
    if (annotation.isPresent()) {
      return getAnnotationProperties(annotation);
    } else
      return Collections.emptyMap();
  }

  private Set<String> extractRequiredPropertyPath(final CsdlExpression expression) {
    if (expression != null)
      return expression.asDynamic().asCollection().getItems().stream()
          .map(CsdlExpression::asDynamic)
          .map(CsdlDynamicExpression::asPropertyPath)
          .map(CsdlPropertyPath::getValue)
          .collect(Collectors.toSet());
    else
      return Collections.emptySet();
  }

  private boolean filteringIsAllowed() {
    final CsdlExpression filterable = properties.get(FILTERABLE);
    return filterable == null
        || (Boolean.valueOf(filterable.asConstant().getValue()));
  }

  private boolean filterIsRequired() {
    final CsdlExpression requiresFilter = properties.get(REQUIRES_FILTER);
    return requiresFilter != null
        && Boolean.valueOf(requiresFilter.asConstant().getValue())
        && !singleEntityRequested;
  }

  private void watchFilterable(final Expression<Boolean> filter) throws ODataJPAFilterException {
    if (!filteringIsAllowed()
        && filter != null)
      throw new ODataJPAFilterException(FILTERING_NOT_SUPPORTED, HttpStatusCode.BAD_REQUEST,
          externalName);
  }

  private void watchRequiredProperties() throws ODataJPAFilterException {
    if (filterIsRequired()
        && !requiredPropertyPath.isEmpty()) {

      final String missingProperties = requiredPropertyPath.stream()
          .collect(Collectors.joining(" ,"));
      // Filter not found for required properties '%1$s' at '%2$s'
      throw new ODataJPAFilterException(FILTERING_MISSING_PROPERTIES, HttpStatusCode.BAD_REQUEST,
          missingProperties, externalName);
    }
  }

  private void watchRequiresFilter(final Expression<Boolean> filter) throws ODataJPAFilterException {
    if (filterIsRequired()
        && filter == null)
      throw new ODataJPAFilterException(FILTERING_REQUIRED, HttpStatusCode.BAD_REQUEST,
          externalName);
  }
}
