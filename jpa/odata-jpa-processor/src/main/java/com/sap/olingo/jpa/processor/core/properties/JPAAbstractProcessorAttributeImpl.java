package com.sap.olingo.jpa.processor.core.properties;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_NOT_ALLOWED_MEMBER;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ORDER_BY_TRANSIENT;
import static org.apache.olingo.commons.api.http.HttpStatusCode.BAD_REQUEST;
import static org.apache.olingo.commons.api.http.HttpStatusCode.FORBIDDEN;

import java.util.List;
import java.util.Map;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

abstract class JPAAbstractProcessorAttributeImpl implements JPAProcessorAttribute {

  final JPAPath path;
  final List<JPAAssociationPath> hops;

  JPAAbstractProcessorAttributeImpl(final JPAPath path, final List<JPAAssociationPath> hops) {
    super();
    this.path = path;
    this.hops = hops;
  }

  <T, S> Join<T, S> createJoinFromPath(final String alias, final List<JPAElement> pathList,
      final From<T, S> root, final JoinType finalJoinType) {

    Join<T, S> join = null;
    JoinType joinType;
    for (int i = 0; i < pathList.size(); i++) {
      if (i == pathList.size() - 1) {
        joinType = finalJoinType;
      } else {
        joinType = JoinType.INNER;
      }
      if (i == 0) {
        join = root.join(pathList.get(i).getInternalName(), joinType);
        join.alias(alias);
      } else if (i < pathList.size()) {
        join = join.join(pathList.get(i).getInternalName(), joinType);
        join.alias(pathList.get(i).getExternalName());
      }
    }
    return join;
  }

  Order addOrderByExpression(final CriteriaBuilder cb, final boolean isDescending,
      final Expression<?> expression) {
    return isDescending ? cb.desc(expression) : cb.asc(expression);
  }

  @SuppressWarnings("unchecked")
  From<Object, Object> asJoin(final From<?, ?> target, final Map<String, From<?, ?>> joinTables) {
    return joinTables.containsKey(getAlias())
        ? (From<Object, Object>) joinTables.get(getAlias())
        : (From<Object, Object>) createJoinFromPath(getAlias(), hops.get(0).getPath(), target, JoinType.LEFT);
  }

  @Override
  public Order createOrderBy(final CriteriaBuilder cb, final List<String> groups) throws ODataJPAQueryException {
    if (path.isTransient())
      throw new ODataJPAQueryException(QUERY_PREPARATION_ORDER_BY_TRANSIENT, BAD_REQUEST, path
          .getLeaf().toString());
    if (!path.isPartOfGroups(groups)) {
      throw new ODataJPAQueryException(QUERY_PREPARATION_NOT_ALLOWED_MEMBER, FORBIDDEN, path.getAlias());
    }
    return addOrderByExpression(cb, sortDescending(), getPath());
  }

  @Override
  public boolean requiresJoin() {
    return !hops.isEmpty();
  }

}
