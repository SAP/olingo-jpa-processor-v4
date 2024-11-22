package com.sap.olingo.jpa.processor.cb.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.Attribute;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;

class CollectionJoinImpl<Z, X> extends AbstractJoinImp<Z, X> {

  private final JPACollectionAttribute attribute;
  private final JoinType joinType;

  CollectionJoinImpl(@Nonnull final JPAPath path, @Nonnull final FromImpl<?, Z> parent,
      @Nonnull final AliasBuilder aliasBuilder, @Nonnull final CriteriaBuilder cb,
      @Nullable final JoinType joinType) throws ODataJPAModelException {

    super(determineEt(path, parent), parent, determinePath(path), aliasBuilder, cb);
    this.attribute = (JPACollectionAttribute) path.getLeaf();
    this.joinType = joinType;

    createOn(attribute.asAssociation()
        .getJoinTable()
        .getRawJoinInformation());
  }

  private static JPAPath determinePath(final JPAPath path) throws ODataJPAModelException {
    return ((JPACollectionAttribute) path.getLeaf())
        .asAssociation()
        .getTargetType() == null
            ? path : null;
  }

  private static JPAEntityType determineEt(@Nonnull final JPAPath path, @Nonnull final FromImpl<?, ?> parent)
      throws ODataJPAModelException {
    return (JPAEntityType) Optional.ofNullable(((JPACollectionAttribute) path.getLeaf())
        .asAssociation()
        .getTargetType())
        .orElseThrow(() -> new IllegalStateException("Entity type for collection attribute '&1' of '&2' not found"
            .replace("&1", path.getLeaf().getInternalName())
            .replace("&2", parent.st.getInternalName())));
  }

  @Override
  public StringBuilder asSQL(final StringBuilder statement) {

    try {
      statement.append(" ")
          .append(SqlJoinType.byJoinType(getJoinType()))
          .append(" ")
          .append(attribute
              .asAssociation()
              .getJoinTable()
              .getTableName());

      tableAlias.ifPresent(alias -> statement.append(" ").append(alias));
      statement.append(" ON ");
      return ((SqlConvertible) on).asSQL(statement);
    } catch (final ODataJPAModelException e) {
      throw new IllegalStateException("Target DB table of collection attribute '&1' of '&2'"
          .replace("&1", attribute.getInternalName())
          .replace("&2", st.getInternalName()), e);
    }
  }

  @Override
  List<Path<Object>> resolvePathElements() {
    final List<Path<Object>> pathList = new ArrayList<>();
    try {
      if (!attribute.isComplex()) {
        final JPAStructuredType source = attribute.asAssociation().getSourceType();
        final var associationPath = source.getPath(attribute.asAssociation().getAlias());
        if (associationPath != null)
          pathList.add(new PathImpl<>(associationPath, parent, st, tableAlias));
        else
          throw new IllegalStateException();
      } else {
        final JPAStructuredType source = attribute.getStructuredType();
        for (final JPAPath p : source.getPathList()) {
          pathList.add(new PathImpl<>(p, parent, st, tableAlias));
        }
      }
    } catch (final ODataJPAModelException e) {
      throw new IllegalStateException(e);
    }
    return pathList;
  }

  @Override
  List<JPAPath> getPathList() {
    final List<JPAPath> pathList = new ArrayList<>();
    try {
      if (!attribute.isComplex()) {
        final JPAStructuredType source = attribute.asAssociation().getSourceType();
        final JPAPath path = source.getPath(this.alias.orElse(attribute.getExternalName()));
        pathList.add(path);
      } else {
        pathList.addAll(attribute.getStructuredType().getPathList().stream()
            .filter(path -> !path.ignore())
            .toList());
      }
    } catch (final ODataJPAModelException e) {
      throw new IllegalStateException(e);
    }
    return pathList;
  }

  /**
   * Return the metamodel attribute corresponding to the join.
   * @return metamodel attribute corresponding to the join
   */
  @Override
  public Attribute<? super Z, ?> getAttribute() {
    throw new NotImplementedException();
  }

  @Override
  public JoinType getJoinType() {
    return joinType == null ? JoinType.INNER : joinType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Objects.hash(attribute);
    return result;
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof final CollectionJoinImpl other)// NOSONAR
      return Objects.equals(attribute, other.attribute);
    return false;
  }
}
