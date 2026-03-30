package com.sap.olingo.jpa.processor.core.util;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoAll;
import org.apache.olingo.server.api.uri.UriInfoBatch;
import org.apache.olingo.server.api.uri.UriInfoCrossjoin;
import org.apache.olingo.server.api.uri.UriInfoEntityId;
import org.apache.olingo.server.api.uri.UriInfoKind;
import org.apache.olingo.server.api.uri.UriInfoMetadata;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriInfoService;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.AliasQueryOption;
import org.apache.olingo.server.api.uri.queryoption.ApplyOption;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.CustomQueryOption;
import org.apache.olingo.server.api.uri.queryoption.DeltaTokenOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.FormatOption;
import org.apache.olingo.server.api.uri.queryoption.IdOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SkipTokenOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;

public class UriInfoDouble implements UriInfo {

  private final SelectOption selectOption;
  private ExpandOption expandOpts;
  private List<UriResource> uriResources;

  public UriInfoDouble(final SelectOption selectOptions) {
    super();
    this.selectOption = selectOptions;
    this.uriResources = new ArrayList<>(0);
  }

  public UriInfoDouble(final UriInfoResource resourcePath) {
    super();
    this.selectOption = null;
    this.uriResources = resourcePath.getUriResourceParts();
  }

  @Override
  public FormatOption getFormatOption() {
    return (FormatOption) failWithNull();
  }

  @Override
  public String getFragment() {
    return (String) failWithNull();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<CustomQueryOption> getCustomQueryOptions() {
    return (List<CustomQueryOption>) failWithNull();
  }

  @Override
  public ExpandOption getExpandOption() {
    return expandOpts;
  }

  @Override
  public FilterOption getFilterOption() {
    return (FilterOption) failWithNull();
  }

  @Override
  public IdOption getIdOption() {
    return (IdOption) failWithNull();
  }

  @Override
  public CountOption getCountOption() {
    return (CountOption) failWithNull();
  }

  @Override
  public OrderByOption getOrderByOption() {
    return (OrderByOption) failWithNull();
  }

  @Override
  public SearchOption getSearchOption() {
    return (SearchOption) failWithNull();
  }

  @Override
  public SelectOption getSelectOption() {
    return selectOption;
  }

  @Override
  public SkipOption getSkipOption() {
    return (SkipOption) failWithNull();
  }

  @Override
  public SkipTokenOption getSkipTokenOption() {
    return (SkipTokenOption) failWithNull();
  }

  @Override
  public TopOption getTopOption() {
    return (TopOption) failWithNull();
  }

  @Override
  public List<UriResource> getUriResourceParts() {
    return uriResources;
  }

  @Override
  public String getValueForAlias(final String alias) {
    return (String) failWithNull();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getEntitySetNames() {
    return (List<String>) failWithNull();
  }

  @Override
  public EdmEntityType getEntityTypeCast() {
    return (EdmEntityType) failWithNull();
  }

  @Override
  public UriInfoKind getKind() {
    return (UriInfoKind) failWithNull();
  }

  @Override
  public UriInfoService asUriInfoService() {
    return (UriInfoService) failWithNull();
  }

  @Override
  public UriInfoAll asUriInfoAll() {
    return (UriInfoAll) failWithNull();
  }

  @Override
  public UriInfoBatch asUriInfoBatch() {
    return (UriInfoBatch) failWithNull();
  }

  @Override
  public UriInfoCrossjoin asUriInfoCrossjoin() {
    return (UriInfoCrossjoin) failWithNull();
  }

  @Override
  public UriInfoEntityId asUriInfoEntityId() {
    return (UriInfoEntityId) failWithNull();
  }

  @Override
  public UriInfoMetadata asUriInfoMetadata() {
    return (UriInfoMetadata) failWithNull();
  }

  @Override
  public UriInfoResource asUriInfoResource() {
    return (UriInfoResource) failWithNull();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<SystemQueryOption> getSystemQueryOptions() {
    return (List<SystemQueryOption>) failWithNull();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<AliasQueryOption> getAliases() {
    return (List<AliasQueryOption>) failWithNull();
  }

  class primitiveDouble implements UriResourcePrimitiveProperty {

    @Override
    public EdmProperty getProperty() {
      return (EdmProperty) failWithNull();
    }

    @Override
    public EdmType getType() {
      return (EdmType) failWithNull();
    }

    @Override
    public boolean isCollection() {
      return (boolean) failWithNull();
    }

    @Override
    public String getSegmentValue(final boolean includeFilters) {
      return (String) failWithNull();
    }

    @Override
    public String toString(final boolean includeFilters) {
      return (String) failWithNull();
    }

    @Override
    public UriResourceKind getKind() {
      return (UriResourceKind) failWithNull();
    }

    @Override
    public String getSegmentValue() {
      return (String) failWithNull();
    }

  }

  class complexDouble implements UriResourceComplexProperty {

    @Override
    public EdmProperty getProperty() {
      return (EdmProperty) failWithNull();
    }

    @Override
    public EdmType getType() {
      return (EdmType) failWithNull();
    }

    @Override
    public boolean isCollection() {
      return (boolean) failWithNull();
    }

    @Override
    public String getSegmentValue(final boolean includeFilters) {
      return (String) failWithNull();
    }

    @Override
    public String toString(final boolean includeFilters) {
      return (String) failWithNull();
    }

    @Override
    public UriResourceKind getKind() {
      return (UriResourceKind) failWithNull();
    }

    @Override
    public String getSegmentValue() {
      return (String) failWithNull();
    }

    @Override
    public EdmComplexType getComplexType() {
      return (EdmComplexType) failWithNull();
    }

    @Override
    public EdmComplexType getComplexTypeFilter() {
      return (EdmComplexType) failWithNull();
    }
  }

  class propertyDouble implements UriResourceProperty {

    @Override
    public EdmType getType() {
      return (EdmType) failWithNull();
    }

    @Override
    public boolean isCollection() {
      return (boolean) failWithNull();
    }

    @Override
    public String getSegmentValue(final boolean includeFilters) {
      return (String) failWithNull();
    }

    @Override
    public String toString(final boolean includeFilters) {
      return (String) failWithNull();
    }

    @Override
    public UriResourceKind getKind() {
      return (UriResourceKind) failWithNull();
    }

    @Override
    public String getSegmentValue() {
      return (String) failWithNull();
    }

    @Override
    public EdmProperty getProperty() {
      return (EdmProperty) failWithNull();
    }
  }

  public void setExpandOpts(final ExpandOption expandOpts) {
    this.expandOpts = expandOpts;
  }

  public void setUriResources(final List<UriResource> uriResources) {
    this.uriResources = uriResources;
  }

  @Override
  public ApplyOption getApplyOption() {
    return null;
  }

  @Override
  public DeltaTokenOption getDeltaTokenOption() {
    return null;
  }

  private final Object failWithNull() {
    fail();
    return null;
  }

}
