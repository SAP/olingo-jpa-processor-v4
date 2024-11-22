package com.sap.olingo.jpa.processor.cb.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;
import com.sap.olingo.jpa.processor.cb.exceptions.InternalServerError;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.SetExpression;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;
import com.sap.olingo.jpa.processor.cb.joiner.StringBuilderCollector;

class CriteriaUpdateImpl<T> implements CriteriaUpdate<T>, SqlConvertible {

  private final Class<T> targetEntity;
  private final RootImpl<T> root;
  private final AliasBuilder aliasBuilder;
  private final CriteriaBuilder cb;
  private final JPAServiceDocument sd;
  private Optional<Expression<Boolean>> where;
  private Optional<List<SetExpression>> set;
  private final ParameterBuffer parameter;
  private final ProcessorSqlPatternProvider sqlPattern;

  CriteriaUpdateImpl(final JPAServiceDocument sd, final CriteriaBuilder cb, final ParameterBuffer parameterBuffer,
      final Class<T> targetEntity, final ProcessorSqlPatternProvider sqlPattern) {

    this.targetEntity = targetEntity;
    this.aliasBuilder = new AliasBuilder();
    this.parameter = parameterBuffer;
    this.cb = cb;
    this.sd = sd;
    this.root = createRoot();
    this.where = Optional.empty();
    this.set = Optional.empty();
    this.sqlPattern = sqlPattern;
  }

  private RootImpl<T> createRoot() {
    try {
      final JPAEntityType et = sd.getEntity(targetEntity);
      return new RootImpl<>(et, aliasBuilder, cb);
    } catch (final ODataJPAModelException e) {
      throw new InternalServerError(e);
    }
  }

  /**
   * Create a subquery of the query.
   * @param type the subquery result type
   * @return subquery
   */
  @Override
  public <U> Subquery<U> subquery(@Nonnull final Class<U> type) {
    return new SubqueryImpl<>(type, this, aliasBuilder, cb, sqlPattern, sd);
  }

  /**
   * Return the predicate that corresponds to the where clause
   * restriction(s), or null if no restrictions have been
   * specified.
   * @return where clause predicate
   */
  @Override
  public Predicate getRestriction() {
    throw new NotImplementedException();
  }

  /**
   * Create and add a query root corresponding to the entity
   * that is the target of the update.
   * A <code>CriteriaUpdate</code> object has a single root, the entity that
   * is being updated.
   * @param entityClass the entity class
   * @return query root corresponding to the given entity
   */
  @Override
  public Root<T> from(final Class<T> entityClass) {
    return getRoot();
  }

  /**
   * Create and add a query root corresponding to the entity
   * that is the target of the update.
   * A <code>CriteriaUpdate</code> object has a single root, the entity that
   * is being updated.
   * @param entity metamodel entity representing the entity
   * of type X
   * @return query root corresponding to the given entity
   */
  @Override
  public Root<T> from(final EntityType<T> entity) {
    return getRoot();
  }

  /**
   * Return the query root.
   * @return the query root
   */
  @Override
  public Root<T> getRoot() {
    return root;
  }

  @Override
  public <Y, X extends Y> CriteriaUpdate<T> set(final SingularAttribute<? super T, Y> attribute, final X value) {
    final Path<Y> path = getRoot().get(attribute.getName());
    return set(path, value);
  }

  @Override
  public <Y> CriteriaUpdate<T> set(final SingularAttribute<? super T, Y> attribute,
      final Expression<? extends Y> value) {

    final Path<Y> path = getRoot().get(attribute.getName());
    return set(path, value);
  }

  /**
   * Update the value of the specified attribute.
   * @param attribute attribute to be updated
   * @param value new value
   * @return the modified update query
   */
  @Override
  public <Y, X extends Y> CriteriaUpdate<T> set(final Path<Y> attribute, final X value) {
    return set(attribute, literal(value, attribute));
  }

  @Override
  public <Y> CriteriaUpdate<T> set(final Path<Y> attribute, final Expression<? extends Y> value) {
    set.ifPresentOrElse(sets -> sets.add(new SetValueExpression<>(attribute, value)),
        () -> this.createSetList(attribute, value));
    return this;
  }

  private <Y> Optional<List<SetExpression>> createSetList(final Path<Y> attribute,
      final Expression<? extends Y> value) {
    final List<SetExpression> sets = new ArrayList<>();
    sets.add(new SetValueExpression<>(attribute, value));
    set = Optional.of(sets);
    return set;
  }

  @Override
  public CriteriaUpdate<T> set(final String attributeName, final Object value) {
    return set(getRoot().get(attributeName), value);
  }

  /**
   * Modify the update query to restrict the target of the update
   * according to the specified boolean expression.
   * Replaces the previously added restriction(s), if any.
   * @param restriction a simple or compound boolean expression
   * @return the modified update query
   */
  @Override
  public CriteriaUpdate<T> where(final Expression<Boolean> restriction) {
    where = Optional.ofNullable(restriction);
    return this;
  }

  /**
   * Modify the update query to restrict the target of the update
   * according to the conjunction of the specified restriction
   * predicates.
   * Replaces the previously added restriction(s), if any.
   * If no restrictions are specified, any previously added
   * restrictions are simply removed.
   * @param restrictions zero or more restriction predicates
   * @return the modified update query
   */
  @Override
  public CriteriaUpdate<T> where(final Predicate... restrictions) {
    throw new NotImplementedException();
  }

  @Override
  public StringBuilder asSQL(final StringBuilder statement) {
    statement.append(SqlKeyWords.UPDATE)
        .append(" ");
    root.asSQL(statement);
    set.ifPresent(list -> statement.append(" ")
        .append(SqlKeyWords.SET)
        .append(" ")
        .append(list.stream()
            .collect(new StringBuilderCollector.SetCollector(statement, ", "))));

    where.ifPresent(a -> {
      statement.append(" ")
          .append(SqlKeyWords.WHERE)
          .append(" ");
      ((SqlConvertible) a).asSQL(statement);
    });
    return statement;
  }

  private <X> Expression<X> literal(@Nonnull final X value, @Nonnull final Expression<? extends X> x) {
    return parameter.addValue(value, x);
  }

  static record SetValueExpression<Y>(Path<Y> attribute, Expression<? extends Y> value) implements
      SetExpression {

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      ((SqlConvertible) attribute).asSQL(statement)
          .append(" = ");
      ((SqlConvertible) value).asSQL(statement);
      return statement;
    }

  }

  ParameterBuffer getParameterBuffer() {
    return parameter;
  }
}
