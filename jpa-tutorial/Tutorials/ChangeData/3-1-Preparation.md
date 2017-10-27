# 3.1 Preparation
We have created metadata and have seen how we can retrieve information from the database. The next piece in our puzzle is to modify the data. `JPAODataGetHandler` is by purpose not able to handle those request. It has to be replaced by `JPAODataCRUDHandler`.

Before we start, we need to do some preparation steps. First we need a tool to perform our requests. This could be e.g. Postman for Google Chrome or an compatible tool like HttpRequest for Firefox or SOAPUI.

When we make changes, we need also some logic, therefore we want to create a new package _tutorial.modify_, which will be the container for the corresponding classes. Last but not least we switch the service implementation, as mentioned, to use `JPAODataCRUDHandler`, so that it looks as follows:

```Java
package tutorial.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.commons.api.ex.ODataException;
import com.sap.olingo.jpa.processor.core.api.JPAODataGetHandler;

public class Servlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String PUNIT_NAME = "Tutorial";

	protected void service(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			EntityManagerFactory emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME,
				DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB));
			em = emf.createEntityManager();
			JPAODataCRUDHandler handler = new JPAODataCRUDHandler(PUNIT_NAME);
			handler.process(req, resp, em);
		} catch (RuntimeException e) {
			throw new ServletException(e);
		} catch (ODataException e) {
			throw new ServletException(e);
		} finally {
			if (em != null)
				em.close();
		}
	}
}
```

With this, we can start to implement a create service: [Tutorial 3.2: Creating Entities](3-2-CreatingEntities.md)
