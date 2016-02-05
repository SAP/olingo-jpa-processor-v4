package org.apache.olingo.jpa.metadata.core.edm.mapper.exception;

import org.apache.olingo.commons.api.ex.ODataException;

/*
 * Copied from org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException
 * See also org.apache.olingo.odata2.jpa.processor.core.exception.ODataJPAMessageServiceDefault
 */
public class ODataJPAModelException extends ODataException {
  /**
   * 
   */
  private static final long serialVersionUID = -7188499882306858747L;

  public static final String INVALID_ENTITY_TYPE = "INVALID_ENTITY_TYPE";
  public static final String INVALID_COMPLEX_TYPE = "INVLAID_COMPLEX_TYPE";
  public static final String INVALID_ASSOCIATION = "INVALID_ASSOCIATION";
  public static final String INVALID_ENTITYSET = "INVALID_ENTITYSET";
  public static final String INVALID_ENTITYCONTAINER = "INVALID_ENTITYCONTAINER";
  public static final String INVALID_ASSOCIATION_SET = "INVALID_ASSOCIATION_SET";
  public static final String INVALID_FUNC_IMPORT = "INVALID_FUNC_IMPORT";

  public static final String BUILDER_NULL = "BUILDER_NULL";
  public static final String TYPE_NOT_SUPPORTED = "TYPE_NOT_SUPPORTED";
  public static final String FUNC_ENTITYSET_EXP = "FUNC_ENTITYSET_EXP";
  public static final String FUNC_RETURN_TYPE_EXP = "FUNC_RETURN_TYPE_EXP";
  public static final String FUNC_RETURN_TYPE_ENTITY_NOT_FOUND = "FUNC_RETURN_TYPE_ENTITY_NOT_FOUND";
  public static final String GENERAL = "GENERAL";
  public static final String INNER_EXCEPTION = "INNER_EXCEPTION";
  public static final String FUNC_PARAM_NAME_EXP = "FUNC_PARAM_NAME_EXP";
  public static final String FUNC_PARAM_OUT_WRONG_TYPE = "FUNC_PARAM_OUT_WRONG_TYPE";
  public static final String FUNC_PARAM_OUT_MISSING = "FUNC_PARAM_OUT_MISSING";
  public static final String FUNC_PARAM_OUT_TO_MANY = "FUNC_PARAM_TO_MANY";
  public static final String REF_ATTRIBUTE_NOT_FOUND = "REF_ATTRIBUTE_NOT_FOUND";
  public static final String TYPE_MAPPER_COULD_NOT_INSANTIATE = "TYPE_MAPPER_COULD_NOT_INSANTIATE";
  public static final String NOT_SUPPORTED_EMBEDDED_KEY = "EMBEDDED_KEY_NOT_SUPPORTED";
  public static final String NOT_SUPPORTED_ATTRIBUTE_TYPE = "ATTRIBUTE_TYPE_NOT_SUPPORTED";
  public static final String DESCRIPTION_LOCALE_FIELD_MISSING = "DESCRIPTION_LOCALE_FIELD_MISSING";

  public static ODataJPAModelException throwException(final String id, final String message, final Throwable e) {

    // ODataJPAMessageService messageService;
    // messageService =
    // ODataJPAFactory.createFactory().getODataJPAAccessFactory().getODataJPAMessageService(DEFAULT_LOCALE);
    // String message = messageService.getLocalizedMessage(messageReference, e);
    return new ODataJPAModelException(id, message, e);
  }

  /**
   * The method creates an exception object of type ODataJPAModelException with localized error texts.
   * 
   * @param messageReference
   * is a <b>mandatory</b> parameter referring to a literal that could be translated to localized error
   * texts.
   * @param e
   * is an optional parameter representing the previous exception in the call stack
   * @return an instance of ODataJPAModelException which can be then raised.
   * @throws ODataJPARuntimeException
   */
  public static ODataJPAModelException throwException(final String message, final Throwable e) {

    // ODataJPAMessageService messageService;
    // messageService =
    // ODataJPAFactory.createFactory().getODataJPAAccessFactory().getODataJPAMessageService(DEFAULT_LOCALE);
    // String message = messageService.getLocalizedMessage(messageReference, e);
    return new ODataJPAModelException(message, e);
  }

  public static ODataJPAModelException throwException(final String id, String message) {
    return new ODataJPAModelException(message);
  }

  private String id;

  public ODataJPAModelException(String id, String localizedMessage, Throwable e) {
    super(localizedMessage, e);
    this.id = id;
  }

  private ODataJPAModelException(final String localizedMessage, final Throwable e) {
    super(localizedMessage, e);
  }

  private ODataJPAModelException(String message) {
    super(message);
  }

  public String getId() {
    return id;
  }

}
