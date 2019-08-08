# 2.1: Retrieving Data
As mentioned in the preparation, we have to replace our current service implementation by a new one.
Up to now we used `JPAEdmProvider`, which creates the service document as well as the metadata document. Now we will use `JPAODataGetHandler`. A handler instance has two contexts objects, on the one hand the service context, which contains information that does not or only changes rarely during lifetime of the service and is shared between all requests, and a request context, which has request specific information.  Please note that an instance of `JPAEdmProvider` is of the service context and gets created automatically. 

In the tutorial the serviece contxt will not change. So it will be put it into the _servlet context_, therefore we created a _listener_ in package _tutorial.service_:
```Java
package tutorial.service;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.ex.ODataException;

import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataServiceContext;

public class Listener implements ServletContextListener {
  private static final String PUNIT_NAME = "Tutorial";

  // Create Service Context
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    final DataSource ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    try {
      final JPAODataCRUDContextAccess serviceContext = JPAODataServiceContext.with()
          .setPUnit(PUNIT_NAME)
          .setDataSource(ds)
          .setTypePackage("tutorial.operations", "tutorial.model")
          .build();
      sce.getServletContext().setAttribute("ServiceContext", serviceContext);
    } catch (ODataException e) {
      // Log error
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    sce.getServletContext().setAttribute("ServiceContext", null);
  }
}
```
To trigger the call of the listener it has to be added to the `web.xml`:

```XML
  ...
  <listener>
    <listener-class>tutorial.service.Listener</listener-class>
  </listener>
</web-app>
```
Now we have all the peaces to build a service that responses to GET requests
```Java
package tutorial.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.commons.api.ex.ODataException;

import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDHandler;

public class Servlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {

    try {
      final JPAODataCRUDContextAccess serviceContext =
          (JPAODataCRUDContextAccess) getServletContext().getAttribute("ServiceContext");      
      new JPAODataCRUDHandler(serviceContext).process(req, resp);
    } catch (RuntimeException | ODataException e) {
      throw new ServletException(e);
    }
  }
}
```
Starting the service we are able to play around with our OData service. We could e.g.:
* Retrieve all the Companies: _http://localhost:8080/Tutorial/Tutorial.svc/Companies_
* Or we want to find out which user had created Company('1'): _http://localhost:8080/Tutorial/Tutorial.svc/Companies('1')/AdministrativeInformation/Created/User_
* Or we want to get all companies wuth role _A_: _http://localhost:8080/Tutorial/Tutorial.svc/Companies?$filter=Roles/any(d:d/RoleCategory eq 'A')_
* Or we want to know which Administrative Division has an Area greater then 40000000: _http://localhost:8080/Tutorial/Tutorial.svc/AdministrativeDivisions?$filter=Area gt 4000000&$count=true_
* Or we look for the parents and children of a certain Administrative Division: _http://localhost:8080/Tutorial/Tutorial.svc/AdministrativeDivisions(DivisionCode='BE254',CodeID='NUTS3',CodePublisher='Eurostat')?$expand=Parent($expand=Parent),Children&$format=json_
* Or we look for ...

Next we will see how we can use functions: [Tutorial 2.3 Using Functions](2-3-UsingFunctions.md)
