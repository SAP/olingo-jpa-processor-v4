package com.sap.olingo.jpa.processor.cb.impl;

import java.nio.channels.IllegalSelectorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;

/**
 * Represents a simple or compound attribute path from a
 * bound type or collection, and is a "primitive" expression.
 *
 * @param <X> the type referenced by the path
 *
 * @author Oliver Grande
 * @since 0.3.8
 */
class PathImpl<X> extends ExpressionImpl<X> implements Path<X> {

  protected final Optional<JPAPath> path;
  protected final Optional<PathImpl<?>> parent;
  protected final Optional<String> tableAlias;
  protected JPAEntityType st;

  static List<Path<Object>> fromSelection(@Nonnull final Selection<?> sel) {
    final List<Path<Object>> pathList = new ArrayList<>();
    final PathImpl<?> compoundPath = (PathImpl<?>) sel;
    for (final JPAPath path : compoundPath.getPathList()) {
      pathList.add(new PathImpl<>(path, compoundPath.parent, compoundPath.st, compoundPath.tableAlias));
    }
    return pathList;
  }

  PathImpl(@Nonnull final JPAPath path, @Nonnull final Optional<PathImpl<?>> parent, final JPAEntityType type,
      final Optional<String> tableAlias) {
    this(Optional.of(path), parent, type, tableAlias);
  }

  PathImpl(final Optional<JPAPath> path, final Optional<PathImpl<?>> parent, final JPAEntityType type,
      final Optional<String> tableAlias) {

    super();
    this.path = Objects.requireNonNull(path);
    this.parent = Objects.requireNonNull(parent);
    this.st = type;
    this.tableAlias = Optional.ofNullable(tableAlias.orElseGet(this::tableAliaseFromParent));
  }

  @Override
  public StringBuilder asSQL(final StringBuilder statment) {
    tableAlias.ifPresent(p -> {
      statment.append(p);
      statment.append(DOT);
    });
    path.ifPresent(p -> statment.append(p.getDBFieldName()));
    return statment;
  }

  /**
   * Create a path corresponding to the referenced
   * map-valued attribute.
   * @param map map-valued attribute
   * @return expression corresponding to the referenced attribute
   */
  @Override
  public <K, V, M extends Map<K, V>> Expression<M> get(final MapAttribute<X, K, V> map) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Create a path corresponding to the referenced
   * collection-valued attribute.
   * @param collection collection-valued attribute
   * @return expression corresponding to the referenced attribute
   */
  @Override
  public <E, C extends Collection<E>> Expression<C> get(final PluralAttribute<X, C, E> collection) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Create a path corresponding to the referenced
   * single-valued attribute.
   * @param attribute single-valued attribute
   * @return path corresponding to the referenced attribute
   */
  @Override
  public <Y> Path<Y> get(final SingularAttribute<? super X, Y> attribute) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Create a path corresponding to the referenced attribute.
   *
   * <p> Note: Applications using the string-based API may need to
   * specify the type resulting from the <code>get</code> operation in order
   * to avoid the use of <code>Path</code> variables.
   *
   * <pre>
   * For example:
   *
   * CriteriaQuery&#060;Person&#062; q = cb.createQuery(Person.class);
   * Root&#060;Person&#062; p = q.from(Person.class);
   * q.select(p)
   * .where(cb.isMember("joe",
   * p.&#060;Set&#060;String&#062;&#062;get("nicknames")));
   *
   * rather than:
   *
   * CriteriaQuery&#060;Person&#062; q = cb.createQuery(Person.class);
   * Root&#060;Person&#062; p = q.from(Person.class);
   * Path&#060;Set&#060;String&#062;&#062; nicknames = p.get("nicknames");
   * q.select(p)
   * .where(cb.isMember("joe", nicknames));
   * </pre>
   *
   * @param attributeName name of the attribute
   * @return path corresponding to the referenced attribute
   * @throws IllegalStateException if invoked on a path that
   * corresponds to a basic type
   * @throws IllegalArgumentException if attribute of the given
   * name does not otherwise exist
   */
  @Override
  public <Y> Path<Y> get(final String attributeName) {

    try {
      JPAStructuredType source;
      if (this.path.isPresent()) {
        if (this.path.get().getLeaf().isComplex()) {
          source = this.path.get().getLeaf().getStructuredType();
        } else {
          throw new IllegalArgumentException("Parent not structured");
        }
      } else {
        source = st;
      }
      final JPAAttribute a = source.getDeclaredAttribute(attributeName)
          .orElseThrow(() -> new IllegalArgumentException("'" + attributeName + "' not found at " + st
              .getInternalName()));
      if (this.path.isPresent()) {
        if (st.getKey().contains(a)) {
          return new PathImpl<>(st.getPath(a.getExternalName()), Optional.of(this), st, tableAlias);
        }
        final StringBuilder pathDescription = new StringBuilder(path.get().getAlias()).append(JPAPath.PATH_SEPARATOR)
            .append(a.getExternalName());
        return new PathImpl<>(st.getPath(pathDescription.toString(), false), Optional.of(this), st, tableAlias);
      } else {
        return new PathImpl<>(st.getPath(a.getExternalName(), false), Optional.of(this), st, tableAlias);
      }
    } catch (final ODataJPAModelException e) {
      throw new IllegalArgumentException("'" + attributeName + "' not found", e);
    }
  }

  /**
   * Return the bindable object that corresponds to the
   * path expression.
   * @return bindable object corresponding to the path
   */
  @Override
  public Bindable<X> getModel() {
    // If required JPAEntityType and related would need to implement Bindable
    throw new NotImplementedException();
  }

  /**
   * Return the parent "node" in the path or null if no parent.
   * @return parent
   */
  @Override
  public Path<?> getParentPath() {
    return parent.orElse(null);
  }

  @Override
  public String toString() {
    return "PathImpl [path=" + path + ", parent=" + parent + ", st=" + st + "]";
  }

  /**
   * Create an expression corresponding to the type of the path.
   * @return expression corresponding to the type of the path
   */
  @Override
  public Expression<Class<? extends X>> type() {
    // TODO Auto-generated method stub
    return null;
  }

  List<JPAPath> getPathList() {
    return Arrays.asList(path.orElseThrow(IllegalSelectorException::new));
  }

  private String tableAliaseFromParent() {
    if (parent.isPresent())
      return parent.get().tableAlias.orElse(null);
    return null;
  }

  List<Path<Object>> resolvePathElements() {
    final List<Path<Object>> pathList = new ArrayList<>();
    for (final JPAPath element : getPathList()) {
      pathList.add(new PathImpl<>(element, parent, st, tableAlias));
    }
    return pathList;
  }

}
