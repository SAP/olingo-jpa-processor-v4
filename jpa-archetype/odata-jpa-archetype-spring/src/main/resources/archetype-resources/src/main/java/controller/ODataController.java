package ${package}.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.commons.api.ex.ODataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDHandler;
import com.sap.olingo.jpa.processor.core.api.example.JPAExampleCUDRequestHandler;

@RestController
@RequestMapping("${punit}/v1/**")
public class ODataController {
  
  @Autowired
  private JPAODataCRUDContextAccess serviceContext;
  
  @RequestMapping(value = "**", method = { RequestMethod.GET, RequestMethod.PATCH, // NOSONAR
      RequestMethod.POST, RequestMethod.DELETE })
  public void crud(final HttpServletRequest req, final HttpServletResponse resp) throws ODataException {

    final JPAODataCRUDHandler handler = new JPAODataCRUDHandler(serviceContext);
    handler.getJPAODataRequestContext().setCUDRequestHandler(new JPAExampleCUDRequestHandler());
    handler.process(req, resp);
  }
}