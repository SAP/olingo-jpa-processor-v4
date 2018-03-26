package com.sap.olingo.jpa.metadata.core.edm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;

/**
 * Can be used to annotate attributes. The annotations are converted into OData annotations.
 * For details see
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752630"
 * >OData Version 4.0 Part 3 - 14.3 Element edm:Annotation</a>
 * 
 * @author Oliver Grande
 *
 */
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface EdmAnnotation {

  /**
   * Full qualified name of term, e.g. Core.MediaType
   */
  String term();

  /**
   * An annotation element MAY provide a SimpleIdentifier value for the Qualifier attribute.
   * The qualifier attribute allows annotation authors a means of conditionally applying an annotation.
   */
  String qualifier() default "";

  ConstantExpression constantExpression() default @ConstantExpression(type = ConstantExpressionType.Int,
      value = "default");

  DynamicExpression dynamicExpression() default @DynamicExpression();

  @interface ConstantExpression {
    ConstantExpressionType type();

    String value() default "";
  }

  @interface DynamicExpression {
    String path() default "";
  }
}
