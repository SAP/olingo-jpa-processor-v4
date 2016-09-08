package org.apache.olingo.jpa.processor.core.filter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;

import org.apache.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import org.apache.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.junit.Before;
import org.junit.Test;

public class TestJPAFunctionOperator {
  private CriteriaBuilder cb;
  private JPAFunctionOperator cut;
  private JPAEntityType jpaET;
  private Member member;
  private Root<?> root;
  private UriResourceFunction uriFunction;

  @Before
  public void setUp() throws Exception {
    String PUNIT_NAME = "org.apache.olingo.jpa";
    EntityManagerFactory emf;
    DataSource ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);

    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, ds);
    EntityManager e = emf.createEntityManager();
    cb = e.getCriteriaBuilder();

    jpaET = mock(JPAEntityType.class);
    member = mock(Member.class);
    root = cb.createTupleQuery().from(AdministrativeDivision.class);
    UriInfoResource info = mock(UriInfoResource.class);
    uriFunction = mock(UriResourceFunction.class);

    List<UriResource> resources = new ArrayList<UriResource>();
    resources.add(uriFunction);

    // cut = new JPAFunctionOperator(jpaET, root, member, cb);
  }

  @Test
  public void testReturnsExpression() throws ODataApplicationException {

    Expression<?> act = cut.get();
    assertNotNull(act);
  }

  @Test
  public void testAbortsOnNoFunction() {

    try {
      cut.get();
    } catch (ODataApplicationException e) {
      return;
    }
    fail("Function provided not checked");
  }
}
