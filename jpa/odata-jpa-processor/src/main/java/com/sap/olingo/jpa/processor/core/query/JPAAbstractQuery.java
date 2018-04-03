package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.debug.RuntimeMeasurement;
import org.apache.olingo.server.api.uri.UriParameter;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public abstract class JPAAbstractQuery {

  protected static final String SELECT_ITEM_SEPERATOR = ",";
  protected static final String SELECT_ALL = "*";
  protected final EntityManager em;
  protected final CriteriaBuilder cb;
  protected final JPAEntityType jpaEntity;
  protected final JPAServiceDocument sd;
  protected final JPAServiceDebugger debugger;
  protected final OData odata;
  protected Locale locale;

  public JPAAbstractQuery(final OData odata, final JPAServiceDocument sd, final JPAEntityType jpaEntityType,
      final EntityManager em) {

    super();
    this.em = em;
    this.cb = em.getCriteriaBuilder();
    this.sd = sd;
    this.jpaEntity = jpaEntityType;
    this.debugger = new EmptyDebugger();
    this.odata = odata;
  }

  public JPAAbstractQuery(final OData odata, final JPAServiceDocument sd, final EdmEntityType edmEntityType,
      final EntityManager em) throws ODataApplicationException {
    super();
    this.em = em;
    this.cb = em.getCriteriaBuilder();
    this.sd = sd;
    try {
      this.jpaEntity = sd.getEntity(edmEntityType);
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
    this.debugger = new EmptyDebugger();
    this.odata = odata;
  }

  public JPAAbstractQuery(final OData odata, final JPAServiceDocument sd, final JPAEntityType jpaEntityType,
      final EntityManager em, final JPAServiceDebugger debugger) {
    super();
    this.em = em;
    this.cb = em.getCriteriaBuilder();
    this.sd = sd;
    this.jpaEntity = jpaEntityType;
    this.debugger = debugger;
    this.odata = odata;
  }

  protected javax.persistence.criteria.Expression<Boolean> createWhereByKey(final From<?, ?> root,
      final javax.persistence.criteria.Expression<Boolean> whereCondition, final List<UriParameter> keyPredicates,
      JPAEntityType et)
      throws ODataApplicationException {
    // .../Organizations('3')
    // .../BusinessPartnerRoles(BusinessPartnerID='6',RoleCategory='C')
    javax.persistence.criteria.Expression<Boolean> compundCondition = whereCondition;

    if (keyPredicates != null) {
      for (final UriParameter keyPredicate : keyPredicates) {
        javax.persistence.criteria.Expression<Boolean> equalCondition;
        try {
          equalCondition = ExpressionUtil.createEQExpression(odata, cb, root, et, keyPredicate);
        } catch (ODataJPAModelException e) {
          throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
        }
        if (compundCondition == null)
          compundCondition = equalCondition;
        else
          compundCondition = cb.and(compundCondition, equalCondition);
      }
    }
    return compundCondition;
  }

  public abstract From<?, ?> getRoot();

  public abstract AbstractQuery<?> getQuery();

  public JPAServiceDebugger getDebugger() {
    return debugger;
  }

  protected abstract Locale getLocale();

  protected void generateDesciptionJoin(final HashMap<String, From<?, ?>> joinTables, final Set<JPAPath> pathSet,
      final From<?, ?> target) {

    for (final JPAPath descriptionFieldPath : pathSet) {
      final JPADescriptionAttribute desciptionField = ((JPADescriptionAttribute) descriptionFieldPath.getLeaf());
      Join<?, ?> join = createJoinFromPath(descriptionFieldPath.getAlias(), descriptionFieldPath.getPath(), target,
          JoinType.LEFT);
      if (desciptionField.isLocationJoin())
        join.on(createOnCondition(join, desciptionField, getLocale().toString()));
      else
        join.on(createOnCondition(join, desciptionField, getLocale().getLanguage()));
      joinTables.put(desciptionField.getInternalName(), join);
    }
  }

  protected Join<?, ?> createJoinFromPath(final String alias, final List<JPAElement> pathList, final From<?, ?> root,
      final JoinType finalJoinType) {

    Join<?, ?> join = null;
    JoinType jt;
    for (int i = 0; i < pathList.size(); i++) {
      if (i == pathList.size() - 1)
        jt = finalJoinType;
      else
        jt = JoinType.INNER;
      if (i == 0) {
        join = root.join(pathList.get(i).getInternalName(), jt);
        join.alias(alias);
      } else if (i < pathList.size()) {
        join = join.join(pathList.get(i).getInternalName(), jt);
        join.alias(pathList.get(i).getExternalName());
      }
    }
    return join;
  }

  private Expression<Boolean> createOnCondition(Join<?, ?> join, JPADescriptionAttribute desciptionField,
      String localValue) {

    Expression<Boolean> result = cb.equal(determienLocalePath(join, desciptionField.getLocaleFieldName()), localValue);
    for (JPAPath value : desciptionField.getFixedValueAssignment().keySet()) {
      result = cb.and(result,
          cb.equal(determienLocalePath(join, value), desciptionField.getFixedValueAssignment().get(value)));
    }
    return result;
  }

  private javax.persistence.criteria.Expression<?> determienLocalePath(final Join<?, ?> join,
      final JPAPath jpaPath) {
    Path<?> p = join;
    for (final JPAElement pathElement : jpaPath.getPath()) {
      p = p.get(pathElement.getInternalName());
    }
    return p;
  }

  abstract JPAODataSessionContextAccess getContext();

  protected javax.persistence.criteria.Expression<Boolean> addWhereClause(
      javax.persistence.criteria.Expression<Boolean> whereCondition,
      final javax.persistence.criteria.Expression<Boolean> additioanlExpression) {

    if (additioanlExpression != null) {
      if (whereCondition == null)
        whereCondition = additioanlExpression;
      else
        whereCondition = cb.and(whereCondition, additioanlExpression);
    }
    return whereCondition;
  }

  // TODO clean-up
  private class EmptyDebugger implements JPAServiceDebugger {

    @Override
    public int startRuntimeMeasurement(final Object instance, String methodName) {
      return 0;
    }

    @Override
    public void stopRuntimeMeasurement(int handle) {
      // not needed
    }

    @Override
    public Collection<? extends RuntimeMeasurement> getRuntimeInformation() {
      return new ArrayList<>();
    }

  }
}