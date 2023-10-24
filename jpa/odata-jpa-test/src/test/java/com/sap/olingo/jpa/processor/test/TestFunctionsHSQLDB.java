package com.sap.olingo.jpa.processor.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;

class TestFunctionsHSQLDB {
  protected static final String PUNIT_NAME = "com.sap.olingo.jpa";
  private static final String ENTITY_MANAGER_DATA_SOURCE = "jakarta.persistence.nonJtaDataSource";
  private static EntityManagerFactory emf;
  private static DataSource ds;

  @BeforeAll
  public static void setupClass() {

    final Map<String, Object> properties = new HashMap<>();

    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);

    properties.put(ENTITY_MANAGER_DATA_SOURCE, ds);
    emf = Persistence.createEntityManagerFactory(PUNIT_NAME, properties);
  }

  private EntityManager em;

  private CriteriaBuilder cb;

  @BeforeEach
  public void setup() {
    em = emf.createEntityManager();
    cb = em.getCriteriaBuilder();
  }

  @Test
  void TestScalarFunctionsWhere() {
    CreateUDFHSQLDB();

    final CriteriaQuery<Tuple> count = cb.createTupleQuery();
    final Root<?> adminDiv = count.from(AdministrativeDivision.class);
    count.multiselect(adminDiv);

    count.where(cb.and(cb.greaterThan(
        //
        cb.function("PopulationDensity", Integer.class, adminDiv.get("area"), adminDiv.get("population")),
        60)), cb.equal(adminDiv.get("countryCode"), cb.literal("BEL")));
    // cb.literal
    final TypedQuery<Tuple> tq = em.createQuery(count);
    final List<Tuple> act = tq.getResultList();
    assertNotNull(act);
    tq.getFirstResult();
  }

  private void CreateUDFHSQLDB() {
    final EntityTransaction t = em.getTransaction();

    // StringBuffer dropString = new StringBuffer("DROP FUNCTION PopulationDensity");

    final StringBuffer sqlString = new StringBuffer();

    sqlString.append("CREATE FUNCTION  PopulationDensity (area INT, population BIGINT ) ");
    sqlString.append("RETURNS INT ");
    sqlString.append("IF area <= 0 THEN RETURN 0;");
    sqlString.append("ELSE RETURN population / area; ");
    sqlString.append("END IF");

    t.begin();
    // Query d = em.createNativeQuery(dropString.toString());
    final Query q = em.createNativeQuery(sqlString.toString());
    // d.executeUpdate();
    q.executeUpdate();
    t.commit();
  }
}
