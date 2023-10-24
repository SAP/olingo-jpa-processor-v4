package com.sap.olingo.jpa.processor.core.processor;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.EXPAND_EXCEEDS_MAX_LEVEL;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.EXPAND_NON_SUPPORTED_AT_ALL;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.EXPAND_NON_SUPPORTED_EXPAND;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.LevelsExpandOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.helper.AbstractWatchDog;
import com.sap.olingo.jpa.processor.core.query.JPAExpandItemInfo;

/**
 * In case an annotatable artifact has been annotated with Capabilities#ExpandRestrictions, it is checked that requested
 * <a href = "https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part2-url-conventions.html#_Toc31361039">
 * $expand</a> fulfill the restrictions. See: <a href =
 * "https://github.com/oasis-tcs/odata-vocabularies/blob/main/vocabularies/Org.OData.Capabilities.V1.xml#L524"><i>ExpandRestrictions</i></a>
 * <p>
 * @author Oliver Grande
 * @since 1.1.1
 * 22.03.2023
 */
class JPAExpandWatchDog extends AbstractWatchDog {

  static final String MAX_LEVELS = "MaxLevels";
  static final String NON_EXPANDABLE_PROPERTIES = "NonExpandableProperties";
  static final String EXPANDABLE = "Expandable";
  static final String VOCABULARY_ALIAS = "Capabilities";
  static final String TERM = "ExpandRestrictions";

  private final String externalName;
  private final Optional<CsdlAnnotation> annotation;
  private final boolean isExpandable;
  private int remainingLevels;
  private final List<String> nonExpandableProperties;
  private final int maxLevels;

  JPAExpandWatchDog(final Optional<JPAAnnotatable> annotatable) throws ODataJPAProcessException {
    super();
    Map<String, CsdlExpression> properties = Collections.emptyMap();
    if (annotatable.isPresent()) {
      try {
        externalName = annotatable.get().getExternalName();
        annotation = Optional.ofNullable(annotatable.get().getAnnotation(VOCABULARY_ALIAS, TERM));
        if (annotation.isPresent()) {
          properties = getAnnotationProperties(annotation);
        }
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    } else {
      annotation = Optional.empty();
      externalName = "";
    }
    maxLevels = remainingLevels = initializeRemainingLevels(properties);
    isExpandable = determineExpandable(properties);
    nonExpandableProperties = determineNonExpandable(properties);
  }

  boolean isExpandable() {
    return isExpandable;
  }

  int getRemainingLevels() {
    return remainingLevels;
  }

  List<String> getNonExpandableProperties() {
    return nonExpandableProperties;
  }

  /**
   * Pre check of an expand request. It is check, if the requested expand operation violates a given ExpandRestrictions
   * annotation. In case the expand operation contains a star on the first level the check is skipped. In case it
   * contains a level=max, the navigation is ignored for the max level check.
   * @param expandOption
   * @param queryPath
   * @throws ODataJPAProcessorException
   */
  void watch(final ExpandOption expandOption, final List<UriResource> queryPath)
      throws ODataJPAProcessorException {
    if (expandOption != null && !isStarOnly(expandOption)) {
      watchExpandable(expandOption);
      watchExpandLevel(expandOption);
      watchExpandPath(expandOption, queryPath);
    }
  }

  List<JPAExpandItemInfo> filter(final List<JPAExpandItemInfo> itemInfoList) {
    remainingLevels--;
    return itemInfoList.stream()
        .filter(info -> !nonExpandableProperties.contains(info.getExpandAssociation().getAlias()))
        .toList();
  }

  private void watchExpandLevel(@Nonnull final ExpandOption expandOption) throws ODataJPAProcessorException {

    for (final ExpandItem item : expandOption.getExpandItems()) {
      final LevelsExpandOption levelOption = item.getLevelsOption();
      if (levelOption != null && levelOption.getValue() > maxLevels)
        throw new ODataJPAProcessorException(EXPAND_EXCEEDS_MAX_LEVEL, HttpStatusCode.BAD_REQUEST, externalName);
    }
  }

  private void watchExpandPath(@Nonnull final ExpandOption expandOption, final List<UriResource> queryPath)
      throws ODataJPAProcessorException {
    int noLevels = 0;
    for (final ExpandItem item : expandOption.getExpandItems()) {
      final UriInfoResource expandPath = item.getResourcePath();
      final String expand = buildExpandPath(queryPath, expandPath);
      if (nonExpandableProperties.contains(expand))
        throw new ODataJPAProcessorException(EXPAND_NON_SUPPORTED_EXPAND, HttpStatusCode.BAD_REQUEST,
            expand, externalName);
      final int nextDepth = determineDepth(item) + 1;
      if (nextDepth > noLevels)
        noLevels = nextDepth;
    }
    if (noLevels + 1 > maxLevels)
      throw new ODataJPAProcessorException(EXPAND_EXCEEDS_MAX_LEVEL, HttpStatusCode.BAD_REQUEST, externalName);
  }

  private void watchExpandable(@Nonnull final ExpandOption expandOption) throws ODataJPAProcessorException {
    if (!isExpandable && expandOption.getExpandItems() != null
        && !expandOption.getExpandItems().isEmpty())
      throw new ODataJPAProcessorException(EXPAND_NON_SUPPORTED_AT_ALL, HttpStatusCode.BAD_REQUEST,
          externalName);
  }

  private int determineDepth(final ExpandItem item) {
    if (item.getExpandOption() != null
        && item.getExpandOption().getExpandItems() != null
        && !item.getExpandOption().getExpandItems().isEmpty()) {
      int subDepth = 0;
      for (final ExpandItem nextItem : item.getExpandOption().getExpandItems()) {
        final int nextDepth = determineDepth(nextItem);
        if (nextDepth > subDepth)
          subDepth = nextDepth;
      }
      return subDepth + 1;
    }
    return 0;
  }

  private boolean isStarOnly(final ExpandOption expandOption) {
    return expandOption.getExpandItems().stream()
        .allMatch(ExpandItem::isStar);
  }

  private String buildExpandPath(final List<UriResource> queryPath, final UriInfoResource expandPath) {
    return Arrays.asList(queryPath.subList(1, queryPath.size()), expandPath.getUriResourceParts())
        .stream()
        .flatMap(List::stream)
        .map(UriResource::getSegmentValue)
        .collect(Collectors.joining(JPAPath.PATH_SEPARATOR));
  }

  private List<String> determineNonExpandable(final Map<String, CsdlExpression> properties) {
    return getNavigationPathItems(properties, NON_EXPANDABLE_PROPERTIES);
  }

  private boolean determineExpandable(final Map<String, CsdlExpression> properties) {
    return determineConstantExpression(properties, EXPANDABLE)
        .map(Boolean::valueOf)
        .orElse(true);
  }

  private int initializeRemainingLevels(final Map<String, CsdlExpression> properties) {
    // CsdlConstantExpression; -1 = no restriction
    return determineConstantExpression(properties, MAX_LEVELS)
        .map(Integer::valueOf)
        .map(i -> i < 0 ? Integer.MAX_VALUE : i)
        .orElse(Integer.MAX_VALUE);
  }

  public void levelProcessed() {
    remainingLevels++;
  }

}
