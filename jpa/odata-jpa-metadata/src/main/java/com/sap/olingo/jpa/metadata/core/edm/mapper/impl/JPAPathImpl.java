package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;

final class JPAPathImpl implements JPAPath {
  private final String alias;
  private final List<JPAElement> pathElements;
  private final String dbFieldName;
  private final boolean ignore;

  JPAPathImpl(final String alias, final String dbFieldName, final IntermediateModelElement element) {
    final List<JPAElement> pathElementsBuffer = new ArrayList<>();

    this.alias = alias;
    pathElementsBuffer.add(element);
    this.pathElements = Collections.unmodifiableList(pathElementsBuffer);
    this.dbFieldName = dbFieldName;
    this.ignore = element.ignore();
  }

  JPAPathImpl(final String selection, final String dbFieldName, final List<JPAElement> attribute) {
    this.alias = selection;
    this.pathElements = Collections.unmodifiableList(attribute);
    this.dbFieldName = dbFieldName;
    this.ignore = ((IntermediateModelElement) pathElements.get(1)).ignore();
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
  public int compareTo(final JPAPath o) {
    return this.alias.compareTo(o.getAlias());
  }

  @Override
  public String toString() {
    return "JPAPathImpl [alias=" + alias + ", pathElements=" + pathElements + ", dbFieldName=" + dbFieldName
        + ", ignore=" + ignore + "]";
  }
}
