package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.api.SqlConvertable;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerProtected;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testmodel.Team;

public abstract class CriteriaBuilderOverallTest {
  protected CriteriaBuilder cb;
  protected EntityManager em;
  protected StringBuilder stmt;
  protected CriteriaQuery<Tuple> q;

  public void setup(final EntityManagerFactory emf, final JPAServiceDocument sd) {
    em = new EntityManagerWrapper(emf.createEntityManager(), sd);
    cb = em.getCriteriaBuilder();
    assertNotNull(cb);
    stmt = new StringBuilder();
    q = cb.createTupleQuery();
  }

  @Test
  public void testCriteriaBuilderImplReturnsSQLQuery() {
    assertTrue(q instanceof SqlConvertable);
  }

  @Test
  public void testSimpleQueryAll() {
    final Root<?> team = q.from(Team.class);

    q.multiselect(team);
    ((SqlConvertable) q).asSQL(stmt);
    assertEquals("SELECT E0.\"TeamKey\", E0.\"Name\" FROM \"OLINGO\".\"Team\" E0", stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(4, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  public void testWhereWithMultipleAnd() {
    // SELECT E0."CodeID" FROM "OLINGO"."AdministrativeDivision" E0 WHERE (((E0."DivisionCode" = ?) AND (E0."CodeID" =
    // ?)) AND (E0."CodePublisher" = ?))
    Root<?> adminDiv = q.from(AdministrativeDivision.class);

    q.multiselect(adminDiv.get("codeID"));
    Predicate[] restrictions = new Predicate[3];
    restrictions[0] = cb.equal(adminDiv.get("codeID"), "NUTS2");
    restrictions[1] = cb.equal(adminDiv.get("divisionCode"), "BE34");
    restrictions[2] = cb.equal(adminDiv.get("codePublisher"), "Eurostat");
    q.where(cb.and(restrictions));
    ((SqlConvertable) q).asSQL(stmt);
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertNotNull(tq);
    assertEquals(1, act.size());
  }

  @Test
  public void testSimpleLikeQueryAll() {
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);

    q.multiselect(adminDiv.get("codeID"));
    q.where(cb.like(adminDiv.get("codeID"), "%6-1"));
    ((SqlConvertable) q).asSQL(stmt);
    assertEquals("SELECT E0.\"CodeID\" FROM \"OLINGO\".\"AdministrativeDivision\" E0 WHERE (E0.\"CodeID\" LIKE ?1)",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(4, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  public void testSimpleLikeQueryAllWithExcape() {

    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<String> p = cb.literal("%6-1");
    final Expression<Character> e = cb.literal('/');

    q.multiselect(adminDiv.get("codeID"));
    q.where(cb.like(adminDiv.get("codeID"), p, e));
    ((SqlConvertable) q).asSQL(stmt);
    assertEquals(
        "SELECT E0.\"CodeID\" FROM \"OLINGO\".\"AdministrativeDivision\" E0 WHERE (E0.\"CodeID\" LIKE ?1 ESCAPE ?2)",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(4, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  public void testOrderByClause() {
    final Root<?> team = q.from(Team.class);

    q.multiselect(team);
    q.orderBy(cb.asc(team.get("name")));
    ((SqlConvertable) q).asSQL(stmt);
    assertEquals("SELECT E0.\"TeamKey\", E0.\"Name\" FROM \"OLINGO\".\"Team\" E0 ORDER BY E0.\"Name\" ASC", stmt
        .toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(4, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  public void testOrderByClauseTwoElements() {
    final Root<?> team = q.from(Team.class);

    q.multiselect(team);
    q.orderBy(cb.asc(team.get("name")), cb.desc(team.get("iD")));
    ((SqlConvertable) q).asSQL(stmt);
    assertEquals(
        "SELECT E0.\"TeamKey\", E0.\"Name\" FROM \"OLINGO\".\"Team\" E0 ORDER BY E0.\"Name\" ASC, E0.\"TeamKey\" DESC",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(4, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  public void testSimpleToLowerQuery() {
    final Root<?> adminDiv = q.from(AdministrativeDivisionDescription.class);
    final Expression<Boolean> equal = cb.equal(adminDiv.get("language"), "de");
    final Expression<Boolean> lower = cb.equal(cb.lower(adminDiv.get("name")), "brandenburg");

    q.multiselect(adminDiv.get("codeID"));
    q.where(cb.and(equal, lower));
    ((SqlConvertable) q).asSQL(stmt);
    assertEquals(
        "SELECT E0.\"CodeID\" FROM \"OLINGO\".\"AdministrativeDivisionDescription\" E0 WHERE ((E0.\"LanguageISO\" = ?1) AND (LOWER(E0.\"Name\") = ?2))",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(1, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  public void testSimpleSubstringQuery() {
    final Root<?> adminDiv = q.from(AdministrativeDivisionDescription.class);
    final Expression<Boolean> equal = cb.equal(adminDiv.get("language"), "de");
    final Expression<String> sub = cb.substring(adminDiv.get("name"), 1, 5);
    final Expression<Boolean> lower = cb.equal(cb.lower(sub), "north");

    q.multiselect(adminDiv.get("codeID"));
    q.where(cb.and(equal, lower));
    ((SqlConvertable) q).asSQL(stmt);
    assertEquals(
        "SELECT E0.\"CodeID\" FROM \"OLINGO\".\"AdministrativeDivisionDescription\" E0 WHERE ((E0.\"LanguageISO\" = ?1) AND (LOWER(SUBSTRING(E0.\"Name\", ?2, ?3)) = ?4))",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(2, act.size());
    assertNotNull(act.get(0));
  }

  // SELECT "CodeID" FROM "OLINGO"."AdministrativeDivisionDescription" WHERE (("LanguageISO" = 'de') AND
  // (LOWER(SUBSTR("Name", 1, 5)) = 'north'))

  @Test
  public void testSimpleLocateQuery() {
    final Root<?> adminDiv = q.from(AdministrativeDivision.class);
    final Expression<Integer> locate = cb.locate(adminDiv.get("divisionCode"), "3");

    q.multiselect(adminDiv.get("codeID"));
    q.where(cb.equal(locate, 4));
    ((SqlConvertable) q).asSQL(stmt);
    assertEquals(
        "SELECT E0.\"CodeID\" FROM \"OLINGO\".\"AdministrativeDivision\" E0 WHERE (LOCATE(?1, E0.\"DivisionCode\") = ?2)",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(7, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  public void testSimpleConcatQuery() {
    final Root<?> person = q.from(Person.class);
    final Expression<String> locate = cb.concat(cb.concat(person.get("lastName"), ","), person.get("firstName"));

    q.multiselect(person.get("iD"));
    q.where(cb.equal(locate, "Mustermann,Max"));
    ((SqlConvertable) q).asSQL(stmt);
    assertEquals(
        "SELECT E0.\"ID\" FROM \"OLINGO\".\"BusinessPartner\" E0 WHERE ((CONCAT(CONCAT(E0.\"NameLine2\", ?1), E0.\"NameLine1\") = ?2) AND (E0.\"Type\" = ?3))",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(1, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  public void testSimpleTimestampQuery() {
    final Root<?> person = q.from(Person.class);
    final Expression<Timestamp> locate = person.get("administrativeInformation").get("created").get("at");

    q.multiselect(person.get("iD"));
    q.where(cb.lessThan(locate, cb.currentTimestamp()));
    ((SqlConvertable) q).asSQL(stmt);
    assertEquals(
        "SELECT E0.\"ID\" FROM \"OLINGO\".\"BusinessPartner\" E0 WHERE ((E0.\"CreatedAt\" < CURRENT_TIMESTAMP) AND (E0.\"Type\" = ?1))",
        stmt.toString().trim());
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(3, act.size());
    assertNotNull(act.get(0));
  }

  @Test
  public void testSelectPrimitiveCollectionProperty() {
    final Root<?> org = q.from(Organization.class);
    final Join<Object, Object> comment = org.join("comment");
    final Path<Object> id = org.get("iD");
    id.alias("ID");
    comment.alias("Comment");
    q.multiselect(id, comment);
    q.where(cb.equal(id, '1'));
    // ((SqlConvertable) q).asSQL(stmt);
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(2, act.size());
  }

  @Test
  public void testSelectComplexCollectionProperty() {
    final Root<?> org = q.from(Person.class);
    final Join<Object, Object> addr = org.join("inhouseAddress");
    final Path<Object> id = org.get("iD");
    id.alias("ID");
    addr.alias("inhouseAddress");
    q.multiselect(id, addr);
    q.where(cb.equal(id, "99"));
    // ((SqlConvertable) q).asSQL(stmt);
    final TypedQuery<Tuple> tq = em.createQuery(q);
    final List<Tuple> act = tq.getResultList();
    assertEquals(2, act.size());
    assertNotNull(act.get(0).get("inhouseAddress.Building"));
  }

  @Test
  public void testSelectCountOneKey() {
    // SELECT COUNT(DISTINCT(*)) FROM "OLINGO"."BusinessPartnerProtected" E0 WHERE (E0."UserName" = ?)
    final CriteriaQuery<Long> qc = cb.createQuery(Long.class);
    final Root<?> org = qc.from(BusinessPartnerProtected.class);
    qc.multiselect(cb.countDistinct(org));
    qc.where(cb.equal(org.get("username"), "Willi"));
    final TypedQuery<Long> tq = em.createQuery(qc);
    final Long act = tq.getSingleResult();
    assertEquals(3L, act);
  }
}