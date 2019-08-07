# 3.1 Preparation
We have created metadata and have seen how we can retrieve information from the database. The next piece in our puzzle is to modify the data. `JPAODataGetHandler` is by purpose not able to handle those request. It has to be replaced by `JPAODataCRUDHandler`.

Before we start, we need to do some preparation steps. First we need a tool to perform our requests. This could be e.g. Postman for Google Chrome or an compatible tool like RESTED for Firefox or SOAPUI.

When we make changes, we need also some logic, therefore we want to create a new package _tutorial.modify_, which will be the container for the corresponding classes. Last but not least we switch the service implementation, as mentioned, to use `JPAODataCRUDHandler`, so that it looks as follows:

```Java
package tutorial.service;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.ex.ODataException;

import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDHandler;

import tutorial.modify.CUDRequestHandler;

public class Servlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final String PUNIT_NAME = "Tutorial";
  private final EntityManagerFactory emf;

  public Servlet() {
    super();
    final DataSource ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, ds);
  }

  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {

    EntityManager em = null;
    try {
      final JPAODataCRUDContextAccess serviceContext =
          (JPAODataCRUDContextAccess) getServletContext().getAttribute("ServiceContext");
      em = emf.createEntityManager();

      final JPAODataCRUDHandler handler = new JPAODataCRUDHandler(serviceContext);
      handler.getJPAODataRequestContext().setEntityManager(em);
      handler.process(req, resp);
    } catch (RuntimeException | ODataException e) {
      throw new ServletException(e);
    } finally {
      if (em != null)
        em.close();

    }
  }
}
```

With this, we can start to implement a create service: [Tutorial 3.2: Creating Entities](3-2-CreatingEntities.md)
