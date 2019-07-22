package com.sap.olingo.jpa.processor.core.query;

/**
 * The exception shall be raised in case no selection left, so it is not necessary to perform a query. <br>
 * It is expected that the exception is handled internally.
 * 
 * @author Oliver Grande
 * Created: 14.07.2019
 *
 */
public class JPANoSelectionException extends Exception {

  private static final long serialVersionUID = -2120984389807283569L;

}
