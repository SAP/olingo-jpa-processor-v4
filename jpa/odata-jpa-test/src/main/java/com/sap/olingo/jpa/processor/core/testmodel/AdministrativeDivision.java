package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctions;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;

@EdmFunctions({
    @EdmFunction(
        name = "Siblings",
        functionName = "\"OLINGO\".\"Siblings\"",
        isBound = false,
        hasFunctionImport = true,
        returnType = @EdmFunction.ReturnType(isCollection = true),
        parameter = {
            @EdmParameter(name = "CodePublisher", parameterName = "\"CodePublisher\"",
                type = String.class, maxLength = 10),
            @EdmParameter(name = "CodeID", parameterName = "\"CodeID\"", type = String.class, maxLength = 10),
            @EdmParameter(name = "DivisionCode", parameterName = "\"DivisionCode\"", type = String.class,
                maxLength = 10) }),
    @EdmFunction(
        name = "PopulationDensity",
        functionName = "\"OLINGO\".\"PopulationDensity\"",
        isBound = false,
        hasFunctionImport = false,
        returnType = @EdmFunction.ReturnType(isCollection = false, type = Double.class),
        parameter = {
            @EdmParameter(name = "Area", parameterName = "UnitArea", type = Integer.class),
            @EdmParameter(name = "Population", parameterName = "Population", type = Long.class) }),
    @EdmFunction(
        name = "ConvertToQkm",
        functionName = "\"OLINGO\".\"ConvertToQkm\"",
        isBound = false,
        hasFunctionImport = false,
        returnType = @EdmFunction.ReturnType(isCollection = false, type = Integer.class),
        parameter = {
            @EdmParameter(name = "Area", parameterName = "UnitArea", type = Integer.class) }),
})

@IdClass(AdministrativeDivisionKey.class)
@Entity(name = "AdministrativeDivision")
@Table(schema = "\"OLINGO\"", name = "\"AdministrativeDivision\"")
public class AdministrativeDivision implements KeyAccess {

  @Id
  @Column(name = "\"CodePublisher\"", length = 10)
  private String codePublisher;
  @Id
  @Column(name = "\"CodeID\"", length = 10)
  private String codeID;
  @Id
  @Column(name = "\"DivisionCode\"", length = 10)
  private String divisionCode;

  @Column(name = "\"CountryISOCode\"", length = 4)
  private String countryCode;
  @Column(name = "\"ParentCodeID\"", length = 10)
  private String parentCodeID;
  @Column(name = "\"ParentDivisionCode\"", length = 10)
  private String parentDivisionCode;
  @Column(name = "\"AlternativeCode\"", length = 10)
  private String alternativeCode;
  @Column(name = "\"Area\"") // , precision = 34, scale = 0)
  private Integer area = new Integer(0);
  @Column(name = "\"Population\"", precision = 34, scale = 0)
  private long population;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, optional = true)
  @JoinColumns({
      @JoinColumn(referencedColumnName = "\"CodePublisher\"", name = "\"CodePublisher\"", nullable = false,
          insertable = false, updatable = false),
      @JoinColumn(referencedColumnName = "\"CodeID\"", name = "\"ParentCodeID\"", nullable = false,
          insertable = false, updatable = false),
      @JoinColumn(referencedColumnName = "\"DivisionCode\"", name = "\"ParentDivisionCode\"", nullable = false,
          insertable = false, updatable = false) })
  private AdministrativeDivision parent;

  @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<AdministrativeDivision> children = new ArrayList<>();

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumns({
      @JoinColumn(name = "\"CodePublisher\"", referencedColumnName = "\"CodePublisher\"", insertable = false,
          updatable = false),
      @JoinColumn(name = "\"CodeID\"", referencedColumnName = "\"CodeID\"", insertable = false, updatable = false),
      @JoinColumn(name = "\"DivisionCode\"", referencedColumnName = "\"DivisionCode\"", insertable = false,
          updatable = false)
  })
  private List<AdministrativeDivisionDescription> allDescriptions;

  public AdministrativeDivision() {
    // required for JPA
  }

  public AdministrativeDivision(final AdministrativeDivisionKey key) {
    codePublisher = key.getCodePublisher();
    codeID = key.getCodeID();
    divisionCode = key.getDivisionCode();
  }

  @PostPersist
  @PostUpdate
  public void adjustParent() {
    for (AdministrativeDivision child : children) {
      child.setParent(this);
    }
  }

  public String getAlternativeCode() {
    return alternativeCode;
  }

  public int getArea() {
    return area;
  }

  public List<AdministrativeDivision> getChildren() {
    return children;
  }

  public String getCodeID() {
    return codeID;
  }

  public String getCodePublisher() {
    return codePublisher;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public String getDivisionCode() {
    return divisionCode;
  }

  @Override
  public Object getKey() {
    return new AdministrativeDivisionKey(codePublisher, codeID, divisionCode);
  }

  public AdministrativeDivision getParent() {
    return parent;
  }

  public String getParentCodeID() {
    return parentCodeID;
  }

  public String getParentDivisionCode() {
    return parentDivisionCode;
  }

  public long getPopulation() {
    return population;
  }

  public void setAlternativeCode(String alternativeCode) {
    this.alternativeCode = alternativeCode;
  }

  public void setArea(int area) {
    this.area = area;
  }

  public void setArea(Integer area) {
    this.area = area;
  }

  public void setChildren(List<AdministrativeDivision> children) {
    this.children = children;
  }

  public void setCodeID(String codeID) {
    this.codeID = codeID;
  }

  public void setCodePublisher(String codePublisher) {
    this.codePublisher = codePublisher;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public void setDivisionCode(String divisionCode) {
    this.divisionCode = divisionCode;
  }

  public void setParent(AdministrativeDivision parent) {
    this.parent = parent;
    this.parentCodeID = parent.getCodeID();
    this.parentDivisionCode = parent.getDivisionCode();

  }

  public void setParentCodeID(String parentCodeID) {
    this.parentCodeID = parentCodeID;
  }

  public void setParentDivisionCode(String parentDivisionCode) {
    this.parentDivisionCode = parentDivisionCode;
  }

  public void setPopulation(long population) {
    this.population = population;
  }
}
