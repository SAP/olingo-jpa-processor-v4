# 3.3 Updating Entities
We had a look at [Creating Entities](3-2-CreatingEntities.md) in the last tutorial, know we want to have a look at update. As update we want to consider all operations that changes a property value. This is broader than the definition in OData, as it not only considers updates of entities, complex types, primitive types or the raw values , but also setting values to `null` via a delete requests.

To implement updates we have to override `updateEntity`. Again we do not look at `validateChange`. `updateEntity` has three parameter:

1. _requestEntity_ is a container that provides access to data and information about a request. Form interest are:
	1. `getEntityType`, which provides an instance of _JPAEntityType_, which provides a bunch of information about the entity to be created. This starts with internal name of the JPA POJO and external name (name of OData entity) and ends with a list of attributes and keys.
	2. `getData`, which provides a map of attributes that are provided by the request. Keys of the map are the attribute names of the POJO. In case of complex/embedded attributes map is deep meaning the attribute is a map.
	3. `getModifyUtil`, which provides an instance of `JPAModifyUtil`, which contains of some helper methods.
	4. `getKeys`, which provides a map with the keys from the request.
2. _em_ is an instance of `EntityManager`. A transaction has already been started, which is done to ensure the transactional integrity required for change sets within batch requests.
3. _method_ contains the http method that was used.

For the tutorial we do not accept a PUT, which makes our live a little bit easier. To perform a PATCH or a DELETE setting values to `null`, the first step is to get the existing version of the entity. To do so we want to use the `find` method of the `EntityManager`, this requires to create an instance of the primary key. `JPAModifyUtil` provides with `createPrimaryKey` a helper method to support this. The method supports _Id Classes_ having the corresponding setter as well as single keys. Next step is to insert the new values into the found instance. After that we can return an instance of `JPAUpdateResult`.  `JPAUpdateResult` has two fields:
1. `wasCreate`, which indicates if a new instance was created if upsert is supported.
2. `modifyedEntity`, the changed (or in case of upsert the created) entity or a map of manipulated attributes.

Putting that together `updateEntity` can look as follows:
```Java
@Override
public JPAUpdateResult updateEntity(final JPARequestEntity requestEntity, final EntityManager em,
		final HttpMethod method) throws ODataJPAProcessException {
		
    if (method == HttpMethod.PATCH || method == HttpMethod.DELETE) {
      final Object instance = em.find(requestEntity.getEntityType().getTypeClass(), requestEntity.getModifyUtil()
          .createPrimaryKey(requestEntity.getEntityType(), requestEntity.getKeys(), requestEntity.getEntityType()));
      requestEntity.getModifyUtil().setAttributesDeep(requestEntity.getData(), instance, requestEntity.getEntityType());
      return new JPAUpdateResult(false, instance);
    }
    return super.updateEntity(requestEntity, em, method);
}
```
To test the PATCH we can start with changing the instance we created during the last tutorial using the following request:
```
...Tutorial/Tutorial.svc/AdministrativeDivisions(DivisionCode='DE1',CodeID='NUTS1',CodePublisher='Eurostat')
{
        "AlternativeCode": "Test"
}
```
Now we have an AlternativeCode, which does not really make sense, so lets get ride of it again by the following DELETE request:
`.../Tutorial/Tutorial.svc/AdministrativeDivisions(DivisionCode='DE1',CodeID='NUTS1',CodePublisher='Eurostat')/AlternativeCode`

Next we want to delete an entity: [Tutorial 3.4 Deleting Entities](3-4-DeletingEntities.md)