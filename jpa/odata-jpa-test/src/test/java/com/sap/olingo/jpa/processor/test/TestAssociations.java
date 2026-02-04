package com.sap.olingo.jpa.processor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.Country;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;

class TestAssociations {
  protected static final String PUNIT_NAME = "com.sap.olingo.jpa";
  private static final String ENTITY_MANAGER_DATA_SOURCE = "jakarta.persistence.nonJtaDataSource";
  private static EntityManagerFactory emf;
  private EntityManager em;
  private CriteriaBuilder cb;

  @BeforeAll
  static void setupClass() {
    final Map<String, Object> properties = new HashMap<>();
    properties.put(ENTITY_MANAGER_DATA_SOURCE, DataSourceHelper.createDataSource(
        DataSourceHelper.DB_HSQLDB));
    emf = Persistence.createEntityManagerFactory(PUNIT_NAME, properties);
  }

  @BeforeEach
  void setup() {
    em = emf.createEntityManager();
    cb = em.getCriteriaBuilder();
  }

  @Test
  void getBuPaRoles() {
    final CriteriaQuery<Tuple> criteriaQuery = cb.createTupleQuery();
    final Root<BusinessPartner> root = criteriaQuery.from(BusinessPartner.class);

    criteriaQuery.multiselect(root.get("roles").alias("roles"));
    final TypedQuery<Tuple> typedQuery = em.createQuery(criteriaQuery);
    final List<Tuple> result = typedQuery.getResultList();
    final BusinessPartnerRole role = (BusinessPartnerRole) result.get(0).get("roles");
    assertNotNull(role);
  }

  @Test
  void getBuPaLocation() {
    final CriteriaQuery<Tuple> criteriaQuery = cb.createTupleQuery();
    final Root<BusinessPartner> root = criteriaQuery.from(BusinessPartner.class);

    criteriaQuery.multiselect(root.get("locationName").alias("L"));
    final TypedQuery<Tuple> typedQuery = em.createQuery(criteriaQuery);
    final List<Tuple> result = typedQuery.getResultList();
    final AdministrativeDivisionDescription act = (AdministrativeDivisionDescription) result.get(0).get("L");
    assertNotNull(act);
  }

  @Test
  void getRoleBuPa() {
    final CriteriaQuery<Tuple> criteriaQuery = cb.createTupleQuery();
    final Root<BusinessPartnerRole> root = criteriaQuery.from(BusinessPartnerRole.class);

    criteriaQuery.multiselect(root.get("businessPartner").alias("BuPa"));
    final TypedQuery<Tuple> typedQuery = em.createQuery(criteriaQuery);
    final List<Tuple> result = typedQuery.getResultList();
    final BusinessPartner bp = (BusinessPartner) result.get(0).get("BuPa");
    assertNotNull(bp);
  }

  @Test
  void getBuPaCountryName() {
    final CriteriaQuery<Tuple> criteriaQuery = cb.createTupleQuery();
    final Root<BusinessPartner> root = criteriaQuery.from(BusinessPartner.class);
    // Works with eclipselink, but not with openjap
    criteriaQuery.multiselect(root.get("address").get("countryName").alias("CN"));
    final TypedQuery<Tuple> typedQuery = em.createQuery(criteriaQuery);
    final List<Tuple> result = typedQuery.getResultList();
    final Country region = (Country) result.get(0).get("CN");
    assertNotNull(region);
  }

  @Test
  void getBuPaRegionName() {
    final CriteriaQuery<Tuple> criteriaQuery = cb.createTupleQuery();
    final Root<BusinessPartner> root = criteriaQuery.from(BusinessPartner.class);

    criteriaQuery.multiselect(root.get("address").get("regionName").alias("RN"));
    final TypedQuery<Tuple> typedQuery = em.createQuery(criteriaQuery);
    final List<Tuple> result = typedQuery.getResultList();
    final AdministrativeDivisionDescription region = (AdministrativeDivisionDescription) result.get(0).get("RN");
    assertNotNull(region);
  }

  @Test
  void getAdministrativeDivisionParent() {
    final CriteriaQuery<Tuple> criteriaQuery = cb.createTupleQuery();
    final Root<AdministrativeDivision> root = criteriaQuery.from(AdministrativeDivision.class);

    criteriaQuery.multiselect(root.get("parent").alias("P"));
    final TypedQuery<Tuple> typedQuery = em.createQuery(criteriaQuery);
    final List<Tuple> result = typedQuery.getResultList();
    final AdministrativeDivision act = (AdministrativeDivision) result.get(0).get("P");
    assertNotNull(act);
  }

  @Test
  void getAdministrativeDivisionOneParent() {
    final CriteriaQuery<Tuple> criteriaQuery = cb.createTupleQuery();
    final Root<AdministrativeDivision> root = criteriaQuery.from(AdministrativeDivision.class);
    root.alias("Source");
    criteriaQuery.multiselect(root.get("parent").alias("P"));
    // criteriaQuery.select((Selection<? extends Tuple>) root);
    criteriaQuery.where(cb.and(
        cb.equal(root.get("codePublisher"), "Eurostat"),
        cb.and(
            cb.equal(root.get("codeID"), "NUTS3"),
            cb.equal(root.get("divisionCode"), "BE251"))));
    final TypedQuery<Tuple> typedQuery = em.createQuery(criteriaQuery);
    final List<Tuple> result = typedQuery.getResultList();
    final AdministrativeDivision act = (AdministrativeDivision) result.get(0).get("P");
    assertNotNull(act);
    assertEquals("NUTS2", act.getCodeID());
    assertEquals("BE25", act.getDivisionCode());
  }

  @Test
  void getAdministrativeDivisionChildrenOfOneParent() {
    final CriteriaQuery<Tuple> criteriaQuery = cb.createTupleQuery();
    final Root<AdministrativeDivision> root = criteriaQuery.from(AdministrativeDivision.class);
    root.alias("Source");
    criteriaQuery.multiselect(root.get("children").alias("C"));
    criteriaQuery.where(cb.and(
        cb.equal(root.get("codePublisher"), "Eurostat"),
        cb.and(
            cb.equal(root.get("codeID"), "NUTS2"),
            cb.equal(root.get("divisionCode"), "BE25"))));
    criteriaQuery.orderBy(cb.desc(root.get("divisionCode")));
    final TypedQuery<Tuple> typedQuery = em.createQuery(criteriaQuery);
    final List<Tuple> result = typedQuery.getResultList();
    final AdministrativeDivision act = (AdministrativeDivision) result.get(0).get("C");
    assertNotNull(act);
    assertEquals(8, result.size());
    assertEquals("NUTS3", act.getCodeID());
    assertEquals("BE251", act.getDivisionCode());
  }
}
