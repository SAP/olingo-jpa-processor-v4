package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.ProcessorSubquery;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerProtected;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

class JPAAbstractSubQueryTest extends TestBase {

  private TestHelper helper;
  private EntityManager em;
  private OData odata;
  private JPAAbstractQuery parent;
  private JPAAssociationPath association;
  private From<?, ?> from;
  private Subquery<Object> subQuery;

  private JPAAbstractSubQuery cut;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setup() throws ODataException {
    helper = getHelper();
    em = mock(EntityManager.class);
    parent = mock(JPAAbstractQuery.class);
    association = helper.getJPAAssociationPath(BusinessPartnerProtected.class, "RolesJoinProtected");
    odata = OData.newInstance();
    from = mock(From.class);
    subQuery = mock(Subquery.class);
    cut = new JPASubQuery(odata, null, null, em, parent, from, association);
  }

  static Stream<Arguments> selectClauseParameter() {
    return Stream.of(
        arguments(false, new String[] { "ID" }),
        arguments(true, new String[] { "ID" }),
        arguments(false, new String[] { "ID", "ParentID" }));
  }

  @SuppressWarnings("unchecked")
  @ParameterizedTest
  @MethodSource("selectClauseParameter")
  void testCreateSelectClauseJoinForExpand(final boolean forInExpression, final String[] attributes) {
    final JPAPath jpaPath = createJpaPath(attributes[0]);

    final Path<Object> path = mock(Path.class);
    final List<JPAPath> conditionItems = Arrays.asList(jpaPath);
    when(from.get(attributes[0])).thenReturn(path);
    cut.createSelectClauseJoin(subQuery, from, conditionItems, forInExpression);

    verify(subQuery).select(path);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCreateSelectClauseJoinForExpandInProcessor() {
    final ProcessorSubquery<Object> subQuery = mock(ProcessorSubquery.class);

    final JPAPath jpaPathId = createJpaPath("ID");
    final JPAPath jpaPathParent = createJpaPath("ParentID");

    final Path<Object> pathId = mock(Path.class);
    final Path<Object> pathParent = mock(Path.class);
    final List<JPAPath> conditionItems = Arrays.asList(jpaPathId, jpaPathParent);
    when(from.get("ID")).thenReturn(pathId);
    when(from.get("ParentID")).thenReturn(pathParent);
    cut.createSelectClauseJoin(subQuery, from, conditionItems, true);

    verify(subQuery).multiselect(Arrays.asList(pathId, pathParent));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCreateSelectClauseJoinForExpandInThrowsExceptionStandard() {

    final JPAPath jpaPathId = createJpaPath("ID");
    final JPAPath jpaPathParent = createJpaPath("ParentID");

    final Path<Object> pathId = mock(Path.class);
    final Path<Object> pathParent = mock(Path.class);
    final List<JPAPath> conditionItems = Arrays.asList(jpaPathId, jpaPathParent);
    when(from.get("ID")).thenReturn(pathId);
    when(from.get("ParentID")).thenReturn(pathParent);

    assertThrows(IllegalStateException.class, () -> cut.createSelectClauseJoin(subQuery, from, conditionItems, true));
  }

  @SuppressWarnings("unchecked")
  @ParameterizedTest
  @MethodSource("selectClauseParameter")
  void testCreateSelectClauseJoinAggregationExpand(final boolean forInExpression, final String[] attributes) {
    final JPAPath jpaPath = createJpaPath(attributes[0]);

    final Path<Object> path = mock(Path.class);
    final JPAOnConditionItem item = mock(JPAOnConditionItem.class);
    final List<JPAOnConditionItem> conditionItems = Arrays.asList(item);
    when(item.getRightPath()).thenReturn(jpaPath);
    when(from.get(attributes[0])).thenReturn(path);
    cut.createSelectClauseAggregation(subQuery, from, conditionItems, forInExpression);

    verify(subQuery).select(path);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCreateSelectClauseAggregationForExpandInProcessor() {
    final ProcessorSubquery<Object> subQuery = mock(ProcessorSubquery.class);

    final JPAPath jpaPathId = createJpaPath("ID");
    final JPAPath jpaPathParent = createJpaPath("ParentID");

    final Path<Object> pathId = mock(Path.class);
    final Path<Object> pathParent = mock(Path.class);
    final JPAOnConditionItem itemId = mock(JPAOnConditionItem.class);
    final JPAOnConditionItem itemParent = mock(JPAOnConditionItem.class);
    when(itemId.getRightPath()).thenReturn(jpaPathId);
    when(itemParent.getRightPath()).thenReturn(jpaPathParent);
    final List<JPAOnConditionItem> conditionItems = Arrays.asList(itemId, itemParent);
    when(from.get("ID")).thenReturn(pathId);
    when(from.get("ParentID")).thenReturn(pathParent);
    cut.createSelectClauseAggregation(subQuery, from, conditionItems, true);

    verify(subQuery).multiselect(Arrays.asList(pathId, pathParent));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCreateSelectClauseAggregationForExpandInThrowsExceptionStandard() {

    final JPAPath jpaPathId = createJpaPath("ID");
    final JPAPath jpaPathParent = createJpaPath("ParentID");

    final Path<Object> pathId = mock(Path.class);
    final Path<Object> pathParent = mock(Path.class);
    final JPAOnConditionItem itemId = mock(JPAOnConditionItem.class);
    final JPAOnConditionItem itemParent = mock(JPAOnConditionItem.class);
    when(itemId.getRightPath()).thenReturn(jpaPathId);
    when(itemParent.getRightPath()).thenReturn(jpaPathParent);
    final List<JPAOnConditionItem> conditionItems = Arrays.asList(itemId, itemParent);
    when(from.get("ID")).thenReturn(pathId);
    when(from.get("ParentID")).thenReturn(pathParent);

    assertThrows(IllegalStateException.class,
        () -> cut.createSelectClauseAggregation(subQuery, from, conditionItems, true));
  }

  private JPAPath createJpaPath(final String attribute) {
    final JPAPath jpaPath = mock(JPAPath.class);
    final JPAElement element = mock(JPAElement.class);
    when(element.getInternalName()).thenReturn(attribute);
    when(jpaPath.getPath()).thenReturn(Collections.singletonList(element));
    return jpaPath;
  }

  private static class JPASubQuery extends JPAAbstractSubQuery {

    JPASubQuery(final OData odata, final JPAServiceDocument sd, final JPAEntityType jpaEntity, final EntityManager em,
        final JPAAbstractQuery parent, final From<?, ?> from, final JPAAssociationPath association) {
      super(odata, sd, jpaEntity, em, parent, from, association);
    }

    @Override
    public <T> Subquery<T> getSubQuery(final Subquery<?> childQuery, final VisitableExpression expression,
        final List<Path<Comparable<?>>> inPath)
        throws ODataApplicationException {
      return null;
    }

    @Override
    public <S, T> From<S, T> getRoot() {
      return null;
    }

    @Override
    public List<Path<Comparable<?>>> getLeftPaths() throws ODataJPAIllegalAccessException {
      return null;
    }

  }
}
