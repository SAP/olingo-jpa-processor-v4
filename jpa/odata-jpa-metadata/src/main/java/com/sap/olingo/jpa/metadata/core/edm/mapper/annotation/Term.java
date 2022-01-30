package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.VARIABLE_NOT_SUPPORTED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * <xs:complexType name="TTerm">
 * <xs:sequence>
 * <xs:element ref="edm:Annotation" minOccurs="0" maxOccurs="unbounded" />
 * </xs:sequence>
 * <xs:attribute name="Name" type="edm:TSimpleIdentifier" use="required" />
 * <xs:attribute name="Type" type="edm:TTypeName" use="required" />
 * <xs:attribute name="BaseTerm" type="edm:TQualifiedName" use="optional" />
 * <xs:attribute name="Nullable" type="xs:boolean" use="optional" />
 * <xs:attribute name="DefaultValue" type="xs:string" use="optional" />
 * <xs:attribute name="AppliesTo" type="edm:TAppliesTo" use="optional" />
 * <xs:attributeGroup ref="edm:TFacetAttributes" />
 * </xs:complexType>
 * <xs:attributeGroup name="TFacetAttributes">
 * <xs:attribute name="MaxLength" type="edm:TMaxLengthFacet" use="optional" />
 * <xs:attribute name="Precision" type="edm:TPrecisionFacet" use="optional" />
 * <xs:attribute name="Scale" type="edm:TScaleFacet" use="optional" />
 * <xs:attribute name="SRID" type="edm:TSridFacet" use="optional" />
 * <xs:attribute name="Unicode" type="edm:TUnicodeFacet" use="optional" />
 * </xs:attributeGroup>}
 */

@JsonIgnoreProperties(ignoreUnknown = true)
class Term extends CsdlTerm {

  @JacksonXmlProperty(localName = "AppliesTo", isAttribute = true)
  void setAppliesTo(final String appliesTo) {
    final List<String> result = new ArrayList<>();
    if (appliesTo != null) {
      final String[] list = appliesTo.split(" ");
      result.addAll(Arrays.asList(list));
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

  /**
   * MUST be a positive integer.
   */
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
  void setSrid(final String srid) {
    super.setSrid(SRID.valueOf(srid));
  }
}
