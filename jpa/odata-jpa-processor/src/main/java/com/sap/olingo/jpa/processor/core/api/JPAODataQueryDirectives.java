package com.sap.olingo.jpa.processor.core.api;

import com.sap.olingo.jpa.processor.core.api.JPAODataServiceContext.Builder;

public sealed interface JPAODataQueryDirectives {

  public static JPAODataQueryDirectivesBuilder with(final Builder builder) {
    return new JPAODataQueryDirectivesBuilderImpl(builder);
  }

  int getMaxValuesInInClause();

  UuidSortOrder getUuidSortOrder();

  static record JPAODataQueryDirectivesImpl(int maxValuesInInClause, UuidSortOrder uuidSortOrder) implements
      JPAODataQueryDirectives {

    @Override
    public int getMaxValuesInInClause() {
      return maxValuesInInClause;
    }

    @Override
    public UuidSortOrder getUuidSortOrder() {
      return uuidSortOrder;
    }

  }

  static class JPAODataQueryDirectivesBuilderImpl implements JPAODataQueryDirectivesBuilder {

    private final Builder parent;
    private int maxValuesInInClause = 0;
    private UuidSortOrder uuidSortOrder = UuidSortOrder.AS_STRING;

    JPAODataQueryDirectivesBuilderImpl(final Builder builder) {
      this.parent = builder;
    }

    @Override
    public JPAODataQueryDirectivesBuilder maxValuesInInClause(final int maxValues) {
      this.maxValuesInInClause = maxValues;
      return this;
    }

    @Override
    public JPAODataServiceContextBuilder build() {
      return parent.setQueryDirectives(new JPAODataQueryDirectivesImpl(maxValuesInInClause, uuidSortOrder));
    }

    @Override
    public JPAODataQueryDirectivesBuilder uuidSortOrder(final UuidSortOrder order) {
      this.uuidSortOrder = order;
      return this;
    }
  }

  public enum UuidSortOrder {
    AS_STRING,
    AS_BYTE_ARRAY,
    AS_JAVA_UUID;
  }

}
