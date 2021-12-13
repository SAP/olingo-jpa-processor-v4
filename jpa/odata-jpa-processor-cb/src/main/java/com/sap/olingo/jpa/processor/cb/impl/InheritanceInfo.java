package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;

class InheritanceInfo {
  private static final Log LOGGER = LogFactory.getLog(InheritanceInfo.class);
  private final Optional<InheritanceType> inType;
  private final Optional<Class<?>> baseClass;
  private final Optional<String> discriminatorColumn;

  InheritanceInfo(final JPAEntityType type) {
    baseClass = Optional.ofNullable(determineBaseClass(type));
    inType = Optional.ofNullable(baseClass.map(this::determineInType).orElse(null));
    discriminatorColumn = Optional.ofNullable(baseClass.map(this::determineColumn).orElse(null));
  }

  Optional<Class<?>> getBaseClass() {
    return baseClass;
  }

  Optional<String> getDiscriminatorColumn() {
    return discriminatorColumn;
  }

  Optional<InheritanceType> getInheritanceType() {
    return inType;
  }

  boolean hasInheritance() {
    return inType.isPresent();
  }

  @CheckForNull
  private Class<?> determineBaseClass(final JPAEntityType et) {
    if (et != null && et.getTypeClass().getSuperclass() != null) {
      final Class<?> superClass = et.getTypeClass().getSuperclass();
      return determineInheritanceByClass(et, superClass);
    }
    return null;
  }

  private String determineColumn(@Nonnull final Class<?> clazz) {
    return inType.filter(t -> t.equals(InheritanceType.SINGLE_TABLE))
        .map(t -> clazz.getDeclaredAnnotation(DiscriminatorColumn.class).name())
        .orElse(null);
  }

  @CheckForNull
  private Class<?> determineInheritanceByClass(final JPAEntityType et, final Class<?> clazz) {
    final Entity jpaEntity = clazz.getDeclaredAnnotation(Entity.class);
    if (jpaEntity != null) {
      final Inheritance inheritance = clazz.getDeclaredAnnotation(Inheritance.class);
      if (inheritance != null) {
        return clazz;
      } else {
        final Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
          return determineInheritanceByClass(et, superClass);
        } else {
          LOGGER.debug("Cloud not find InheritanceType for " + et.getInternalName());
        }
      }
    }
    return null;
  }

  private InheritanceType determineInType(@Nonnull final Class<?> baseClass) {
    final Inheritance inheritance = baseClass.getDeclaredAnnotation(Inheritance.class);
    return inheritance.strategy();
  }
}