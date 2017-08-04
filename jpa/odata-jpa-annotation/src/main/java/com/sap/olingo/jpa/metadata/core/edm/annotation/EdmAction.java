package com.sap.olingo.jpa.metadata.core.edm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface EdmAction {

  /**
   * Defines the name of the action in the service document
   * @return
   */
  String name();

  /**
   * Define the return type of this action
   * 
   * @return return type of this action
   */
  ReturnType returnType();

  /**
   * Indicates that the action is bound. <p>
   * If isBound is false an action is treated as <i>unbound</i>, so it can be accessed either via an Action Import or
   * be used in <i>filter</i> or <i>orderby</i> expression. Otherwise the function is treated as bound.
   * For details see:
   * <a href =
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398013"
   * />OData Version 4.0 Part 3 - 12.2.2 Attribute IsBound</a> <p>
   * <b>If the function is java based isBound is ignored and always set to false</b>
   * @return
   */
  boolean isBound() default true;

  EdmFunctionParameter[] parameter() default {};
}
