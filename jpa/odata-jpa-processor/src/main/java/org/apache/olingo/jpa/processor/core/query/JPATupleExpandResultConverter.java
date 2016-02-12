package org.apache.olingo.jpa.processor.core.query;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.server.api.ODataApplicationException;

public class JPATupleExpandResultConverter extends JPATupleAbstractConverter {
  private final Tuple parentRow;
  private final JPAAssociationPath assoziation;
  private final URI parentUri;

  public JPATupleExpandResultConverter(URI uri, JPAExpandResult jpaExpandResult, Tuple parentRow,
      JPAAssociationPath assoziation) {
    super((JPAEntityType) assoziation.getTargetType(), jpaExpandResult);
    this.parentRow = parentRow;
    this.assoziation = assoziation;
    this.parentUri = uri;
  }

  public Link getResult() throws ODataApplicationException {
    Link link = new Link();
    link.setTitle(assoziation.getLeaf().getExternalName());
    link.setRel(Constants.NS_ASSOCIATION_LINK_REL + link.getTitle());
    link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
    EntityCollection expandCollection = createEntityCollection();
    if (assoziation.getLeaf().isCollection()) {
      link.setInlineEntitySet(expandCollection);
      expandCollection.setCount(new Integer(5));
      // TODO link.setHref(parentUri.toASCIIString());
    } else {
      Entity expandEntity = expandCollection.getEntities().get(0);
      link.setInlineEntity(expandEntity);
      // TODO link.setHref(expandCollection.getId().toASCIIString());
    }
    return link;
  }

  private EntityCollection createEntityCollection() throws ODataApplicationException {

    EntityCollection odataEntityCollection = new EntityCollection();
    List<Entity> odataResults = odataEntityCollection.getEntities();

    List<Tuple> subResult = null;
    try {
      subResult = jpaQueryResult.getResult(buildConcatenatedKey(parentRow, assoziation.getJoinColumnsList())
          .toString());
    } catch (ODataJPAModelException e) {
      throw new ODataApplicationException("Mapping Error", 500, Locale.ENGLISH, e);
    }

    if (subResult != null) {
      for (Tuple row : subResult) {
        Entity odataEntity = convertRow(jpaConversionTargetEntity, row);
        odataResults.add(odataEntity);
      }
    }
    // TODO odataEntityCollection.setId(createId());
    return odataEntityCollection;
  }

  private URI createId() {
    StringBuffer id = new StringBuffer();
    id.append(parentUri.toString());
    id.append(JPAAssociationPath.PATH_SEPERATOR);
    id.append(assoziation.getAlias());
    try {
      return new URI(id.toString());
    } catch (URISyntaxException e) {
      throw new ODataRuntimeException("Unable to create (Atom) id for entity", e);
    }

  }

  @Override
  protected URI createId(List<? extends JPAAttribute> keyAttributes, Tuple row) throws ODataApplicationException,
      ODataRuntimeException {
    return null;
  }

}
