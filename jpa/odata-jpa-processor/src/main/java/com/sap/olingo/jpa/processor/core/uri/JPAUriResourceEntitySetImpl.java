package com.sap.olingo.jpa.processor.core.uri;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.core.api.JPAODataPageExpandInfo;

class JPAUriResourceEntitySetImpl implements UriResourceEntitySet {

  private final UriResourceEntitySet original;
  private final List<UriParameter> keyPredicates;

  public JPAUriResourceEntitySetImpl(final UriResourceEntitySet uriResource,
      final JPAODataPageExpandInfo jpaODataPageExpandInfo) {
    this.original = uriResource;
    final var keys = uriResource.getEntityType().getKeyPropertyRefs();
    final var keyValues = jpaODataPageExpandInfo.keyPath().split(JPAPath.PATH_SEPARATOR);
    this.keyPredicates = new ArrayList<>(keys.size());

    for (int i = 0; i < keys.size(); i++) {
      keyPredicates.add(new JPAUriParameterImpl(keys.get(i), keyValues[i]));
    }

  }

  @Override
  public EdmType getType() {
    return original.getType();
  }

  @Override
  public boolean isCollection() {
    return false;
  }

  @Override
  public String getSegmentValue(final boolean includeFilters) {
    return original.getSegmentValue(includeFilters);
  }

  @Override
  public String toString(final boolean includeFilters) {
    return original.toString(includeFilters);
  }

  @Override
  public UriResourceKind getKind() {
    return original.getKind();
  }

  @Override
  public String getSegmentValue() {
    return original.getSegmentValue();
  }

  @Override
  public EdmEntitySet getEntitySet() {
    return original.getEntitySet();
  }

  @Override
  public EdmEntityType getEntityType() {
    return original.getEntityType();
  }

  @Override
  public List<UriParameter> getKeyPredicates() {
    return keyPredicates;
  }

  @Override
  public EdmType getTypeFilterOnCollection() {
    return original.getTypeFilterOnCollection();
  }

  @Override
  public EdmType getTypeFilterOnEntry() {
    return original.getTypeFilterOnEntry();
  }

}
