package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_CHECK_ASCENDING_REQUIRED_FOR;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_CHECK_DESCENDING_REQUIRED_FOR;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_CHECK_SORTING_NOT_SUPPORTED;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_CHECK_SORTING_NOT_SUPPORTED_FOR;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.criteria.Order;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.helper.AbstractWatchDog;

/**
 * In case an annotatable artifact has been annotated with Capabilities#SortRestrictions, it is checked that the
 * generated ORDER BY fulfills the specified restrictions. See:
 * <a href =
 * "https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Capabilities.V1.md#SortRestrictionsType"><i>SortRestrictions</i></a>
 * @author Oliver Grande
 * @since 1.1.1
 * 20.03.2023
 */
public class JPAOrderByBuilderWatchDog extends AbstractWatchDog {

  static final String DESCENDING_ONLY_PROPERTIES = "DescendingOnlyProperties";
  static final String NON_SORTABLE_PROPERTIES = "NonSortableProperties";
  static final String SORTABLE = "Sortable";
  static final String ASCENDING_ONLY_PROPERTIES = "AscendingOnlyProperties";
  static final String VOCABULARY_ALIAS = "Capabilities";
  static final String TERM = "SortRestrictions";

  private final Optional<CsdlAnnotation> annotation;
  private final Map<String, CsdlExpression> properties;
  private final String externalName;

  JPAOrderByBuilderWatchDog(@Nonnull final JPAAnnotatable annotatable) throws ODataJPAQueryException {

    try {
      externalName = annotatable.getExternalName();
      annotation = Optional.ofNullable(annotatable.getAnnotation(VOCABULARY_ALIAS, TERM));
      if (annotation.isPresent()) {
        properties = getAnnotationProperties(annotation);
      } else
        properties = Collections.emptyMap();
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  JPAOrderByBuilderWatchDog() {
    annotation = Optional.empty();
    properties = Collections.emptyMap();
    externalName = "";
  }

  void watch(final List<Order> result) throws ODataJPAQueryException {

    if (annotation.isPresent()) {
      watchSortable(result);

      final List<CsdlExpression> nonSortable = getItems(properties, NON_SORTABLE_PROPERTIES);
      final List<CsdlExpression> ascendingOnly = getItems(properties, ASCENDING_ONLY_PROPERTIES);
      final List<CsdlExpression> descendingOnly = getItems(properties, DESCENDING_ONLY_PROPERTIES);
      for (final Order orderBy : result) {
        final String path = orderBy.getExpression().getAlias();
        watchNonSortable(nonSortable, path);
        watchAscendingOnly(ascendingOnly, orderBy, path);
        watchDescendingOnly(descendingOnly, orderBy, path);
      }
    }
  }

  private void watchDescendingOnly(final List<CsdlExpression> descendingOnly, final Order orderBy, final String path)
      throws ODataJPAQueryException {
    for (final CsdlExpression descending : descendingOnly) {
      if (descending.asDynamic().asPropertyPath().getValue().equals(path)
          && orderBy.isAscending())
        throw new ODataJPAQueryException(QUERY_CHECK_DESCENDING_REQUIRED_FOR, HttpStatusCode.BAD_REQUEST,
            externalName, path);
    }
  }

  private void watchAscendingOnly(final List<CsdlExpression> ascendingOnly, final Order orderBy, final String path)
      throws ODataJPAQueryException {
    for (final CsdlExpression ascending : ascendingOnly) {
      if (ascending.asDynamic().asPropertyPath().getValue().equals(path)
          && !orderBy.isAscending())
        throw new ODataJPAQueryException(QUERY_CHECK_ASCENDING_REQUIRED_FOR, HttpStatusCode.BAD_REQUEST,
            externalName, path);
    }
  }

  private void watchNonSortable(final List<CsdlExpression> nonSortables, final String path)
      throws ODataJPAQueryException {
    for (final CsdlExpression nonSortable : nonSortables) {
      if (nonSortable.asDynamic().asPropertyPath().getValue().equals(path))
        throw new ODataJPAQueryException(QUERY_CHECK_SORTING_NOT_SUPPORTED_FOR, HttpStatusCode.BAD_REQUEST,
            externalName, path);
    }
  }

  private void watchSortable(final List<Order> result) throws ODataJPAQueryException {
    if (!isSortable() && !result.isEmpty())
      throw new ODataJPAQueryException(QUERY_CHECK_SORTING_NOT_SUPPORTED, HttpStatusCode.BAD_REQUEST,
          externalName);
  }

  private boolean isSortable() {
    return Optional.ofNullable(properties.get(SORTABLE))
        .map(CsdlExpression::asConstant)
        .map(CsdlConstantExpression::getValue)
        .map(Boolean::valueOf)
        .orElse(true);
  }

}
