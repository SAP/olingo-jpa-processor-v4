/**
 * 
 */
package com.sap.olingo.jpa.metadata.core.edm.annotation;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Tuple;

/**
 * Transient property converter.<p>
 * The converter provides the
 * An instance of the converter is created once per OData request. That is it could be created multiple times per http
 * request in case of a $batch request. <p>
 * An implementing class may provide <b>one</b> constructor having no parameter or a combination of the following:
 * <ul>
 * <li>An Entity Manager: javax.persistence.EntityManager</li>
 * <li>All header: java.util.Map<String, List<String>></li>
 * </ul>
 * @author Oliver Grande<br>
 * Created: 14.03.2020
 *
 */
public interface EdmTransientPropertyCalculator<I> {
  /**
   * This method is called in case the transient property is a primitive, simple property.
   * @param row one row read from the database
   * @return calculated value for the property
   */
  default @Nullable I calculateProperty(@Nonnull final Tuple row) {
    return null;
  }

  /**
   * This method is called in case the transient property is a collection property.
   * @param row one row read from the database
   * @return list of calculated values for the collection property
   */
  default @Nonnull List<I> calculateCollectionProperty(@Nonnull final Tuple row) {
    return Collections.emptyList();
  }
}
