package com.sap.olingo.jpa.processor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescriptionKey;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import com.sap.olingo.jpa.processor.core.testmodel.Membership;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.testmodel.Team;

class TestCriteriaBuilder {
  protected static final String PUNIT_NAME = "com.sap.olingo.jpa";
  private static final String ENTITY_MANAGER_DATA_SOURCE = "javax.persistence.nonJtaDataSource";
  private static EntityManagerFactory emf;
  private EntityManager em;
  private CriteriaBuilder cb;

  @BeforeAll
  public static void setupClass() {
    final Map<String, Object> properties = new HashMap<>();
    properties.put(ENTITY_MANAGER_DATA_SOURCE, DataSourceHelper.createDataSource(
        DataSourceHelper.DB_HSQLDB));
    emf = Persistence.createEntityManagerFactory(PUNIT_NAME, properties);
  }

  @BeforeEach
  void setup() {
    em = emf.createEntityManager();
    assertNotNull(em);
    cb = em.getCriteriaBuilder();
    assertNotNull(cb);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testSubstringWithExpression() {
    final CriteriaQuery<Tuple> adminQ = cb.createTupleQuery();
    final Root<AdministrativeDivisionDescription> adminRoot1 = adminQ.from(AdministrativeDivisionDescription.class);
    final Path<?> p = adminRoot1.get("name");

    final Expression<Integer> sum = cb.sum(cb.literal(1), cb.literal(4));

    adminQ.where(cb.equal(cb.substring((Expression<String>) (p), cb.literal(1), sum), "North"));
    adminQ.multiselect(adminRoot1.get("name"));
    final TypedQuery<Tuple> tq = em.createQuery(adminQ);
    assertFalse(tq.getResultList().isEmpty());
  }

  @Disabled("To time consuming")
  @Test
  void testSubSelect() {
    // https://stackoverflow.com/questions/29719321/combining-conditional-expressions-with-and-and-or-predicates-using-the-jpa-c
    final CriteriaQuery<Tuple> adminQ1 = cb.createTupleQuery();
    final Subquery<Long> adminQ2 = adminQ1.subquery(Long.class);
    final Subquery<Long> adminQ3 = adminQ2.subquery(Long.class);
    final Subquery<Long> org = adminQ3.subquery(Long.class);

    final Root<AdministrativeDivision> adminRoot1 = adminQ1.from(AdministrativeDivision.class);
    final Root<AdministrativeDivision> adminRoot2 = adminQ2.from(AdministrativeDivision.class);
    final Root<AdministrativeDivision> adminRoot3 = adminQ3.from(AdministrativeDivision.class);
    final Root<Organization> org1 = org.from(Organization.class);

    org.where(cb.and(cb.equal(org1.get("iD"), "3")), createParentOrg(org1, adminRoot3));
    org.select(cb.literal(1L));

    adminQ3.where(cb.and(createParentAdmin(adminRoot3, adminRoot2), cb.exists(org)));
    adminQ3.select(cb.literal(1L));

    adminQ2.where(cb.and(createParentAdmin(adminRoot2, adminRoot1), cb.exists(adminQ3)));
    adminQ2.select(cb.literal(1L));

    adminQ1.where(cb.exists(adminQ2));
    adminQ1.multiselect(adminRoot1.get("divisionCode"));

    final TypedQuery<Tuple> tq = em.createQuery(adminQ1);
    assertNotNull(tq.getResultList());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testSubSelectTopOrderBy() {
    // https://stackoverflow.com/questions/9321916/jpa-criteriabuilder-how-to-use-in-comparison-operator
    // https://stackoverflow.com/questions/24109412/in-clause-with-a-composite-primary-key-in-jpa-criteria#24265131
    final CriteriaQuery<Tuple> roleQ = cb.createTupleQuery();
    final Root<BusinessPartnerRole> roleRoot = roleQ.from(BusinessPartnerRole.class);

    final Subquery<BusinessPartner> bupaQ = roleQ.subquery(BusinessPartner.class);
    @SuppressWarnings("rawtypes")
    final Root bupaRoot = roleQ.from(BusinessPartner.class);

    bupaQ.select(bupaRoot.get("iD"));
//    Expression<String> exp = scheduleRequest.get("createdBy");
//    Predicate predicate = exp.in(myList);
//    criteria.where(predicate);

    final List<String> ids = new ArrayList<>();
    ids.add("1");
    ids.add("2");
    bupaQ.where(bupaRoot.get("iD").in(ids));
//    bupaQ.select(
//        (Expression<BusinessPartner>) cb.construct(
//            BusinessPartner.class,
//            bupaRoot.get("ID")));

    // roleQ.where(cb.in(roleRoot.get("businessPartnerID")).value(bupaQ));
    roleQ.where(cb.in(roleRoot.get("businessPartnerID")).value(bupaQ));
    roleQ.multiselect(roleRoot.get("businessPartnerID"));
    final TypedQuery<Tuple> tq = em.createQuery(roleQ);
    assertNotNull(tq.getResultList());
  }

  @Test
  void testFilterOnPrimitiveCollectionAttribute() {
    final CriteriaQuery<Tuple> orgQ = cb.createTupleQuery();
    final Root<Organization> orgRoot = orgQ.from(Organization.class);
    orgQ.select(orgRoot.get("iD"));
    orgQ.where(cb.like(orgRoot.get("comment"), "%just%"));
    final TypedQuery<Tuple> tq = em.createQuery(orgQ);
    final List<Tuple> act = tq.getResultList();
    assertEquals(1, act.size());
  }

  @Test
  void testFilterOnEmbeddedCollectionAttribute() {
    final CriteriaQuery<Tuple> pQ = cb.createTupleQuery();
    final Root<Person> pRoot = pQ.from(Person.class);
    pQ.select(pRoot.get("iD"));
    pQ.where(cb.equal(pRoot.get("inhouseAddress").get("taskID"), "MAIN"));
    final TypedQuery<Tuple> tq = em.createQuery(pQ);
    final List<Tuple> act = tq.getResultList();
    assertEquals(1, act.size());
  }

  @Test
  void testExpandCount() {
    final CriteriaQuery<Tuple> count = cb.createTupleQuery();
    final Root<?> roles = count.from(BusinessPartnerRole.class);

    count.multiselect(roles.get("businessPartnerID").alias("S0"), cb.count(roles).alias("$count"));
    count.groupBy(roles.get("businessPartnerID"));
    count.orderBy(cb.desc(cb.count(roles)));
    final TypedQuery<Tuple> tq = em.createQuery(count);
    tq.getResultList();
    assertEquals(0, tq.getFirstResult());
  }

  @Test
  void testAnd() {
    final CriteriaQuery<Tuple> count = cb.createTupleQuery();
    final Root<?> adminDiv = count.from(AdministrativeDivision.class);

    count.multiselect(adminDiv);
    final Predicate[] restrictions = new Predicate[3];
    restrictions[0] = cb.equal(adminDiv.get("codeID"), "NUTS2");
    restrictions[1] = cb.equal(adminDiv.get("divisionCode"), "BE34");
    restrictions[2] = cb.equal(adminDiv.get("codePublisher"), "Eurostat");
    count.where(cb.and(restrictions));
    final TypedQuery<Tuple> tq = em.createQuery(count);
    assertNotNull(tq.getResultList());
  }

  @Disabled("To be checked")
  @Test
  void testSearchEmbeddedId() {
    final CriteriaQuery<Tuple> cq = cb.createTupleQuery();
    final Root<?> adminDiv = cq.from(AdministrativeDivisionDescription.class);
    cq.multiselect(adminDiv);

    final Subquery<AdministrativeDivisionDescriptionKey> sq = cq.subquery(AdministrativeDivisionDescriptionKey.class);
    final Root<AdministrativeDivisionDescription> text = sq.from(AdministrativeDivisionDescription.class);
    sq.where(cb.function("CONTAINS", Boolean.class, text.get("name"), cb.literal("luettich")));
    final Expression<AdministrativeDivisionDescriptionKey> exp = text.get("key");
    sq.select(exp);

    cq.where(cb.and(cb.equal(adminDiv.get("key").get("codeID"), "NUTS2"),
        cb.in(sq).value(sq)));
    final TypedQuery<Tuple> tq = em.createQuery(cq);
    final List<Tuple> act = tq.getResultList();
    System.out.println(act.size());
    assertNotNull(act);
  }

  @Disabled("To be checked")
  @Test
  void testSearchNoSubquery() {
    final CriteriaQuery<Tuple> cq = cb.createTupleQuery();
    final Root<?> adminDiv = cq.from(AdministrativeDivisionDescription.class);
    cq.multiselect(adminDiv);

    // Predicate[] restrictions = new Predicate[2];
    cq.where(
        cb.and(cb.equal(cb.conjunction(),
            cb.function("CONTAINS", Boolean.class, adminDiv.get("name"), cb.literal("luettich"))),
            cb.equal(adminDiv.get("key").get("codeID"), "NUTS2")));

    final TypedQuery<Tuple> tq = em.createQuery(cq);
    final List<Tuple> act = tq.getResultList();
    System.out.println(act.size());
    assertNotNull(act);
  }

  @Test
  void testInClauseSimpleKey() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
      NoSuchMethodException, SecurityException {

    final CriteriaQuery<Tuple> cq = cb.createTupleQuery();
    final Root<?> bupa = cq.from(BusinessPartner.class);
    cq.select(bupa.get("iD"));

    cq.where(cb.in(bupa.get("iD")).value("3"));
    // (bupa.get("iD").in(Arrays.asList("3")));

    final TypedQuery<Tuple> tq = em.createQuery(cq);
    Object dq;
    String sqlMethod;
    if ("org.eclipse.persistence.internal.jpa.EJBQueryImpl".equals(tq.getClass().getCanonicalName())) {
      dq = tq.getClass().getMethod("getDatabaseQuery").invoke(tq);
      sqlMethod = "getSQLString";
    } else {
      dq = tq;
      sqlMethod = "toString";
    }
    System.out.println(dq.getClass().getMethod(sqlMethod).invoke(dq));
    final List<Tuple> act = tq.getResultList();
    System.out.println(dq.getClass().getMethod(sqlMethod).invoke(dq));
    Assertions.assertEquals(1, act.size());
  }

  @Test
  void testEntityTransaction() {
    Assertions.assertFalse(em.getTransaction().isActive());
    em.getTransaction().begin();
    Assertions.assertTrue(em.getTransaction().isActive());
  }

  @Test
  void testInClauseComplexKey() {

    final CriteriaQuery<Tuple> cq = cb.createTupleQuery();
    final Root<?> adminDiv = cq.from(AdministrativeDivisionDescription.class);
    final AdministrativeDivisionDescriptionKey key = new AdministrativeDivisionDescriptionKey();
    cq.multiselect(adminDiv);

    key.setCodeID("3166-1");
    key.setCodePublisher("ISO");
    key.setDivisionCode("DEU");
    key.setLanguage("de");
    // Create IN step by step
    final In<Object> in = cb.in(adminDiv.get("key"));
    in.value(key);
    cq.where(in);
    // Execute query
    final TypedQuery<Tuple> tq = em.createQuery(cq);
    final List<Tuple> act = tq.getResultList();
    if ("org.apache.openjpa.persistence.criteria.CriteriaBuilderImpl".equals(cb.getClass().getCanonicalName()))
      assertEquals(1, act.size());
    else
      // Ensure EclipseLink problem still exists: ("WHERE ((NULL, NULL, NULL, NULL) IN "));
      assertEquals(0, act.size());
  }

  @Test
  void testManyToMany() {
    final CriteriaQuery<Tuple> cq = cb.createTupleQuery();
    final Root<Person> root = cq.from(Person.class);
    final Join<Person, Team> join = root.join("teams");
    cq.multiselect(root.get("iD"), join.get("iD"));

    final TypedQuery<Tuple> tq = em.createQuery(cq);
    final List<Tuple> act = tq.getResultList();
    assertEquals(5, act.size());
  }

  @Test
  void testManyToManySubquery() {
    final CriteriaQuery<Tuple> cq = cb.createTupleQuery();
    final Root<Team> root = cq.from(Team.class);

    final Subquery<String> subquery = cq.subquery(String.class);
    final Root<Person> subRoot = subquery.from(Person.class);
    subquery.select(subRoot.get("iD"));
    final Root<Membership> subJoin = subquery.from(Membership.class);
    subquery.where(
        cb.and(
            cb.equal(subRoot.get("country"), "DEU"),
            cb.and(
                cb.equal(subRoot.get("iD"), subJoin.get("personID")),
                cb.equal(root.get("iD"), subJoin.get("teamID")))));

    cq.where(cb.exists(subquery));
    cq.multiselect(root.get("iD"));

    final TypedQuery<Tuple> tq = em.createQuery(cq);
    final List<Tuple> act = tq.getResultList();
    assertEquals(2, act.size());
  }

  private Expression<Boolean> createParentAdmin(final Root<AdministrativeDivision> subQuery,
      final Root<AdministrativeDivision> query) {
    return cb.and(
        cb.equal(query.get("codePublisher"), subQuery.get("codePublisher")),
        cb.and(
            cb.equal(query.get("codeID"), subQuery.get("parentCodeID")),
            cb.equal(query.get("divisionCode"), subQuery.get("parentDivisionCode"))));
  }

  private Predicate createParentOrg(final Root<Organization> org1, final Root<AdministrativeDivision> adminRoot3) {
    return cb.and(
        cb.equal(adminRoot3.get("codePublisher"), org1.get("address").get("regionCodePublisher")),
        cb.and(
            cb.equal(adminRoot3.get("codeID"), org1.get("address").get("regionCodeID")),
            cb.equal(adminRoot3.get("divisionCode"), org1.get("address").get("region"))));
  }
}