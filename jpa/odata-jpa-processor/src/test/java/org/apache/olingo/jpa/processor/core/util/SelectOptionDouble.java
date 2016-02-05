package org.apache.olingo.jpa.processor.core.util;

import java.util.List;

import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;

public class SelectOptionDouble implements SelectOption {

  private final String text;

  public SelectOptionDouble(String text) {
    super();
    this.text = text;
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
  public String getText() {
    return text;
  }

  @Override
  public List<SelectItem> getSelectItems() {
    return null;
  }

}
