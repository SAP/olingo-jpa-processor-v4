package com.sap.olingo.jpa.processor.core.modify;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataPageExpandInfo;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPATuple;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.query.ExpressionUtility;

abstract class JPACreateResult implements JPAExpandResult {

  protected final JPAEntityType et;
  protected final Map<JPAAssociationPath, JPAExpandResult> children;
  protected final List<JPAPath> pathList;
  protected final Locale locale;
  protected final JPAConversionHelper helper;
  protected final Map<String, List<String>> requestHeaders;

  JPACreateResult(final JPAEntityType et, final Map<String, List<String>> requestHeaders)
      throws ODataJPAModelException {

    this.et = et;
    this.helper = new JPAConversionHelper();
    this.children = new HashMap<>(0);
    this.pathList = et.getPathList();
    this.locale = ExpressionUtility.determineFallbackLocale(requestHeaders);
    this.requestHeaders = requestHeaders;
  }

  @Override
  public void convert(final JPATupleChildConverter converter) throws ODataApplicationException {
    // No implementation required for CUD operations
  }

  @Override
  public JPAExpandResult getChild(final JPAAssociationPath associationPath) {
    return children.get(associationPath);
  }

  @Override
  public Map<JPAAssociationPath, JPAExpandResult> getChildren() {
    return children;
  }

  @Override
  public Long getCount(final String key) {
    return null;
  }

  @Override
  public JPAEntityType getEntityType() {
    return et;
  }

  @Override
  public boolean hasCount() {
    return false;
  }

  @Override
  public String getSkipToken(@Nonnull final List<JPAODataPageExpandInfo> expandInfo) {
    // No paging for modifying requests
    return null;
  }

  protected void addValueToTuple(final JPATuple tuple, final JPAPath path, final int index, final Object value)
      throws ODataJPAProcessorException {
    if (path.getPath().get(index) instanceof final JPADescriptionAttribute descriptionAttribute) {
      @SuppressWarnings("unchecked")
      final Collection<Object> values = (Collection<Object>) value;
      if (values != null) {
        for (final Object entry : values) {
          final Map<String, Object> descriptionGetterMap = entryAsMap(entry);
          final String providedLocale = determineLocale(descriptionGetterMap, descriptionAttribute);
          if (locale.getLanguage().equals(providedLocale)
              || locale.toString().equals(providedLocale)) {
            final Object description = descriptionGetterMap.get(descriptionAttribute.getDescriptionAttribute()
                .getInternalName());
            tuple.addElement(path.getAlias(), path.getLeaf().getType(), description);
            break;
          }
        }
      } else {
        tuple.addElement(path.getAlias(), path.getLeaf().getType(), null);
      }
    } else {
      tuple.addElement(path.getAlias(), path.getLeaf().getType(), value);
    }
  }

  protected void convertPathToTuple(final JPATuple tuple, final Map<String, Object> jpaEntity, final JPAPath path,
      final int index) throws ODataJPAProcessorException {

    final Object value = jpaEntity.get(path.getPath().get(index).getInternalName());
    if (path.getPath().size() == index + 1 || value == null) {
      addValueToTuple(tuple, path, index, value);
    } else {
      final Map<String, Object> embeddedGetterMap = entryAsMap(value);
      convertPathToTuple(tuple, embeddedGetterMap, path, index + 1);
    }
  }

  protected abstract String determineLocale(final Map<String, Object> descriptionGetterMap,
      JPAPath localeAttribute, final int index) throws ODataJPAProcessorException;

  @SuppressWarnings("unchecked")
  protected Map<String, Object> entryAsMap(final Object entry) throws ODataJPAProcessorException { // NOSONAR
    return (Map<String, Object>) entry;
  }

  protected boolean notContainsCollection(final JPAPath path) {
    for (final JPAElement element : path.getPath())
      if (element instanceof final JPAAttribute attribute
          && attribute.isCollection())
        return false;
    return true;
  }

  private String determineLocale(final Map<String, Object> descriptionGetterMap,
      final JPADescriptionAttribute descriptionAttribute) throws ODataJPAProcessorException {
    return determineLocale(descriptionGetterMap, descriptionAttribute.getLocaleFieldName(), 0);
  }

}