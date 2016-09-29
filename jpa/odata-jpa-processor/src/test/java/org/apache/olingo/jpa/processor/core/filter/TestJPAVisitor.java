package org.apache.olingo.jpa.processor.core.filter;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;

import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServiceDocument;
import org.apache.olingo.jpa.processor.core.api.JPAServiceDebugger;
import org.apache.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import org.apache.olingo.jpa.processor.core.query.JPAAbstractQuery;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.junit.Before;
import org.junit.Test;

public class TestJPAVisitor {

  private JPAFilterComplierAccess compiler;
  private JPAAbstractQuery query;
  private JPAVisitor cut;
  private JPAODataDatabaseOperations extension;
  private JPAOperationConverter converter;

  @Before
  public void setUp() {
    extension = mock(JPAODataDatabaseOperations.class);
    converter = new JPAOperationConverter(mock(CriteriaBuilder.class), extension);
    compiler = mock(JPAFilterComplierAccess.class);
    query = mock(JPAAbstractQuery.class);

    when(compiler.getConverter()).thenReturn(converter);
    when(compiler.getParent()).thenReturn(query);
    when(query.getDebugger()).thenReturn(mock(JPAServiceDebugger.class));

    cut = new JPAVisitor(compiler);
  }

//return new JPAFunctionOperator(jpaFunction, odataParams, this.jpaComplier.getParent().getRoot(), jpaComplier.getConverter().cb); 

  @Test
  public void createFunctionOperation() throws ExpressionVisitException, ODataApplicationException {

//  final UriResource resource = member.getResourcePath().getUriResourceParts().get(0);
    Member member = mock(Member.class);
    UriInfoResource info = mock(UriInfoResource.class);
    UriResourceFunction uriFunction = mock(UriResourceFunction.class);

    List<UriResource> resources = new ArrayList<UriResource>();
    resources.add(uriFunction);

    when(member.getResourcePath()).thenReturn(info);
    when(info.getUriResourceParts()).thenReturn(resources);
//  final JPAFunction jpaFunction = this.jpaComplier.getSd().getFunction(((UriResourceFunction) resource).getFunction());
    ServiceDocument sd = mock(ServiceDocument.class);
    JPAFunction jpaFunction = mock(JPAFunction.class);
    EdmFunction edmFunction = mock(EdmFunction.class);

    when(uriFunction.getFunction()).thenReturn(edmFunction);
    when(compiler.getSd()).thenReturn(sd);
    when(sd.getFunction(edmFunction)).thenReturn(jpaFunction);
    when(uriFunction.getParameters()).thenReturn(new ArrayList<UriParameter>());

    if (!(cut.visitMember(member) instanceof JPAFunctionOperator)) {
      fail();
    }
  }

}
