/**
 *
 */
package com.sap.olingo.jpa.processor.core.errormodel;

import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmVisibleFor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Version;

/**
 * @author Oliver Grande
 * Created: 29.06.2019
 *
 */
@Entity()
public class MandatoryPartOfGroup {
  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @Version
  @EdmVisibleFor("Person")
  @Column(name = "\"ETag\"", nullable = false)
  protected long eTag;

  @ManyToMany
  @JoinTable(name = "\"Membership\"", schema = "\"OLINGO\"",
      joinColumns = @JoinColumn(name = "\"PersonID\""),
      inverseJoinColumns = @JoinColumn(name = "\"TeamID\""))
  private List<Team> teams;
}
