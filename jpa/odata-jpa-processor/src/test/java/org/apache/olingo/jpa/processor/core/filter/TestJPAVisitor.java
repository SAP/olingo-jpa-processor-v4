package org.apache.olingo.jpa.processor.core.filter;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.jpa.processor.core.api.JPAServiceDebugger;
import org.apache.olingo.jpa.processor.core.query.JPAAbstractQuery;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.junit.Test;

public class TestJPAVisitor {

  @Test
  public void createFunctionOperation() throws ExpressionVisitException, ODataApplicationException {
    JPAFilterComplierAccess compiler = mock(JPAFilterComplierAccess.class);
    JPAAbstractQuery query = mock(JPAAbstractQuery.class);
    Member member = mock(Member.class);
    UriInfoResource info = mock(UriInfoResource.class);
    UriResourceFunction uriFunction = mock(UriResourceFunction.class);

    List<UriResource> resources = new ArrayList<UriResource>();
    resources.add(uriFunction);

    when(compiler.getParent()).thenReturn(query);
    when(query.getDebugger()).thenReturn(mock(JPAServiceDebugger.class));

    when(member.getResourcePath()).thenReturn(info);
    when(info.getUriResourceParts()).thenReturn(resources);

    JPAVisitor cut = new JPAVisitor(compiler);

    if (!(cut.visitMember(member) instanceof JPAFunctionOperator)) {
      fail();
    }
  }

}
