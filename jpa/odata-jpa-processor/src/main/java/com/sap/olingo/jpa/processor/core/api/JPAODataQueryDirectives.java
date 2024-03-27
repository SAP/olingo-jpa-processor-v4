package com.sap.olingo.jpa.processor.core.api;

import com.sap.olingo.jpa.processor.core.api.JPAODataServiceContext.Builder;

public sealed interface JPAODataQueryDirectives {

  public static JPAODataQueryDirectivesBuilder with(final Builder builder) {
    return new JPAODataQueryDirectivesBuilderImpl(builder);
  }

  int getMaxValuesInInClause();

  static record JPAODataQueryDirectivesImpl(int maxValuesInInClause) implements JPAODataQueryDirectives {

    @Override
    public int getMaxValuesInInClause() {
      return maxValuesInInClause;
    }

  }

  static class JPAODataQueryDirectivesBuilderImpl implements JPAODataQueryDirectivesBuilder {

    private final Builder parent;
    private int maxValuesInInClause = 0;

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
      return parent.setQueryDirectives(new JPAODataQueryDirectivesImpl(maxValuesInInClause));
    }
  }
}
