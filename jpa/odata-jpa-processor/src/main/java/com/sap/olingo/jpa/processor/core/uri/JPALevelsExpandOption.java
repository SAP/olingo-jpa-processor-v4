package com.sap.olingo.jpa.processor.core.uri;

import org.apache.olingo.server.api.uri.queryoption.LevelsExpandOption;

class JPALevelsExpandOption implements LevelsExpandOption {

  private final boolean isMax;
  private int value = 0;

  JPALevelsExpandOption(final LevelsExpandOption levelsOption) {
    this.isMax = levelsOption.isMax();
    this.value = levelsOption.getValue();
  }

  @Override
  public boolean isMax() {
    return isMax;
  }

  @Override
  public int getValue() {
    return value;
  }

  LevelsExpandOption levelResolved() {
    if (value > 0)
      value -= 1;
    return this;
  }
}
