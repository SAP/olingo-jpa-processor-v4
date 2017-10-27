# 3.5 Deep Insert

OData provides the option to create entities together with related entities or to link them to other entities in one POST request, which is also called deep insert.
## Related Entity
Let's assume we want to start to create also AdministrativeDivisions based on [Nomenclature of Territorial Units for Statistics](https://en.wikipedia.org/wiki/Nomenclature_of_Territorial_Units_for_Statistics) (NUTS) for Germany. One option is to create all levels step by step. The other option is to create e.g. one unit including the sub units with one request. The payload to create NUTS1, NUTS2 and NUTS3 for DE5 would look as follows:

```
{
	"DivisionCode": "DE5",
	"CodeID": "NUTS1",
	"CodePublisher": "Eurostat",
	"CountryCode": "DEU",
	"Children": [{
		"DivisionCode": "DE50",
		"CodeID": "NUTS2",
		"CodePublisher": "Eurostat",
		"CountryCode": "DEU",
		"Children": [{
			"DivisionCode": "DE501",
			"CodeID": "NUTS3",
			"CodePublisher": "Eurostat",
			"CountryCode": "DEU"
		},
		{
			"DivisionCode": "DE502",
			"CodeID": "NUTS3",
			"CodePublisher": "Eurostat",
			"CountryCode": "DEU"
		}]
	}]
}
```
Processing such a request means that we have to create entities over and over again, so we extract the corresponding code from `createEntity`:

```Java
private Object createOneEntity(final JPARequestEntity requestEntity, final EntityManager em)
		throws ODataJPAProcessException {
	final Object instance = createInstance(getConstructor(requestEntity.getEntityType()));
	requestEntity.getModifyUtil().setAttributesDeep(requestEntity.getData(), instance, requestEntity.getEntityType());
	em.persist(instance);
	entityBuffer.put(instance, requestEntity);
	return instance;
}
```

Having done that we need to process the related entities, so NUTS2 and NUTS3 level in our example. These are provided via `requestEntity.getRelatedEntities()` grouped by the association: `Map<JPAAssociationPath, List<JPARequestEntity>>`. We had defined, at least for AdministrativeDivision, that the relation is created via the associations and not via the properties, see [Navigation Properties And Complex Types](1-6-NavigationAndComplexTypes.md). This allows us to use method `linkEntities` of `JPAModifyUtil` to create it. As we do not want to think to much about which association we have to deal with, we set the relation on both sides. Last but not least we shall not forget that we have to process the levels recursively.
```Java
private void processRelatedEntities(final Map<JPAAssociationPath, List<JPARequestEntity>> relatedEntities,
		final JPARequestEntity parent, final Object parentInstance, final JPAModifyUtil util, final EntityManager em)
		throws ODataJPAProcessException {

	for (final Map.Entry<JPAAssociationPath, List<JPARequestEntity>> entity : relatedEntities.entrySet()) {
		final JPAAssociationPath pathInfo = entity.getKey();
		for (final JPARequestEntity requestEntity : entity.getValue()) {
			final Object newInstance = createOneEntity(requestEntity, em);
			util.linkEntities(parentInstance, newInstance, pathInfo);
			if (pathInfo.getPartner() != null) {
				try {
					util.linkEntities(newInstance, parentInstance, pathInfo.getPartner().getPath());
				} catch (ODataJPAModelException e) {
					throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
				}
			}
			util.linkEntities(parentInstance, newInstance, pathInfo);
			processRelatedEntities(requestEntity.getRelatedEntities(), requestEntity, newInstance, util, em);
		}
	}
}
```
Last but not least we have to change the implementation of `createEntity`:
```Java
@Override
public Object createEntity(final JPARequestEntity requestEntity, final EntityManager em)
    throws ODataJPAProcessException {

  final Object instance = createOneEntity(requestEntity, em);
  processRelatedEntities(requestEntity.getRelatedEntities(), requestEntity, instance, em);
  return instance;
}
```
If we now perform a POST on `.../Tutorial/Tutorial.svc/AdministrativeDivisions` using the payload from above, we create four new entries on the database. We can find them using the following GET request:
`.../Tutorial/Tutorial.svc/AdministrativeDivisions?$filter=CountryCode eq 'DEU' and CodePublisher eq 'Eurostat'&$format=json`
## Binding Links
As already mentioned there is another way to create a new entity together with a link to another entity. To demonstrate this want to create a sub division and link it to its parent:

```
{
	"DivisionCode": "04011000",
	"CodeID": "LAU2",
	"CodePublisher": "Eurostat",
	"CountryCode": "DEU",
	"Parent@odata.bind": "AdministrativeDivisions(DivisionCode='DE501',CodeID='NUTS3',CodePublisher='Eurostat')"
}
```
The links are provided via `requestEntity.getRelationLinks()`, which returns a map similar to that we had for related entities `Map<JPAAssociationPath, List<JPARequestLink>>`. In opposite to what we did before we have to find the link target first and then link both entities:

```Java
private void processBindingLinks(final Map<JPAAssociationPath, List<JPARequestLink>> relationLinks,
		final Object instance, final JPAModifyUtil util, final EntityManager em) throws ODataJPAProcessException {

	for (final Entry<JPAAssociationPath, List<JPARequestLink>> entity : relationLinks.entrySet()) {
		final JPAAssociationPath pathInfo = entity.getKey();
		for (final JPARequestLink requestLink : entity.getValue()) {
			final Object targetKey = util.createPrimaryKey((JPAEntityType) pathInfo.getTargetType(), requestLink
					.getRelatedKeys(), (JPAEntityType) pathInfo.getSourceType());
			final Object target = em.find(pathInfo.getTargetType().getTypeClass(), targetKey);
			util.linkEntities(instance, target, pathInfo);
		}
	}
}
```
Please note that here the processing of the partner association was skipped, but it should not be to hard to add that.
As a last step we need to call `processBindingLinks`:

```Java
@Override
public Object createEntity(final JPARequestEntity requestEntity, final EntityManager em)
    throws ODataJPAProcessException {

  final Object instance = createOneEntity(requestEntity, em);
  processRelatedEntities(requestEntity.getRelatedEntities(), requestEntity, instance, requestEntity.getModifyUtil(),
      em);
  processBindingLinks(requestEntity.getRelationLinks(), instance, requestEntity.getModifyUtil(), em);
  return instance;
}

```
The last tutorial shall show how batch processing is supported : [Tutorial 3.6 Batch Requests](3-6-BatchRequests.md)
