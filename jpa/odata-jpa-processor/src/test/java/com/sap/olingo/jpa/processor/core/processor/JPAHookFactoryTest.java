package com.sap.olingo.jpa.processor.core.processor;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAQueryExtension;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.errormodel.DummyPropertyCalculator;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.testmodel.CurrentUserQueryExtension;
import com.sap.olingo.jpa.processor.core.testmodel.FullNameCalculator;
import com.sap.olingo.jpa.processor.core.testobjects.HeaderParamTransientPropertyConverter;
import com.sap.olingo.jpa.processor.core.testobjects.ThreeParameterTransientPropertyConverter;
import com.sap.olingo.jpa.processor.core.testobjects.TwoParameterTransientPropertyConverter;

class JPAHookFactoryTest {
  private JPAHookFactory cut;
  private EntityManager em;
  private JPAHttpHeaderMap header;
  private JPARequestParameterMap parameter;

  @BeforeEach
  void setup() {
    em = mock(EntityManager.class);
    header = new JPAHttpHeaderHashMap();
    parameter = new JPARequestParameterHashMap();
    cut = new JPAHookFactory(em, header, parameter);
  }

  @Test
  void testGetCalculatorReturnsEmptyOptionalIfNotTransient() throws ODataJPAModelException,
      ODataJPAProcessorException {
    final JPAAttribute attribute = mock(JPAAttribute.class);
    when(attribute.isTransient()).thenReturn(false);
    assertFalse(cut.getTransientPropertyCalculator(attribute).isPresent());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCalculatorReturnsInstanceNoParameter() throws ODataJPAModelException, ODataJPAProcessorException,
      NoSuchMethodException, SecurityException {

    final JPAAttribute attribute = mock(JPAAttribute.class);
    final Constructor<?> c = FullNameCalculator.class.getConstructor();
    when(attribute.isTransient()).thenReturn(true);
    when(attribute.getCalculatorConstructor()).thenReturn((Constructor<EdmTransientPropertyCalculator<?>>) c);

    assertTrue(cut.getTransientPropertyCalculator(attribute).isPresent());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCalculatorReturnsInstanceFromCache() throws ODataJPAModelException, ODataJPAProcessorException,
      NoSuchMethodException, SecurityException {

    final JPAAttribute attribute = mock(JPAAttribute.class);
    final Constructor<?> c = FullNameCalculator.class.getConstructor();
    when(attribute.isTransient()).thenReturn(true);
    when(attribute.getCalculatorConstructor()).thenReturn((Constructor<EdmTransientPropertyCalculator<?>>) c);
    final Optional<EdmTransientPropertyCalculator<?>> act = cut.getTransientPropertyCalculator(attribute);
    assertEquals(act.get(), cut.getTransientPropertyCalculator(attribute).get());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCalculatorReturnsInstanceEntityManager() throws ODataJPAModelException, ODataJPAProcessorException,
      NoSuchMethodException, SecurityException {

    final JPAAttribute attribute = mock(JPAAttribute.class);
    final Constructor<?> c = DummyPropertyCalculator.class.getConstructor(EntityManager.class);

    cut = new JPAHookFactory(mock(EntityManager.class), header, parameter);
    when(attribute.isTransient()).thenReturn(true);
    when(attribute.getCalculatorConstructor()).thenReturn((Constructor<EdmTransientPropertyCalculator<?>>) c);

    final Optional<EdmTransientPropertyCalculator<?>> act = cut.getTransientPropertyCalculator(attribute);
    assertTrue(act.isPresent());
    assertNotNull(((DummyPropertyCalculator) act.get()).getEntityManager());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCalculatorReturnsInstanceHeader() throws ODataJPAModelException, ODataJPAProcessorException,
      NoSuchMethodException, SecurityException {

    final JPAAttribute attribute = mock(JPAAttribute.class);
    final Constructor<?> c = HeaderParamTransientPropertyConverter.class.getConstructor(JPAHttpHeaderMap.class);
    when(attribute.isTransient()).thenReturn(true);
    when(attribute.getCalculatorConstructor()).thenReturn((Constructor<EdmTransientPropertyCalculator<?>>) c);
    final Optional<EdmTransientPropertyCalculator<?>> act = cut.getTransientPropertyCalculator(attribute);
    assertTrue(act.isPresent());
    assertNotNull(((HeaderParamTransientPropertyConverter) act.get()).getHeader());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCalculatorReturnsInstanceThreeParams() throws ODataJPAModelException, ODataJPAProcessorException,
      NoSuchMethodException, SecurityException {

    final JPAAttribute attribute = mock(JPAAttribute.class);
    final Constructor<?> c = ThreeParameterTransientPropertyConverter.class.getConstructors()[0];
    when(attribute.isTransient()).thenReturn(true);
    when(attribute.getCalculatorConstructor()).thenReturn((Constructor<EdmTransientPropertyCalculator<?>>) c);
    final Optional<EdmTransientPropertyCalculator<?>> act = cut.getTransientPropertyCalculator(attribute);
    assertTrue(act.isPresent());
    assertNotNull(((ThreeParameterTransientPropertyConverter) act.get()).getEntityManager());
    assertNotNull(((ThreeParameterTransientPropertyConverter) act.get()).getHeader());
    assertNotNull(((ThreeParameterTransientPropertyConverter) act.get()).getParameter());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetCalculatorReturnsInstanceTwoParameter() throws ODataJPAModelException, ODataJPAProcessorException,
      NoSuchMethodException, SecurityException {

    final JPAAttribute attribute = mock(JPAAttribute.class);
    final Constructor<?> c = TwoParameterTransientPropertyConverter.class.getConstructor(EntityManager.class,
        JPAHttpHeaderMap.class);
    cut = new JPAHookFactory(mock(EntityManager.class), header, parameter);
    when(attribute.isTransient()).thenReturn(true);
    when(attribute.getCalculatorConstructor()).thenReturn((Constructor<EdmTransientPropertyCalculator<?>>) c);

    final Optional<EdmTransientPropertyCalculator<?>> act = cut.getTransientPropertyCalculator(attribute);
    assertTrue(act.isPresent());
    assertNotNull(((TwoParameterTransientPropertyConverter) act.get()).getEntityManager());
    assertNotNull(((TwoParameterTransientPropertyConverter) act.get()).getHeader());
  }

  @Test
  void testGetQueryExtensionReturnsEmptyOptionalIfSet() throws ODataJPAModelException,
      ODataJPAProcessorException {
    final JPAEntityType et = mock(JPAEntityType.class);
    when(et.getQueryExtension()).thenReturn(Optional.empty());
    assertFalse(cut.getQueryExtensionProvider(et).isPresent());
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  void testGetQueryExtensionReturnsInstanceNoParameter() throws ODataJPAModelException, ODataJPAProcessorException,
      NoSuchMethodException, SecurityException {

    final JPAEntityType et = mock(JPAEntityType.class);
    final JPAQueryExtension extension = mock(JPAQueryExtension.class);
    when(et.getQueryExtension()).thenReturn(Optional.of(extension));
    when(extension.getConstructor()).thenAnswer(new Answer<Constructor<? extends EdmQueryExtensionProvider>>() {
      @Override
      public Constructor<? extends EdmQueryExtensionProvider> answer(final InvocationOnMock invocation)
          throws Throwable {
        return (Constructor<? extends EdmQueryExtensionProvider>) CurrentUserQueryExtension.class
            .getConstructors()[0];
      }
    });
    assertTrue(cut.getQueryExtensionProvider(et).isPresent());
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  void testGetQueryExtensionReturnsInstanceEntityManagerParameter() throws ODataJPAModelException,
      ODataJPAProcessorException, NoSuchMethodException, SecurityException {

    final JPAEntityType et = mock(JPAEntityType.class);
    final JPAQueryExtension extension = mock(JPAQueryExtension.class);
    when(et.getQueryExtension()).thenReturn(Optional.of(extension));
    when(extension.getConstructor()).thenAnswer(new Answer<Constructor<? extends EdmQueryExtensionProvider>>() {
      @Override
      public Constructor<? extends EdmQueryExtensionProvider> answer(final InvocationOnMock invocation)
          throws Throwable {
        return (Constructor<? extends EdmQueryExtensionProvider>) ExtensionProviderWithHeaderParameter.class
            .getConstructors()[0];
      }
    });
    assertTrue(cut.getQueryExtensionProvider(et).isPresent());
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  void testGetQueryExtensionReturnsInstanceAllParameter() throws ODataJPAModelException,
      ODataJPAProcessorException, NoSuchMethodException, SecurityException {

    final JPAEntityType et = mock(JPAEntityType.class);
    final JPAQueryExtension extension = mock(JPAQueryExtension.class);
    when(et.getQueryExtension()).thenReturn(Optional.of(extension));
    when(extension.getConstructor()).thenAnswer(new Answer<Constructor<? extends EdmQueryExtensionProvider>>() {
      @Override
      public Constructor<? extends EdmQueryExtensionProvider> answer(final InvocationOnMock invocation)
          throws Throwable {
        return (Constructor<? extends EdmQueryExtensionProvider>) ExtensionProviderWithAllowedParameter.class
            .getConstructors()[0];
      }
    });
    assertTrue(cut.getQueryExtensionProvider(et).isPresent());
  }

  @SuppressWarnings("unused")
  private static class ExtensionProviderWithAllowedParameter implements EdmQueryExtensionProvider {
    private final Map<String, List<String>> header;
    private final JPARequestParameterMap parameter;

    @Override
    public Expression<Boolean> getFilterExtension(final CriteriaBuilder cb, final From<?, ?> from) {
      return null;
    }

    public ExtensionProviderWithAllowedParameter(final JPAHttpHeaderMap header,
        final JPARequestParameterMap parameter) {
      this.header = requireNonNull(header);
      this.parameter = requireNonNull(parameter);
    }
  }

  private static class ExtensionProviderWithHeaderParameter implements EdmQueryExtensionProvider {
    @SuppressWarnings("unused")
    private final Map<String, List<String>> header;

    @Override
    public Expression<Boolean> getFilterExtension(final CriteriaBuilder cb, final From<?, ?> from) {
      return null;
    }

    @SuppressWarnings("unused")
    public ExtensionProviderWithHeaderParameter(final JPAHttpHeaderMap header) {
      this.header = header;
    }
  }

}
