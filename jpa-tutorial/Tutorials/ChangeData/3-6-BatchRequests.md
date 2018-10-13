# 3.6 Batch Requests

In the last tutorial we want to have a look at batch request and want to learn more about method `validateChanges`, which we have neglected up to now.

Again a set of additional AdministrativeDivisions shall be created, but instead of using a [Deep Insert](3-5-DeepInsert.md), as in the last tutorial, we want to us the following batch request:

URL: `.../Tutorial/Tutorial.svc/$batch`
Header:
- Content-Type: multipart/mixed;boundary=abc
- Prefer: return=minimal

Body:
```
--abc
Content-Type: multipart/mixed; boundary=xyz


--xyz
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-Id: 1
Prefer: return=minimal

POST AdministrativeDivisions HTTP/1.1
Accept: application/json
Content-Type: application/json

{
	"DivisionCode": "DE600",
	"CodeID": "NUTS3",
	"CodePublisher": "Eurostat",
	"CountryCode": "DEU",
	"Parent@odata.bind": "AdministrativeDivisions(DivisionCode='DE60',CodeID='NUTS2',CodePublisher='Eurostat')"
}

--xyz
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-Id: 2
Prefer: return=minimal

POST AdministrativeDivisions HTTP/1.1
Accept: application/json
Content-Type: application/json

{
	"DivisionCode": "DE60",
	"CodeID": "NUTS2",
	"CodePublisher": "Eurostat",
	"CountryCode": "DEU",
	"Parent@odata.bind": "AdministrativeDivisions(DivisionCode='DE6',CodeID='NUTS1',CodePublisher='Eurostat')"
}

--xyz
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-Id: 3
Prefer: return=minimal

POST AdministrativeDivisions HTTP/1.1
Accept: application/json
Content-Type: application/json

{
	"DivisionCode": "DE6",
	"CodeID": "NUTS1",
	"CodePublisher": "Eurostat",
	"CountryCode": "DEU"
}

--xyz--
--abc--
```
By purpose the oder of the AdministrativeDivisions is from bottom to top, so the links point to entities that do not exist when a AdministrativeDivision is created. As a consequence we would get an error if we now execute the request. A response may look like this:

```
--batch_7b9f8232-e51d-46b3-8b3d-74d06b02477b
Content-Type: application/http
Content-Transfer-Encoding: binary

HTTP/1.1 500 Internal Server Error
OData-Version: 4.0
Content-Type: application/json;odata.metadata=minimal
Content-Length: 158

{"error":{"code":null,"message":"while trying to invoke the method java.lang.Object.getClass() of a null object loaded from local variable 'targetInstance'"}}
--batch_7b9f8232-e51d-46b3-8b3d-74d06b02477b--
```

The processing of the request aborts, as the link can not be created. To solve the problem we need to implement, as mentioned, `validateChanges` and create the links there. To be able to do so, we have to buffer the entities to process. The buffer shall be called `entityBuffer` and will be created within the constructor:
```Java
private final Map<Object, JPARequestEntity> entityBuffer;

public CUDRequestHandler() {
  entityBuffer = new HashMap<Object, JPARequestEntity>();
}
```
When ever an entity was changed we need to put it into the buffer. As an example this is done at `createOneEntity`:
```Java
private Object createOneEntity(final JPARequestEntity requestEntity, final EntityManager em)
    throws ODataJPAProcessorException {
  final Object instance = createInstance(getConstructor(requestEntity.getEntityType()));
  requestEntity.getModifyUtil().setAttributesDeep(requestEntity.getData(), instance);
  em.persist(instance);
  entityBuffer.put(instance, requestEntity);
  return instance;
}
```
Having done that we can implement `validateChanges`:
```Java
@Override
public void validateChanges(EntityManager em) throws ODataJPAProcessException {
  for (Entry<Object, JPARequestEntity> entity : entityBuffer.entrySet()) {
    processBindingLinks(entity.getValue().getRelationLinks(), entity.getKey(), entity.getValue().getModifyUtil(), em);
  }
}
```
And remove the call of `processBindingLinks` from `createEntity`:

```Java
@Override
public Object createEntity(final JPARequestEntity requestEntity, final EntityManager em)
    throws ODataJPAProcessException {

  final Object instance = createOneEntity(requestEntity, em);
  processRelatedEntities(requestEntity.getRelatedEntities(), requestEntity, instance, requestEntity.getModifyUtil(),
      em);
  return instance;
}
```
If now the request is rerun, we get a success response:
```
--batch_cdfc9535-29e1-4b91-9bb0-f851cf4248a4
Content-Type: multipart/mixed; boundary=changeset_7831e243-f658-48e1-bbdd-efbc4209d5ec

--changeset_7831e243-f658-48e1-bbdd-efbc4209d5ec
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-ID: 1

HTTP/1.1 201 Created
OData-Version: 4.0
Content-Type: application/json;odata.metadata=minimal
Location: <Host:Port>/Tutorial/Tutorial.svc/AdministrativeDivisions(DivisionCode='DE600',CodeID='NUTS3',CodePublisher='Eurostat')
Content-Length: 240
...
```

Beside the linkage of entities the purpose of `validateChanges` is to give the opportunity to check the state of an entity in the context of other changed entities. E.g. you want to ensure that the certain value does not exceeds a limit or range given by another entity, but your should be able to change both with the same request.

Since OData version 3 an insert request has to contain a Content-Id. Up to now we had not utilized it. The following example should give a hint how to use it:

```
--abc
Content-Type: multipart/mixed; boundary=xyz


--xyz
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-Id: 1
Prefer: return=minimal

POST AdministrativeDivisions HTTP/1.1
Accept: application/json
Content-Type: application/json

{
	"DivisionCode": "DEF",
	"CodeID": "NUTS1",
	"CodePublisher": "Eurostat",
	"CountryCode": "DEU"
}

--xyz
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-Id: 2
Prefer: return=minimal

POST $1/Children HTTP/1.1
Accept: application/json
Content-Type: application/json

{
	"DivisionCode": "DEF0",
	"CodeID": "NUTS2",
	"CodePublisher": "Eurostat",
	"CountryCode": "DEU"
}

--xyz
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-Id: 3
Prefer: return=minimal

POST $2/Children HTTP/1.1
Accept: application/json
Content-Type: application/json

{
	"DivisionCode": "DEF0B",
	"CodeID": "NUTS3",
	"CodePublisher": "Eurostat",
	"CountryCode": "DEU"
}
--xyz--
--abc--
```
In this example we start with top down, so the first entity that is created is the parent of the next. The later on is created via a link. Nevertheless the POST is executed on `AdministrativeDivisions(DivisionCode='DEF',CodeID='NUTS1',CodePublisher='Eurostat')`, but it does not contain any properties of it. As a consequence when method `createEntity` is called `requestEntity.jpaAttributes` is empty, whereas `requestEntity.jpaKeys` is filled. To be able to handle such requests we have to touch `createEntity` again. In case the key of an entity is provided we won't create it, but read it from the database or from the buffer:

```Java
@Override
public Object createEntity(final JPARequestEntity requestEntity, final EntityManager em)
		throws ODataJPAProcessException {

	Object instance = null;

	if (requestEntity.getKeys().isEmpty()) {
		// POST an Entity
		instance = createOneEntity(requestEntity, em);
	} else {
		// POST on Link only
		instance = findEntity(requestEntity, em);
	}
	processRelatedEntities(requestEntity.getRelatedEntities(), instance, requestEntity.getModifyUtil(), em);
	return instance;
}
```

A small helper method shall return an existing entity:

```Java
private Object findEntity(final JPARequestEntity requestEntity, EntityManager em) throws ODataJPAProcessorException,
		ODataJPAInvocationTargetException {

	final Object key = requestEntity.getModifyUtil().createPrimaryKey(requestEntity.getEntityType(), requestEntity
			.getKeys(), requestEntity.getEntityType());
	return em.find(requestEntity.getEntityType().getTypeClass(), key);
}
```

Please note that error handling has been skipped and that the interface of method `processRelatedEntities` has refactored.

After we have executed the batch POST request, we can check if everthing went well with the following GET request:  `.../Tutorial/Tutorial.svc/AdministrativeDivisions?$filter=contains(DivisionCode, 'DEF')&$expand=Parent`
