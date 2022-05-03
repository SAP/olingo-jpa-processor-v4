# How to get own names for the OData artifacts?

The JPA Processor generates out of the JPA annotations or metadata the corresponding OData metadata. This includes also the generation of the names. By default e.g. all OData artifacts start with an upper case character and entity sets are in plural. There are multiple reasons to have another naming. E.g. different naming conventions, like properties shall be in lower case, or a noun used as entity name has no plural.

Our service has an entity set _Persons_, which shall be named _People_ instead.

The names of the artifacts are created by a class implementing interface [JPAEdmNameBuilder](https://github.com/SAP/olingo-jpa-processor-v4/blob/master/jpa/odata-jpa-metadata/src/main/java/com/sap/olingo/jpa/metadata/core/edm/mapper/api/JPAEdmNameBuilder.java). As mentioned, for our service, we just want to override _Persons_. For the rest we take the default names.

As a first step we create an implementation of `JPAEdmNameBuilder` called `APINameBuilder`. It creates an instance of `JPADefaultEdmNameBuilder` to fallback to the default names and returns `People` as entity set name for entity type `Person`:

```Java
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;

class APINameBuilder implements JPAEdmNameBuilder {
  private final JPAEdmNameBuilder defaultNameBuilder;

  APINameBuilder(final String punit) {
    defaultNameBuilder = new JPADefaultEdmNameBuilder(punit);
  }

  @Override
  public String buildComplexTypeName(final EmbeddableType<?> jpaEmbeddedType) {
    return defaultNameBuilder.buildComplexTypeName(jpaEmbeddedType);
  }

  @Override
  public String buildContainerName() {
    return defaultNameBuilder.buildContainerName();
  }

  @Override
  public String buildEntitySetName(final String entityTypeName) {
    return "Person".equals(entityTypeName) ? "People" : defaultNameBuilder.buildEntitySetName(entityTypeName);
  }

  @Override
  public String buildEntityTypeName(final EntityType<?> jpaEntityType) {
    return defaultNameBuilder.buildEntityTypeName(jpaEntityType);
  }

  @Override
  public String buildEnumerationTypeName(final Class<? extends Enum<?>> javaEnum) {
    return defaultNameBuilder.buildEnumerationTypeName(javaEnum);
  }

  @Override
  public String buildNaviPropertyName(final Attribute<?, ?> jpaAttribute) {
    return defaultNameBuilder.buildNaviPropertyName(jpaAttribute);
  }

  @Override
  public String buildOperationName(final String internalOperationName) {
    return defaultNameBuilder.buildOperationName(internalOperationName);
  }

  @Override
  public String buildPropertyName(final String jpaAttributeName) {
    return defaultNameBuilder.buildPropertyName(jpaAttributeName);
  }

  @Override
  public String getNamespace() {
    return defaultNameBuilder.getNamespace();
  }
}
```

The second step is to hand over the name builder to the session context. The session context is created in class `ProcessorConfiguration` by method `sessionContext`. We add `.setEdmNameBuilder(new APINameBuilder(punit))`:

```Java
  @Bean
  public JPAODataSessionContextAccess sessionContext(@Autowired final EntityManagerFactory emf) throws ODataException {

    return JPAODataServiceContext.with()
        .setPUnit(punit)
        .setEntityManagerFactory(emf)
        .setTypePackage(rootPackages)
        .setRequestMappingPath("Trippin/v1")
        .setEdmNameBuilder(new APINameBuilder(punit))
        .build();
  }
```

Starting the service and executing http://localhost:9010/Trippin/v1/$metadata will return:

```XML
...
<EntityContainer Name="TrippinContainer">
  <EntitySet Name="People" EntityType="Trippin.Person">
    <NavigationPropertyBinding Path="Trips" Target="Trips"/>
  </EntitySet>
  <EntitySet Name="Trips" EntityType="Trippin.Trip"/>
</EntityContainer>
...
```
