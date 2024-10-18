package com.sap.olingo.jpa.processor.core.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.Nonnull;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;

import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalArgumentException;

public class JPAOrderByPropertyFactory {
  /**
   *
   * @param orderByItem
   * @param et
   * @return
   * @throws ODataJPAIllegalArgumentException
   */
  public JPAProcessorAttribute createProperty(final OrderByItem orderByItem, final JPAEntityType et,
      final Locale locale) {
    try {
      final var orderInfo = extractOrderInfo(et, orderByItem);
      return createProperty(orderByItem, locale, orderInfo);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAIllegalArgumentException(orderByItem.getExpression().toString());
    }
  }

  public JPAProcessorAttribute createProperty(final From<?, ?> target, final JPAPath path, final CriteriaBuilder cb) {
    final var orderByAttribute = new JPAProcessorSimpleAttributeImpl(path, Collections.emptyList(), false);
    return orderByAttribute.setTarget(target, Collections.emptyMap(), cb);
  }

  private OrderInfo extractOrderInfo(final JPAEntityType et, final OrderByItem orderByItem)
      throws ODataJPAModelException {

    final List<JPAAssociationPath> hops = new ArrayList<>();
    var path = new StringBuilder();
    JPAStructuredType type = et;
    if (orderByItem.getExpression() instanceof final Member member) {
      for (final var part : member.getResourcePath().getUriResourceParts()) {
        if (part instanceof final UriResourceProperty property) {
          if (property.isCollection()) {
            path.append(property.getProperty().getName());
            final var associationPath = ((JPACollectionAttribute) getJPAPath(type, path.toString()).getLeaf())
                .asAssociation();
            type = associationPath.getTargetType();
            hops.add(associationPath);
            path = new StringBuilder();
          } else {
            path.append(property.getProperty().getName()).append(JPAPath.PATH_SEPARATOR);
          }
        } else if (part instanceof final UriResourceNavigation navigation) {
          path.append(navigation.getProperty().getName());
          final var associationPath = type.getAssociationPath(path.toString());
          type = associationPath.getTargetType();
          hops.add(associationPath);
          path = new StringBuilder();
        }
      }
    } else {
      // TODO Support methods like tolower for order by as well
      throw new ODataJPAIllegalArgumentException(orderByItem.getExpression().toString());
    }
    return new OrderInfo(hops, path, type);
  }

  private JPAProcessorAttribute createProperty(final OrderByItem orderByItem, final Locale locale,
      final OrderInfo orderInfo) throws ODataJPAModelException {
    switch (getKindOfLastPart(orderByItem)) {
      case primitiveProperty:
        final var pathString = orderInfo.path.deleteCharAt(orderInfo.path.length() - 1).toString();
        final var lastType = orderInfo.type;
        return Optional.ofNullable(lastType.getPath(pathString))
            .filter(attribute -> attribute.getLeaf() instanceof JPADescriptionAttribute)
            .map(attribute -> createDescriptionProperty(orderByItem, orderInfo.hops, attribute, locale))
            .or(() -> this.createPrimitiveProperty(orderByItem, orderInfo.hops, pathString, lastType))
            .orElseThrow(() -> new ODataJPAIllegalArgumentException(orderByItem.getExpression().toString()));
      case count:
        return new JPAProcessorCountAttributeImpl(orderInfo.hops, orderByItem.isDescending());
      default:
        throw new ODataJPAIllegalArgumentException(orderByItem.getExpression().toString());
    }
  }

  private JPAProcessorAttribute createDescriptionProperty(final OrderByItem orderByItem,
      final List<JPAAssociationPath> hops, final JPAPath attribute, final Locale locale) {
    return new JPAProcessorDescriptionAttributeImpl(attribute, hops, orderByItem.isDescending(), locale);
  }

  private Optional<JPAProcessorAttribute> createPrimitiveProperty(final OrderByItem orderByItem,
      final List<JPAAssociationPath> hops, final String pathString, final JPAStructuredType type) {

    try {
      return Optional.ofNullable(type.getPath(pathString))
          .map(attribute -> new JPAProcessorSimpleAttributeImpl(attribute, hops, orderByItem.isDescending()));
    } catch (final ODataJPAModelException e) {
      return Optional.empty();
    }
  }

  private UriResourceKind getKindOfLastPart(final OrderByItem orderByItem) {
    final var parts = ((Member) orderByItem.getExpression()).getResourcePath().getUriResourceParts();
    return parts.get(parts.size() - 1).getKind();
  }

  @Nonnull
  private JPAPath getJPAPath(final JPAStructuredType st, final String name) {

    try {
      final var jpaPath = st.getPath(name);
      if (jpaPath != null)
        return jpaPath;
      else
        throw new ODataJPAIllegalArgumentException(name);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAIllegalArgumentException(e);
    }
  }

  private static record OrderInfo(List<JPAAssociationPath> hops, StringBuilder path, JPAStructuredType type) {

  }
}
