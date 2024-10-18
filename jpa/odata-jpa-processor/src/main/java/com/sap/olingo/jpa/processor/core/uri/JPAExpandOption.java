package com.sap.olingo.jpa.processor.core.uri;

import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.LevelsExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class JPAExpandOption implements ExpandOption {

  private final ExpandOption original;
  private final List<ExpandItem> expandItems;

  JPAExpandOption(final ExpandOption expandOption, final LevelsExpandOption levelsOption) {
    this.original = expandOption;
    this.expandItems = new ArrayList<>(expandOption.getExpandItems());
    final var lastIndex = this.expandItems.size() - 1;
    this.expandItems.set(lastIndex, new JPAExpandItem(this.expandItems.get(lastIndex), levelsOption));

  }

  JPAExpandOption(ExpandOption expandOption, List<ExpandItem> expandItems) {
    this.original = expandOption;
    this.expandItems = expandItems;
  }

  public JPAExpandOption(LevelsExpandOption levels) {
    this.original = null;
    this.expandItems = Collections.singletonList(new JPAExpandItem(levels));
  }

  @Override
  public SystemQueryOptionKind getKind() {
    return SystemQueryOptionKind.EXPAND;
  }

  @Override
  public String getName() {
    return original.getName();
  }

  @Override
  public String getText() {
    return original.getText();
  }

  @Override
  public List<ExpandItem> getExpandItems() {
    return expandItems;
  }

}
