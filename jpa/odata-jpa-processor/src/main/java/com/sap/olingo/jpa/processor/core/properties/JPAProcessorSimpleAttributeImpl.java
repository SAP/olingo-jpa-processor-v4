package com.sap.olingo.jpa.processor.core.properties;

import static com.sap.olingo.jpa.processor.core.query.ExpressionUtility.convertToCriteriaPath;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalArgumentException;

class JPAProcessorSimpleAttributeImpl extends JPAAbstractProcessorAttributeImpl implements JPAProcessorSimpleAttribute {

  private final boolean sortDescending;
  private Optional<Path<Object>> criteriaPath;
  private Optional<From<?, ?>> from;

  JPAProcessorSimpleAttributeImpl(final JPAPath path, final List<JPAAssociationPath> hops, final boolean descending) {
    super(path, hops);
    this.sortDescending = descending;
    this.criteriaPath = Optional.empty();
    this.from = Optional.empty();
    if (hops.size() > 1)
      throw new ODataJPAIllegalArgumentException(path.getAlias());
  }

  @Override
  public String getAlias() {
    return hops.isEmpty() ? path.getAlias() : hops.get(0).getAlias();
  }

  @Override
  public boolean isSortable() {
    return !path.isTransient();
  }

  @Override
  public boolean sortDescending() {
    return sortDescending;
  }

  @Override
  public boolean requiresJoin() {
    return !hops.isEmpty();
  }

  @Override
  public JPAProcessorAttribute setTarget(final From<?, ?> target, final Map<String, From<?, ?>> joinTables,
      final CriteriaBuilder cb) {
    determineFrom(target, joinTables);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T, S> Join<T, S> createJoin() {
    return requiresJoin()
        ? (Join<T, S>) from.orElseThrow(IllegalAccessError::new)
        : null;
  }

  @Override
  public Path<Object> getPath() {
    if (criteriaPath.isEmpty()) {
      criteriaPath = Optional.of(convertToCriteriaPath(
          from.orElseThrow(IllegalAccessError::new),
          path.getPath()));
      criteriaPath.get().alias(path.getAlias());
    }
    return criteriaPath.get();
  }

  void determineFrom(final From<?, ?> target, final Map<String, From<?, ?>> joinTables) {
    from = Optional.of(requiresJoin()
        ? asJoin(target, joinTables)
        : target);
  }
}
