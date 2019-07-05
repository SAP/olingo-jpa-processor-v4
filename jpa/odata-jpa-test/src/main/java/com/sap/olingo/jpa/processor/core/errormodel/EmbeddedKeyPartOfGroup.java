/**
 * 
 */
package com.sap.olingo.jpa.processor.core.errormodel;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Version;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmVisibleFor;
import com.sap.olingo.jpa.processor.core.testmodel.CountryKey;

/**
 * @author Oliver Grande
 * Created: 29.06.2019
 *
 */
@Entity
public class EmbeddedKeyPartOfGroup {
  @Id
  @EdmVisibleFor("Person")
  @EmbeddedId
  private CountryKey key;

  @Version
  @Column(name = "\"ETag\"", nullable = false)
  protected long eTag;

  @ManyToMany
  @JoinTable(name = "\"Membership\"", schema = "\"OLINGO\"",
      joinColumns = @JoinColumn(name = "\"PersonID\""),
      inverseJoinColumns = @JoinColumn(name = "\"TeamID\""))
  private List<Team> teams;
}
