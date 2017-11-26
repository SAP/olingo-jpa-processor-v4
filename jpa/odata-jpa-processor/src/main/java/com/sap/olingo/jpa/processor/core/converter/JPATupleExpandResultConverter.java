package com.sap.olingo.jpa.processor.core.converter;

import java.util.List;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriHelper;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

class JPATupleExpandResultConverter extends JPATupleAbstractConverter {
  private final Tuple parentRow;
  private final JPAAssociationPath assoziation;

  JPATupleExpandResultConverter(final JPAExpandResult jpaExpandResult, final Tuple parentRow,
      final JPAAssociationPath assoziation, final UriHelper uriHelper, final JPAServiceDocument sd,
      final ServiceMetadata serviceMetadata) throws ODataApplicationException {

    super(jpaExpandResult, uriHelper, sd, serviceMetadata);
    this.parentRow = parentRow;
    this.assoziation = assoziation;
  }

  public Link getResult() throws ODataApplicationException {
    final Link link = new Link();
    link.setTitle(assoziation.getLeaf().getExternalName());
    link.setRel(Constants.NS_NAVIGATION_LINK_REL + link.getTitle());
    link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
    final EntityCollection expandCollection = createEntityCollection();
    expandCollection.setCount(determineCount(expandCollection));
    if (assoziation.getLeaf().isCollection()) {
      link.setInlineEntitySet(expandCollection);
      // TODO link.setHref(parentUri.toASCIIString());
    } else {
      if (expandCollection.getEntities() != null && !expandCollection.getEntities().isEmpty()) {
        final Entity expandEntity = expandCollection.getEntities().get(0);
        link.setInlineEntity(expandEntity);
        // TODO link.setHref(expandCollection.getId().toASCIIString());
      }
    }
    return link;
  }

  private Integer determineCount(final EntityCollection expandCollection) throws ODataJPAQueryException {
    try {
      Long count = jpaQueryResult.getCount(buildConcatenatedKey(parentRow, assoziation.getJoinColumnsList()));
      return count != null ? Integer.valueOf(count.intValue()) : null;
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
  }

  private EntityCollection createEntityCollection() throws ODataApplicationException {

    List<Tuple> subResult = null;
    try {
      subResult = jpaQueryResult.getResult(buildConcatenatedKey(parentRow, assoziation.getJoinColumnsList()));
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }

    final EntityCollection odataEntityCollection = new EntityCollection();
    if (subResult != null) {
      for (final Tuple row : subResult) {
        final Entity odataEntity = convertRow(jpaConversionTargetEntity, row);
        final List<Entity> odataResults = odataEntityCollection.getEntities();
        odataResults.add(odataEntity);
      }
    }
    // TODO odataEntityCollection.setId(createId());
    return odataEntityCollection;
  }

}
