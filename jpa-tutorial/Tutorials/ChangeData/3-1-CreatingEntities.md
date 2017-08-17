# 3.1 Creating Entities
As the first modifying request we want to have a closer look at creating entities. _JPAODataCRUDHandler_ was designed to take over repetitive work like preparing the changes or creating a response depending on the request header. The business logic itself has to be implemented in a class that extends _JPAAbstractCUDRequestHandler_, we want to call it _CUDRequestHandler_, locate it in our new package _tutorial.modify_ and overwrite method _createEntity_. The method has three parameter.  
1. _et_ is an instance of _JPAEntityType_, which provides a bunch of information about the entity to be created. This starts with internal (name of POJO) and external name (name of OData entity) and ends with a list of attributes and keys.  
2. _jpaAttributes_ is a map of attributes that are provided by the request. Keys of the map are the attribute names of the POJO. In case of complex/embedded attributes jpaAttributes is deep meaning the attribute is an other map.  
3. _em_ is an instance of EntityManager. A transaction has already been started, which is done to ensure the transactional integrity required for change sets within $batch requests.  
The method shall returns an instance of the newly created POJO.  

So lets start creating a new AdministrativeDivision. As a first step we generate setter and getter methods. This can be done in Eclipse after opening _AdministrativeDivision.java_ by choosing _Alt+Shift+S_ and then _Generate Getters and Setters..._ select all others then _children_ and _parent_. Please note that all attributes should be typed with wrapper classes instead of primitive types.

Having done that we can start with a simple implementation of our create method. Here we will simply take the values out of the map and put them into a new POJO instance:

```Java
package tutorial.modify;

import java.util.Map;

import javax.persistence.EntityManager;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.api.JPAAbstractCUDRequestHandler;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;

import tutorial.model.AdministrativeDivision;

public class CUDRequestHandler extends JPAAbstractCUDRequestHandler {

	@Override
	public Object createEntity(JPAEntityType et, Map<String, Object> jpaAttributes, EntityManager em)
			throws ODataJPAProcessException {

		if (et.getExternalName().equals("AdministrativeDivision")) {
			AdministrativeDivision result = new AdministrativeDivision();

			result.setCodeID((String) jpaAttributes.get("codeID"));
			result.setCodePublisher((String) jpaAttributes.get("codePublisher"));
			result.setDivisionCode((String) jpaAttributes.get("divisionCode"));

			result.setCountryCode((String) jpaAttributes.get("countryCode"));
			result.setParentCodeID((String) jpaAttributes.get("parentCodeID"));
			result.setParentDivisionCode((String) jpaAttributes.get("parentDivisionCode"));

			result.setAlternativeCode((String) jpaAttributes.get("alternativeCode"));
			result.setArea((Integer) jpaAttributes.get("area"));
			result.setPopulation((Long) jpaAttributes.get("population"));
			
			em.persist(result);			
			return result;
		} else {
			return super.createEntity(et, jpaAttributes, em);
		}
	}

}
```  
As the last step before we can test our implementation, we have to make a small change at the Service implementation. Up to know we haven't provided the _JPAODataCRUDHandler_ which is our handler implementation. So we have to add _handler.getJPAODataContext().setCUDRequestHandler(new CUDRequestHandler());_:

```Java
	...
			JPAODataCRUDHandler handler = new JPAODataCRUDHandler(PUNIT_NAME,
					DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB));
			handler.getJPAODataContext().setCUDRequestHandler(new CUDRequestHandler());
			handler.process(req, resp);
		} catch (RuntimeException e) {
	...		
```  

Now we are ready to check our implementation with our Rest client (e.g Postman). If we would use e.g. the following JSON

    {
        "CodePublisher": "Eurostat",
        "CodeID": "NUTS1",
        "DivisionCode": "DE1",
        "CountryCode": "DEU"
    }
    
and send a POST, we should get the following response:  

![Result POST AdministrativeDivision](Images/CreateAdminDiv.png)    

Next you can try the following:

    {  
        "CodePublisher": "Eurostat",  
        "CodeID": "NUTS2",  
        "DivisionCode": "DE11",  
        "CountryCode": "DEU",  
        "ParentDivisionCode": "DE1",  
        "ParentCodeID": "NUTS2"  
    }  

If we want to play around with other entities we could go ahead the same approach as a above, so manually create an instance of the POJO and fill it step by step, which would get boring. We want to do some more generic stuff.  

__Please note__ that we have used a simplified model for this tutorial where we map a database field one to one to a property in our API. This is not recommended, as this could make a database change to an API change.  

We want to create an instance base on the information of the entity type. The first step is to get the Constructors:

```Java
	private Constructor<?> getConstructor(JPAStructuredType st) {
		Constructor<?> cons = null;
		Constructor<?>[] constructors = st.getTypeClass().getConstructors();
		for (int i = 0; i < constructors.length; i++) {
			cons = constructors[i];
			if (cons.getParameterCount() == 0) {
				break;
			}
		}
		return cons;
	}
```
Which we can use to create the POJO instance:
```Java
	private Object createInstance(Constructor<?> cons) throws ODataJPAProcessorException {
		Object instance;
		try {
			instance = cons.newInstance();
		} catch (InstantiationException e) {
			throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
		} catch (IllegalAccessException e) {
			throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
		} catch (IllegalArgumentException e) {
			throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
		} catch (InvocationTargetException e) {
			throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
		}
		return instance;
	}
```
Next we would need to set the attributes. We want to use the generated getter to do that, so do not forget to generate getter and setter for every entity and embedded type you like to test:
```Java
	private Method findGetter(String attributeName, Method[] methods) {
		final String getterName = "get" + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);
		Method method = null;
		for (Method meth : methods) {
			if (getterName.equals(meth.getName())) {
				method = meth;
				break;
			}
		}
		return method;
	}
```
Now we are able to set the attributes. Here we have to respect embedded types:
```Java
	@SuppressWarnings("unchecked")
	private void setAttributes(JPAStructuredType st, Map<String, Object> jpaAttributes, Object instance)
			throws ODataJPAProcessorException {
		Method[] methods = instance.getClass().getMethods();
		for (Method meth : methods) {
			if (meth.getName().substring(0, 3).equals("set")) {
				String attributeName = meth.getName().substring(3, 4).toLowerCase() + meth.getName().substring(4);
				Object value = jpaAttributes.get(attributeName);
				if (value != null) {
					try {
						if (value instanceof Map<?, ?>) {
							final JPAAttribute jpaEmbedded = st.getAttribute(attributeName);
							Method getter = findGetter(attributeName, methods);
							Object subInstance = null;
							if (getter != null) {
								subInstance = getter.invoke(instance);
								if (subInstance == null) {
									subInstance = createPOJO(jpaEmbedded.getStructuredType(),
											(Map<String, Object>) value);
								} else {
									setAttributes(jpaEmbedded.getStructuredType(), (Map<String, Object>) value, subInstance);
								}
							} else
								subInstance = createPOJO(jpaEmbedded.getStructuredType(), (Map<String, Object>) value);
							meth.invoke(instance, subInstance);
						} else
							meth.invoke(instance, value);
					} catch (IllegalAccessException e) {
						throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
					} catch (IllegalArgumentException e) {
						throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
					} catch (InvocationTargetException e) {
						throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
					} catch (ODataJPAModelException e) {
						throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
					}
				} else {
					try {
						final JPAAttribute attribute = st.getAttribute(attributeName);
						if (attribute != null && attribute.isComplex() && attribute.isKey()) {
							meth.invoke(instance, createPOJO(attribute.getStructuredType(), jpaAttributes));
						}
					} catch (ODataJPAModelException e) {
						throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
					} catch (IllegalAccessException e) {
						throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
					} catch (IllegalArgumentException e) {
						throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
					} catch (InvocationTargetException e) {
						throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
					}
				}
			}
		}
	}
```	

As the last step before we re-factor createEntity we create a helper method to create the instance:
```Java
	private Object createPOJO(JPAStructuredType st, Map<String, Object> jpaAttributes)
			throws ODataJPAProcessorException {
		Constructor<?> cons = getConstructor(st);
		Object instance = createInstance(cons);
		setAttributes(st, jpaAttributes, instance);
		return instance;
	}
```

Now we can replace the old implementation of createEntity with the following code:
```Java
	...
		Object instance = createPOJO(et, jpaAttributes);
		em.persist(instance);
		return instance;	
	...
```
Now lets create a new Person:

    {
        "ID" : "A34",
        "Country" : "CHE",
        "BirthDay": "1992-02-10",
        "LastName": "Müller",
        "FirstName": "Frida",  
        "Address" : {
            "Country" : "DEU",
            "StreetName": "Test Starße",
            "CityName": "Berlin",
            "PostalCode": "10116",
            "HouseNumber": "23",
            "RegionCodePublisher": "ISO",
            "Region": "DE-BE",
            "RegionCodeID": "3166-2"    
        },
        "AdministrativeInformation": {
            "Created": {
                "At": "2016-12-20T07:21:23Z",
                "By": "99"
            },
            "Updated": {
                "At": null,
                "By": ""
            }
        }
    }

Next we want to be able to make change to an existing entity: [Tutorial 3.2 Updating Entities](3.2 Updating Entities.md)