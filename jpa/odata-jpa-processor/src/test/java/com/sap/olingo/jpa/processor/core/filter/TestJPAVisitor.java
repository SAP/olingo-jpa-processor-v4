package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;

import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADataBaseFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import com.sap.olingo.jpa.processor.core.query.JPAAbstractQuery;

class TestJPAVisitor {

  private JPAFilterComplierAccess compiler;
  private JPAAbstractQuery query;
  private JPAExpressionVisitor cut;
  private JPAODataDatabaseOperations extension;
  private JPAOperationConverter converter;

  @BeforeEach
  public void setUp() {
    extension = mock(JPAODataDatabaseOperations.class);
    converter = new JPAOperationConverter(mock(CriteriaBuilder.class), extension);
    compiler = mock(JPAFilterComplierAccess.class);
    query = mock(JPAAbstractQuery.class);

    when(compiler.getConverter()).thenReturn(converter);
    when(compiler.getParent()).thenReturn(query);
    when(compiler.getDebugger()).thenReturn(mock(JPAServiceDebugger.class));
    when(query.getDebugger()).thenReturn(mock(JPAServiceDebugger.class));

    cut = new JPAVisitor(compiler);
  }

  @Test
  void createFunctionOperation() throws ExpressionVisitException, ODataApplicationException {

//  final UriResource resource = member.getResourcePath().getUriResourceParts().get(0);
    final Member member = mock(Member.class);
    final UriInfoResource info = mock(UriInfoResource.class);
    final UriResourceFunction uriFunction = mock(UriResourceFunction.class);

    final List<UriResource> resources = new ArrayList<>();
    resources.add(uriFunction);

    when(member.getResourcePath()).thenReturn(info);
    when(info.getUriResourceParts()).thenReturn(resources);
//  final JPAFunction jpaFunction = this.jpaComplier.getSd().getFunction(((UriResourceFunction) resource).getFunction());
    final JPAServiceDocument sd = mock(JPAServiceDocument.class);
    final JPADataBaseFunction jpaFunction = mock(JPADataBaseFunction.class);
    final EdmFunction edmFunction = mock(EdmFunction.class);

    when(uriFunction.getFunction()).thenReturn(edmFunction);
    when(compiler.getSd()).thenReturn(sd);
    when(sd.getFunction(edmFunction)).thenReturn(jpaFunction);
    when(uriFunction.getParameters()).thenReturn(new ArrayList<UriParameter>());

    assertTrue(cut.visitMember(member) instanceof JPAFunctionOperator);
  }

}