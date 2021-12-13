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

  public enum MessageKeys implements ODataJPAMessageKey {

    INVALID_DESCRIPTION_PROPERTY,
    INVALID_COLLECTION_TYPE,
    INVALID_TOP_LEVEL_SETTING,

    TYPE_NOT_SUPPORTED,
    FUNC_UNBOUND_ENTITY_SET,
    FUNC_RETURN_TYPE_ENTITY_SET,
    FUNC_RETURN_TYPE_EXP,
    FUNC_RETURN_TYPE_UNKNOWN,
    FUNC_RETURN_TYPE_INVALID,
    FUNC_RETURN_NOT_SUPPORTED,
    FUNC_PARAM_ANNOTATION_MISSING,

    FUNC_PARAM_ONLY_PRIMITIVE,
    FUNC_CONV_ERROR,
    FUNC_CONSTRUCTOR_MISSING,
    ACTION_RETURN_TYPE_EXP,
    ACTION_PARAM_ANNOTATION_MISSING,
    ACTION_PARAM_ONLY_PRIMITIVE,
    ACTION_PARAM_BINDING_NOT_FOUND,
    ACTION_UNBOUND_ENTITY_SET,
    ENUMERATION_ANNOTATION_MISSING,
    ENUMERATION_UNSUPPORTED_TYPE,
    ENUMERATION_NO_NEGATIVE_VALUE,
    ENUMERATION_UNKNOWN,

    TYPE_MAPPER_COULD_NOT_INSTANTIATED,
    JOIN_TABLE_NOT_FOUND,
    NOT_SUPPORTED_ATTRIBUTE_TYPE,
    NOT_SUPPORTED_NO_IMPLICIT_COLUMNS,
    NOT_SUPPORTED_NO_IMPLICIT_COLUMNS_COMPLEX,
    NOT_SUPPORTED_EMBEDDED_STREAM,
    NOT_SUPPORTED_PROTECTED_COLLECTION,
    NOT_SUPPORTED_PROTECTED_NAVIGATION,
    NOT_SUPPORTED_NAVIGATION_PART_OF_GROUP,
    NOT_SUPPORTED_MANDATORY_PART_OF_GROUP,
    NOT_SUPPORTED_KEY_PART_OF_GROUP,
    NOT_SUPPORTED_MIXED_PART_OF_GROUP,

    DESCRIPTION_LOCALE_FIELD_MISSING,
    DESCRIPTION_ANNOTATION_MISSING,
    DESCRIPTION_FIELD_WRONG_TYPE,

    PROPERTY_DEFAULT_ERROR,
    PROPERTY_MISSING_PRECISION,
    PROPERTY_PRECISION_NOT_IN_RANGE,
    PROPERTY_REQUIRED_UNKNOWN,
    COMPLEX_PROPERTY_MISSING_PROTECTION_PATH,
    COMPLEX_PROPERTY_WRONG_PROTECTION_PATH,
    REFERENCED_PROPERTY_NOT_FOUND,
    TRANSIENT_CALCULATOR_TOO_MANY_CONSTRUCTORS,
    TRANSIENT_CALCULATOR_WRONG_PARAMETER,
    TRANSIENT_KEY_NOT_SUPPORTED,
    EXTENSION_PROVIDER_TOO_MANY_CONSTRUCTORS,
    EXTENSION_PROVIDER_WRONG_PARAMETER,
    INHERITANCE_NOT_ALLOWED,
    TO_MANY_STREAMS,
    MISSING_TERM_NAMESPACE,
    ANNOTATION_STREAM_INCOMPLETE,
    ANNOTATION_PARSE_ERROR,
    ANNOTATION_PATH_NOT_FOUND,
    VARIABLE_NOT_SUPPORTED,
    ODATA_ANNOTATION_TWO_EXPRESSIONS,
    NAVI_PROPERTY_NOT_FOUND,
    ON_LEFT_ATTRIBUTE_NULL,
    ON_RIGHT_ATTRIBUTE_NULL,
    NO_JOIN_TABLE_TYPE,
    PATH_ELEMENT_NOT_FOUND,
    FILE_NOT_FOUND;

    @Override
    public String getKey() {
      return name();
    }

  }

  private static final String BUNDLE_NAME = "metadata-exceptions-i18n";

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
    return BUNDLE_NAME;
  }

  public String getId() {
    return id;
  }
}
