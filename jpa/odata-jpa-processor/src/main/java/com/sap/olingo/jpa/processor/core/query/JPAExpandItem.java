package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.olingo.server.api.uri.UriInfoResource;
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
import org.apache.olingo.server.api.uri.queryoption.TopOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.api.JPAODataSkipTokenProvider;

public interface JPAExpandItem extends UriInfoResource {

  @Override
  default ApplyOption getApplyOption() {
    return null;
  }

  @Override
  default CountOption getCountOption() {
    return null;
  }

  @Override
  default List<CustomQueryOption> getCustomQueryOptions() {
    return new ArrayList<>(0);
  }

  @Override
  default DeltaTokenOption getDeltaTokenOption() {
    return null;
  }

  @Override
  default ExpandOption getExpandOption() {
    return null;
  }

  @Override
  default FilterOption getFilterOption() {
    return null;
  }

  @Override
  default FormatOption getFormatOption() {
    return null;
  }

  @Override
  default IdOption getIdOption() {
    return null;
  }

  @Override
  default OrderByOption getOrderByOption() {
    return null;
  }

  @Override
  default SearchOption getSearchOption() {
    return null;
  }

  @Override
  default SelectOption getSelectOption() {
    return null;
  }

  @Override
  default SkipOption getSkipOption() {
    return null;
  }

  @Override
  default SkipTokenOption getSkipTokenOption() {
    return null;
  }

  @Override
  default TopOption getTopOption() {
    return null;
  }

  default JPAEntityType getEntityType() {
    return null;
  }

  default Optional<JPAODataSkipTokenProvider> getSkipTokenProvider() {
    return Optional.empty();
  }

}