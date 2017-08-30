# 2.1: Retrieving Data
As mentioned in the preparation, we have to replace our current service implementation by a new one.
Up to now we used JPAEdmProvider, which created to service document as well as the metadata document. Now we will use JPAODataGetHandler:
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

			JPAODataGetHandler handler = new JPAODataGetHandler(PUNIT_NAME,
					DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB));

			handler.process(req, resp);
		} catch (RuntimeException e) {
			throw new ServletException(e);
		} catch (ODataException e) {
			throw new ServletException(e);
		}
	}
}
```
Now we are able to play around with our OData service. We could e.g.:
* Retrieve all the Companies: _http://localhost:8080/Tutorial/Tutorial.svc/Companies_
* Or we want to find out which user had created Company('1'): _http://localhost:8080/Tutorial/Tutorial.svc/Companies('1')/AdministrativeInformation/Created/User_
* Or we want to get all companies wuth role _A_: _http://localhost:8080/Tutorial/Tutorial.svc/Companies?$filter=Roles/any(d:d/RoleCategory eq 'A')_
* Or we want to know which Administrative Division has an Area greater then 40000000: _http://localhost:8080/Tutorial/Tutorial.svc/AdministrativeDivisions?$filter=Area gt 4000000&$count=true_
* Or we look for the parents and children of a certain Administrative Division: _http://localhost:8080/Tutorial/Tutorial.svc/AdministrativeDivisions(DivisionCode='BE254',CodeID='NUTS3',CodePublisher='Eurostat')?$expand=Parent($expand=Parent),Children&$format=json_
* Or we look for ...
