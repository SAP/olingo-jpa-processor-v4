package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import javax.persistence.Tuple;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CriteriaBuilder.Trimspec;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;
import com.sap.olingo.jpa.processor.core.testmodel.AccessRights;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

class CriteriaBuilderImplTest extends BuilderBaseTest {
  CriteriaBuilderImpl cut;
  private StringBuilder stmt;
  private CriteriaQuery<Tuple> q;

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
        arguments(c.getMethod("in", Expression.class)),
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
        arguments(c.getMethod("currentTime")));
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
    stmt = new StringBuilder();
    q = cut.createTupleQuery();
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

    final Root<?> adminDiv = q.from(AdministrativeDivision.class);

    final Object[] params = { adminDiv.get("codeID"), adminDiv.get("parentCodeID") };
    final Predicate act = (Predicate) m.invoke(cut, params);
    assertNotNull(act);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
    assertEquals(0, cut.getParameter().getParameter().size());
  }

  @ParameterizedTest
  @MethodSource("binaryValueImplemented")
  void testBinaryExpressionWithObject(final Method m, final String exp) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {

    final Root<?> adminDiv = q.from(AdministrativeDivision.class);

    final Object[] params = { adminDiv.get("codeID"), "NUTS2" };
    final Predicate act = (Predicate) m.invoke(cut, params);

    assertNotNull(act);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
    assertEquals(1, cut.getParameter().getParameter().size());
    assertEquals("NUTS2", cut.getParameter().getParameter().get(1).getValue());
  }

  @ParameterizedTest
  @MethodSource("binaryImplementedNumeric")
  void testBinaryNumericExpressionWithExpression(final Method m, final String exp) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {

    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Object[] params = { adminDiv.get("area"), adminDiv.get("population") };
    final Expression<?> act = (Expression<?>) m.invoke(cut, params);
    assertNotNull(act);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
    assertEquals(0, cut.getParameter().getParameter().size());
  }

  @ParameterizedTest
  @MethodSource("binaryValueImplementedNumeric")
  void testBinaryNumericExpressionWithObject(final Method m, final String exp) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {

    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Object[] params = { adminDiv.get("area"), 1000 };
    final Expression<?> act = (Expression<?>) m.invoke(cut, params);

    assertNotNull(act);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
    assertEquals(1, cut.getParameter().getParameter().size());
    assertEquals(1000, cut.getParameter().getParameter().get(1).getValue());
  }

  @ParameterizedTest
  @MethodSource("binaryValueImplementedNumericInverse")
  void testBinaryNumericExpressionWithObjectFirst(final Method m, final String exp)
      throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {

    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Object[] params = { 1000, adminDiv.get("area") };
    final Expression<?> act = (Expression<?>) m.invoke(cut, params);

    assertNotNull(act);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
    assertEquals(1, cut.getParameter().getParameter().size());
    assertEquals(1000, cut.getParameter().getParameter().get(1).getValue());
  }

  @ParameterizedTest
  @MethodSource("unaryFunctionsImplemented")
  void testCreateUnaryFunction(final Method m, final String exp) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    final Root<?> adminDiv = q.from(AdministrativeDivisionDescription.class);
    final Object[] params = { adminDiv.get("name") };
    final Expression<?> act = (Expression<?>) m.invoke(cut, params);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @ParameterizedTest
  @MethodSource("subQueryExpressionsImplemented")
  void testCreateSubQuery(final Method m, final String exp) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    final Subquery<Long> sub = q.subquery(Long.class);
    sub.select(cut.literal(1L));
    sub.from(AdministrativeDivision.class);
    final Object[] params = { sub };
    final Expression<?> act = (Expression<?>) m.invoke(cut, params);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
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
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Predicate act = cut.ge(adminDiv.get("area"), adminDiv.get("population"));

    assertNotNull(act);
    assertEquals("(E0.\"Area\" >= E0.\"Population\")", ((SqlConvertible) act).asSQL(stmt).toString());
    assertEquals(0, cut.getParameter().getParameter().size());
  }

  @Test
  void testThrowsNullPointerExpressionNull() {
    assertThrows(NullPointerException.class, () -> cut.equal(null, "NUTS2"));
  }

  @Test
  void testCreateMultiAnd() {
    final String exp = "(((E0.\"CodeID\" = ?1) AND (E0.\"DivisionCode\" = ?2)) AND (E0.\"CodePublisher\" = ?3))";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Predicate[] restrictions = new Predicate[3];
    restrictions[0] = cut.equal(adminDiv.get("codeID"), "NUTS2");
    restrictions[1] = cut.equal(adminDiv.get("divisionCode"), "BE34");
    restrictions[2] = cut.equal(adminDiv.get("codePublisher"), "Eurostat");
    final Predicate act = cut.and(restrictions);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateMultiAndThrowsExceptionOnWrongParameter() {
    final Predicate[] rNull = null;
    assertThrows(IllegalArgumentException.class, () -> cut.and(rNull));

    final Predicate[] rEmpty = new Predicate[3];
    assertThrows(IllegalArgumentException.class, () -> cut.and(rEmpty));

    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Predicate[] rOneEntry = new Predicate[1];
    rOneEntry[0] = cut.equal(adminDiv.get("codeID"), "NUTS2");
    assertThrows(IllegalArgumentException.class, () -> cut.and(rOneEntry));
  }

  @Test
  void testCreateOneOr() {
    final String exp = "((E0.\"CodeID\" = ?1) OR (E0.\"CodeID\" = ?2))";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Predicate one = cut.equal(adminDiv.get("codeID"), "NUTS2");
    final Predicate two = cut.equal(adminDiv.get("codeID"), "NUTS3");
    final Predicate act = cut.or(one, two);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateMultiOrThrowsExceptionOnWrongParameter() {
    final Predicate[] rNull = null;
    assertThrows(IllegalArgumentException.class, () -> cut.or(rNull));

    final Predicate[] rEmpty = new Predicate[3];
    assertThrows(IllegalArgumentException.class, () -> cut.or(rEmpty));

    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Predicate[] rOneEntry = new Predicate[1];
    rOneEntry[0] = cut.equal(adminDiv.get("codeID"), "NUTS2");
    assertThrows(IllegalArgumentException.class, () -> cut.and(rOneEntry));
  }

  @Test
  void testCreateMultiOr() {
    final String exp = "(((E0.\"CodeID\" = ?1) OR (E0.\"DivisionCode\" = ?2)) OR (E0.\"CodePublisher\" = ?3))";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Predicate[] restrictions = new Predicate[3];
    restrictions[0] = cut.equal(adminDiv.get("codeID"), "NUTS2");
    restrictions[1] = cut.equal(adminDiv.get("divisionCode"), "BE34");
    restrictions[2] = cut.equal(adminDiv.get("codePublisher"), "Eurostat");
    final Predicate act = cut.or(restrictions);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateNot() {
    final String exp = "(NOT (E0.\"CodeID\" = ?1))";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Predicate one = cut.equal(adminDiv.get("codeID"), "NUTS2");
    final Predicate act = cut.not(one);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testLiteralExpressionReturnsParameter() {
    final String exp = "?1";
    final Expression<LocalDate> act = cut.literal(LocalDate.now());
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
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
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Predicate act = cut.like(adminDiv.get("codeID"), "6-1");
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateNotLikeExpressionWithString() {
    final String exp = "(NOT (E0.\"CodeID\" LIKE ?1))";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Predicate act = cut.notLike(adminDiv.get("codeID"), "6-1");
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateLikeExpressionWithStringAndEscape() {
    final String exp = "(E0.\"CodeID\" LIKE ?1 ESCAPE ?2)";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Predicate act = cut.like(adminDiv.get("codeID"), "%6-1", '/');
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateNotLikeExpressionWithStringAndEscape() {
    final String exp = "(NOT (E0.\"CodeID\" LIKE ?1 ESCAPE ?2))";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Predicate act = cut.notLike(adminDiv.get("codeID"), "%6-1", '/');
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateLikeExpressionWithLiteral() {
    final String exp = "(E0.\"CodeID\" LIKE ?1)";
    final Expression<String> literal = cut.literal("%6-1");
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Predicate act = cut.like(adminDiv.get("codeID"), literal);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateNotLikeExpressionWithLiteral() {
    final String exp = "(NOT (E0.\"CodeID\" LIKE ?1))";
    final Expression<String> literal = cut.literal("%6-1");
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Predicate act = cut.notLike(adminDiv.get("codeID"), literal);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateLikeExpressionWithLiteralLiteral() {
    final String exp = "(E0.\"CodeID\" LIKE ?1 ESCAPE ?2)";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<String> p = cut.literal("6-1");
    final Expression<Character> e = cut.literal('/');
    final Predicate act = cut.like(adminDiv.get("codeID"), p, e);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateNotLikeExpressionWithLiteralLiteral() {
    final String exp = "(NOT (E0.\"CodeID\" LIKE ?1 ESCAPE ?2))";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<String> p = cut.literal("6-1");
    final Expression<Character> e = cut.literal('/');
    final Predicate act = cut.notLike(adminDiv.get("codeID"), p, e);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateLikeExpressionWithLiteralString() {
    final String exp = "(E0.\"CodeID\" LIKE ?2 ESCAPE ?1)";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<Character> e = cut.literal('/');
    final Predicate act = cut.like(adminDiv.get("codeID"), "%6-1", e);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateNotLikeExpressionWithLiteralString() {
    final String exp = "(NOT (E0.\"CodeID\" LIKE ?2 ESCAPE ?1))";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<Character> e = cut.literal('/');
    final Predicate act = cut.notLike(adminDiv.get("codeID"), "%6-1", e);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateLikeExpressionWithStrngLiteral() {
    final String exp = "(E0.\"CodeID\" LIKE ?1 ESCAPE ?2)";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<String> p = cut.literal("6-1");
    final Predicate act = cut.like(adminDiv.get("codeID"), p, '/');
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateNotLikeExpressionWithStrngLiteral() {
    final String exp = "(NOT (E0.\"CodeID\" LIKE ?1 ESCAPE ?2))";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<String> p = cut.literal("6-1");
    final Predicate act = cut.notLike(adminDiv.get("codeID"), p, '/');
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateOrderByDescending() {
    final String exp = "E0.\"CodeID\" DESC";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Order act = cut.desc(adminDiv.get("codeID"));
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateOrderByAscending() {
    final String exp = "E0.\"CodeID\" ASC";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Order act = cut.asc(adminDiv.get("codeID"));
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateOrderByAscendingCount() {
    final String exp = "COUNT(E0.\"CodeID\") ASC";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<?> count = cut.count(adminDiv.get("codeID"));
    final Order act = cut.asc(count);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateOrderByEntity() {
    final String exp = "E0.\"CodePublisher\" ASC, E0.\"CodeID\" ASC, E0.\"DivisionCode\" ASC";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Order act = cut.asc(adminDiv);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateIsNull() {
    final String exp = "(E0.\"ParentCodeID\" IS NULL)";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<?> act = cut.isNull(adminDiv.get("parentCodeID"));
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateIsNotNull() {
    final String exp = "(E0.\"ParentCodeID\" IS NOT NULL)";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<?> act = cut.isNotNull(adminDiv.get("parentCodeID"));
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateLocateExpressionWithStringNull() {
    final String exp = "LOCATE(?1, E0.\"DivisionCode\")";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<Integer> act = cut.locate(adminDiv.get("divisionCode"), "3");
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateLocateExpressionWithLiteralNull() {
    final String exp = "LOCATE(?1, E0.\"DivisionCode\")";
    final Expression<String> literal = cut.literal("3");
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<Integer> act = cut.locate(adminDiv.get("divisionCode"), literal);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateLocateExpressionWithLiteralLiteral() {
    final String exp = "LOCATE(?1, E0.\"DivisionCode\", ?2)";
    final Expression<String> literal = cut.literal("3");
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<Integer> act = cut.locate(adminDiv.get("divisionCode"), literal, cut.literal(2));
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateLocateExpressionWithStringInt() {
    final String exp = "LOCATE(?1, E0.\"DivisionCode\", ?2)";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<Integer> act = cut.locate(adminDiv.get("divisionCode"), "3", 2);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateSubstringExpressionWithIntInt() {
    final String exp = "SUBSTRING(E0.\"Name\", ?1, ?2)";
    final Root<?> adminDiv = q.from(AdministrativeDivisionDescription.class);
    final Expression<String> act = cut.substring(adminDiv.get("name"), 1, 5);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateSubstringExpressionWithInt() {
    final String exp = "SUBSTRING(E0.\"Name\", ?1)";
    final Root<?> adminDiv = q.from(AdministrativeDivisionDescription.class);
    final Expression<String> act = cut.substring(adminDiv.get("name"), 1);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateSubstringExpressionWithLiteralLiteral() {
    final String exp = "SUBSTRING(E0.\"Name\", ?1, ?2)";
    final Expression<Integer> from = cut.literal(1);
    final Expression<Integer> len = cut.literal(5);
    final Root<?> adminDiv = q.from(AdministrativeDivisionDescription.class);
    final Expression<String> act = cut.substring(adminDiv.get("name"), from, len);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateSubstringExpressionWithLiteral() {
    final String exp = "SUBSTRING(E0.\"Name\", ?1)";
    final Expression<Integer> literal = cut.literal(1);
    final Root<?> adminDiv = q.from(AdministrativeDivisionDescription.class);
    final Expression<String> act = cut.substring(adminDiv.get("name"), literal);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateCoalesceExpressionWithExpressionExpression() {
    final String exp = "COALESCE(E0.\"Area\", E0.\"ParentCodeID\")";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<String> act = cut.coalesce(adminDiv.get("area"), adminDiv.get("parentCodeID"));
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateCoalesceExpressionWithExpressionValue() {
    final String exp = "COALESCE(E0.\"Area\", ?1)";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<Integer> act = cut.coalesce(adminDiv.get("area"), 10);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateCountDistinctExpression() {
    final String exp = "COUNT(DISTINCT(E0.\"CodeID\"))";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<Long> act = cut.countDistinct(adminDiv.get("codeID"));
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateFunctionExpression() {
    // return cb.function(jpaFunction.getDBName(), jpaFunction.getResultParameter().getType(), jpaParameter);
    final String exp = "\"OLINGO\".\"PopulationDensity\"(E0.\"Area\", E0.\"Population\")";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);

    final Expression<Double> act = cut.function("\"OLINGO\".\"PopulationDensity\"", Double.class,
        adminDiv.get("area"), adminDiv.get("population"));
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateEqualWithValueAndConverter() {
    // return cb.function(jpaFunction.getDBName(), jpaFunction.getResultParameter().getType(), jpaParameter);
    final String exp = "(E0.\"AccessRights\" = ?1)";
    final Root<?> person = q.from(Person.class);
    final AccessRights[] rights = { AccessRights.Read, AccessRights.Delete };
    final Expression<Boolean> act = cut.equal(person.get("accessRights"), rights);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
    assertEquals((short) 9, cut.getParameter().getParameter().get(1).getValue());
  }

  @Test
  void testCreateEqualWithLiteralAndConverter() {
    // return cb.function(jpaFunction.getDBName(), jpaFunction.getResultParameter().getType(), jpaParameter);
    // AccessRights[] accessRights
    final String exp = "(E0.\"AccessRights\" = ?1)";
    final Root<?> person = q.from(Person.class);
    final AccessRights[] rights = { AccessRights.Read, AccessRights.Delete };
    final Expression<Boolean> act = cut.equal(person.get("accessRights"), cut.literal(rights));
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
    assertEquals((short) 9, cut.getParameter().getParameter().get(1).getValue());
  }

  @Test
  void testCreateBetweenObject() {
    final String exp = "(E0.\"BusinessPartnerRole\" BETWEEN ?1 AND ?2)";
    final Root<?> roles = q.from(BusinessPartnerRole.class);
    final Expression<Boolean> act = cut.between(roles.get("roleCategory"), "A", "B");
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateBetweenExpression() {
    final String exp = "(E0.\"BusinessPartnerRole\" BETWEEN ?1 AND ?2)";
    final Root<?> roles = q.from(BusinessPartnerRole.class);
    final Expression<String> low = cut.literal("A");
    final Expression<String> high = cut.literal("B");
    final Expression<Boolean> act = cut.between(roles.get("roleCategory"), low, high);
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateRowNumber() {
    final String exp = "ROW_NUMBER() OVER()";
    final Selection<Long> act = cut.rowNumber();
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateRowNumberWithAlice() {
    final String exp = "ROW_NUMBER() OVER()";
    final Selection<Long> act = cut.rowNumber().alias("\"A\"");
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateRowNumberWithOrderBy() {
    final String exp = "ROW_NUMBER() OVER( ORDER BY E0.\"CodeID\" ASC)";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Selection<Long> act = cut.rowNumber().orderBy(cut.asc(adminDiv.get("codeID")));
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateRowNumberWithOrderByPrimaryKey() {
    final String exp =
        "ROW_NUMBER() OVER( ORDER BY E0.\"CodePublisher\" ASC, E0.\"CodeID\" ASC, E0.\"DivisionCode\" ASC)";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Selection<Long> act = cut.rowNumber().orderBy(cut.asc(adminDiv));
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateRowNumberWithPartitionBy() {
    final String exp = "ROW_NUMBER() OVER( PARTITION BY E0.\"CodeID\")";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Selection<Long> act = cut.rowNumber().partitionBy(adminDiv.get("codeID"));
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }

  @Test
  void testCreateRowNumberWithPartitionAndOrder() {
    final String exp = "ROW_NUMBER() OVER( PARTITION BY E0.\"CodeID\" ORDER BY E0.\"CodeID\" ASC)";
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Selection<Long> act = cut.rowNumber()
        .partitionBy(adminDiv.get("codeID"))
        .orderBy(cut.asc(adminDiv.get("codeID")));
    assertEquals(exp, ((SqlConvertible) act).asSQL(stmt).toString());
  }
}
