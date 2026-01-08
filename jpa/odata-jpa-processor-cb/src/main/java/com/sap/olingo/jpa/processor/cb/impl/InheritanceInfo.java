package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;

class InheritanceInfo {
  private static final Log LOGGER = LogFactory.getLog(InheritanceInfo.class);
  private final Optional<InheritanceType> inType;
  private final Optional<Class<?>> baseClass;
  private final Optional<String> discriminatorColumn;

  InheritanceInfo(@Nonnull final JPAEntityType type) {
    baseClass = Optional.ofNullable(determineBaseClass(type));
    inType = Optional.ofNullable(baseClass.map(this::determineInType).orElse(null));
    discriminatorColumn = Optional.ofNullable(baseClass.map(this::determineColumn).orElse(null));
  }

  /**
   *
   * @return Root of an inheritance hierarchy
   */
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
    return inType
        .map(type -> clazz.getDeclaredAnnotation(DiscriminatorColumn.class))
        .map(column -> column.name())
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