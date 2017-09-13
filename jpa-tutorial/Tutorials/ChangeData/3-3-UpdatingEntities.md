# 3.2 Updating Entities
We had a look at [Creating Entities](3-2-CreatingEntities.md) in the last tutorial, we know want to have a look at update. As Update we want to consider all operations that changes a property value. This is broader than the defintion in OData. That is why not only the update of an entity, a complex type, a primitive type or the raw value of a primitive type, but also set values to `null` via a delete request.

To implement updates we have to override `updateEntity`. Again we do not look at `validateChange`. `updateEntity has three




PUT or PATCH

PUT primitive type equivalent to a PATCH on an entity type setting just that property
PATCH on a complex type semantically to PATCH on the an entity type manipulating corresponding property.
PUT on a complex type is different and cannot be matched to a PATCH on the entity type.

C:\Users\D023143\git\olingo-jpa-processor-v4\jpa-tutorial
