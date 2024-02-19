package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CollectionJoin;
import jakarta.persistence.criteria.CriteriaBuilder.Coalesce;
import jakarta.persistence.criteria.CriteriaBuilder.Trimspec;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.ListJoin;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.SetJoin;
import jakarta.persistence.criteria.Subquery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.impl.ExpressionImpl.ConcatExpression;
import com.sap.olingo.jpa.processor.cb.impl.ExpressionImpl.ParameterExpression;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;
import com.sap.olingo.jpa.processor.core.testmodel.AccessRights;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

class CriteriaBuilderImplTest extends BuilderBaseTest {
  CriteriaBuilderImpl cut;
  private StringBuilder statement;
  private CriteriaQuery<Tuple> query;

  static Stream<Arguments> notImplemented() throws NoSuchMethodException, SecurityException {
    final Class<CriteriaBuilderImpl> c = CriteriaBuilderImpl.class;
    return Stream.of(
        arguments(c.getMethod("isTrue", Expression.class)),
        arguments(c.getMethod("isFalse", Expression.class)),
        arguments(c.getMethod("sum", Expression.class)),
        arguments(c.getMethod("sumAsDouble", Expression.class)),
        arguments(c.getMethod("sumAsLong", Expression.class)),
        arguments(c.getMethod("avg", Expression.class)),
        arguments(c.getMethod("max", Expression.class)),
        arguments(c.getMethod("min", Expression.class)),
        arguments(c.getMethod("greatest", Expression.class)),
        arguments(c.getMethod("least", Expression.class)),
        arguments(c.getMethod("neg", Expression.class)),
        arguments(c.getMethod("abs", Expression.class)),
        arguments(c.getMethod("trim", Trimspec.class, Expression.class)),
        arguments(c.getMethod("trim", Trimspec.class, Expression.class, Expression.class)),
        arguments(c.getMethod("trim", Expression.class, Expression.class)),
        arguments(c.getMethod("trim", char.class, Expression.class)),
        arguments(c.getMethod("trim", Trimspec.class, char.class, Expression.class)),
        arguments(c.getMethod("toBigDecimal", Expression.class)),
        arguments(c.getMethod("toBigInteger", Expression.class)),
        arguments(c.getMethod("toDouble", Expression.class)),
        arguments(c.getMethod("toFloat", Expression.class)),
        arguments(c.getMethod("toInteger", Expression.class)),
        arguments(c.getMethod("toLong", Expression.class)),
        arguments(c.getMethod("toString", Expression.class)),
        arguments(c.getMethod("array", Selection[].class)),
        arguments(c.getMethod("tuple", Selection[].class)),
        arguments(c.getMethod("construct", Class.class, Selection[].class)),
        arguments(c.getMethod("values", Map.class)),
        arguments(c.getMethod("keys", Map.class)),
        arguments(c.getMethod("treat", Root.class, Class.class)),
        arguments(c.getMethod("treat", Path.class, Class.class)),
        arguments(c.getMethod("treat", MapJoin.class, Class.class)),
        arguments(c.getMethod("treat", ListJoin.class, Class.class)),
        arguments(c.getMethod("treat", SetJoin.class, Class.class)),
        arguments(c.getMethod("treat", CollectionJoin.class, Class.class)),
        arguments(c.getMethod("treat", Join.class, Class.class)),
        arguments(c.getMethod("isEmpty", Expression.class)),
        arguments(c.getMethod("isNotEmpty", Expression.class)),
        arguments(c.getMethod("isMember", Expression.class, Expression.class)),
        arguments(c.getMethod("isMember", Object.class, Expression.class)),
        arguments(c.getMethod("isNotMember", Expression.class, Expression.class)),
        arguments(c.getMethod("isNotMember", Object.class, Expression.class)),
        arguments(c.getMethod("size", Expression.class)),
        arguments(c.getMethod("size", Collection.class)),
        arguments(c.getMethod("nullif", Expression.class, Expression.class)),
        arguments(c.getMethod("nullif", Expression.class, Object.class)),
        arguments(c.getMethod("nullLiteral", Class.class)),
        arguments(c.getMethod("disjunction")),
        arguments(c.getMethod("conjunction")),
        arguments(c.getMethod("selectCase")),
        arguments(c.getMethod("selectCase", Expression.class)),
        arguments(c.getMethod("parameter", Class.class)),
        arguments(c.getMethod("parameter", Class.class, String.class)),
        arguments(c.getMethod("currentDate")),
        arguments(c.getMethod("currentTime")),
        arguments(c.getMethod("sign", Expression.class)),
        arguments(c.getMethod("ceiling", Expression.class)),
        arguments(c.getMethod("floor", Expression.class)),
        arguments(c.getMethod("exp", Expression.class)),
        arguments(c.getMethod("power", Expression.class, Expression.class)),
        arguments(c.getMethod("power", Expression.class, Number.class)),
        arguments(c.getMethod("round", Expression.class, Integer.class)),
        arguments(c.getMethod("localDate")),
        arguments(c.getMethod("localDateTime")),
        arguments(c.getMethod("localTime")),
        arguments(c.getMethod("ln", Expression.class)),
        arguments(c.getMethod("sqrt", Expression.class)));
  }

  static Stream<Arguments> binaryImplemented() throws NoSuchMethodException, SecurityException {
    final Class<CriteriaBuilderImpl> c = CriteriaBuilderImpl.class;
    return Stream.of(
        arguments(c.getMethod("equal", Expression.class, Expression.class),
            "(E0.\"CodeID\" = E0.\"ParentCodeID\")"),
        arguments(c.getMethod("notEqual", Expression.class, Expression.class),
            "(E0.\"CodeID\" <> E0.\"ParentCodeID\")"),
        arguments(c.getMethod("greaterThanOrEqualTo", Expression.class, Expression.class),
            "(E0.\"CodeID\" >= E0.\"ParentCodeID\")"),
        arguments(c.getMethod("greaterThan", Expression.class, Expression.class),
            "(E0.\"CodeID\" > E0.\"ParentCodeID\")"),
        arguments(c.getMethod("lessThanOrEqualTo", Expression.class, Expression.class),
            "(E0.\"CodeID\" <= E0.\"ParentCodeID\")"),
        arguments(c.getMethod("lessThan", Expression.class, Expression.class),
            "(E0.\"CodeID\" < E0.\"ParentCodeID\")"));
  }

  static Stream<Arguments> binaryValueImplemented() throws NoSuchMethodException, SecurityException {
    final Class<CriteriaBuilderImpl> c = CriteriaBuilderImpl.class;
    return Stream.of(
        arguments(c.getMethod("equal", Expression.class, Object.class),
            "(E0.\"CodeID\" = ?1)"),
        arguments(c.getMethod("notEqual", Expression.class, Object.class),
            "(E0.\"CodeID\" <> ?1)"),
        arguments(c.getMethod("greaterThanOrEqualTo", Expression.class, Comparable.class),
            "(E0.\"CodeID\" >= ?1)"),
        arguments(c.getMethod("greaterThan", Expression.class, Comparable.class),
            "(E0.\"CodeID\" > ?1)"),
        arguments(c.getMethod("lessThanOrEqualTo", Expression.class, Comparable.class),
            "(E0.\"CodeID\" <= ?1)"),
        arguments(c.getMethod("lessThan", Expression.class, Comparable.class),
            "(E0.\"CodeID\" < ?1)"));
  }

  static Stream<Arguments> binaryImplementedNumeric() throws NoSuchMethodException, SecurityException {
    final Class<CriteriaBuilderImpl> c = CriteriaBuilderImpl.class;
    return Stream.of(
        arguments(c.getMethod("ge", Expression.class, Expression.class),
            "(E0.\"Area\" >= E0.\"Population\")"),
        arguments(c.getMethod("gt", Expression.class, Expression.class),
            "(E0.\"Area\" > E0.\"Population\")"),
        arguments(c.getMethod("le", Expression.class, Expression.class),
            "(E0.\"Area\" <= E0.\"Population\")"),
        arguments(c.getMethod("lt", Expression.class, Expression.class),
            "(E0.\"Area\" < E0.\"Population\")"),
        arguments(c.getMethod("sum", Expression.class, Expression.class),
            "(E0.\"Area\" + E0.\"Population\")"),
        arguments(c.getMethod("diff", Expression.class, Expression.class),
            "(E0.\"Area\" - E0.\"Population\")"),
        arguments(c.getMethod("prod", Expression.class, Expression.class),
            "(E0.\"Area\" * E0.\"Population\")"),
        arguments(c.getMethod("quot", Expression.class, Expression.class),
            "(E0.\"Area\" / E0.\"Population\")"),
        arguments(c.getMethod("mod", Expression.class, Expression.class),
            "(E0.\"Area\" % E0.\"Population\")"));
  }

  static Stream<Arguments> binaryValueImplementedNumeric() throws NoSuchMethodException, SecurityException {
    final Class<CriteriaBuilderImpl> c = CriteriaBuilderImpl.class;
    return Stream.of(
        arguments(c.getMethod("ge", Expression.class, Number.class),
            "(E0.\"Area\" >= ?1)"),
        arguments(c.getMethod("gt", Expression.class, Number.class),
            "(E0.\"Area\" > ?1)"),
        arguments(c.getMethod("le", Expression.class, Number.class),
            "(E0.\"Area\" <= ?1)"),
        arguments(c.getMethod("lt", Expression.class, Number.class),
            "(E0.\"Area\" < ?1)"),
        arguments(c.getMethod("sum", Expression.class, Number.class),
            "(E0.\"Area\" + ?1)"),
        arguments(c.getMethod("diff", Expression.class, Number.class),
            "(E0.\"Area\" - ?1)"),
        arguments(c.getMethod("prod", Expression.class, Number.class),
            "(E0.\"Area\" * ?1)"),
        arguments(c.getMethod("quot", Expression.class, Number.class),
            "(E0.\"Area\" / ?1)"),
        arguments(c.getMethod("mod", Expression.class, Integer.class),
            "(E0.\"Area\" % ?1)"));
  }

  static Stream<Arguments> binaryValueImplementedNumericInverse() throws NoSuchMethodException, SecurityException {
    final Class<CriteriaBuilderImpl> c = CriteriaBuilderImpl.class;
    return Stream.of(
        arguments(c.getMethod("sum", Number.class, Expression.class),
            "(?1 + E0.\"Area\")"),
        arguments(c.getMethod("diff", Number.class, Expression.class),
            "(?1 - E0.\"Area\")"),
        arguments(c.getMethod("prod", Number.class, Expression.class),
            "(?1 * E0.\"Area\")"),
        arguments(c.getMethod("quot", Number.class, Expression.class),
            "(?1 / E0.\"Area\")"),
        arguments(c.getMethod("mod", Integer.class, Expression.class),
            "(?1 % E0.\"Area\")"));
  }

  static Stream<Arguments> unaryFunctionsImplemented() throws NoSuchMethodException, SecurityException {
    final Class<CriteriaBuilderImpl> c = CriteriaBuilderImpl.class;
    return Stream.of(
        arguments(c.getMethod("lower", Expression.class), "LOWER(E0.\"Name\")"),
        arguments(c.getMethod("upper", Expression.class), "UPPER(E0.\"Name\")"),
        arguments(c.getMethod("length", Expression.class), "LENGTH(E0.\"Name\")"),
        arguments(c.getMethod("trim", Expression.class), "TRIM(E0.\"Name\")"));
  }

  static Stream<Arguments> subQueryExpressionsImplemented() throws NoSuchMethodException, SecurityException {
    final Class<CriteriaBuilderImpl> c = CriteriaBuilderImpl.class;
    return Stream.of(
        arguments(c.getMethod("any", Subquery.class),
            "ANY (SELECT ?1 S0 FROM \"OLINGO\".\"AdministrativeDivision\" E0)"),
        arguments(c.getMethod("all", Subquery.class),
            "ALL (SELECT ?1 S0 FROM \"OLINGO\".\"AdministrativeDivision\" E0)"),
        arguments(c.getMethod("exists", Subquery.class),
            "EXISTS (SELECT ?1 S0 FROM \"OLINGO\".\"AdministrativeDivision\" E0)"),
        arguments(c.getMethod("some", Subquery.class),
            "SOME (SELECT ?1 S0 FROM \"OLINGO\".\"AdministrativeDivision\" E0)"));
  }

  @BeforeEach
  void setup() {
    cut = new CriteriaBuilderImpl(sd, new ParameterBuffer());
    statement = new StringBuilder();
    query = cut.createTupleQuery();
  }

  @ParameterizedTest
  @MethodSource("notImplemented")
  void testThrowsNotImplemented(final Method m) throws IllegalAccessException, IllegalArgumentException {
    testNotImplemented(m, cut);
  }

  @ParameterizedTest
  @MethodSource("binaryImplemented")
  void testBinaryExpressionWithExpression(final Method m, final String exp) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {

    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);

    final Object[] params = { administrativeDivision.get("codeID"), administrativeDivision.get("parentCodeID") };
    final Predicate act = (Predicate) m.invoke(cut, params);
    assertNotNull(act);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
    assertEquals(0, cut.getParameter().getParameter().size());
  }

  @ParameterizedTest
  @MethodSource("binaryValueImplemented")
  void testBinaryExpressionWithObject(final Method m, final String exp) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {

    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);

    final Object[] params = { administrativeDivision.get("codeID"), "NUTS2" };
    final Predicate act = (Predicate) m.invoke(cut, params);

    assertNotNull(act);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
    assertEquals(1, cut.getParameter().getParameter().size());
    for (final ParameterExpression<?, ?> parameter : cut.getParameter().getParameter().values()) {
      if (parameter.getPosition() == 1)
        assertEquals("NUTS2", parameter.getValue());
    }
  }

  @ParameterizedTest
  @MethodSource("binaryImplementedNumeric")
  void testBinaryNumericExpressionWithExpression(final Method m, final String exp) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {

    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Object[] params = { administrativeDivision.get("area"), administrativeDivision.get("population") };
    final Expression<?> act = (Expression<?>) m.invoke(cut, params);
    assertNotNull(act);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
    assertEquals(0, cut.getParameter().getParameter().size());
  }

  @ParameterizedTest
  @MethodSource("binaryValueImplementedNumeric")
  void testBinaryNumericExpressionWithObject(final Method m, final String exp) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {

    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Object[] params = { administrativeDivision.get("area"), 1000 };
    final Expression<?> act = (Expression<?>) m.invoke(cut, params);

    assertNotNull(act);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
    assertEquals(1, cut.getParameter().getParameter().size());
    for (final ParameterExpression<?, ?> parameter : cut.getParameter().getParameter().values()) {
      if (parameter.getPosition() == 1)
        assertEquals(1000, parameter.getValue());
    }
  }

  @ParameterizedTest
  @MethodSource("binaryValueImplementedNumericInverse")
  void testBinaryNumericExpressionWithObjectFirst(final Method m, final String exp)
      throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {

    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Object[] params = { 1000, administrativeDivision.get("area") };
    final Expression<?> act = (Expression<?>) m.invoke(cut, params);

    assertNotNull(act);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
    assertEquals(1, cut.getParameter().getParameter().size());
    for (final ParameterExpression<?, ?> parameter : cut.getParameter().getParameter().values()) {
      if (parameter.getPosition() == 1)
        assertEquals(1000, parameter.getValue());
    }
  }

  @ParameterizedTest
  @MethodSource("unaryFunctionsImplemented")
  void testCreateUnaryFunction(final Method m, final String exp) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    final Root<?> administrativeDivision = query.from(AdministrativeDivisionDescription.class);
    final Object[] params = { administrativeDivision.get("name") };
    final Expression<?> act = (Expression<?>) m.invoke(cut, params);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @ParameterizedTest
  @MethodSource("subQueryExpressionsImplemented")
  void testCreateSubQuery(final Method m, final String exp) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    final Subquery<Long> sub = query.subquery(Long.class);
    sub.select(cut.literal(1L));
    sub.from(AdministrativeDivision.class);
    final Object[] params = { sub };
    final Expression<?> act = (Expression<?>) m.invoke(cut, params);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testReturnsObjectQuery() {
    assertNotNull(cut.createQuery());
    assertEquals(Object.class, cut.createQuery().getResultType());
  }

  @Test
  void testReturnsTupleQuery() {
    assertNotNull(cut.createTupleQuery());
    assertEquals(Tuple.class, cut.createTupleQuery().getResultType());
  }

  @Test
  void testReturnsLongQuery() {
    assertNotNull(cut.createQuery(Long.class));
    assertEquals(Long.class, cut.createQuery(Long.class).getResultType());
  }

  @Test
  void testCreateCriteriaUpdateThrowsNotImplemented() {
    assertThrows(NotImplementedException.class, () -> cut.createCriteriaUpdate(Organization.class));
  }

  @Test
  void testCreateCriteriaDeleteThrowsNotImplemented() {
    assertThrows(NotImplementedException.class, () -> cut.createCriteriaDelete(Organization.class));
  }

  @Test
  void testCreateEqualThrowsNullPointerExpressionNull() {
    assertThrows(NullPointerException.class, () -> cut.equal(null, "NUTS2"));
  }

  @Test
  void testCreateGeExpressionWithExpression() {
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Predicate act = cut.ge(administrativeDivision.get("area"), administrativeDivision.get("population"));

    assertNotNull(act);
    assertEquals("(E0.\"Area\" >= E0.\"Population\")", ((SqlConvertible) act).asSQL(statement).toString());
    assertEquals(0, cut.getParameter().getParameter().size());
  }

  @Test
  void testThrowsNullPointerExpressionNull() {
    assertThrows(NullPointerException.class, () -> cut.equal(null, "NUTS2"));
  }

  @Test
  void testCreateAnd() {
    final String exp = "((E0.\"CodeID\" = ?1) AND (E0.\"DivisionCode\" = ?2))";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Predicate restriction1 = cut.equal(administrativeDivision.get("codeID"), "NUTS2");
    final Predicate restriction2 = cut.equal(administrativeDivision.get("divisionCode"), "BE34");
    final Predicate act = cut.and(restriction1, restriction2);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateMultiAnd() {
    final String exp = "(((E0.\"CodeID\" = ?1) AND (E0.\"DivisionCode\" = ?2)) AND (E0.\"CodePublisher\" = ?3))";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Predicate[] restrictions = new Predicate[3];
    restrictions[0] = cut.equal(administrativeDivision.get("codeID"), "NUTS2");
    restrictions[1] = cut.equal(administrativeDivision.get("divisionCode"), "BE34");
    restrictions[2] = cut.equal(administrativeDivision.get("codePublisher"), "Eurostat");
    final Predicate act = cut.and(restrictions);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateMultiAndThrowsExceptionOnWrongParameter() {
    final Predicate[] rNull = null;
    assertThrows(IllegalArgumentException.class, () -> cut.and(rNull));

    final Predicate[] rEmpty = new Predicate[3];
    assertThrows(IllegalArgumentException.class, () -> cut.and(rEmpty));

    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Predicate[] rOneEntry = new Predicate[1];
    rOneEntry[0] = cut.equal(administrativeDivision.get("codeID"), "NUTS2");
    assertThrows(IllegalArgumentException.class, () -> cut.and(rOneEntry));
  }

  @Test
  void testCreateOneOr() {
    final String exp = "((E0.\"CodeID\" = ?1) OR (E0.\"CodeID\" = ?2))";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Predicate one = cut.equal(administrativeDivision.get("codeID"), "NUTS2");
    final Predicate two = cut.equal(administrativeDivision.get("codeID"), "NUTS3");
    final Predicate act = cut.or(one, two);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateMultiOrThrowsExceptionOnWrongParameter() {
    final Predicate[] rNull = null;
    assertThrows(IllegalArgumentException.class, () -> cut.or(rNull));

    final Predicate[] rEmpty = new Predicate[3];
    assertThrows(IllegalArgumentException.class, () -> cut.or(rEmpty));

    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Predicate[] rOneEntry = new Predicate[1];
    rOneEntry[0] = cut.equal(administrativeDivision.get("codeID"), "NUTS2");
    assertThrows(IllegalArgumentException.class, () -> cut.and(rOneEntry));
  }

  @Test
  void testCreateMultiOr() {
    final String exp = "(((E0.\"CodeID\" = ?1) OR (E0.\"DivisionCode\" = ?2)) OR (E0.\"CodePublisher\" = ?3))";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Predicate[] restrictions = new Predicate[3];
    restrictions[0] = cut.equal(administrativeDivision.get("codeID"), "NUTS2");
    restrictions[1] = cut.equal(administrativeDivision.get("divisionCode"), "BE34");
    restrictions[2] = cut.equal(administrativeDivision.get("codePublisher"), "Eurostat");
    final Predicate act = cut.or(restrictions);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateNot() {
    final String exp = "(NOT (E0.\"CodeID\" = ?1))";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Predicate one = cut.equal(administrativeDivision.get("codeID"), "NUTS2");
    final Predicate act = cut.not(one);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testLiteralExpressionReturnsParameter() {
    final String exp = "?1";
    final Expression<LocalDate> act = cut.literal(LocalDate.now());
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
    assertNotNull(cut.getParameter());
    assertEquals(1, cut.getParameter().getParameter().size());
  }

  @Test
  void testLiteralThrowsExceptionOnNullValue() {
    assertThrows(IllegalArgumentException.class, () -> cut.literal(null));
  }

  @Test
  void testCreateLikeExpressionWithString() {
    final String exp = "(E0.\"CodeID\" LIKE ?1)";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Predicate act = cut.like(administrativeDivision.get("codeID"), "6-1");
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateNotLikeExpressionWithString() {
    final String exp = "(NOT (E0.\"CodeID\" LIKE ?1))";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Predicate act = cut.notLike(administrativeDivision.get("codeID"), "6-1");
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateLikeExpressionWithStringAndEscape() {
    final String exp = "(E0.\"CodeID\" LIKE ?1 ESCAPE ?2)";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Predicate act = cut.like(administrativeDivision.get("codeID"), "%6-1", '/');
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateNotLikeExpressionWithStringAndEscape() {
    final String exp = "(NOT (E0.\"CodeID\" LIKE ?1 ESCAPE ?2))";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Predicate act = cut.notLike(administrativeDivision.get("codeID"), "%6-1", '/');
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateLikeExpressionWithLiteral() {
    final String exp = "(E0.\"CodeID\" LIKE ?1)";
    final Expression<String> literal = cut.literal("%6-1");
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Predicate act = cut.like(administrativeDivision.get("codeID"), literal);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateNotLikeExpressionWithLiteral() {
    final String exp = "(NOT (E0.\"CodeID\" LIKE ?1))";
    final Expression<String> literal = cut.literal("%6-1");
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Predicate act = cut.notLike(administrativeDivision.get("codeID"), literal);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateLikeExpressionWithLiteralLiteral() {
    final String exp = "(E0.\"CodeID\" LIKE ?1 ESCAPE ?2)";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Expression<String> p = cut.literal("6-1");
    final Expression<Character> e = cut.literal('/');
    final Predicate act = cut.like(administrativeDivision.get("codeID"), p, e);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateNotLikeExpressionWithLiteralLiteral() {
    final String exp = "(NOT (E0.\"CodeID\" LIKE ?1 ESCAPE ?2))";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Expression<String> p = cut.literal("6-1");
    final Expression<Character> e = cut.literal('/');
    final Predicate act = cut.notLike(administrativeDivision.get("codeID"), p, e);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateLikeExpressionWithLiteralString() {
    final String exp = "(E0.\"CodeID\" LIKE ?2 ESCAPE ?1)";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Expression<Character> e = cut.literal('/');
    final Predicate act = cut.like(administrativeDivision.get("codeID"), "%6-1", e);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateNotLikeExpressionWithLiteralString() {
    final String exp = "(NOT (E0.\"CodeID\" LIKE ?2 ESCAPE ?1))";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Expression<Character> e = cut.literal('/');
    final Predicate act = cut.notLike(administrativeDivision.get("codeID"), "%6-1", e);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateLikeExpressionWithStringLiteral() {
    final String exp = "(E0.\"CodeID\" LIKE ?1 ESCAPE ?2)";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Expression<String> p = cut.literal("6-1");
    final Predicate act = cut.like(administrativeDivision.get("codeID"), p, '/');
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateNotLikeExpressionWithStringLiteral() {
    final String exp = "(NOT (E0.\"CodeID\" LIKE ?1 ESCAPE ?2))";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Expression<String> p = cut.literal("6-1");
    final Predicate act = cut.notLike(administrativeDivision.get("codeID"), p, '/');
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateOrderByDescending() {
    final String exp = "E0.\"CodeID\" DESC";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Order act = cut.desc(administrativeDivision.get("codeID"));
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateOrderByAscending() {
    final String exp = "E0.\"CodeID\" ASC";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Order act = cut.asc(administrativeDivision.get("codeID"));
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateOrderByAscendingCount() {
    final String exp = "COUNT(E0.\"CodeID\") ASC";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Expression<?> count = cut.count(administrativeDivision.get("codeID"));
    final Order act = cut.asc(count);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateOrderByEntity() {
    final String exp = "E0.\"CodePublisher\" ASC, E0.\"CodeID\" ASC, E0.\"DivisionCode\" ASC";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Order act = cut.asc(administrativeDivision);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateIsNull() {
    final String exp = "(E0.\"ParentCodeID\" IS NULL)";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Expression<?> act = cut.isNull(administrativeDivision.get("parentCodeID"));
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateIsNotNull() {
    final String exp = "(E0.\"ParentCodeID\" IS NOT NULL)";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Expression<?> act = cut.isNotNull(administrativeDivision.get("parentCodeID"));
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateLocateExpressionWithStringNull() {
    final String exp = "LOCATE(?1, E0.\"DivisionCode\")";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Expression<Integer> act = cut.locate(administrativeDivision.get("divisionCode"), "3");
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateLocateExpressionWithLiteralNull() {
    final String exp = "LOCATE(?1, E0.\"DivisionCode\")";
    final Expression<String> literal = cut.literal("3");
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Expression<Integer> act = cut.locate(administrativeDivision.get("divisionCode"), literal);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateLocateExpressionWithLiteralLiteral() {
    final String exp = "LOCATE(?1, E0.\"DivisionCode\", ?2)";
    final Expression<String> literal = cut.literal("3");
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Expression<Integer> act = cut.locate(administrativeDivision.get("divisionCode"), literal, cut.literal(2));
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateLocateExpressionWithStringInt() {
    final String exp = "LOCATE(?1, E0.\"DivisionCode\", ?2)";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Expression<Integer> act = cut.locate(administrativeDivision.get("divisionCode"), "3", 2);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateSubstringExpressionWithIntInt() {
    final String exp = "SUBSTRING(E0.\"Name\", ?1, ?2)";
    final Root<?> administrativeDivision = query.from(AdministrativeDivisionDescription.class);
    final Expression<String> act = cut.substring(administrativeDivision.get("name"), 1, 5);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateSubstringExpressionWithInt() {
    final String exp = "SUBSTRING(E0.\"Name\", ?1)";
    final Root<?> administrativeDivision = query.from(AdministrativeDivisionDescription.class);
    final Expression<String> act = cut.substring(administrativeDivision.get("name"), 1);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateSubstringExpressionWithLiteralLiteral() {
    final String exp = "SUBSTRING(E0.\"Name\", ?1, ?2)";
    final Expression<Integer> from = cut.literal(1);
    final Expression<Integer> len = cut.literal(5);
    final Root<?> administrativeDivision = query.from(AdministrativeDivisionDescription.class);
    final Expression<String> act = cut.substring(administrativeDivision.get("name"), from, len);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateSubstringExpressionWithLiteral() {
    final String exp = "SUBSTRING(E0.\"Name\", ?1)";
    final Expression<Integer> literal = cut.literal(1);
    final Root<?> administrativeDivision = query.from(AdministrativeDivisionDescription.class);
    final Expression<String> act = cut.substring(administrativeDivision.get("name"), literal);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateCoalesceExpressionWithExpressionExpression() {
    final String exp = "COALESCE(E0.\"Area\", E0.\"ParentCodeID\")";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Expression<String> act = cut.coalesce(administrativeDivision.get("area"), administrativeDivision.get(
        "parentCodeID"));
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateCoalesceExpression() {
    final Coalesce<String> act = cut.coalesce();
    assertThrows(NotImplementedException.class, () -> act.value(""));
  }

  @Test
  void testCreateCoalesceExpressionWithExpressionValue() {
    final String exp = "COALESCE(E0.\"Area\", ?1)";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Expression<Integer> act = cut.coalesce(administrativeDivision.get("area"), 10);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateCountDistinctExpression() {
    final String exp = "COUNT(DISTINCT(E0.\"CodeID\"))";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Expression<Long> act = cut.countDistinct(administrativeDivision.get("codeID"));
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateFunctionExpression() {
    // return cb.function(jpaFunction.getDBName(), jpaFunction.getResultParameter().getType(), jpaParameter);
    final String exp = "\"OLINGO\".\"PopulationDensity\"(E0.\"Area\", E0.\"Population\")";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);

    final Expression<Double> act = cut.function("\"OLINGO\".\"PopulationDensity\"", Double.class,
        administrativeDivision.get("area"), administrativeDivision.get("population"));
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
    assertEquals(Double.class, act.getJavaType());
  }

  @Test
  void testCreateEqualWithValueAndConverter() {
    // return cb.function(jpaFunction.getDBName(), jpaFunction.getResultParameter().getType(), jpaParameter);
    final String exp = "(E0.\"AccessRights\" = ?1)";
    final Root<?> person = query.from(Person.class);
    final AccessRights[] rights = { AccessRights.READ, AccessRights.DELETE };
    final Expression<Boolean> act = cut.equal(person.get("accessRights"), rights);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
    for (final ParameterExpression<?, ?> parameter : cut.getParameter().getParameter().values()) {
      if (parameter.getPosition() == 1)
        assertEquals((short) 9, parameter.getValue());
    }
  }

  @Test
  void testCreateEqualWithLiteralAndConverter() {
    // return cb.function(jpaFunction.getDBName(), jpaFunction.getResultParameter().getType(), jpaParameter);
    // AccessRights[] accessRights
    final String exp = "(E0.\"AccessRights\" = ?1)";
    final Root<?> person = query.from(Person.class);
    final AccessRights[] rights = { AccessRights.READ, AccessRights.DELETE };
    final Expression<Boolean> act = cut.equal(person.get("accessRights"), cut.literal(rights));
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
    for (final ParameterExpression<?, ?> parameter : cut.getParameter().getParameter().values()) {
      if (parameter.getPosition() == 1)
        assertEquals((short) 9, parameter.getValue());
    }
  }

  @Test
  void testCreateBetweenObject() {
    final String exp = "(E0.\"BusinessPartnerRole\" BETWEEN ?1 AND ?2)";
    final Root<?> roles = query.from(BusinessPartnerRole.class);
    final Expression<Boolean> act = cut.between(roles.get("roleCategory"), "A", "B");
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateBetweenExpression() {
    final String exp = "(E0.\"BusinessPartnerRole\" BETWEEN ?1 AND ?2)";
    final Root<?> roles = query.from(BusinessPartnerRole.class);
    final Expression<String> low = cut.literal("A");
    final Expression<String> high = cut.literal("B");
    final Expression<Boolean> act = cut.between(roles.get("roleCategory"), low, high);
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateRowNumber() {
    final String exp = "ROW_NUMBER() OVER()";
    final Selection<Long> act = cut.rowNumber();
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateRowNumberWithAlice() {
    final String exp = "ROW_NUMBER() OVER()";
    final Selection<Long> act = cut.rowNumber().alias("\"A\"");
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateRowNumberWithOrderBy() {
    final String exp = "ROW_NUMBER() OVER( ORDER BY E0.\"CodeID\" ASC)";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Selection<Long> act = cut.rowNumber().orderBy(cut.asc(administrativeDivision.get("codeID")));
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateRowNumberWithOrderByPrimaryKey() {
    final String exp =
        "ROW_NUMBER() OVER( ORDER BY E0.\"CodePublisher\" ASC, E0.\"CodeID\" ASC, E0.\"DivisionCode\" ASC)";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Selection<Long> act = cut.rowNumber().orderBy(cut.asc(administrativeDivision));
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateRowNumberWithPartitionBy() {
    final String exp = "ROW_NUMBER() OVER( PARTITION BY E0.\"CodeID\")";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Selection<Long> act = cut.rowNumber().partitionBy(administrativeDivision.get("codeID"));
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateRowNumberWithPartitionAndOrder() {
    final String exp = "ROW_NUMBER() OVER( PARTITION BY E0.\"CodeID\" ORDER BY E0.\"CodeID\" ASC)";
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    final Selection<Long> act = cut.rowNumber()
        .partitionBy(administrativeDivision.get("codeID"))
        .orderBy(cut.asc(administrativeDivision.get("codeID")));
    assertEquals(exp, ((SqlConvertible) act).asSQL(statement).toString());
  }

  @Test
  void testCreateConcatExpression() {
    final String stringA = "A";
    final String stringB = "B";

    final Expression<String> literalA = cut.literal(stringA);
    final Expression<String> literalB = cut.literal(stringB);

    assertConcatExpression(stringA, stringB, cut.concat(literalA, literalB));
    assertConcatExpression(stringA, stringB, cut.concat(stringA, literalB));
    assertConcatExpression(stringA, stringB, cut.concat(literalA, stringB));
  }

  void assertConcatExpression(final String stringA, final String stringB, final Expression<String> act) {
    final StringBuilder builder = new StringBuilder();
    assertTrue(act instanceof ConcatExpression);
    assertEquals("CONCAT(?1, ?2)", ((ExpressionImpl<String>) act).asSQL(builder).toString());
    final Map<Integer, ParameterExpression<Object, Object>> actMap = cut.getParameter().getParameter();
    assertEquals(2, actMap.size());
    boolean aFound = false;
    boolean bFound = false;
    for (final ParameterExpression<?, ?> parameter : actMap.values()) {
      if (stringA.equals(parameter.getValue()))
        aFound = true;
      if (stringB.equals(parameter.getValue()))
        bFound = true;
    }
    assertTrue(aFound);
    assertTrue(bFound);
  }

  @Test
  void testInReturnsInExpression() {
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);
    assertNotNull(cut.in(administrativeDivision.get("codeID")));
  }
}
