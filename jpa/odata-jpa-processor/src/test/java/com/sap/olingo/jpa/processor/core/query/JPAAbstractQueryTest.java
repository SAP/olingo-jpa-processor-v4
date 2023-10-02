package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.From;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.apply.AggregateExpression;
import org.apache.olingo.server.api.uri.queryoption.expression.Alias;
import org.apache.olingo.server.api.uri.queryoption.expression.Binary;
import org.apache.olingo.server.api.uri.queryoption.expression.Enumeration;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.LambdaRef;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Method;
import org.apache.olingo.server.api.uri.queryoption.expression.TypeLiteral;
import org.apache.olingo.server.api.uri.queryoption.expression.Unary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.util.TestQueryBase;

class JPAAbstractQueryTest extends TestQueryBase {

  private JPAAbstractQuery cut;
  private EntityManager em;

  @Override
  @BeforeEach
  public void setup() throws ODataException, ODataJPAIllegalAccessException {
    super.setup();
    em = mock(EntityManager.class);
    cut = new Query(null, helper.sd, jpaEntityType, em, Optional.empty());
  }

  static Stream<Expression> expressionProvider() {
    return Stream.of(
        mock(Method.class),
        mock(Literal.class),
        mock(TypeLiteral.class),
        mock(Unary.class),
        mock(LambdaRef.class),
        mock(Enumeration.class),
        mock(Binary.class),
        mock(Alias.class),
        mock(AggregateExpression.class));
  }

  @MethodSource("expressionProvider")
  @ParameterizedTest
  void testExtractOrderByNavigationAttributesThrowsExceptionIfNotSupported(final Expression orderByExpression) {
    final OrderByOption orderBy = mock(OrderByOption.class);
    final OrderByItem item = mock(OrderByItem.class);
    when(orderBy.getOrders()).thenReturn(Collections.singletonList(item));
    when(item.getExpression()).thenReturn(orderByExpression);
    assertThrows(ODataJPAQueryException.class, () -> cut.extractOrderByNavigationAttributes(orderBy));
  }

  private static class Query extends JPAAbstractQuery {

    Query(final OData odata, final JPAServiceDocument sd, final JPAEntityType jpaEntityType, final EntityManager em,
        final Optional<JPAODataClaimProvider> claimsProvider) {
      super(odata, sd, jpaEntityType, em, claimsProvider);
    }

    @Override
    public <T> AbstractQuery<T> getQuery() {
      throw new IllegalAccessError();
    }

    @Override
    public <S, T> From<S, T> getRoot() {
      throw new IllegalAccessError();
    }

    @Override
    protected Locale getLocale() {
      throw new IllegalAccessError();
    }

    @Override
    JPAODataRequestContextAccess getContext() {
      throw new IllegalAccessError();
    }

  }
}
