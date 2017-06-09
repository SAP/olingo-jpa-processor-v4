package com.sap.olingo.jpa.metadata.core.edm.mapper.exception;

/*
 * Copied from org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException
 * See also org.apache.olingo.odata2.jpa.processor.core.exception.ODataJPAMessageServiceDefault
 */
public class ODataJPAModelException extends ODataJPAException {
  /**
   * 
   */
  private static final long serialVersionUID = -7188499882306858747L;

  public static enum MessageKeys implements ODataJPAMessageKey {
    INVALID_ENTITY_TYPE,
    INVALID_COMPLEX_TYPE,
    INVALID_ASSOCIATION,
    INVALID_ENTITY_SET,
    INVALID_ENTITY_CONTAINER,
    INVALID_ASSOCIATION_SET,
    INVALID_FUNC_IMPORT,
    INVALID_DESCIPTION_PROPERTY,

    BUILDER_NULL,
    TYPE_NOT_SUPPORTED,
    FUNC_ENTITY_SET_EXP,
    FUNC_RETURN_TYPE_EXP,
    FUNC_RETURN_TYPE_ENTITY_NOT_FOUND,
    FUNC_RETURN_TYPE_UNKNOWN,
    FUNC_RETURN_TYPE_INVALID,
    GENERAL,
    INNER_EXCEPTION,
    FUNC_PARAM_NAME_EXP,
    FUNC_PARAM_ANNOTATION_MISSING,
    FUNC_PARAM_OUT_WRONG_TYPE,
    FUNC_PARAM_OUT_MISSING,
    FUNC_PARAM_OUT_TO_MANY,
    FUNC_PARAM_ONLY_PRIMITIVE,
    FUNC_CONSTRUCTOR_MISSING,

    REF_ATTRIBUTE_NOT_FOUND,
    TYPE_MAPPER_COULD_NOT_INSANTIATE,
    NOT_SUPPORTED_EMBEDDED_KEY,
    NOT_SUPPORTED_ATTRIBUTE_TYPE,
    NOT_SUPPORTED_NO_IMPLICIT_COLUMNS,
    NOT_SUPPORTED_EMBEDDED_STREAM,

    DESCRIPTION_LOCALE_FIELD_MISSING,
    DESCRIPTION_ANNOTATION_MISSING,
    DESCRIPTION_FIELD_WRONG_TYPE,

    PROPERTY_DEFAULT_ERROR,
    PROPERTY_MISSING_PRECISION,
    REFERENCED_PROPERTY_NOT_FOUND,
    INHERITANCE_NOT_ALLOWED,
    TO_MANY_STREAMS,
    ANNOTATION_STREAM_INCOMPLETE,
    ANNOTATION_PARSE_ERROR,
    ODATA_ANNOTATION_TWO_EXPRESSIONS,
    NAVI_PROPERTY_NOT_FOUND,
    ON_LEFT_ATTRIBUTE_NULL,
    ON_RIGHT_ATTRIBUTE_NULL,
    PATH_ELEMENT_NOT_FOUND;

    @Override
    public String getKey() {
      return name();
    }

  }

  private static final String BUNDEL_NAME = "metadata-exceptions-i18n";

  public ODataJPAModelException(final Throwable e) {
    super(e);
  }

  public ODataJPAModelException(final MessageKeys messageKey, final Throwable e, final String... params) {
    super(messageKey.name(), e, params);
  }

  public ODataJPAModelException(final MessageKeys messageKey) {
    super(messageKey.getKey());
  }

  public ODataJPAModelException(final MessageKeys messageKey, final String... params) {
    super(messageKey.getKey(), params);
  }

  public ODataJPAModelException(final MessageKeys messageKey, final Throwable e) {
    super(messageKey.getKey(), e);
  }

  @Override
  protected String getBundleName() {
    return BUNDEL_NAME;
  }

  public String getId() {
    return id;
  }
}
