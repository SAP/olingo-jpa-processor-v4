package com.sap.olingo.jpa.processor.core.modify;

public final class JPAUpdateResult {
  private final boolean wasCreate;
  private final Object modifyedEntity;

  public JPAUpdateResult(boolean wasCreate, Object modifyedEntity) {
    super();
    this.wasCreate = wasCreate;
    this.modifyedEntity = modifyedEntity;
  }

  public boolean wasCreate() {
    return wasCreate;
  }

  public Object getModifyedEntity() {
    return modifyedEntity;
  }
}
