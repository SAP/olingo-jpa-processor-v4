/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.annotation;

import javax.annotation.CheckForNull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

/**
 * Provides a set to methods to extend or influence the generated query.<p>
 *
 * An instance of the extension provider is created once per OData request. That is, it could be created multiple times
 * per http request in case of $batch requests. <p>
 * An implementing class may provide <b>one</b> constructor having no parameter or a combination of the following:
 * <ul>
 * <li>All header: com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap</li></li>
 * <li>Access to own request parameter: com.sap.olingo.jpa.metadata.api.JPARequestParameterMap</li>
 * </ul>
 *
 * <b>
 * This currently under construction. Method interfaces may change incompatible in the upcoming releases.
 * </b>
 * @author Oliver Grande
 * @since 1.0.3
 * 10.10.2021
 */
public interface EdmQueryExtensionProvider {

  /**
   * Provide an additional WHERE condition. This condition will be concatenated using AND
   */
  @CheckForNull
  Expression<Boolean> getFilterExtension(final CriteriaBuilder cb, final From<?, ?> from);
}
