package com.sap.olingo.jpa.metadata.core.edm.mapper.extension;

/**
 * Tag Interface to indicate that a java class represents an OData <a href =
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398010">
 * Function.</a>
 * <p>
 * An instance of the converter is created once per OData request. That is, it could be created multiple times per http
 * request in case of $batch requests.
 * <p>
 * An implementing class may provide <b>one</b> constructor having no parameter or a combination of the following:
 * <ul>
 * <li>An Entity Manager: javax.persistence.EntityManager</li>
 * <li>All header: com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap</li>
 * <li>Access to own request parameter: com.sap.olingo.jpa.metadata.api.JPARequestParameterMap</li>
 * <li></li>
 * </ul>
 *
 * @author Oliver Grande
 *
 */
public interface ODataFunction extends ODataOperation {

}
