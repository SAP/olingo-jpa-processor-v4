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
 * A converter take a row returned from the database and calculates, based on its values, either one additional value or
 * a collection of values.<br>
 * The converter is mentioned in the {@link EdmTransient} annotation at a POJO attribute. E.g.:<p>
 * <code>
 * {@literal @}EdmTransient(requiredAttributes = { "lastName", "firstName" }, calculator = FullNameCalculator.class)<br>
 * {@literal @}Transient<br>
 * private String fullName;<br>
 * </code><p>
 *
 * An instance of the converter is created once per OData request. That is, it could be created multiple times per http
 * request in case of $batch requests. <p>
 * An implementing class may provide <b>one</b> constructor having no parameter or a combination of the following:
 * <ul>
 * <li>An Entity Manager: javax.persistence.EntityManager</li>
 * <li>All header: com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap</li>
 * <li>Access to own request parameter: com.sap.olingo.jpa.processor.core.api.JPAODataRequestParameterAccess</li>
 * </ul>
 * @author Oliver Grande<br>
 * Created: 14.03.2020
 *
 */
public interface EdmTransientPropertyCalculator<I> {
  /**
   * This method is called in case the transient property is a primitive, simple property.
   * @param row One row read from the database
   * @return Calculated value for the property
   */
  default @Nullable I calculateProperty(@Nonnull final Tuple row) {
    return null;
  }

  /**
   * This method is called in case the transient property is a collection property.
   * @param row One row read from the database
   * @return List of calculated values for the collection property
   */
  default @Nonnull List<I> calculateCollectionProperty(@Nonnull final Tuple row) {
    return Collections.emptyList();
  }
}
