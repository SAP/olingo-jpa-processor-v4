package ${package}.controller;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.commons.api.ex.ODataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDHandler;
import com.sap.olingo.jpa.processor.core.api.JPAODataServiceContext;
import com.sap.olingo.jpa.processor.core.api.example.JPAExampleCUDRequestHandler;

@RestController
@RequestMapping("${punit}/v1/**")
public class ODataController {
  
  @Value("${odata.jpa.request_mapping}")
  private String requestMapping;
  private final JPAODataCRUDContextAccess serviceContext;

  public ODataController(@Value("${odata.jpa.punit_name}") final String punit,
      @Autowired final EntityManagerFactory emf, @Value("${odata.jpa.root_packages}") final String rootPackages)
      throws ODataException {
    this.serviceContext = JPAODataServiceContext.with()
        .setPUnit(punit)
        .setEntityManagerFactory(emf)
        .setTypePackage(rootPackages)
        .build();
  }
  
  @RequestMapping(value = "**", method = { RequestMethod.GET, RequestMethod.PATCH, // NOSONAR
      RequestMethod.POST, RequestMethod.DELETE })
  public void crud(final HttpServletRequest req, final HttpServletResponse resp) throws ODataException {

    EntityManager em = null;
    try {
      em = serviceContext.getEntityManagerFactory().get().createEntityManager(); // NOSONAR
      final JPAODataCRUDHandler handler = new JPAODataCRUDHandler(serviceContext);
      final HttpServletRequestWrapper request = prepareRequestMapping(req);

      handler.getJPAODataRequestContext().setCUDRequestHandler(new JPAExampleCUDRequestHandler());
      handler.getJPAODataRequestContext().setEntityManager(em);
      handler.process(request, resp);
    } finally {
      if (em != null)
        em.close();
    }
  }

  private HttpServletRequestWrapper prepareRequestMapping(final HttpServletRequest req) {
    HttpServletRequestWrapper request = new HttpServletRequestWrapper(req);
    request.setAttribute(requestMapping, "${punit}/v1");
    return request;
  }
}
