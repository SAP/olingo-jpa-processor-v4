/**
 *
 */
package com.sap.olingo.jpa.processor.core.errormodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmVisibleFor;

/**
 * @author Oliver Grande
 * Created: 29.06.2019
 *
 */
@Entity
public class KeyPartOfGroup {
  @Id
  @EdmVisibleFor("Person")
  @Column(name = "\"ID\"")
  protected String iD;

  @Version
  @Column(name = "\"ETag\"", nullable = false)
  protected long eTag;

  public String getiD() {
    return iD;
  }

  public void setiD(final String iD) {
    this.iD = iD;
  }

  public long geteTag() {
    return eTag;
  }

  public void seteTag(final long eTag) {
    this.eTag = eTag;
  }

}
