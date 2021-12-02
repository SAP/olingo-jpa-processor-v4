package com.sap.olingo.jpa.metadata.core.edm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to mark an jpa entity as an additional entity set of another entity type.
 * This is only allowed for leafs in an inheritance hierarchy. The jpa entity must not have own columns<p>
 * <a href=
 * "https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752596"
 * >OData Version 4.0 Part 3 - 13 Entity Container Example 30</a>
 * @deprecated (since 1.0.3, replace with {@link EdmEntityType}, deleted with 1.1.0 )
 */
@Deprecated
@Target({ ElementType.TYPE })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface EdmAsEntitySet {

}
