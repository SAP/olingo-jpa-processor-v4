import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import org.apache.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import org.apache.olingo.jpa.processor.core.testmodel.Organization;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestCriteriaBuilder {
  protected static final String PUNIT_NAME = "org.apache.olingo.jpa";
  private static final String ENTITY_MANAGER_DATA_SOURCE = "javax.persistence.nonJtaDataSource";
  private static EntityManagerFactory emf;
  private EntityManager em;
  private CriteriaBuilder cb;

  @BeforeClass
  public static void setupClass() {
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(ENTITY_MANAGER_DATA_SOURCE, DataSourceHelper.createDataSource(
        DataSourceHelper.DB_H2));
    emf = Persistence.createEntityManagerFactory(PUNIT_NAME, properties);
  }

  @Before
  public void setup() {
    em = emf.createEntityManager();
    cb = em.getCriteriaBuilder();
  }

  @Test
  public void testSubSelect() {
    //https://stackoverflow.com/questions/29719321/combining-conditional-expressions-with-and-and-or-predicates-using-the-jpa-c
    CriteriaQuery<Tuple> adminQ1 = cb.createTupleQuery();
    Subquery<Long> adminQ2 = adminQ1.subquery(Long.class);
    Subquery<Long> adminQ3 = adminQ2.subquery(Long.class);
    Subquery<Long> org = adminQ3.subquery(Long.class);

    Root<AdministrativeDivision> adminRoot1 = adminQ1.from(AdministrativeDivision.class);
    Root<AdministrativeDivision> adminRoot2 = adminQ2.from(AdministrativeDivision.class);
    Root<AdministrativeDivision> adminRoot3 = adminQ3.from(AdministrativeDivision.class);
    Root<Organization> org1 = org.from(Organization.class);

    org.where(cb.and(cb.equal(org1.get("ID"), "3")), createParentOrg(org1, adminRoot3));
    org.select(cb.literal(1L));

    adminQ3.where(cb.and(createParentAdmin(adminRoot3, adminRoot2), cb.exists(org)));
    adminQ3.select(cb.literal(1L));

    adminQ2.where(cb.and(createParentAdmin(adminRoot2, adminRoot1), cb.exists(adminQ3)));
    adminQ2.select(cb.literal(1L));

    adminQ1.where(cb.exists(adminQ2));
    adminQ1.multiselect(adminRoot1.get("divisionCode"));

    TypedQuery<Tuple> tq = em.createQuery(adminQ1);
    tq.getResultList();
  }

  private Expression<Boolean> createParentAdmin(Root<AdministrativeDivision> subQuery,
      Root<AdministrativeDivision> query) {
    return cb.and(cb.equal(query.get("codePublisher"), subQuery.get("codePublisher")),
        cb.and(cb.equal(query.get("codeID"), subQuery.get("parentCodeID")),
            cb.equal(query.get("divisionCode"), subQuery.get("parentDivisionCode"))));
  }

  private Predicate createParentOrg(Root<Organization> org1, Root<AdministrativeDivision> adminRoot3) {
    return cb.and(cb.equal(adminRoot3.get("codePublisher"), org1.get("address").get("regionCodePublisher")),
        cb.and(cb.equal(adminRoot3.get("codeID"), org1.get("address").get("regionCodeID")),
            cb.equal(adminRoot3.get("divisionCode"), org1.get("address").get("region"))));
  }
}
