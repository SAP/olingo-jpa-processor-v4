package com.sap.olingo.jpa.processor.core.converter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriHelper;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class JPAEntityResultConverter extends JPAStructuredResultConverter {
  private final EdmEntitySet edmEntitySet;
  private final UriHelper odataUriHelper;

  public JPAEntityResultConverter(final UriHelper uriHelper, final JPAServiceDocument sd,
      final List<?> jpaQueryResult, final EdmEntitySet edmEntitySet)
      throws ODataJPAModelException {
    super(jpaQueryResult, sd.getEntity(edmEntitySet.getName()));
    this.edmEntitySet = edmEntitySet;
    this.odataUriHelper = uriHelper;
  }

  @Override
  public EntityCollection getResult() throws ODataApplicationException, SerializerException, URISyntaxException {
    final EntityCollection odataEntityCollection = new EntityCollection();
    final List<Entity> odataResults = odataEntityCollection.getEntities();

    for (final Object row : jpaQueryResult) {
      final Entity odataEntity = new Entity();
      odataEntity.setType(edmEntitySet.getEntityType().getFullQualifiedName().getFullQualifiedNameAsString());
      final List<Property> properties = odataEntity.getProperties();
      convertProperties(row, properties, jpaTopLevelType);
      odataEntity.setId(new URI(odataUriHelper.buildKeyPredicate(edmEntitySet.getEntityType(), odataEntity)));
      odataResults.add(odataEntity);
    }
    return odataEntityCollection;
  }

}
