package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "OneToManyTarget")
@Table(schema = "\"OLINGO\"", name = "\"AssociationOneToManyTarget\"")
public class AssociationOneToManyTarget {

  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @Column(name = "source_key")
  protected String sourceKey;

  /**
   * Implicit join column.
   * The default column name is defined for JPA 2.2 in
   * <a href="https://download.oracle.com/otn-pub/jcp/persistence-2_2-mrel-spec/JavaPersistence.pdf">11.1.25 JoinColumn
   * Annotation<a>:
   * <p>
   * <i>The concatenation of the following: the name of the referencing relationship property or field of the
   * referencing entity or embeddable class; "_"; the name of the referenced primary key column. ...</i>
   * <p>
   * Starting with Jarkata the naming convention changes, as described in
   * <a href="https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a14945">JPA 3.0:
   * 11.1.25. JoinColumn Annotation</a>:
   * <p>
   * <i>The concatenation of the following: the name of the referencing relationship property or field of the
   * referencing entity or embeddable class; ""; the name of the referenced primary key column. ...</i>
   * <p>
   *
   * The name has to be here &lt;SourceEntity&gt;_&lt;SourceKeyProperty&gt; so: [DEFAULTSOURCE_KEY]
   */
  @ManyToOne(fetch = FetchType.LAZY)
  private AssociationOneToManySource defaultSource;

}
