package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import com.sap.olingo.jpa.metadata.api.JPAJoinColumn;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.cb.api.SqlConvertible;
import com.sap.olingo.jpa.processor.cb.api.SqlJoinType;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.impl.PredicateImpl.BinaryExpressionPredicate;
import com.sap.olingo.jpa.processor.cb.joiner.StringBuilderCollector;

abstract class AbstractJoinImp<Z, X> extends FromImpl<Z, X> implements Join<Z, X> {

  protected Predicate on;
  protected final From<?, Z> related;

  AbstractJoinImp(@Nonnull final JPAEntityType type, @Nonnull final From<?, Z> parent,
      @Nonnull final AliasBuilder ab, @Nonnull CriteriaBuilder cb) {
    super(type, ab, cb);
    this.related = parent;
  }

  AbstractJoinImp(@Nonnull final JPAEntityType type, @Nonnull final From<?, Z> parent, final JPAPath path,
      @Nonnull final AliasBuilder ab, @Nonnull CriteriaBuilder cb) {
    super(type, path, ab, cb);
    this.related = parent;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public StringBuilder asSQL(final StringBuilder statement) {

    statement.append(" ")
        .append(SqlJoinType.byJoinType(getJoinType()))
        .append(" ");
    if (!getJoins().isEmpty())
      statement.append(OPENING_BRACKET);
    statement.append(st.getTableName());
    tableAlias.ifPresent(p -> statement.append(" ").append(p));
    getJoins().stream().collect(new StringBuilderCollector.ExpressionCollector(statement, " "));
    if (!getJoins().isEmpty())
      statement.append(CLOSING_BRACKET);
    statement.append(" ON ");
    ((SqlConvertible) on).asSQL(statement);
    return statement;
  }

  /**
   * Return the predicate that corresponds to the ON
   * restriction(s) on the join, or null if no ON condition
   * has been specified.
   * @return the ON restriction predicate
   * @since Java Persistence 2.1
   */
  @Override
  public Predicate getOn() {
    return on;
  }

  /**
   * Return the parent of the join.
   * @return join parent
   */
  @Override
  public From<?, Z> getParent() {
    return related;
  }

  /**
   * Modify the join to restrict the result according to the
   * specified ON condition and return the join object.
   * Replaces the previous ON condition, if any.
   * @param restriction a simple or compound boolean expression
   * @return the modified join object
   * @since Java Persistence 2.1
   */
  @Override
  public Join<Z, X> on(@Nonnull final Expression<Boolean> restriction) {
    on = (Predicate) restriction;
    return this;
  }

  /**
   * Modify the join to restrict the result according to the
   * specified ON condition and return the join object.
   * Replaces the previous ON condition, if any.
   * @param restrictions zero or more restriction predicates
   * @return the modified join object
   * @since Java Persistence 2.1
   */
  @Override
  public Join<Z, X> on(@Nonnull final Predicate... restrictions) {
    on = PredicateImpl.and(restrictions);
    return this;
  }

  protected void createOn(final List<JPAOnConditionItem> items, final JPAEntityType targetType) {
    for (final JPAOnConditionItem item : items) {
      final Predicate eq = createOnElement(item, targetType);
      if (on == null)
        on = eq;
      else
        on = new PredicateImpl.AndPredicate(on, eq);
    }
  }

  private Predicate createOnElement(final JPAOnConditionItem item, final JPAEntityType target) {
    final Expression<?> left = new PathImpl<>(item.getLeftPath(), Optional.of((PathImpl<?>) related),
        ((PathImpl<?>) related).st, ((FromImpl<?, ?>) related).tableAlias);
    final Expression<?> right = new PathImpl<>(item.getRightPath(), Optional.of(this),
        target, tableAlias);
    return new PredicateImpl.BinaryExpressionPredicate(PredicateImpl.BinaryExpressionPredicate.Operation.EQ, left,
        right);
  }

  protected <T extends JPAJoinColumn> void createOn(final List<T> joinInformation) {
    for (final JPAJoinColumn column : joinInformation) {
      final Predicate eq = createOnElement(column);
      if (on == null)
        on = eq;
      else
        on = new PredicateImpl.AndPredicate(on, eq);
    }
  }

  @SuppressWarnings("unchecked")
  private BinaryExpressionPredicate createOnElement(final JPAJoinColumn column) {
    return new PredicateImpl.BinaryExpressionPredicate(PredicateImpl.BinaryExpressionPredicate.Operation.EQ,
        new RawPath<>(column.getName(), ((FromImpl<Z, X>) related).tableAlias),
        new RawPath<>(column.getReferencedColumnName(), tableAlias));
  }

  class RawPath<X> extends ExpressionImpl<X> implements Path<X> {

    private final String dbFieldName;
    private final Optional<String> table;

    public RawPath(final String dbFieldName, final Optional<String> table) {
      this.dbFieldName = dbFieldName;
      this.table = table;
    }

    @Override
    public StringBuilder asSQL(StringBuilder statement) {
      table.ifPresent(p -> {
        statement.append(p);
        statement.append(DOT);
      });
      statement.append(dbFieldName);
      return statement;
    }

    @Override
    public Bindable<X> getModel() {
      throw new NotImplementedException();
    }

    @Override
    public Path<?> getParentPath() {
      throw new NotImplementedException();
    }

    @Override
    public <Y> Path<Y> get(SingularAttribute<? super X, Y> attribute) {
      throw new NotImplementedException();
    }

    @Override
    public <E, C extends Collection<E>> Expression<C> get(PluralAttribute<X, C, E> collection) {
      throw new NotImplementedException();
    }

    @Override
    public <K, V, M extends Map<K, V>> Expression<M> get(MapAttribute<X, K, V> map) {
      throw new NotImplementedException();
    }

    @Override
    public Expression<Class<? extends X>> type() {
      throw new NotImplementedException();
    }

    @Override
    public <Y> Path<Y> get(String attributeName) {
      throw new NotImplementedException();
    }
  }
}