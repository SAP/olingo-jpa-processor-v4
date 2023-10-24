/**
 *
 */
package com.sap.olingo.jpa.processor.core.errormodel;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmVisibleFor;
import com.sap.olingo.jpa.processor.core.testmodel.CountryKey;

/**
 * @author Oliver Grande
 * Created: 29.06.2019
 *
 */
@Entity
public class EmbeddedKeyPartOfGroup {

  @EdmVisibleFor("Person")
  @EmbeddedId
  private CountryKey key;

  @Version
  @Column(name = "\"ETag\"", nullable = false)
  protected long eTag;

}
