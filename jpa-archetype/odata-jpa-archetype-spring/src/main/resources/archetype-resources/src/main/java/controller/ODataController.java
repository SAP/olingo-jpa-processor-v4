package ${package}.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.commons.api.ex.ODataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;

@RestController
@RequestMapping("${punit}/v1/")
@RequestScope
public class ODataController {
  
  @Autowired
  private JPAODataSessionContextAccess serviceContext;
  @Autowired
  private JPAODataRequestContext requestContext;
  
  @RequestMapping(value = "**", method = { RequestMethod.GET, RequestMethod.PATCH, // NOSONAR
      RequestMethod.POST, RequestMethod.DELETE })
  public void crud(final HttpServletRequest req, final HttpServletResponse resp) throws ODataException {

    new JPAODataRequestHandler(serviceContext, requestContext).process(req, resp);
  }
}
