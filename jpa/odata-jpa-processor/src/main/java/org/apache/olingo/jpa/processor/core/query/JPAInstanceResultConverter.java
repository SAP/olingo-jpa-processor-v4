package org.apache.olingo.jpa.processor.core.query;

import java.util.List;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.server.api.ODataApplicationException;

public class JPAInstanceResultConverter {
  public static final String ACCESS_MODIFIER_GET = "get";
  public static final String ACCESS_MODIFIER_SET = "set";
  public static final String ACCESS_MODIFIER_IS = "is";

  private final List<Object> jpaQueryResult;
  private final EdmEntitySet edmEntitySet;

  public JPAInstanceResultConverter(List<Object> jpaQueryResult, EdmEntitySet edmEntitySet) {
    super();
    this.jpaQueryResult = jpaQueryResult;
    this.edmEntitySet = edmEntitySet;
  }

  public EntityCollection getResult() throws ODataApplicationException {
    EntityCollection odataEntityCollection = new EntityCollection();
    List<Entity> odataResults = odataEntityCollection.getEntities();

    for (Object row : jpaQueryResult) {

    }
    return odataEntityCollection;
  }

}
