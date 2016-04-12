package org.apache.olingo.jpa.metadata.core.edm.mapper.exception;

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
    INVALID_ENTITYSET,
    INVALID_ENTITYCONTAINER,
    INVALID_ASSOCIATION_SET,
    INVALID_FUNC_IMPORT,

    BUILDER_NULL,
    TYPE_NOT_SUPPORTED,
    FUNC_ENTITYSET_EXP,
    FUNC_RETURN_TYPE_EXP,
    FUNC_RETURN_TYPE_ENTITY_NOT_FOUND,
    GENERAL,
    INNER_EXCEPTION,
    FUNC_PARAM_NAME_EXP,
    FUNC_PARAM_OUT_WRONG_TYPE,
    FUNC_PARAM_OUT_MISSING,
    FUNC_PARAM_OUT_TO_MANY,
    REF_ATTRIBUTE_NOT_FOUND,
    TYPE_MAPPER_COULD_NOT_INSANTIATE,
    NOT_SUPPORTED_EMBEDDED_KEY,
    NOT_SUPPORTED_ATTRIBUTE_TYPE,
    NOT_SUPPORTED_NO_IMPLICIT_COLUMNS,
    DESCRIPTION_LOCALE_FIELD_MISSING;

    @Override
    public String getKey() {
      return name();
    }

  }

  private static final String BUNDEL_NAME = "exceptions-i18n";

  public ODataJPAModelException(Throwable e) {
    super(e);
  }

  public ODataJPAModelException(MessageKeys messageKey, Throwable e, String[] params) {
    super(messageKey.name(), e, params);
  }

  public ODataJPAModelException(MessageKeys messageKey) {
    super(messageKey.getKey());
  }

  public ODataJPAModelException(MessageKeys messageKey, String... params) {
    super(messageKey.getKey(), params);
  }

  public ODataJPAModelException(MessageKeys messageKey, Throwable e) {
    super(messageKey.getKey(), e);
  }

  @Override
  protected String getBundleName() {
    return BUNDEL_NAME;
  }

}
