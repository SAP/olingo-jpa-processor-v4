package com.sap.olingo.jpa.processor.core.properties;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ORDER_BY_TRANSIENT;
import static org.apache.olingo.commons.api.http.HttpStatusCode.NOT_IMPLEMENTED;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalArgumentException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

class JPAProcessorCountAttributeImpl extends JPAAbstractProcessorAttributeImpl implements JPAProcessorCountAttribute {

  private final boolean descending;
  private Optional<From<?, ?>> from;

  JPAProcessorCountAttributeImpl(final List<JPAAssociationPath> hops, final boolean descending) {
    super(null, hops);
    this.descending = descending;
    this.from = Optional.empty();
    if (hops.size() > 1)
      throw new ODataJPAIllegalArgumentException(hops.get(1).getAlias());
  }

  @Override
  public String getAlias() {
    return hops.isEmpty() ? "Count" : hops.get(0).getAlias();
  }

  @Override
  public boolean isSortable() {
    return !hops.isEmpty() && !hops.get(0).getLeaf().isTransient();
  }

  @Override
  public boolean sortDescending() {
    return descending;
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
    if (from.isEmpty())
      throw new IllegalAccessError();
    return null;
  }

  @Override
  public Order createOrderBy(final CriteriaBuilder cb, final List<String> groups) throws ODataJPAQueryException {
    if (isTransient())
      throw new ODataJPAQueryException(QUERY_PREPARATION_ORDER_BY_TRANSIENT, NOT_IMPLEMENTED, hops.get(0).getAlias());
    return addOrderByExpression(cb, sortDescending(), cb.count(from.get()));
  }

  private boolean isTransient() {
    for (final var hop : hops) {
      for (final var part : hop.getPath()) {
        if (part instanceof final JPAAttribute attribute
            && attribute.isTransient())
          return true;
      }
    }
    return false;
  }

  void determineFrom(final From<?, ?> target, final Map<String, From<?, ?>> joinTables) {
    from = Optional.of(requiresJoin()
        ? asJoin(target, joinTables)
        : target);
  }
}
