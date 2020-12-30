package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NOT_SUPPORTED_MIXED_PART_OF_GROUP;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

final class JPAPathImpl implements JPAPath {
  private static final List<String> EMPTY_FILED_GROUPS = Collections.emptyList();
  private final String alias;
  private final List<JPAElement> pathElements;
  private final String dbFieldName;
  private final boolean ignore;
  private final List<String> fieldGroups;
  private Optional<Boolean> isTransient;

  JPAPathImpl(final String alias, final String dbFieldName, final IntermediateProperty element)
      throws ODataJPAModelException {

    this(alias, dbFieldName, Arrays.asList(element));
  }

  JPAPathImpl(final String alias, final String dbFieldName, final List<JPAElement> attribute)
      throws ODataJPAModelException {

    this.alias = alias;
    this.pathElements = Collections.unmodifiableList(attribute);
    this.dbFieldName = dbFieldName;
    this.ignore = ((IntermediateModelElement) pathElements.get(pathElements.size() - 1)).ignore();
    this.fieldGroups = determineFieldGroups();
    this.isTransient = Optional.empty();
  }

  @Override
  public int compareTo(final JPAPath o) {
    return this.alias.compareTo(o.getAlias());
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final JPAPathImpl other = (JPAPathImpl) obj;
    if (alias == null) {
      if (other.alias != null) return false;
    } else if (!alias.equals(other.alias)) return false;
    if (pathElements == null) {
      if (other.pathElements != null) return false;
    } else if (!pathElements.equals(other.pathElements)) return false;
    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAPath#getAlias()
   */
  @Override
  public String getAlias() {
    return alias;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAPath#getDBFieldName()
   */
  @Override
  public String getDBFieldName() {
    return dbFieldName;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAPath#getLeaf()
   */
  @Override
  public JPAAttribute getLeaf() {
    return (JPAAttribute) pathElements.get(pathElements.size() - 1);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAPath#getPath()
   */
  @Override
  public List<JPAElement> getPath() {
    return pathElements;
  }

  @Override
  public boolean isTransient() {
    return isTransient.orElseGet(this::determineIsTransient);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((alias == null) ? 0 : alias.hashCode());
    result = prime * result + ((pathElements == null) ? 0 : pathElements.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAPath#ignore()
   */
  @Override
  public boolean ignore() {
    return ignore;
  }

  @Override
  public boolean isPartOfGroups(final List<String> groups) {

    return fieldGroups == EMPTY_FILED_GROUPS || fieldGroupMatches(groups);
  }

  @Override
  public String toString() {
    return "JPAPathImpl [alias=" + alias + ", pathElements=" + pathElements + ", dbFieldName=" + dbFieldName
        + ", ignore=" + ignore + ", fieldGroups=" + fieldGroups + "]";
  }

  /**
   * @return
   * @throws ODataJPAModelException
   */
  private List<String> determineFieldGroups() throws ODataJPAModelException {
    List<String> groups = null;
    for (final JPAElement pathElement : pathElements) {
      if (pathElement instanceof IntermediateProperty && ((IntermediateProperty) pathElement).isPartOfGroup()) {
        if (groups == null)
          groups = ((IntermediateProperty) pathElement).getGroups();
        else {
          final List<String> newGroups = ((IntermediateProperty) pathElement).getGroups();
          if (groups.size() != newGroups.size() || !groups.stream().allMatch(newGroups::contains))
            throw new ODataJPAModelException(NOT_SUPPORTED_MIXED_PART_OF_GROUP, alias);
        }
      }
    }
    return groups == null ? EMPTY_FILED_GROUPS : groups;
  }

  /**
   * @param groups
   * @return
   */
  private boolean fieldGroupMatches(final List<String> groups) {
    for (final String group : groups) {
      if (fieldGroups.contains(group))
        return true;
    }
    return false;
  }

  private Boolean determineIsTransient() {
    isTransient = Optional.of(
        pathElements.stream()
            .filter(e -> e instanceof JPAAttribute)
            .map(e -> (JPAAttribute) e)
            .anyMatch(JPAAttribute::isTransient));
    return isTransient.get();

  }
}
