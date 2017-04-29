package com.sap.olingo.jpa.processor.core.filter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAFunctionParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAFunctionResultParameter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.query.ExpressionUtil;

public class JPALiteralOperator implements JPAOperator {
  private final Literal literal;
  private final OData odata;

  public JPALiteralOperator(final OData odata, final Literal literal) {
    this.literal = literal;
    this.odata = odata;
  }

  @Override
  public Object get() throws ODataApplicationException {
    final EdmPrimitiveType edmType = ((EdmPrimitiveType) literal.getType());
    try {

      final Class<?> defaultType = edmType.getDefaultType();
      final Constructor<?> c = defaultType.getConstructor(String.class);
      return c.newInstance(edmType.fromUriLiteral(literal.getText()));
    } catch (InstantiationException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (IllegalAccessException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (IllegalArgumentException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (InvocationTargetException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (EdmPrimitiveTypeException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (NoSuchMethodException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (SecurityException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Converts a literal value into system type of attribute
   */
  public Object get(final JPAAttribute attribute) throws ODataApplicationException {
    return ExpressionUtil.convertValueOnAttribute(odata, attribute, literal.getText());
  }

  public Object get(JPAFunctionResultParameter returnType) throws ODataApplicationException {
    return ExpressionUtil.convertValueOnFacet(odata, returnType, literal.getText());
  }

  public Object get(JPAFunctionParameter jpaParameter) throws ODataApplicationException {

    return ExpressionUtil.convertValueOnFacet(odata, jpaParameter, literal.getText());
  }

  Literal getLiteral() {
    return literal;
  }
}
