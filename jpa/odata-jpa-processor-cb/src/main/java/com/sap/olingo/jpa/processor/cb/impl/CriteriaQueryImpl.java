package com.sap.olingo.jpa.processor.cb.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.persistence.AttributeConverter;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Predicate.BooleanOperator;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.api.ProcessorSelection;
import com.sap.olingo.jpa.processor.cb.api.SqlConvertible;
import com.sap.olingo.jpa.processor.cb.api.SqlKeyWords;
import com.sap.olingo.jpa.processor.cb.exeptions.InternalServerError;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.ExpressionCollector;
import com.sap.olingo.jpa.processor.cb.joiner.StringBuilderCollector;

class CriteriaQueryImpl<T> implements CriteriaQuery<T>, SqlConvertible {
  private final Class<T> resultType;
  private final Set<FromImpl<?, ?>> roots = new HashSet<>();
  private final JPAServiceDocument sd;
  private ProcessorSelection<? extends T> selection;
  private Optional<Expression<Boolean>> where;
  private boolean distinct;
  private final AliasBuilder aliasBuilder;
  private Optional<List<Order>> orderList;
  private Optional<List<Expression<?>>> groupBy;
  private Optional<Expression<Boolean>> having;
  private final CriteriaBuilder cb;

  CriteriaQueryImpl(final Class<T> clazz, final JPAServiceDocument sd, final AliasBuilder ab,
      final CriteriaBuilder cb) {
    super();
    this.resultType = clazz;
    this.sd = sd;
    this.where = Optional.empty();
    this.orderList = Optional.empty();
    this.groupBy = Optional.empty();
    this.having = Optional.empty();
    this.aliasBuilder = ab;
    this.cb = cb;
  }

  CriteriaQueryImpl(final Class<T> clazz, final JPAServiceDocument sd, final CriteriaBuilder cb) {
    this(clazz, sd, new AliasBuilder(), cb);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public StringBuilder asSQL(final StringBuilder statement) {
    final List<Expression<Boolean>> filterExpressions = new ArrayList<>();
    where.ifPresent(filterExpressions::add);
    roots.stream().forEach(r -> addInheritanceWhere(r, filterExpressions));

    where = Optional.ofNullable(filterExpressions.stream().filter(Objects::nonNull).collect(new ExpressionCollector<>(
        cb, BooleanOperator.AND)));

    statement.append(SqlKeyWords.SELECT)
        .append(" ")
        .append(addDistinct());
    selection.asSQL(statement);
    statement.append(" ")
        .append(SqlKeyWords.FROM)
        .append(" ");
    roots.stream().collect(new StringBuilderCollector.ExpressionCollector(statement, ", "));
    where.ifPresent(e -> {
      statement.append(" ")
          .append(SqlKeyWords.WHERE)
          .append(" ");
      ((SqlConvertible) e).asSQL(statement);
    });
    groupBy.ifPresent(g -> {
      statement.append(" ").append(SqlKeyWords.GROUPBY).append(" ");
      g.stream().collect(new StringBuilderCollector.ExpressionCollector(statement, ", "));
    });
    orderList.ifPresent(l -> {
      statement.append(" ").append(SqlKeyWords.ORDERBY).append(" ");
      l.stream().collect(new StringBuilderCollector.OrderCollector(statement, ", "));
    });
    having.ifPresent(e -> {
      statement.append(" ")
          .append(SqlKeyWords.HAVING)
          .append(" ");
      ((SqlConvertible) e).asSQL(statement);
    });
    return statement;
  }

  @Override
  public CriteriaQuery<T> distinct(final boolean distinct) {
    this.distinct = distinct;
    return this;
  }

  @Override
  public <X> Root<X> from(final Class<X> entityClass) {
    try {
      final JPAEntityType et = sd.getEntity(entityClass);
      final Root<X> root = new RootImpl<>(et, aliasBuilder, cb);
      roots.add((FromImpl<?, ?>) root);
      return root;
    } catch (final ODataJPAModelException e) {
      throw new InternalServerError(e);
    }
  }

  @Override
  public <X> Root<X> from(final EntityType<X> entity) {
    return from(entity.getJavaType());
  }

  /**
   * Return a list of the grouping expressions. Returns empty
   * list if no grouping expressions have been specified.
   * Modifications to the list do not affect the query.
   * @return the list of grouping expressions
   */
  @Override
  public List<Expression<?>> getGroupList() {
    return groupBy.orElse(Collections.emptyList());
  }

  /**
   * Return the predicate that corresponds to the restriction(s)
   * over the grouping items, or null if no restrictions have
   * been specified.
   * @return having clause predicate
   */
  @Override
  public Predicate getGroupRestriction() {
    return (Predicate) having.orElse(null);
  }

  @Override
  public List<Order> getOrderList() {
    return orderList.orElse(Collections.emptyList());
  }

  @Override
  public Set<ParameterExpression<?>> getParameters() {
    return Collections.emptySet();
  }

  /**
   * Return the predicate that corresponds to the where clause
   * restriction(s), or null if no restrictions have been
   * specified.
   * @return where clause predicate
   */
  @Override
  public Predicate getRestriction() {
    return (Predicate) where.orElse(null);
  }

  @Override
  public Class<T> getResultType() {
    return resultType;
  }

  @Override
  public Set<Root<?>> getRoots() {
    return roots.stream().map(r -> (Root<?>) r).collect(Collectors.toSet());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Selection<T> getSelection() {
    return (Selection<T>) selection;
  }

  /**
   * Specify the expressions that are used to form groups over
   * the query results.
   * Replaces the previous specified grouping expressions, if any.
   * If no grouping expressions are specified, any previously
   * added grouping expressions are simply removed.
   * This method only overrides the return type of the
   * corresponding <code>AbstractQuery</code> method.
   * @param grouping zero or more grouping expressions
   * @return the modified query
   */
  @Override
  public CriteriaQuery<T> groupBy(final Expression<?>... grouping) {
    return groupBy(grouping != null ? Arrays.asList(grouping) : null);
  }

  /**
   * Specify the expressions that are used to form groups over
   * the query results.
   * Replaces the previous specified grouping expressions, if any.
   * If no grouping expressions are specified, any previously
   * added grouping expressions are simply removed.
   * This method only overrides the return type of the
   * corresponding <code>AbstractQuery</code> method.
   * @param grouping list of zero or more grouping expressions
   * @return the modified query
   */
  @Override
  public CriteriaQuery<T> groupBy(final List<Expression<?>> grouping) {
    groupBy = Optional.ofNullable(grouping);
    return this;
  }

  /**
   * Specify a restriction over the groups of the query.
   * Replaces the previous having restriction(s), if any.
   * This method only overrides the return type of the
   * corresponding <code>AbstractQuery</code> method.
   * @param restriction a simple or compound boolean expression
   * @return the modified query
   */
  @Override
  public CriteriaQuery<T> having(final Expression<Boolean> restriction) {
    return having(new Predicate[] { (Predicate) restriction }); // NOSONAR
  }

  /**
   * Specify restrictions over the groups of the query
   * according the conjunction of the specified restriction
   * predicates.
   * Replaces the previously added having restriction(s), if any.
   * If no restrictions are specified, any previously added
   * restrictions are simply removed.
   * This method only overrides the return type of the
   * corresponding <code>AbstractQuery</code> method.
   * @param restrictions zero or more restriction predicates
   * @return the modified query
   */
  @Override
  public CriteriaQuery<T> having(final Predicate... restrictions) {
    final Predicate p = restrictions.length > 1
        ? cb.and(restrictions)
        : restrictions.length == 1 // NOSONAR
            ? restrictions[0]
            : null;
    having = Optional.ofNullable(p);
    return this;
  }

  @Override
  public boolean isDistinct() {
    return distinct;
  }

  /**
   * Specify the selection items that are to be returned in the
   * query result.
   * Replaces the previously specified selection(s), if any.
   *
   * The type of the result of the query execution depends on
   * the specification of the type of the criteria query object
   * created as well as the arguments to the multiselect method.
   * <p> An argument to the multiselect method must not be a tuple-
   * or array-valued compound selection item.
   *
   * <p>The semantics of this method are as follows:
   * <ul>
   * <li>
   * If the type of the criteria query is
   * <code>CriteriaQuery&#060;Tuple&#062;</code> (i.e., a criteria
   * query object created by either the
   * <code>createTupleQuery</code> method or by passing a
   * <code>Tuple</code> class argument to the
   * <code>createQuery</code> method), a <code>Tuple</code> object
   * corresponding to the arguments of the <code>multiselect</code>
   * method, in the specified order, will be instantiated and
   * returned for each row that results from the query execution.
   *
   * <li> If the type of the criteria query is <code>CriteriaQuery&#060;X&#062;</code> for
   * some user-defined class X (i.e., a criteria query object
   * created by passing a X class argument to the <code>createQuery</code>
   * method), the arguments to the <code>multiselect</code> method will be
   * passed to the X constructor and an instance of type X will be
   * returned for each row.
   *
   * <li> If the type of the criteria query is <code>CriteriaQuery&#060;X[]&#062;</code> for
   * some class X, an instance of type X[] will be returned for
   * each row. The elements of the array will correspond to the
   * arguments of the <code>multiselect</code> method, in the
   * specified order.
   *
   * <li> If the type of the criteria query is <code>CriteriaQuery&#060;Object&#062;</code>
   * or if the criteria query was created without specifying a
   * type, and only a single argument is passed to the <code>multiselect</code>
   * method, an instance of type <code>Object</code> will be returned for
   * each row.
   *
   * <li> If the type of the criteria query is <code>CriteriaQuery&#060;Object&#062;</code>
   * or if the criteria query was created without specifying a
   * type, and more than one argument is passed to the <code>multiselect</code>
   * method, an instance of type <code>Object[]</code> will be instantiated
   * and returned for each row. The elements of the array will
   * correspond to the arguments to the <code> multiselect</code> method,
   * in the specified order.
   * </ul>
   *
   * @param selections selection items corresponding to the
   * results to be returned by the query
   * @return the modified query
   * @throws IllegalArgumentException if a selection item is
   * not valid or if more than one selection item has
   * the same assigned alias
   */
  @Override
  public CriteriaQuery<T> multiselect(final List<Selection<?>> selectionList) {
    selection = new CompoundSelectionImpl<>(selectionList, resultType);
    return this;
  }

  @Override
  public CriteriaQuery<T> multiselect(final Selection<?>... selections) {
    return multiselect(Arrays.asList(selections));
  }

  /**
   * Specify the ordering expressions that are used to
   * order the query results.
   * Replaces the previous ordering expressions, if any.
   * If no ordering expressions are specified, the previous
   * ordering, if any, is simply removed, and results will
   * be returned in no particular order.
   * The order of the ordering expressions in the list
   * determines the precedence, whereby the first element in the
   * list has highest precedence.
   * @param o list of zero or more ordering expressions
   * @return the modified query
   */
  @Override
  public CriteriaQuery<T> orderBy(final List<Order> o) {
    if (o != null && o.isEmpty())
      this.orderList = Optional.empty();
    else
      this.orderList = Optional.ofNullable(o);
    return this;
  }

  @Override
  public CriteriaQuery<T> orderBy(final Order... o) {
    if (o == null) {
      this.orderList = Optional.empty();
      return this;
    }
    return this.orderBy(Arrays.asList(o));

  }

  @Override
  public CriteriaQuery<T> select(final Selection<? extends T> selection) {
    this.selection = new SelectionImpl<>(selection, resultType);
    return this;
  }

  @Override
  public <U> Subquery<U> subquery(@Nonnull final Class<U> type) {
    return new SubqueryImpl<>(type, this, aliasBuilder, cb);
  }

  /**
   * Modify the query to restrict the query result according
   * to the specified boolean expression.
   * Replaces the previously added restriction(s), if any.
   * This method only overrides the return type of the
   * corresponding <code>AbstractQuery</code> method.
   * @param restriction a simple or compound boolean expression
   * @return the modified query
   */
  @Override
  public CriteriaQuery<T> where(@Nonnull final Expression<Boolean> restriction) {
    where = Optional.of(restriction);
    return this;
  }

  /**
   * Modify the query to restrict the query result according
   * to the conjunction of the specified restriction predicates.
   * Replaces the previously added restriction(s), if any.
   * If no restrictions are specified, any previously added
   * restrictions are simply removed.
   * This method only overrides the return type of the
   * corresponding <code>AbstractQuery</code> method.
   * @param restrictions zero or more restriction predicates
   * @return the modified query
   */
  @Override
  public CriteriaQuery<T> where(final Predicate... restrictions) {
    throw new NotImplementedException();
  }

  JPAServiceDocument getServiceDocument() {
    return sd;
  }

  private String addDistinct() {
    if (distinct)
      return SqlKeyWords.DISTINCT + " ";
    return "";
  }

  private void addInheritanceWhere(final FromImpl<?, ?> from, final List<Expression<Boolean>> inheritanceWhere) {

    inheritanceWhere.add(from.createInheritanceWhere());
    for (final Join<?, ?> join : from.getJoins()) {
      addInheritanceWhere((FromImpl<?, ?>) join, inheritanceWhere);
    }
  }

  private static class CompoundSelectionImpl<X> extends SelectionImpl<X> {
    public CompoundSelectionImpl(final List<Selection<?>> selections, final Class<X> resultType) {
      super(selections, resultType);
    }

    @Override
    public StringBuilder asSQL(@Nonnull final StringBuilder statement) {
      final List<Expression<?>> selItems = new ArrayList<>();
      for (final Selection<?> sel : selections) {
        if (sel instanceof PathImpl<?>) {
          selItems.addAll(((PathImpl<?>) sel).resolvePathElements());
        } else {
          selItems.add((Expression<?>) sel);
        }
      }
      selItems.stream()
          .map(s -> (ExpressionImpl<?>) s)
          .collect(new StringBuilderCollector.ExpressionCollector<>(statement, ", "));
      return statement;
    }

    /**
     * Return the selection items composing a compound selection.
     * Modifications to the list do not affect the query.
     * @return list of selection items
     * @throws IllegalStateException if selection is not a
     * compound selection
     */
    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
      return selections;
    }

    /**
     * Whether the selection item is a compound selection.
     * @return boolean indicating whether the selection is a compound
     * selection
     */
    @Override
    public boolean isCompoundSelection() {
      return true;
    }

    @Override
    protected List<Map.Entry<String, JPAPath>> resolveSelectionLate() {
      final AliasBuilder ab = new AliasBuilder("S");
      final List<Map.Entry<String, JPAPath>> resolved = new ArrayList<>();
      for (final Selection<?> sel : selections) {
        if (sel instanceof PathImpl<?>) {
          final List<JPAPath> selItems = ((PathImpl<?>) sel).getPathList();
          if (selItems.size() == 1) {
            resolved.add(new ProcessorSelection.SelectionItem(sel.getAlias().isEmpty()
                ? ab.getNext() : sel.getAlias(), selItems.get(0)));
          } else {
            for (final JPAPath p : ((PathImpl<?>) sel).getPathList()) {
              resolved.add(new ProcessorSelection.SelectionItem(sel.getAlias().isEmpty()
                  ? ab.getNext() : sel.getAlias() + "." + p.getAlias(), p));
            }
          }
        } else if (sel instanceof ExpressionImpl<?>) {
          resolved.add(new ProcessorSelection.SelectionItem(sel.getAlias(), new JPAPathWrapper(sel)));
        }
      }
      resolvedSelection = Optional.of(resolved);
      return resolvedSelection.get();
    }
  }

  private static class SelectionImpl<X> implements ProcessorSelection<X> {
    private Optional<String> alias;
    private final Class<X> resultType;
    protected final List<Selection<?>> selections;
    protected Optional<List<Map.Entry<String, JPAPath>>> resolvedSelection = Optional.empty();

    public SelectionImpl(final List<Selection<?>> selections, final Class<X> resultType) {
      this.resultType = resultType;
      this.selections = selections;
    }

    public SelectionImpl(final Selection<?> selection, final Class<X> resultType) {
      this(Arrays.asList(selection), resultType);
    }

    /**
     * Assigns an alias to the selection item.
     * Once assigned, an alias cannot be changed or reassigned.
     * Returns the same selection item.
     * @param name alias
     * @return selection item
     */
    @Override
    public Selection<X> alias(@Nonnull final String name) {
      if (!alias.isPresent())
        alias = Optional.of(name);
      return this;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      selections.stream().collect(new StringBuilderCollector.ExpressionCollector(statement, ", "));
      return statement;
    }

    /**
     * Return the alias assigned to the tuple element or null,
     * if no alias has been assigned.
     * @return alias
     */

    @Override
    public String getAlias() {
      return alias.orElse("");
    }

    /**
     * Return the selection items composing a compound selection.
     * Modifications to the list do not affect the query.
     * @return list of selection items
     * @throws IllegalStateException if selection is not a
     * compound selection
     */
    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
      throw new IllegalStateException("Call of getCompoundSelectionItems on single selection");
    }

    /**
     * Return the Java type of the tuple element.
     * @return the Java type of the tuple element
     */
    @Override
    public Class<? extends X> getJavaType() {
      return resultType;
    }

    @Override
    public List<Map.Entry<String, JPAPath>> getResolvedSelection() {
      return resolvedSelection.orElseGet(this::resolveSelectionLate);
    }

    /**
     * Whether the selection item is a compound selection.
     * @return boolean indicating whether the selection is a compound
     * selection
     */
    @Override
    public boolean isCompoundSelection() {
      return false;
    }

    protected List<Map.Entry<String, JPAPath>> resolveSelectionLate() {
      return Collections.emptyList();
    }
  }

  private static class JPAPathWrapper implements JPAPath {

    private final Selection<?> selection;

    public JPAPathWrapper(final Selection<?> sel) {
      this.selection = sel;
    }

    @Override
    public int compareTo(final JPAPath o) {
      return 0;
    }

    @Override
    public String getAlias() {
      return selection.getAlias();
    }

    @Override
    public String getDBFieldName() {
      return null;
    }

    @Override
    public JPAAttribute getLeaf() {
      return new JPAAttributeWrapper(selection);
    }

    @Override
    public List<JPAElement> getPath() {
      return Collections.singletonList(getLeaf());
    }

    @Override
    public boolean ignore() {
      return false;
    }

    @Override
    public boolean isPartOfGroups(final List<String> groups) {
      return false;
    }

    @Override
    public boolean isTransient() {
      return false;
    }
  }

  private static class JPAAttributeWrapper implements JPAAttribute {
    private final Selection<?> selection;

    public JPAAttributeWrapper(final Selection<?> sel) {
      this.selection = sel;
    }

    @Override
    public FullQualifiedName getExternalFQN() {
      return null;
    }

    @Override
    public String getExternalName() {
      return null;
    }

    @Override
    public String getInternalName() {
      return null;
    }

    @Override
    public <X, Y> AttributeConverter<X, Y> getConverter() {
      return null;
    }

    @Override
    public EdmPrimitiveTypeKind getEdmType() throws ODataJPAModelException {
      return null;
    }

    @Override
    public CsdlAbstractEdmItem getProperty() throws ODataJPAModelException {
      return null;
    }

    @Override
    public JPAStructuredType getStructuredType() throws ODataJPAModelException {
      return null;
    }

    @Override
    public Set<String> getProtectionClaimNames() {
      return Collections.emptySet();
    }

    @Override
    public List<String> getProtectionPath(final String claimName) throws ODataJPAModelException {
      return Collections.emptyList();
    }

    @Override
    public Class<?> getType() {
      return selection.getJavaType();
    }

    @Override
    public boolean isAssociation() {
      return false;
    }

    @Override
    public boolean isCollection() {
      return false;
    }

    @Override
    public boolean isComplex() {
      return false;
    }

    @Override
    public boolean isEnum() {
      return false;
    }

    @Override
    public boolean isEtag() {
      return false;
    }

    @Override
    public boolean isKey() {
      return false;
    }

    @Override
    public boolean isSearchable() {
      return false;
    }

    @Override
    public boolean hasProtection() {
      return false;
    }

    @Override
    public boolean isTransient() {
      return false;
    }

    @Override
    public <T extends EdmTransientPropertyCalculator<?>> Constructor<T> getCalculatorConstructor() {
      return null;
    }

    @Override
    public List<String> getRequiredProperties() {
      return Collections.emptyList();
    }

  }
}
