package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

@Entity(name = "OneToManySource")
@Table(schema = "\"OLINGO\"", name = "\"AssociationOneToOneSource\"")
public class AssociationOneToManySource {

  @Id
  @Column(name = "\"ID\"")
  protected String key;

  /*
   * NOT SUPPORTED:
   * Association using default column name without a related association on target side. This requires a virtual Join
   * Table. The name of this table must be &lt;SourceEntity&gt;_&lt;TargetEntity&gt;. The table contains two columns.
   * The first one takes the source key, with the name &lt;SourceEntity&gt;_&lt;SourceKeyProperty&gt;. The second one
   * the target key and has the name &lt;TargetEntity&gt;_&lt;TargetKeyProperty&gt;. <br>
   * In case a join table is needed it has to be given explicit. Otherwise the association can also be modeled at the
   * foreign key table see {@link PostalAddressData}#AdministrativeDivision
   */

  /**
   * Association using default column name without a related association on target side.<br>
   * The default column name is defined e.g. in
   * <a href="https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a14945">JPA 3.0:
   * 11.1.25. JoinColumn Annotation</a>
   *
   */
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "defaultSource")
  private List<AssociationOneToManyTarget> defaultMappedTarget;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "ignoreSource")
  @EdmIgnore
  private List<AssociationOneToManyTarget> ignoreMappedSource;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "ignoreTarget")
  private List<AssociationOneToManyTarget> ignoreMappedTarget;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "ignoreTarget")
  @JoinColumn(name = "\"IGNORESOURCE_KEY\"", referencedColumnName = "\"ID\"")
  private List<AssociationOneToManyTarget> explicitColumnTarget;

}
