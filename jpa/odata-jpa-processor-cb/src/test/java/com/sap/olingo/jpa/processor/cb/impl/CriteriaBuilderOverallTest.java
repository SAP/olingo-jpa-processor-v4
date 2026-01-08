package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaBuilder;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerProtected;
import com.sap.olingo.jpa.processor.core.testmodel.DateTimeTest;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testmodel.Team;

abstract class CriteriaBuilderOverallTest {
  protected ProcessorCriteriaBuilder cb;
  protected EntityManager em;
  protected StringBuilder statement;
  protected CriteriaQuery<Tuple> query;

  void setup(final EntityManagerFactory emf, final JPAServiceDocument sd,
      final ProcessorSqlPatternProvider sqlPattern) {
    em = new EntityManagerWrapper(emf.createEntityManager(), sd, sqlPattern);
    cb = (ProcessorCriteriaBuilder) em.getCriteriaBuilder();
    assertNotNull(cb);
    statement = new StringBuilder();
    query = cb.createTupleQuery();
  }

  @Test
  void testCriteriaBuilderImplReturnsSQLQuery() {
    assertTrue(query instanceof SqlConvertible);
  }

  @Test
  void testSimpleQueryAll() {
    final Root<?> team = query.from(Team.class);

    query.multiselect(team);
    ((SqlConvertible) query).asSQL(statement);
    assertEquals("SELECT E0.\"Active\" S0, E0.\"TeamKey\" S1, E0.\"Name\" S2 FROM \"OLINGO\".\"Team\" E0", statement
        .toString().trim());
    final TypedQuery<Tuple> typedQuery = em.createQuery(query);
    final List<Tuple> act = typedQuery.getResultList();
    assertEquals(4, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testSimpleQueryMultiSelect() {
    final Root<?> org = query.from(Organization.class);

    query.multiselect(org.get("type").alias("count"), org.get("iD"), org.get("eTag"));
    ((SqlConvertible) query).asSQL(statement);
    assertEquals(
        "SELECT E0.\"Type\" S0, E0.\"ID\" S1, E0.\"ETag\" S2 FROM \"OLINGO\".\"BusinessPartner\" E0 WHERE (E0.\"Type\" = ?1)",
        statement.toString().trim());
    final TypedQuery<Tuple> typedQuery = em.createQuery(query);
    final List<Tuple> act = typedQuery.getResultList();
    assertEquals(10, act.size());
    assertNotNull(act.get(0));
    assertEquals("2", act.get(0).get("count"));
  }

  @Test
  void testWhereWithMultipleAnd() {
    // SELECT E0."CodeID" FROM "OLINGO"."AdministrativeDivision" E0 WHERE (((E0."DivisionCode" = ?) AND (E0."CodeID" =
    // ?)) AND (E0."CodePublisher" = ?))
    final Root<?> administrativeDivision = query.from(AdministrativeDivision.class);

    query.multiselect(administrativeDivision.get("codeID"));
    final Predicate[] restrictions = new Predicate[3];
    restrictions[0] = cb.equal(administrativeDivision.get("codeID"), "NUTS2");
    restrictions[1] = cb.equal(administrativeDivision.get("divisionCode"), "BE34");
    restrictions[2] = cb.equal(administrativeDivision.get("codePublisher"), "Eurostat");
    query.where(cb.and(restrictions));
    ((SqlConvertible) query).asSQL(statement);
    final TypedQuery<Tuple> typedQuery = em.createQuery(query);
    final List<Tuple> act = typedQuery.getResultList();
    assertNotNull(typedQuery);
    assertEquals(1, act.size());
  }

  @Test
  void testSimpleLikeQueryAll() {
    final Root<?> adminDiv = query.from(AdministrativeDivision.class);

    query.multiselect(adminDiv.get("codeID"));
    query.where(cb.like(adminDiv.get("codeID"), "%6-1"));
    ((SqlConvertible) query).asSQL(statement);
    assertEquals("SELECT E0.\"CodeID\" S0 FROM \"OLINGO\".\"AdministrativeDivision\" E0 WHERE (E0.\"CodeID\" LIKE ?1)",
        statement.toString().trim());
    final TypedQuery<Tuple> typedQuery = em.createQuery(query);
    final List<Tuple> act = typedQuery.getResultList();
    assertEquals(4, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testSimpleLikeQueryAllWithEscape() {

    final Root<?> adminDiv = query.from(AdministrativeDivision.class);
    final Expression<String> p = cb.literal("%6-1");
    final Expression<Character> e = cb.literal('/');

    query.multiselect(adminDiv.get("codeID"));
    query.where(cb.like(adminDiv.get("codeID"), p, e));
    ((SqlConvertible) query).asSQL(statement);
    assertEquals(
        "SELECT E0.\"CodeID\" S0 FROM \"OLINGO\".\"AdministrativeDivision\" E0 WHERE (E0.\"CodeID\" LIKE ?1 ESCAPE ?2)",
        statement.toString().trim());
    final TypedQuery<Tuple> typedQuery = em.createQuery(query);
    final List<Tuple> act = typedQuery.getResultList();
    assertEquals(4, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testOrderByClause() {
    final Root<?> team = query.from(Team.class);

    query.multiselect(team);
    query.orderBy(cb.asc(team.get("name")));
    ((SqlConvertible) query).asSQL(statement);
    assertEquals(
        "SELECT E0.\"Active\" S0, E0.\"TeamKey\" S1, E0.\"Name\" S2 FROM \"OLINGO\".\"Team\" E0 ORDER BY E0.\"Name\" ASC",
        statement
            .toString().trim());
    final TypedQuery<Tuple> typedQuery = em.createQuery(query);
    final List<Tuple> act = typedQuery.getResultList();
    assertEquals(4, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testOrderByClauseTwoElements() {
    final Root<?> team = query.from(Team.class);

    query.multiselect(team);
    query.orderBy(cb.asc(team.get("name")), cb.desc(team.get("iD")));
    ((SqlConvertible) query).asSQL(statement);
    assertEquals(
        "SELECT E0.\"Active\" S0, E0.\"TeamKey\" S1, E0.\"Name\" S2 FROM \"OLINGO\".\"Team\" E0 ORDER BY E0.\"Name\" ASC, E0.\"TeamKey\" DESC",
        statement.toString().trim());
    final TypedQuery<Tuple> typedQuery = em.createQuery(query);
    final List<Tuple> act = typedQuery.getResultList();
    assertEquals(4, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testSimpleToLowerQuery() {
    final Root<?> adminDiv = query.from(AdministrativeDivisionDescription.class);
    final Expression<Boolean> equal = cb.equal(adminDiv.get("language"), "de");
    final Expression<Boolean> lower = cb.equal(cb.lower(adminDiv.get("name")), "brandenburg");

    query.multiselect(adminDiv.get("codeID"));
    query.where(cb.and(equal, lower));
    ((SqlConvertible) query).asSQL(statement);
    assertEquals(
        "SELECT E0.\"CodeID\" S0 FROM \"OLINGO\".\"AdministrativeDivisionDescription\" E0 WHERE ((E0.\"LanguageISO\" = ?1) AND (LOWER(E0.\"Name\") = ?2))",
        statement.toString().trim());
    final TypedQuery<Tuple> typedQuery = em.createQuery(query);
    final List<Tuple> act = typedQuery.getResultList();
    assertEquals(1, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testSimpleSubstringQuery() {
    final Root<?> adminDiv = query.from(AdministrativeDivisionDescription.class);
    final Expression<Boolean> equal = cb.equal(adminDiv.get("language"), "de");
    final Expression<String> sub = cb.substring(adminDiv.get("name"), 1, 5);
    final Expression<Boolean> lower = cb.equal(cb.lower(sub), "north");

    query.multiselect(adminDiv.get("codeID"));
    query.where(cb.and(equal, lower));
    ((SqlConvertible) query).asSQL(statement);
    assertEquals(
        expectedQuerySubstring(),
        statement.toString().trim());
    final TypedQuery<Tuple> typedQuery = em.createQuery(query);
    final List<Tuple> act = typedQuery.getResultList();
    assertEquals(2, act.size());
    assertNotNull(act.get(0));
  }

  // SELECT "CodeID" FROM "OLINGO"."AdministrativeDivisionDescription" WHERE (("LanguageISO" = 'de') AND
  // (LOWER(SUBSTR("Name", 1, 5)) = 'north'))

  @Test
  void testSimpleLocateQuery() {
    final Root<?> adminDiv = query.from(AdministrativeDivision.class);
    final Expression<Integer> locate = cb.locate(adminDiv.get("divisionCode"), "3");

    query.multiselect(adminDiv.get("codeID"));
    query.where(cb.equal(locate, 4));
    ((SqlConvertible) query).asSQL(statement);
    assertEquals(
        "SELECT E0.\"CodeID\" S0 FROM \"OLINGO\".\"AdministrativeDivision\" E0 WHERE (LOCATE(?1, E0.\"DivisionCode\") = ?2)",
        statement.toString().trim());
    final TypedQuery<Tuple> typedQuery = em.createQuery(query);
    final List<Tuple> act = typedQuery.getResultList();
    assertEquals(7, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testSimpleConcatQuery() {
    final Root<?> person = query.from(Person.class);
    final Expression<String> concat = cb.concat(cb.concat(person.get("lastName"), ","), person.get("firstName"));

    query.multiselect(person.get("iD"));
    query.where(cb.equal(concat, "Mustermann,Max"));
    ((SqlConvertible) query).asSQL(statement);
    assertEquals(
        expectedQueryConcat(),
        statement.toString().trim());
    final TypedQuery<Tuple> typedQuery = em.createQuery(query);
    final List<Tuple> act = typedQuery.getResultList();
    assertEquals(1, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testSimpleTimestampQuery() {
    final Root<?> person = query.from(Person.class);
    final Expression<Timestamp> locate = person.get("administrativeInformation").get("created").get("at");

    query.multiselect(person.get("iD"), person.get("creationDateTime"));
    query.where(cb.lessThan(locate, cb.currentTimestamp()));
    ((SqlConvertible) query).asSQL(statement);
    assertEquals(
        "SELECT E0.\"ID\" S0, E0.\"CreatedAt\" S1 FROM \"OLINGO\".\"BusinessPartner\" E0 WHERE ((E0.\"CreatedAt\" < CURRENT_TIMESTAMP) AND (E0.\"Type\" = ?1))",
        statement.toString().trim());
    final TypedQuery<Tuple> typedQuery = em.createQuery(query);
    final List<Tuple> act = typedQuery.getResultList();
    assertEquals(3, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testSelectPrimitiveCollectionProperty() {
    final Root<?> org = query.from(Organization.class);
    final Join<Object, Object> comment = org.join("comment");
    final Path<Object> id = org.get("iD");
    id.alias("ID");
    comment.alias("Comment");
    query.multiselect(id, comment);
    query.where(cb.equal(id, '1'));
    final TypedQuery<Tuple> typedQuery = em.createQuery(query);
    final List<Tuple> act = typedQuery.getResultList();
    assertEquals(2, act.size());
  }

  @Test
  void testSelectComplexCollectionProperty() {
    final Root<?> org = query.from(Person.class);
    final Join<Object, Object> addr = org.join("inhouseAddress");
    final Path<Object> id = org.get("iD");
    id.alias("ID");
    addr.alias("inhouseAddress");
    query.multiselect(id, addr);
    query.where(cb.equal(id, "99"));
    final TypedQuery<Tuple> typedQuery = em.createQuery(query);
    final List<Tuple> act = typedQuery.getResultList();
    assertEquals(2, act.size());
    assertNotNull(act.get(0).get("inhouseAddress.Building"));
  }

  @Test
  void testSelectCountOneKey() {
    // SELECT COUNT(DISTINCT(*)) FROM "OLINGO"."BusinessPartnerProtected" E0 WHERE (E0."UserName" = ?)
    final CriteriaQuery<Number> qc = cb.createQuery(Number.class);
    final Root<?> org = qc.from(BusinessPartnerProtected.class);
    qc.multiselect(cb.countDistinct(org));
    qc.where(cb.equal(org.get("userName"), "Willi"));
    final TypedQuery<Number> tq = em.createQuery(qc);
    final Long act = tq.getSingleResult().longValue();
    assertEquals(3L, act);
  }

  @Test
  void testSelectDateTime() {
    final Root<?> dateTime = query.from(DateTimeTest.class);
    final Path<Object> id = dateTime.get("iD");
    query.multiselect(dateTime);
    query.where(cb.equal(id, "99"));
    final TypedQuery<Tuple> typedQuery = em.createQuery(query);
    final List<Tuple> act = typedQuery.getResultList();
    assertEquals(1, act.size());
    assertEquals(LocalDate.parse("1999-04-01"), act.get(0).get("S0"));
    assertEquals(LocalDateTime.parse("2016-01-20T09:21:23"), act.get(0).get("S2"));
  }

  @Test
  void testSelectLimitOffset() {
    final Root<?> person = query.from(Person.class);

    query.multiselect(person.get("iD"));
    final TypedQuery<Tuple> typedQuery = em.createQuery(query);
    typedQuery.setFirstResult(1);
    typedQuery.setMaxResults(1);
    ((SqlConvertible) query).asSQL(statement);
    assertEquals(expectedQueryLimitOffset(), statement.toString().trim());
    final List<Tuple> act = typedQuery.getResultList();
    assertEquals(1, act.size());
    assertNotNull(act.get(0));
  }

  protected String expectedQueryLimitOffset() {
    return "SELECT E0.\"ID\" S0 FROM \"OLINGO\".\"BusinessPartner\" E0 WHERE (E0.\"Type\" = ?1) LIMIT 1 OFFSET 1";
  }

  protected String expectedQueryConcat() {
    return "SELECT E0.\"ID\" S0 FROM \"OLINGO\".\"BusinessPartner\" E0 WHERE ((CONCAT(CONCAT(E0.\"NameLine2\", ?1), E0.\"NameLine1\") = ?2) AND (E0.\"Type\" = ?3))";
  }

  protected String expectedQuerySubstring() {
    return "SELECT E0.\"CodeID\" S0 FROM \"OLINGO\".\"AdministrativeDivisionDescription\" E0 WHERE ((E0.\"LanguageISO\" = ?1) AND (LOWER(SUBSTRING(E0.\"Name\", ?2, ?3)) = ?4))";
  }
}