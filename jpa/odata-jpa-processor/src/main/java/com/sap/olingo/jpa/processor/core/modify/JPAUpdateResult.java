package com.sap.olingo.jpa.processor.core.modify;

public final record JPAUpdateResult(boolean wasCreate, Object modifiedEntity) {}
