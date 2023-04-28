package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.CountRestrictions;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.ExpandRestrictions;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.FilterRestrictions;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.SortRestrictions;
import com.sap.olingo.jpa.metadata.odata.v4.core.terms.ComputedDefaultValue;
import com.sap.olingo.jpa.metadata.odata.v4.core.terms.Immutable;

@FilterRestrictions(requiredProperties = { "parentCodeID", "parentDivisionCode" })
@SortRestrictions(ascendingOnlyProperties = { "parentDivisionCode" }, descendingOnlyProperties = { "countryCode" },
    nonSortableProperties = { "alternativeCode" })
@ExpandRestrictions(maxLevels = 2, nonExpandableProperties = { "children" })
@CountRestrictions(nonCountableNavigationProperties = { "children" })
//@EdmEntityType(extensionProvider = LauFilter.class)
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
  private final Integer area = 0;
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
