# 1.13 Collection Properties
If you like to express a composition relationship between an entity type and another type, OData provides the option to create collections of complex or primitive types. As a small example we want to allow our users to comment companies. So a comment belongs exactly to one company and shall be deleted if the company gets deleted.    
As JPA provides an analog concept with the annotation `@ElementCollection` and `@CollectionTable` we use those to realize the new requirement. We extend Company as follows:
```Java
public class Company extends BusinessPartner {

  @Column(name = "\"NameLine1\"")
  private String name1;

  @Column(name = "\"NameLine2\"")
  private String name2;

  @Enumerated(value = EnumType.STRING)
  @Column(name = "\"ABCClass\"")
  private ABCClassifiaction abcClass;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(schema = "\"OLINGO\"", name = "\"Comment\"",
      joinColumns = @JoinColumn(name = "\"BusinessPartnerID\""))
  @Column(name = "\"Text\"")
  private List<String> comment = new ArrayList<>();
  
  ...

}
```
The resulting metadata will look as follows:
```XML
	<EntityType Name="Company" BaseType="Tutorial.BusinessPartner">
		<Property Name="AbcClass" Type="Tutorial.ABCClassifiaction"/>
		<Property Name="Comment" Type="Collection(Edm.String)" MaxLength="255"/>
		<Property Name="Name2" Type="Edm.String" MaxLength="255"/>
		<Property Name="Name1" Type="Edm.String" MaxLength="255"/>
	</EntityType>
```

If we want to be able to filter on the collection attribute (e.g. with `http://localhost:8080/Tutorial/Tutorial.svc/Companies?$filter=Comment/$count%20gt%200`), for technical reasons, we also need a POJO. This shall not be part of the API, so we make it with `@EdmIgnore`:
```Java
package tutorial.model;

import java.sql.Clob;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

@EdmIgnore
@Entity
@Table(schema = "\"OLINGO\"", name = "\"Comment\"")
public class Comment {

  @Id
  @Column(name = "\"BusinessPartnerID\"")
  private String businessPartnerID;

  @Column(name = "\"Order\"")
  private String order;

  @Lob
  @Column(name = "\"Text\"")
  @Basic(fetch = FetchType.LAZY)
  private Clob text;

  public Comment() {
    super();
  }

  public String getBusinessPartnerID() {
    return this.businessPartnerID;
  }

  public void setID(String ID) {
    this.businessPartnerID = ID;
  }
}
``` 

Not supported are nested collection properties as well as navigation properties being part of a collection.