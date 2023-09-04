package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;

import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.UriParameter;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameterFacet;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public final class ExpressionUtil {
  public static final int CONTAINS_ONLY_LANGU = 1;
  public static final int CONTAINS_LANGU_COUNTRY = 2;
  public static final String SELECT_ITEM_SEPARATOR = ",";

  private ExpressionUtil() {}

  public static Expression<Boolean> createEQExpression(final OData odata, final CriteriaBuilder cb,
      final From<?, ?> root, final JPAEntityType jpaEntity, final UriParameter keyPredicate)
      throws ODataJPAFilterException, ODataJPAModelException {

    final JPAPath path = jpaEntity.getPath(keyPredicate.getName());
    final JPAAttribute attribute = path.getLeaf();

    return cb.equal(convertToCriteriaPath(root, path.getPath()), convertValueOnAttribute(odata, attribute, keyPredicate
        .getText()));

  }

  /**
   * Converts the jpaPath into a Criteria Path.
   * @param joinTables
   * @param root
   * @param jpaPath
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> Path<T> convertToCriteriaPath(final Map<String, From<?, ?>> joinTables, final From<?, ?> root,
      final List<JPAElement> jpaPath) {
    Path<?> p = root;
    for (final JPAElement jpaPathElement : jpaPath)
      if (jpaPathElement instanceof JPADescriptionAttribute) {
        final Join<?, ?> join = (Join<?, ?>) joinTables.get(jpaPathElement.getInternalName());
        p = join.get(((JPADescriptionAttribute) jpaPathElement).getDescriptionAttribute().getInternalName());
      } else if (jpaPathElement instanceof JPACollectionAttribute) {
        p = joinTables.get(jpaPathElement.getExternalName());
      } else {
        p = p.get(jpaPathElement.getInternalName());
      }
    return (Path<T>) p;
  }

  public static Path<?> convertToCriteriaPath(final From<?, ?> root, final List<JPAElement> jpaPath) {
    Path<?> p = root;
    for (final JPAElement jpaPathElement : jpaPath)
      p = p.get(jpaPathElement.getInternalName());
    return p;
  }

  /**
   * Converts an OData attribute into an JPA path. Sets the alias to the alias of the OData path of the attribute.
   * @param root From the path be derived from
   * @param et OData Entity Type
   * @param jpaAttributes Attribute to be converted into an JPA path
   * @return
   * @throws ODataJPAQueryException
   */
  public static List<Path<Object>> convertToCriteriaPathList(final From<?, ?> root, final JPAEntityType et,
      final List<JPAAttribute> jpaAttributes) throws ODataJPAQueryException {

    try {
      final List<Path<Object>> result = new ArrayList<>(jpaAttributes.size());
      for (final JPAAttribute attribute : jpaAttributes) {
        final JPAPath path = et.getPath(attribute.getExternalName());
        @SuppressWarnings("unchecked")
        final Path<Object> p = (Path<Object>) convertToCriteriaPath(root, path.getPath());
        p.alias(path.getAlias());
        result.add(p);
      }
      return result;
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  public static Object convertValueOnAttribute(final OData odata, final JPAAttribute attribute, final String value)
      throws ODataJPAFilterException {
    return convertValueOnAttribute(odata, attribute, value, true);
  }

  @SuppressWarnings("unchecked")
  public static <T> Object convertValueOnAttribute(final OData odata, final JPAAttribute attribute, final String value,
      final Boolean isUri) throws ODataJPAFilterException {

    try {
      final CsdlProperty edmProperty = (CsdlProperty) attribute.getProperty();

      // TODO literal does not convert decimals without scale properly
      String targetValue = null;
      final EdmPrimitiveType edmType = odata.createPrimitiveTypeInstance(attribute.getEdmType());
      if (Boolean.TRUE.equals(isUri)) {
        targetValue = edmType.fromUriLiteral(value);
      } else {
        targetValue = value;
      }
      // Converter
      if (attribute.getConverter() != null) {
        final AttributeConverter<?, T> dbConverter = attribute.getConverter();
        return dbConverter.convertToEntityAttribute(
            (T) edmType.valueOfString(targetValue, edmProperty.isNullable(), edmProperty.getMaxLength(),
                edmProperty.getPrecision(), edmProperty.getScale(), true, attribute.getType()));
      } else {
        return edmType.valueOfString(targetValue, edmProperty.isNullable(), edmProperty.getMaxLength(),
            edmProperty.getPrecision(), edmProperty.getScale(), true, attribute.getType());
      }
    } catch (EdmPrimitiveTypeException | ODataJPAModelException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  public static Object convertValueOnFacet(final OData odata, final JPAParameterFacet returnType, final String value)
      throws ODataJPAFilterException {
    try {
      final EdmPrimitiveTypeKind edmTypeKind = EdmPrimitiveTypeKind.valueOfFQN(returnType.getTypeFQN());
      final EdmPrimitiveType edmType = odata.createPrimitiveTypeInstance(edmTypeKind);
      String targetValue;

      targetValue = edmType.fromUriLiteral(value);
      return edmType.valueOfString(targetValue, true, returnType.getMaxLength(), returnType.getPrecision(), returnType
          .getScale(), true, returnType.getType());
    } catch (EdmPrimitiveTypeException | ODataJPAModelException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  public static Locale determineFallbackLocale(final Map<String, List<String>> headers) {
    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html (14.4 accept language header
    // example: Accept-Language : da, en-gb;q=0.8, en;q=0.7)
    final List<String> languageHeaders = headers.get("accept-language");
    if (languageHeaders != null) {
      final String languageHeader = languageHeaders.get(0);
      if (languageHeader != null) {
        final String[] localeList = languageHeader.split(SELECT_ITEM_SEPARATOR);
        final String locale = localeList[0];
        final String[] languCountry = locale.split("-");
        if (languCountry.length == CONTAINS_LANGU_COUNTRY)
          return new Locale(languCountry[0], languCountry[1]);
        else if (languCountry.length == CONTAINS_ONLY_LANGU)
          return new Locale(languCountry[0]);
        else
          return Locale.ENGLISH;
      }
    }
    return Locale.ENGLISH;
  }
}
