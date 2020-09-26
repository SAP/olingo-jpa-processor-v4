# 4.5 Field Groups

It may occur that it is required that different user groups shall be able to access different subsets of properties of an entity types. Usually providing two entity types for the same database table/view can fullfil that requirement. In case of multiple user groups and a large set of attributes it is more handy to have just one entity type and mark those attributes that shall be accessible for members of an user groups. To do so the annotation `@EdmVisibleFor` can be used. The annotation takes a set of _group names_. With that the value of that attribute will only be returned in case at least one of the _group names_ is provided for a request. Otherwise null or an empty collection is returned. In case an attribute is not annotated, it will be provided every time the corresponding property is requested.

As an example we want to create a variant of the business partner:

```Java
package tutorial.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Version;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmVisibleFor;

@Entity
@Table(schema = "\"OLINGO\"", name = "\"BusinessPartner\"")
public class BusinessPartnerWithGroups {

  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @Version
  @Column(name = "\"ETag\"", nullable = false)
  protected long eTag;

  @Column(name = "\"Type\"", length = 1, insertable = false, updatable = false, nullable = false)
  private String type;

  @EdmVisibleFor("Company")
  @Column(name = "\"CreatedAt\"", precision = 3, insertable = false, updatable = false)
  private Timestamp creationDateTime;

  @EdmVisibleFor("Person")
  @Column(name = "\"Country\"", length = 4)
  private String country;

  @EdmVisibleFor("Company")
  @ElementCollection(fetch = FetchType.LAZY)
  @OrderColumn(name = "\"Order\"")
  @CollectionTable(schema = "\"OLINGO\"", name = "\"Comment\"",
      joinColumns = @JoinColumn(name = "\"BusinessPartnerID\""))
  @Column(name = "\"Text\"")
  private List<String> comment = new ArrayList<>();

  @EdmVisibleFor({ "Company", "Person" })
  @Embedded
  private PostalAddressData address = new PostalAddressData();
}
```

You should have noticed that we have assigned `creationDateTime`, `comment` and `address` to group _Company_ and `country`and `address` to group _Person_. 

Next we need to do is to assign user to groups. To keep it simple, we hard code the relation. In a productive case the relation is more likely stored on the database or is taken from a JWT.

The next code sipped, which we will place in our servlet, assigns _Marvin_ to _Person_ and _Willi_ to _Company_:

```Java
  private JPAODataGroupsProvider createGroups(final HttpServletRequest req) {
    final String auth = req.getHeader("Authorization");
    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    if (auth != null && !auth.isEmpty()) {
      final String[] authDetails = auth.split(" ");
      if (authDetails.length == 2 && authDetails[0].equals("Basic")) {
        final String[] baseAuth = new String(Base64.getDecoder().decode(authDetails[1]), StandardCharsets.UTF_8)
            .split(":");
        if ("Marvin".equals(baseAuth[0]))
          groups.addGroup("Person");
        else if ("Willi".equals(baseAuth[0]))
          groups.addGroup("Company");
      }
    }
    return groups;
  }
```

As the group assignment may change for each request, the groups have to be provided via the request context:

```Java
  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
      ...
      handler.getJPAODataRequestContext().setGroupsProvider(createGroups(req));
      handler.process(req, resp);
```

Now we are done with the Java  code and can start the service. Lets have a look the different results we get for:
_http://localhost:8080/Tutorial/Tutorial.svc/BusinessPartnerWithGroups('1')_.

First without providing a user via Basic Authentication:

```Json
{
  "@odata.context": "$metadata#Tutorial.BusinessPartnerWithGroup",
  "@odata.etag": "0",
  "Country": null,
  "Address": {
    "Country": null,
    "StreetName": null,
    "POBox": null,
    "CityName": null,
    "PostalCode": null,
    "HouseNumber": null,
    "RegionCodePublisher": null,
    "Region": null,
    "RegionCodeID": null
  },
  "ETag": 0,
  "Comment": [],
  "ID": "1",
  "Type": "2",
  "CreationDateTime": null
}
```

You can see that all properties that are assigned to a group are `null`.
In case we send _Marvin_  as user within the Basic Authentication header also the properties of group _Person_ are provided:

```Json
{
  "@odata.context": "$metadata#Tutorial.BusinessPartnerWithGroup",
  "@odata.etag": "0",
  "Country": "USA",
  "Address": {
    "Country": "USA",
    "StreetName": "Test Road",
    "POBox": "",
    "CityName": "Test City",
    "PostalCode": "94321",
    "HouseNumber": "23",
    "RegionCodePublisher": "ISO",
    "Region": "US-CA",
    "RegionCodeID": "3166-2"
  },
  "ETag": 0,
  "Comment": [],
  "ID": "1",
  "Type": "2",
  "CreationDateTime": null
}
```

For modifying requests the group names can be retrieved from the
 provided instance of [JPARequestEntity](../../../jpa/odata-jpa-processor/src/main/java/com/sap/olingo/jpa/processor/core/processor/JPARequestEntity.java) via method `getGroups`.

__Notes:__ The types of attributes that can be annotated with `@EdmVisibleFor` is restricted. Not allowed are:

* Key attributes
* Version attribute
* Mandatory attributes
* Associations
