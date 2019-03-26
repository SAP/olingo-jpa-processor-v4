# 4.3 Server Driven Paging
## Implementation
OData describes that a server can restrict the number of returned records e.g. to prevent DoS attack respectively to prevent that the server die with an OutOfMemory exception. Implementing this so called [Server-Driven Paging](http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398310) requires the knowledge about a couple of details such as:

- Heap Size of the web server
- Width respectively memory consumption of an entity
- Expected number of results of an $expand
- Number of parallel processed request on a server instance
- ...

This makes a general implementation impossible. Instead of that a hook implementation can be provided. This hook has to implement interface `com.sap.olingo.jpa.processor.core.api.JPAODataPagingProvider`, which contains two methods `getFristPage` and `getNextPage`. `getFristPage` is called in case a query does not contain a `$skiptoken`. I either returns an instance of `JPAODataPage`, so only a subset of the requested entities will be returned or null and all entities are returned. If a request has a `$skiptoken` method `getNextPage` is called. If this method does not return a `JPAODataPage` instance, a _410, "Gone"_, exception is raised.

To get started we want to use `com.sap.olingo.jpa.processor.core.api.example.JPAExamplePagingProvider` as our paging provider. An instance of the class shall be accessible right in the moment the service is started. Therefore it is created in a _listener_ and put in to the servlet context:

```Java
package tutorial.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.sap.olingo.jpa.processor.core.api.example.JPAExamplePagingProvider;

public class Listener implements ServletContextListener {
  private static final int BUFFER_SIZE = 10;

  // Create Paging Provider
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    final Map<String, Integer> pageSizes = new HashMap<>();

    pageSizes.put("Companies", 5);
    pageSizes.put("AdministrativeDivisions", 10);

    sce.getServletContext().setAttribute("PagingProvider", new JPAExamplePagingProvider(pageSizes, BUFFER_SIZE));
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    sce.getServletContext().setAttribute("PagingProvider", null);
  }
}
```
As you can see the constructor takes two parameter. On the one hand the buffer size, which describes how many pages a cache inside the paging provider holds, and on the other hand a map that holds the page size for some chosen entity sets. To trigger the call of the listener it has to be added to the `web.xml`:

```
  ...
  <listener>
    <listener-class>tutorial.service.Listener</listener-class>
  </listener>
</web-app>
```
Last, but not least we have to register the hook

```Java
public class Servlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final String PUNIT_NAME = "Tutorial";

  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
  ...
  handler.getJPAODataContext().setPagingProvider((JPAODataPagingProvider) getServletContext().getAttribute(
    "PagingProvider"));
  handler.process(req, resp, em);  
  ...  
```
Now we are ready to start the web server.
In case we try to retrieve all the Companies: _http://localhost:8080/Tutorial/Tutorial.svc/Companies_, only the first five are returned. We can do the same for _http://localhost:8080/Tutorial/Tutorial.svc/AdministrativeDivisions_ and get just ten. By using the provided next link _http://localhost:8080/Tutorial/Tutorial.svc/AdministrativeDivisions?$skiptoken=<...>_ we get the next page. If we want to go back after doing that at least 10 times to the second page we will get an error message stating _	"Requested page '<...>' is gone."_

## Not Supported
As of now paging for expanded to-many navigation properties is not supported. This is because it would only make sense if it could be pushed to the database. Unfortunately SQL, and with that JPA, does not support LIMIT, OFFSET per parent row. Midterm a solution based on windows function _row_number_ shall be enabled:

```SQL
ROW_NUMBER() OVER (PARTITION BY <patent key> ORDER BY <...>)
```

It is important to mention that this windows function, especially with the required _over_, is not supported by all databases.

## Additional Remarks
The presented paging provider has only limited functionality and would only work if the service is only deploy once or if requests of a user is always routed to the same service instance. In case of a micro service architecture with multiple service instances and a load balance an external cache on the db (own deprecation handling required) or e.g external cache as Redis, which provides with the EXPIRE command also a deprecation mechanism, is required.
