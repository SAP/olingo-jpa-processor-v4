package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.lang.reflect.Constructor;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;

public interface JPAQueryExtension<X extends EdmQueryExtensionProvider> {

  Constructor<X> getConstructor();
}
