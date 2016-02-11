import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.ParameterMode;
import javax.persistence.Persistence;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.apache.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import org.apache.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestFunctions {
  protected static final String PUNIT_NAME = "org.apache.olingo.jpa";
  private static final String ENTITY_MANAGER_DATA_SOURCE = "javax.persistence.nonJtaDataSource";
  private static EntityManagerFactory emf;
  private EntityManager em;
  private CriteriaBuilder cb;

  @BeforeClass
  public static void setupClass() {
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(ENTITY_MANAGER_DATA_SOURCE, DataSourceHelper.createDataSource(
        DataSourceHelper.DB_HANA));
    emf = Persistence.createEntityManagerFactory(PUNIT_NAME, properties);
  }

  @Before
  public void setup() {
    em = emf.createEntityManager();
    cb = em.getCriteriaBuilder();
  }

  @Test
  public void TestScalarFunctionsWhere() {
    CriteriaQuery<Tuple> count = cb.createTupleQuery();
    Root<?> adminDiv = count.from(AdministrativeDivision.class);
    count.multiselect(adminDiv);
    count.where(cb.equal(
        cb.function("\"GRANDEO\".\"tmp.d023143.mri::IsPrime\"", Integer.class, cb.literal(5)),
        new Integer(1)));
    // cb.literal
    TypedQuery<Tuple> tq = em.createQuery(count);
    List<Tuple> act = tq.getResultList();
    tq.getFirstResult();

    Path<?> p;
  }

  @Test
  public void TestProcedure() {
    StoredProcedureQuery pc = em.createStoredProcedureQuery("\"OLINGO\".\"org.apache.olingo.jpa::Siblings\"");
    pc.registerStoredProcedureParameter("CodePublisher", String.class, ParameterMode.IN);
    pc.registerStoredProcedureParameter("CodeID", String.class, ParameterMode.IN);
    pc.registerStoredProcedureParameter("DivisionCode", String.class, ParameterMode.IN);
    // pc.registerStoredProcedureParameter("?", ArrayList.class, ParameterMode.OUT);
    pc.setParameter("CodePublisher", "Eurostat"); // nvarchar(10), IN nvarchar(10), IN nvarchar(10))
    pc.setParameter("CodeID", "NUTS2");
    pc.setParameter("DivisionCode", "BE25");
    pc.execute();
    List<?> r = pc.getResultList();
    Object[] one = (Object[]) r.get(0);
    assertNotNull(one);
  }
}
