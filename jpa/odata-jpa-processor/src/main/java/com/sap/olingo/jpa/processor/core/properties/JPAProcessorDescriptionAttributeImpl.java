package com.sap.olingo.jpa.processor.core.properties;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalArgumentException;

public class JPAProcessorDescriptionAttributeImpl extends JPAAbstractProcessorAttributeImpl implements
    JPAProcessorDescriptionAttribute {

  private final boolean sortDescending;
  private final Locale locale;
  private Optional<Path<Object>> criteriaPath;
  private Optional<From<?, ?>> from;

  public JPAProcessorDescriptionAttributeImpl(final JPAPath path, final List<JPAAssociationPath> hops,
      final boolean descending, final Locale locale) {
    super(path, hops);
    this.sortDescending = descending;
    this.locale = locale;
    this.criteriaPath = Optional.empty();
    this.from = Optional.empty();
    if (hops.size() > 1)
      throw new ODataJPAIllegalArgumentException(path.getAlias());
  }

  @Override
  public String getAlias() {
    return path.getAlias();
  }

  @Override
  public boolean isSortable() {
    return true;
  }

  @Override
  public boolean sortDescending() {
    return sortDescending;
  }

  @Override
  public boolean requiresJoin() {
    return true;
  }

  @Override
  public Path<Object> getPath() {
    if (criteriaPath.isEmpty()) {
      criteriaPath = Optional.of(
          from.orElseThrow(IllegalAccessError::new)
              .get(((JPADescriptionAttribute) path.getLeaf()).getDescriptionAttribute().getInternalName()));
    }
    return criteriaPath.get();
  }

  @Override
  public JPAProcessorAttribute setTarget(final From<?, ?> target, final Map<String, From<?, ?>> joinTables,
      final CriteriaBuilder cb) {
    determineFrom(target, cb, joinTables);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T, S> Join<T, S> createJoin() {
    return (Join<T, S>) from.orElseThrow(IllegalAccessError::new);
  }

  @SuppressWarnings("unchecked")
  void determineFrom(final From<?, ?> target, final CriteriaBuilder cb,
      final Map<String, From<?, ?>> joinTables) throws IllegalAccessError {

    from = Optional.of(
        joinTables.containsKey(getAlias())
            ? (From<Object, Object>) joinTables.get(getAlias())
            : (From<Object, Object>) createJoinFromPath(target, cb));
  }

  @SuppressWarnings("unchecked")
  From<Object, Object> createJoinFromPath(final From<?, ?> target, final CriteriaBuilder cb) {
    final JPADescriptionAttribute descriptionField = ((JPADescriptionAttribute) path.getLeaf());

    final var parentFrom = (!hops.isEmpty())
        ? createJoinFromPath(hops.get(0).getAlias(), hops.get(0).getPath(), target, JoinType.LEFT)
        : target;

    final Join<?, ?> join = createJoinFromPath(getAlias(), path.getPath(), parentFrom, JoinType.LEFT);
    if (descriptionField.isLocationJoin()) {
      join.on(createOnCondition(join, descriptionField, locale.toString(), cb));
    } else {
      join.on(createOnCondition(join, descriptionField, locale.getLanguage(), cb));
    }
    return (From<Object, Object>) join;
  }

  private Expression<Boolean> createOnCondition(final Join<?, ?> join, final JPADescriptionAttribute descriptionField,
      final String localValue, final CriteriaBuilder cb) {
    final Predicate existingOn = join.getOn();
    Expression<Boolean> result = cb.equal(determinePath(join, descriptionField.getLocaleFieldName()), localValue);
    if (existingOn != null) {
      result = cb.and(existingOn, result);
    }
    for (final var value : descriptionField.getFixedValueAssignment().entrySet()) {
      result = cb.and(result,
          cb.equal(determinePath(join, value.getKey()), value.getValue()));
    }
    return result;
  }

  private Expression<?> determinePath(final Join<?, ?> join, final JPAPath jpaPath) {
    Path<?> attributePath = join;
    for (final JPAElement pathElement : jpaPath.getPath()) {
      attributePath = attributePath.get(pathElement.getInternalName());
    }
    return attributePath;
  }
}
