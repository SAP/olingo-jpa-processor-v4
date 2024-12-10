package com.sap.olingo.jpa.processor.core.api;

import com.sap.olingo.jpa.processor.core.api.JPAODataQueryDirectives.UuidSortOrder;

public interface JPAODataQueryDirectivesBuilder {

  JPAODataQueryDirectivesBuilder maxValuesInInClause(int i);

  JPAODataQueryDirectivesBuilder uuidSortOrder(UuidSortOrder order);

  JPAODataServiceContextBuilder build();

}