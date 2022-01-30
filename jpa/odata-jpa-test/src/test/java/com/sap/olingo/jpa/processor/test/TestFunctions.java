package com.sap.olingo.jpa.processor.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.ParameterMode;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;

class TestFunctions {
  protected static final String PUNIT_NAME = "com.sap.olingo.jpa";
  private static final String ENTITY_MANAGER_DATA_SOURCE = "javax.persistence.nonJtaDataSource";
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
  void setup() {
    em = emf.createEntityManager();
    cb = em.getCriteriaBuilder();
  }

  @Disabled("Not implemented")
  @Test
  void TestProcedure() throws SQLException {
    final StoredProcedureQuery pc = em.createStoredProcedureQuery("\"OLINGO\".\"org.apache.olingo.jpa::Siblings\"");

    pc.registerStoredProcedureParameter("CodePublisher", String.class, ParameterMode.IN);
    pc.setParameter("CodePublisher", "Eurostat");
    pc.registerStoredProcedureParameter("CodeID", String.class, ParameterMode.IN);
    pc.setParameter("CodeID", "NUTS2");
    pc.registerStoredProcedureParameter("DivisionCode", String.class, ParameterMode.IN);
    pc.setParameter("DivisionCode", "BE25");
//    pc.setParameter("CodePublisher", "Eurostat");
//    pc.setParameter("CodeID", "NUTS2");
//    pc.setParameter("DivisionCode", "BE25");

    final Connection conn = ds.getConnection();
    final DatabaseMetaData meta = conn.getMetaData();
    final ResultSet metaR = meta.getProcedures(conn.getCatalog(), "OLINGO", "%");

    while (metaR.next()) {
      final String procedureCatalog = metaR.getString(1);
      final String procedureSchema = metaR.getString(2);
      final String procedureName = metaR.getString(3);
//          reserved for future use
//          reserved for future use
//          reserved for future use
      final String remarks = metaR.getString(7);
      final Short procedureTYpe = metaR.getShort(8);
//      String specificName = metaR.getString(9);

      System.out.println("procedureCatalog=" + procedureCatalog);
      System.out.println("procedureSchema=" + procedureSchema);
      System.out.println("procedureName=" + procedureName);
      System.out.println("remarks=" + remarks);
      System.out.println("procedureType=" + procedureTYpe);
//      System.out.println("specificName=" + specificName);
    }
    final ResultSet rs = meta.getProcedureColumns(conn.getCatalog(),
        "OLINGO", "%", "%");

    while (rs.next()) {
      // get stored procedure metadata
      final String procedureCatalog = rs.getString(1);
      final String procedureSchema = rs.getString(2);
      final String procedureName = rs.getString(3);
      final String columnName = rs.getString(4);
      final short columnReturn = rs.getShort(5);
      final int columnDataType = rs.getInt(6);
      final String columnReturnTypeName = rs.getString(7);
      final int columnPrecision = rs.getInt(8);
      final int columnByteLength = rs.getInt(9);
      final short columnScale = rs.getShort(10);
      final short columnRadix = rs.getShort(11);
      final short columnNullable = rs.getShort(12);
      final String columnRemarks = rs.getString(13);

      System.out.println("stored Procedure name=" + procedureName);
      System.out.println("procedureCatalog=" + procedureCatalog);
      System.out.println("procedureSchema=" + procedureSchema);
      System.out.println("procedureName=" + procedureName);
      System.out.println("columnName=" + columnName);
      System.out.println("columnReturn=" + columnReturn);
      System.out.println("columnDataType=" + columnDataType);
      System.out.println("columnReturnTypeName=" + columnReturnTypeName);
      System.out.println("columnPrecision=" + columnPrecision);
      System.out.println("columnByteLength=" + columnByteLength);
      System.out.println("columnScale=" + columnScale);
      System.out.println("columnRadix=" + columnRadix);
      System.out.println("columnNullable=" + columnNullable);
      System.out.println("columnRemarks=" + columnRemarks);
    }
    conn.close();
    pc.execute();
    final List<?> r = pc.getResultList();

    final Object[] one = (Object[]) r.get(0);
    assertNotNull(one);
  }

  @Disabled("Not implemented")
  @Test
  void TestScalarFunctionsWhere() {
    CreateUDFDerby();

    final CriteriaQuery<Tuple> count = cb.createTupleQuery();
    final Root<?> adminDiv = count.from(AdministrativeDivision.class);
    count.multiselect(adminDiv);
    count.where(cb.equal(
        cb.function("IS_PRIME", boolean.class, cb.literal(5)),
        Boolean.TRUE));
    // cb.literal
    final TypedQuery<Tuple> tq = em.createQuery(count);
    final List<Tuple> act = tq.getResultList();
    assertNotNull(act);
    tq.getFirstResult();
  }

  private void CreateUDFDerby() {
    final EntityTransaction t = em.getTransaction();

    final StringBuffer dropString = new StringBuffer("DROP FUNCTION IS_PRIME");

    final StringBuffer sqlString = new StringBuffer();

    sqlString.append("CREATE FUNCTION IS_PRIME(number Integer) RETURNS Integer ");
    sqlString.append("PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA ");
    sqlString.append("EXTERNAL NAME 'com.sap.olingo.jpa.processor.core.test_udf.isPrime'");

    t.begin();
    final Query d = em.createNativeQuery(dropString.toString());
    final Query q = em.createNativeQuery(sqlString.toString());
    d.executeUpdate();
    q.executeUpdate();
    t.commit();
  }
}
