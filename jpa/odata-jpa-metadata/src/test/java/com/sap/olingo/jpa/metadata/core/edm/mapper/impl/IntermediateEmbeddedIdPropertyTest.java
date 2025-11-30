/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.TRANSIENT_KEY_NOT_SUPPORTED;
import static com.sap.olingo.jpa.processor.core.util.Assertions.assertException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.errormodel.TeamWithTransientEmbeddableKey;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;

/**
 * @author Oliver Grande
 * Created: 22.03.2020
 *
 */
class IntermediateEmbeddedIdPropertyTest extends TestMappingRoot {
  private TestHelper helper;
  private TestHelper errorHelper;
  private JPAEdmNameBuilder nameBuilder;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
    errorHelper = new TestHelper(errorEmf.getMetamodel(), ERROR_PUNIT);
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
  }

  @Test
  void checkEmbeddedIdCanBeCreated() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(AdministrativeDivisionDescription.class);
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(et, "key");
    assertNotNull(new IntermediateEmbeddedIdProperty(new JPADefaultEdmNameBuilder(PUNIT_NAME), jpaAttribute,
        helper.schema));
  }

  @Test
  void checkEmbeddedIdIsKey() throws ODataJPAModelException {
    final EntityType<?> et = helper.getEntityType(AdministrativeDivisionDescription.class);
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(et, "key");
    final IntermediateEmbeddedIdProperty cut = new IntermediateEmbeddedIdProperty(new JPADefaultEdmNameBuilder(
        PUNIT_NAME), jpaAttribute, helper.schema);
    assertTrue(cut.isKey());
  }

  @Test
  void checkEmbeddedIdThrowsExceptionIfTransient() {
    final EntityType<?> et = errorHelper.getEntityType(TeamWithTransientEmbeddableKey.class);
    final Attribute<?, ?> jpaAttribute = helper.getAttribute(et, "key");
    assertException(ODataJPAModelException.class,
        () -> new IntermediateEmbeddedIdProperty(nameBuilder, jpaAttribute, helper.schema),
        TRANSIENT_KEY_NOT_SUPPORTED.getKey());
  }
}
