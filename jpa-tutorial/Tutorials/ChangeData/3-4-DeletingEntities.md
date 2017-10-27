# 3.4 Deleting Entities
After performing the tutorials [Creating Entities](3-2-CreatingEntities.md) and [Updating Entities](3-3-Updating Entities.md) implementing the delete should be straight forward.

The implementation starts with overriding `deleteEntity` of `CUDRequestHandler`. All we have to do now is to find the instance and remove it. As DELETE is idempotent we ignore if the instance does not exists. Please note that, as within the tutorials before, we neglect all kinds of checks.

```Java
@Override
public void deleteEntity(JPARequestEntity requestEntity, EntityManager em) throws ODataJPAProcessException {

  final Object instance = em.find(requestEntity.getEntityType().getTypeClass(),
      requestEntity.getModifyUtil().createPrimaryKey(requestEntity.getEntityType(), requestEntity.getKeys(),
          requestEntity.getEntityType()));
  if (instance != null)
    em.remove(instance);
}
```

Next see how we can create an entity together with related entities: [Tutorial 3.5 Deep Insert](3-5-DeepInsert.md)
