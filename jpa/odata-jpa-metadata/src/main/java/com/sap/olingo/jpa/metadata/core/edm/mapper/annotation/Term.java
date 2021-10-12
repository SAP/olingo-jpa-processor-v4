package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.VARIABLE_NOT_SUPPORTED;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

@JsonIgnoreProperties(ignoreUnknown = true)
class Term extends CsdlTerm {

  @JacksonXmlProperty(localName = "AppliesTo", isAttribute = true)
  void setAppliesTo(String appliesTo) {
    List<String> result = new ArrayList<>();
    if (appliesTo != null) {
      String[] list = appliesTo.split(" ");
      for (String apply : list) {
        result.add(apply);
      }
    }
    super.setAppliesTo(result);
  }

  @Override
  @JacksonXmlProperty(localName = "Name", isAttribute = true)
  public CsdlTerm setName(final String name) {
    return super.setName(name);
  }

  @Override
  @JacksonXmlProperty(localName = "Type", isAttribute = true)
  public CsdlTerm setType(final String type) {
    return super.setType(type);
  }

  @Override
  @JacksonXmlProperty(localName = "BaseTerm", isAttribute = true)
  public CsdlTerm setBaseTerm(final String baseTerm) {
    return super.setBaseTerm(baseTerm);
  }

  @Override
  public CsdlTerm setAppliesTo(final List<String> appliesTo) {
    return this;
  }

  @Override
  @JacksonXmlProperty(localName = "DefaultValue", isAttribute = true)
  public CsdlTerm setDefaultValue(final String defaultValue) {
    return super.setDefaultValue(defaultValue);
  }

  @Override
  @JacksonXmlProperty(localName = "Nullable", isAttribute = true)
  public CsdlTerm setNullable(final boolean nullable) {
    return super.setNullable(nullable);
  }

  @Override
  @JacksonXmlProperty(localName = "MaxLength", isAttribute = true)
  public CsdlTerm setMaxLength(final Integer maxLength) {
    return super.setMaxLength(maxLength);
  }

  @Override
  @JacksonXmlProperty(localName = "Precision", isAttribute = true)
  public CsdlTerm setPrecision(final Integer precision) {
    return super.setPrecision(precision);
  }
  /**
   * A non negative integer or floating or variable.</p>
   * The value <b>floating</b> means that the decimal property represents a decimal floating-point number whose number
   * of
   * significant digits is the value of the Precision facet. OData 4.0 responses MUST NOT specify the value floating.
   * </p>
   * The value <b>variable</b> means that the number of digits to the right of the decimal point may vary from zero to
   * the value of the Precision facet.
   * {@link https://docs.oasis-open.org/odata/odata-csdl-xml/v4.01/odata-csdl-xml-v4.01.html#sec_Scale}
   * @throws ODataJPAModelException
   */
  @JacksonXmlProperty(localName = "Scale", isAttribute = true)
  public CsdlTerm setScale(final String scaleValue) throws ODataJPAModelException {
    try {
      final Integer scale = Integer.valueOf(scaleValue);
      return super.setScale(scale);
    } catch (final NumberFormatException e) {
      throw new ODataJPAModelException(VARIABLE_NOT_SUPPORTED, e, "Scale");
    }
  }

  /**
   * For a geometry or geography property the SRID facet identifies which spatial reference system is applied to values
   * of the property on type instances.
   * </p>
   * The value of the SRID facet MUST be a non-negative integer or the special value variable. If no value is specified,
   * the attribute defaults to 0 for Geometry types or 4326 for Geography types.
   * </p>
   * The valid values of the SRID facet and their meanings are as defined by the European Petroleum Survey Group [EPSG].
   * @param srid
   */
  @JacksonXmlProperty(localName = "SRID", isAttribute = true)
  void setSrid(final String srid) throws ODataJPAModelException {
    super.setSrid(SRID.valueOf(srid));
  }
}
