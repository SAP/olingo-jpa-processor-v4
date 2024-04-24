/**
 *
 */
package com.sap.olingo.jpa.processor.core.errormodel;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

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

  public String getID() {
    return iD;
  }

  public void setID(final String iD) {
    this.iD = iD;
  }

  public long getETag() {
    return eTag;
  }

  public void setETag(final long eTag) {
    this.eTag = eTag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(eTag, iD);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof final KeyPartOfGroup other)

      return eTag == other.eTag && Objects.equals(iD, other.iD);
    return false;
  }

}
