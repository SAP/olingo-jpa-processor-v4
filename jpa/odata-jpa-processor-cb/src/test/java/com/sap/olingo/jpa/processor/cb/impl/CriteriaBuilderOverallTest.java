package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaBuilder;
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
  protected StringBuilder stmt;
  protected CriteriaQuery<Tuple> q;

  void setup(final EntityManagerFactory emf, final JPAServiceDocument sd) {
    em = new EntityManagerWrapper(emf.createEntityManager(), sd);
    cb = (ProcessorCriteriaBuilder) em.getCriteriaBuilder();
    assertNotNull(cb);
    stmt = new StringBuilder();
    q = cb.createTupleQuery();
  }

  @Test
  void testCriteriaBuilderImplReturnsSQLQuery() {
    assertTrue(q instanceof SqlConvertible);
  }

  @Test
  void testSimpleQueryAll() {
    final Root<?> team = q.from(Team.class);

    q.multiselect(team);
    ((SqlConvertible) q).asSQL(stmt);
    assertEquals("SELECT E0.\"TeamKey\" S0, E0.\"Name\" S1 FROM \"OLINGO\".\"Team\" E0", stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(4, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testSimpleQueryMultiSelect() {
    final Root<?> org = q.from(Organization.class);

    q.multiselect(org.get("type").alias("count"), org.get("iD"), org.get("eTag"));
    ((SqlConvertible) q).asSQL(stmt);
    assertEquals(
        "SELECT E0.\"Type\" S0, E0.\"ID\" S1, E0.\"ETag\" S2 FROM \"OLINGO\".\"BusinessPartner\" E0 WHERE (E0.\"Type\" = ?1)",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(10, act.size());
    assertNotNull(act.get(0));
    assertEquals("2", act.get(0).get("count"));
  }

  @Test
  void testWhereWithMultipleAnd() {
    // SELECT E0."CodeID" FROM "OLINGO"."AdministrativeDivision" E0 WHERE (((E0."DivisionCode" = ?) AND (E0."CodeID" =
    // ?)) AND (E0."CodePublisher" = ?))
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);

    q.multiselect(adminDiv.get("codeID"));
    final Predicate[] restrictions = new Predicate[3];
    restrictions[0] = cb.equal(adminDiv.get("codeID"), "NUTS2");
    restrictions[1] = cb.equal(adminDiv.get("divisionCode"), "BE34");
    restrictions[2] = cb.equal(adminDiv.get("codePublisher"), "Eurostat");
    q.where(cb.and(restrictions));
    ((SqlConvertible) q).asSQL(stmt);
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertNotNull(tq);
    assertEquals(1, act.size());
  }

  @Test
  void testSimpleLikeQueryAll() {
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);

    q.multiselect(adminDiv.get("codeID"));
    q.where(cb.like(adminDiv.get("codeID"), "%6-1"));
    ((SqlConvertible) q).asSQL(stmt);
    assertEquals("SELECT E0.\"CodeID\" S0 FROM \"OLINGO\".\"AdministrativeDivision\" E0 WHERE (E0.\"CodeID\" LIKE ?1)",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(4, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testSimpleLikeQueryAllWithEscape() {

    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<String> p = cb.literal("%6-1");
    final Expression<Character> e = cb.literal('/');

    q.multiselect(adminDiv.get("codeID"));
    q.where(cb.like(adminDiv.get("codeID"), p, e));
    ((SqlConvertible) q).asSQL(stmt);
    assertEquals(
        "SELECT E0.\"CodeID\" S0 FROM \"OLINGO\".\"AdministrativeDivision\" E0 WHERE (E0.\"CodeID\" LIKE ?1 ESCAPE ?2)",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(4, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testOrderByClause() {
    final Root<?> team = q.from(Team.class);

    q.multiselect(team);
    q.orderBy(cb.asc(team.get("name")));
    ((SqlConvertible) q).asSQL(stmt);
    assertEquals("SELECT E0.\"TeamKey\" S0, E0.\"Name\" S1 FROM \"OLINGO\".\"Team\" E0 ORDER BY E0.\"Name\" ASC", stmt
        .toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(4, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testOrderByClauseTwoElements() {
    final Root<?> team = q.from(Team.class);

    q.multiselect(team);
    q.orderBy(cb.asc(team.get("name")), cb.desc(team.get("iD")));
    ((SqlConvertible) q).asSQL(stmt);
    assertEquals(
        "SELECT E0.\"TeamKey\" S0, E0.\"Name\" S1 FROM \"OLINGO\".\"Team\" E0 ORDER BY E0.\"Name\" ASC, E0.\"TeamKey\" DESC",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(4, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testSimpleToLowerQuery() {
    final Root<?> adminDiv = q.from(AdministrativeDivisionDescription.class);
    final Expression<Boolean> equal = cb.equal(adminDiv.get("language"), "de");
    final Expression<Boolean> lower = cb.equal(cb.lower(adminDiv.get("name")), "brandenburg");

    q.multiselect(adminDiv.get("codeID"));
    q.where(cb.and(equal, lower));
    ((SqlConvertible) q).asSQL(stmt);
    assertEquals(
        "SELECT E0.\"CodeID\" S0 FROM \"OLINGO\".\"AdministrativeDivisionDescription\" E0 WHERE ((E0.\"LanguageISO\" = ?1) AND (LOWER(E0.\"Name\") = ?2))",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(1, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testSimpleSubstringQuery() {
    final Root<?> adminDiv = q.from(AdministrativeDivisionDescription.class);
    final Expression<Boolean> equal = cb.equal(adminDiv.get("language"), "de");
    final Expression<String> sub = cb.substring(adminDiv.get("name"), 1, 5);
    final Expression<Boolean> lower = cb.equal(cb.lower(sub), "north");

    q.multiselect(adminDiv.get("codeID"));
    q.where(cb.and(equal, lower));
    ((SqlConvertible) q).asSQL(stmt);
    assertEquals(
        "SELECT E0.\"CodeID\" S0 FROM \"OLINGO\".\"AdministrativeDivisionDescription\" E0 WHERE ((E0.\"LanguageISO\" = ?1) AND (LOWER(SUBSTRING(E0.\"Name\", ?2, ?3)) = ?4))",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(2, act.size());
    assertNotNull(act.get(0));
  }

  // SELECT "CodeID" FROM "OLINGO"."AdministrativeDivisionDescription" WHERE (("LanguageISO" = 'de') AND
  // (LOWER(SUBSTR("Name", 1, 5)) = 'north'))

  @Test
  void testSimpleLocateQuery() {
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<Integer> locate = cb.locate(adminDiv.get("divisionCode"), "3");

    q.multiselect(adminDiv.get("codeID"));
    q.where(cb.equal(locate, 4));
    ((SqlConvertible) q).asSQL(stmt);
    assertEquals(
        "SELECT E0.\"CodeID\" S0 FROM \"OLINGO\".\"AdministrativeDivision\" E0 WHERE (LOCATE(?1, E0.\"DivisionCode\") = ?2)",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(7, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testSimpleConcatQuery() {
    final Root<?> person = q.from(Person.class);
    final Expression<String> locate = cb.concat(cb.concat(person.get("lastName"), ","), person.get("firstName"));

    q.multiselect(person.get("iD"));
    q.where(cb.equal(locate, "Mustermann,Max"));
    ((SqlConvertible) q).asSQL(stmt);
    assertEquals(
        "SELECT E0.\"ID\" S0 FROM \"OLINGO\".\"BusinessPartner\" E0 WHERE ((CONCAT(CONCAT(E0.\"NameLine2\", ?1), E0.\"NameLine1\") = ?2) AND (E0.\"Type\" = ?3))",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(1, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testSimpleTimestampQuery() {
    final Root<?> person = q.from(Person.class);
    final Expression<Timestamp> locate = person.get("administrativeInformation").get("created").get("at");

    q.multiselect(person.get("iD"), person.get("creationDateTime"));
    q.where(cb.lessThan(locate, cb.currentTimestamp()));
    ((SqlConvertible) q).asSQL(stmt);
    assertEquals(
        "SELECT E0.\"ID\" S0, E0.\"CreatedAt\" S1 FROM \"OLINGO\".\"BusinessPartner\" E0 WHERE ((E0.\"CreatedAt\" < CURRENT_TIMESTAMP) AND (E0.\"Type\" = ?1))",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(3, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  void testSelectPrimitiveCollectionProperty() {
    final Root<?> org = q.from(Organization.class);
    final Join<Object, Object> comment = org.join("comment");
    final Path<Object> id = org.get("iD");
    id.alias("ID");
    comment.alias("Comment");
    q.multiselect(id, comment);
    q.where(cb.equal(id, '1'));
    // ((SqlConvertible) q).asSQL(stmt);
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(2, act.size());
  }

  @Test
  void testSelectComplexCollectionProperty() {
    final Root<?> org = q.from(Person.class);
    final Join<Object, Object> addr = org.join("inhouseAddress");
    final Path<Object> id = org.get("iD");
    id.alias("ID");
    addr.alias("inhouseAddress");
    q.multiselect(id, addr);
    q.where(cb.equal(id, "99"));
    // ((SqlConvertible) q).asSQL(stmt);
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(2, act.size());
    assertNotNull(act.get(0).get("inhouseAddress.Building"));
  }

  @Test
  void testSelectCountOneKey() {
    // SELECT COUNT(DISTINCT(*)) FROM "OLINGO"."BusinessPartnerProtected" E0 WHERE (E0."UserName" = ?)
    final CriteriaQuery<Long> qc = cb.createQuery(Long.class);
    final Root<?> org = qc.from(BusinessPartnerProtected.class);
    qc.multiselect(cb.countDistinct(org));
    qc.where(cb.equal(org.get("username"), "Willi"));
    final TypedQuery<Long> tq = em.createQuery(qc);
    final Long act = tq.getSingleResult();
    assertEquals(3L, act);
  }

  @Test
  void testSelectDateTime() {
    final Root<?> dateTime = q.from(DateTimeTest.class);
    final Path<Object> id = dateTime.get("iD");
    q.multiselect(dateTime);
    q.where(cb.equal(id, "99"));
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(1, act.size());
    assertEquals(LocalDate.parse("1999-04-01"), act.get(0).get("S2"));
    assertEquals(LocalDateTime.parse("2016-01-20T09:21:23"), act.get(0).get("S1"));
  }
}