package com.sap.olingo.jpa.processor.core.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;

public class SelectOptionDouble implements SelectOption {
  protected static final String SELECT_ITEM_SEPERATOR = ",";
  protected static final String SELECT_ALL = "*";
  protected static final String SELECT_PATH_SEPERATOR = "/";
  private final String text;
  private final List<SelectItem> selItems;

  public SelectOptionDouble(String text) {
    super();
    this.text = text;
    this.selItems = new ArrayList<>();
    parseText();
  }

  @Override
  public SystemQueryOptionKind getKind() {
    return SystemQueryOptionKind.SELECT;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public List<SelectItem> getSelectItems() {
    return Collections.unmodifiableList(selItems);
  }

  @Override
  public String getText() {
    return text;
  }

  private void parseText() {
    if (SELECT_ALL.equals(text)) {
      final SelectItem item = mock(SelectItem.class);
      when(item.isStar()).thenReturn(true);
      selItems.add(item);
    } else {
      final String[] selectList = text.split(SELECT_ITEM_SEPERATOR);
      for (String elementText : selectList) {
        final List<UriResource> uriResources = new ArrayList<>();
        final UriInfoResource resource = mock(UriInfoResource.class);
        final SelectItem item = mock(SelectItem.class);
        when(resource.getUriResourceParts()).thenReturn(uriResources);
        when(item.isStar()).thenReturn(false);
        when(item.getResourcePath()).thenReturn(resource);
        selItems.add(item);
        String[] elements = elementText.split(SELECT_PATH_SEPERATOR);
        for (String element : elements) {
          final UriResourceProperty property = mock(UriResourceProperty.class);
          when(property.getSegmentValue()).thenReturn(element);
          uriResources.add(property);
        }
      }
    }
  }

}
