package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.CountRestrictions;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.ExpandRestrictions;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.FilterRestrictions;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.SortRestrictions;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.UpdateMethod;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.UpdateRestrictions;
import com.sap.olingo.jpa.metadata.odata.v4.core.terms.ComputedDefaultValue;
import com.sap.olingo.jpa.metadata.odata.v4.core.terms.Example;
import com.sap.olingo.jpa.metadata.odata.v4.core.terms.Immutable;

@FilterRestrictions(requiresFilter = true, requiredProperties = { "parentCodeID", "parentDivisionCode" })
@SortRestrictions(ascendingOnlyProperties = { "parentDivisionCode" }, descendingOnlyProperties = { "countryCode" },
    nonSortableProperties = { "alternativeCode" })
@ExpandRestrictions(maxLevels = 2, nonExpandableProperties = { "children" })
@CountRestrictions(nonCountableNavigationProperties = { "children" })
@UpdateRestrictions(updateMethod = UpdateMethod.PATCH, description = "Just to test")
@Example(description = "Get the roots", externalValue = "../AnnotationsParent?$filter=Parent eq null")
//@EdmEntityType(extensionProvider = LauFilter.class)
@IdClass(AdministrativeDivisionKey.class)
@Entity(name = "AnnotationsParent")
@Table(schema = "\"OLINGO\"", name = "\"AdministrativeDivision\"")
public class AnnotationsParent {

  public static final String CODE_PUBLISHER = "codePublisher";
  public static final String CODE_ID = "codeID";

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
  @Immutable
  @Column(name = "\"AlternativeCode\"", length = 10)
  private String alternativeCode;
  @ComputedDefaultValue
  @Column(name = "\"Area\"") // , precision = 34, scale = 0)
  private Integer area;
  @Column(name = "\"Population\"", precision = 34, scale = 0)
  private long population;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, optional = true)
  @JoinColumn(referencedColumnName = "\"CodePublisher\"", name = "\"CodePublisher\"", nullable = false,
      insertable = false, updatable = false)
  @JoinColumn(referencedColumnName = "\"CodeID\"", name = "\"ParentCodeID\"", nullable = false,
      insertable = false, updatable = false)
  @JoinColumn(referencedColumnName = "\"DivisionCode\"", name = "\"ParentDivisionCode\"", nullable = false,
      insertable = false, updatable = false)
  private AnnotationsParent parent;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, optional = true)
  @JoinColumn(referencedColumnName = "\"CodePublisher\"", name = "\"CodePublisher\"", nullable = false,
      insertable = false, updatable = false)
  @JoinColumn(referencedColumnName = "\"CodeID\"", name = "\"ParentCodeID\"", nullable = false,
      insertable = false, updatable = false)
  @JoinColumn(referencedColumnName = "\"DivisionCode\"", name = "\"ParentDivisionCode\"", nullable = false,
      insertable = false, updatable = false)
  private AnnotationsParent actualParent;

  @Example(description = "Get the leaves", externalValue = "../AnnotationsParent?$filter=Children/$count eq 0")
  @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private final List<AnnotationsParent> children = new ArrayList<>();

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "\"CodePublisher\"", referencedColumnName = "\"CodePublisher\"", insertable = false,
      updatable = false)
  @JoinColumn(name = "\"CodeID\"", referencedColumnName = "\"CodeID\"", insertable = false, updatable = false)
  @JoinColumn(name = "\"DivisionCode\"", referencedColumnName = "\"DivisionCode\"", insertable = false,
      updatable = false)
  private List<AdministrativeDivisionDescription> allDescriptions;
}
