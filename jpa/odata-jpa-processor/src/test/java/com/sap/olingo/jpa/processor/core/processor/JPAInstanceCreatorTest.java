package com.sap.olingo.jpa.processor.core.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.UriParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.testobjects.ClassWithIdClassConstructor;
import com.sap.olingo.jpa.processor.core.testobjects.ClassWithIdClassWithoutConstructor;
import com.sap.olingo.jpa.processor.core.testobjects.ClassWithMultipleKeysConstructor;
import com.sap.olingo.jpa.processor.core.testobjects.ClassWithMultipleKeysNoConstructor;
import com.sap.olingo.jpa.processor.core.testobjects.ClassWithMultipleKeysSetter;
import com.sap.olingo.jpa.processor.core.testobjects.ClassWithOneKeyAndEmptyConstructor;
import com.sap.olingo.jpa.processor.core.testobjects.ClassWithOneKeyConstructor;

class JPAInstanceCreatorTest {

  private JPAInstanceCreator<?> cut;
  private OData odata;
  private JPAEntityType et;

  @BeforeEach
  void setup() {
    odata = OData.newInstance();
    et = mock(JPAEntityType.class);
  }

  @Test
  void testGetConstructorWithIdClass() throws ODataJPAModelException, ODataJPAProcessorException {
    fillCompoundKey();
    buildTypeWithCompoundKey(ClassWithIdClassConstructor.class);
    cut = new JPAInstanceCreator<>(odata, et);

    final Optional<Constructor<Object>> constructor = cut.determinePreferredConstructor();

    assertTrue(constructor.isPresent());
    assertEquals(1, constructor.get().getParameterCount());
  }

  @Test
  void testGetConstructorWithCompoundKey() throws ODataJPAModelException, ODataJPAProcessorException {
    fillCompoundKey();
    buildTypeWithCompoundKey(ClassWithMultipleKeysSetter.class);
    cut = new JPAInstanceCreator<>(odata, et);

    final Optional<Constructor<Object>> constructor = cut.determinePreferredConstructor();

    assertTrue(constructor.isPresent());
    assertEquals(0, constructor.get().getParameterCount());
  }

  @Test
  void testGetConstructorNoSetterReturnsNull() throws ODataJPAModelException, ODataJPAProcessorException {
    fillCompoundKey();
    buildTypeWithCompoundKey(ClassWithMultipleKeysConstructor.class);
    cut = new JPAInstanceCreator<>(odata, et);

    final Optional<Constructor<Object>> constructor = cut.determinePreferredConstructor();

    assertFalse(constructor.isPresent());
  }

  @Test
  void testGetConstructorSingleKey() throws ODataJPAModelException, ODataJPAProcessorException {
    final JPAAttribute key = mock(JPAAttribute.class);
    final List<JPAAttribute> keys = Arrays.asList(key);
    when(key.getInternalName()).thenReturn("key");
    when(key.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return String.class;
      }
    });
    when(et.getKey()).thenReturn(keys);
    when(et.getTypeClass()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return ClassWithOneKeyConstructor.class;
      }
    });
    when(et.getKeyType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return UUID.class;
      }
    });
    when(et.hasCompoundKey()).thenReturn(false);
    cut = new JPAInstanceCreator<>(odata, et);
    final Optional<Constructor<Object>> constructor = cut.determinePreferredConstructor();
    assertTrue(constructor.isPresent());
    assertEquals(1, constructor.get().getParameterCount());
    assertEquals(UUID.class, constructor.get().getParameterTypes()[0]);
  }

  @Test
  void testGetConstructorSingleKeyNoKey() throws ODataJPAModelException, ODataJPAProcessorException {

    final List<JPAAttribute> keys = Arrays.asList(fillOneKey("key", UUID.class, EdmPrimitiveTypeKind.Guid));
    buildTypeWithSingleKey(keys);
    cut = new JPAInstanceCreator<>(odata, et);
    final Optional<Constructor<Object>> constructor = cut.determinePreferredConstructor();
    assertTrue(constructor.isPresent());
    assertEquals(1, constructor.get().getParameterCount());
    assertEquals(UUID.class, constructor.get().getParameterTypes()[0]);
  }

  @Test
  void testGetConstructorNoResult() throws ODataJPAModelException, ODataJPAProcessorException {
    fillCompoundKey();
    when(et.getTypeClass()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return ClassWithMultipleKeysNoConstructor.class;
      }
    });
    when(et.getKeyType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return ClassWithMultipleKeysNoConstructor.class;
      }
    });
    when(et.hasCompoundKey()).thenReturn(true);
    cut = new JPAInstanceCreator<>(odata, et);
    final Optional<Constructor<Object>> constructor = cut.determinePreferredConstructor();
    assertFalse(constructor.isPresent());
  }

  @Test
  void testGetConstructorRethrowsException() throws ODataJPAModelException {

    when(et.getKey()).thenThrow(ODataJPAModelException.class);
    when(et.getTypeClass()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return ClassWithOneKeyAndEmptyConstructor.class;
      }
    });
    when(et.getKeyType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return UUID.class;
      }
    });
    when(et.hasCompoundKey()).thenReturn(false);
    cut = new JPAInstanceCreator<>(odata, et);
    assertThrows(ODataJPAProcessorException.class, () -> cut.determinePreferredConstructor());
  }

  @Test
  void testCreateInstanceWithIdClass() throws ODataJPAProcessorException, ODataJPAModelException {
    fillCompoundKey();
    buildTypeWithCompoundKey(ClassWithIdClassConstructor.class);
    final UriParameter keyParam1 = fillUriParameter("id1", "'Test'");
    final UriParameter keyParam2 = fillUriParameter("id2", "12");
    final UriParameter keyParam3 = fillUriParameter("id3", "'654645'");
    final List<UriParameter> keyPredicates = Arrays.asList(keyParam1, keyParam2, keyParam3);
    cut = new JPAInstanceCreator<>(odata, et);

    final Optional<Object> instance = cut.createInstance(keyPredicates);

    assertNotNull(instance);
    assertTrue(instance.isPresent());
    final ClassWithIdClassConstructor act = (ClassWithIdClassConstructor) instance.get();
    assertEquals("Test", act.getKey().getId1());
    assertEquals(12, act.getKey().getId2());
    assertEquals("654645", act.getKey().getId3());
  }

  @Test
  void testCreateInstanceWithCompoundKeySetter() throws ODataJPAProcessorException, ODataJPAModelException {
    fillCompoundKey();
    buildTypeWithCompoundKey(ClassWithMultipleKeysSetter.class);
    final UriParameter keyParam1 = fillUriParameter("id1", "'Test'");
    final UriParameter keyParam2 = fillUriParameter("id2", "12");
    final UriParameter keyParam3 = fillUriParameter("id3", "'654645'");
    final List<UriParameter> keyPredicates = Arrays.asList(keyParam1, keyParam2, keyParam3);
    cut = new JPAInstanceCreator<>(odata, et);

    final Optional<Object> instance = cut.createInstance(keyPredicates);

    assertNotNull(instance);
    assertTrue(instance.isPresent());
    final ClassWithMultipleKeysSetter act = (ClassWithMultipleKeysSetter) instance.get();
    assertEquals("Test", act.getId1());
    assertEquals(12, act.getId2());
    assertEquals("654645", act.getId3());
  }

  @Test
  void testCreateInstanceWithSingleKey() throws ODataJPAProcessorException, ODataJPAModelException {

    final List<JPAAttribute> keys = Arrays.asList(fillOneKey("key", UUID.class, EdmPrimitiveTypeKind.Guid));
    buildTypeWithSingleKey(keys);
    final UUID value = UUID.randomUUID();
    final UriParameter keyParam1 = fillUriParameter("key", value.toString());
    final List<UriParameter> keyPredicates = Arrays.asList(keyParam1);
    cut = new JPAInstanceCreator<>(odata, et);

    final Optional<Object> instance = cut.createInstance(keyPredicates);

    assertNotNull(instance);
    assertTrue(instance.isPresent());
    final ClassWithOneKeyAndEmptyConstructor act = (ClassWithOneKeyAndEmptyConstructor) instance.get();
    assertEquals(value, act.getKey());
  }

  @Test
  void testCreateInstanceReturnsEmptyIfNoConstructorFound() throws ODataJPAModelException, ODataJPAProcessorException {

    fillCompoundKey();
    final UriParameter keyParam1 = fillUriParameter("id1", "'Test'");
    final UriParameter keyParam2 = fillUriParameter("id2", "12");
    final UriParameter keyParam3 = fillUriParameter("id3", "'654645'");
    final List<UriParameter> keyPredicates = Arrays.asList(keyParam1, keyParam2, keyParam3);

    when(et.getTypeClass()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return ClassWithMultipleKeysNoConstructor.class;
      }
    });
    when(et.getKeyType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return ClassWithMultipleKeysNoConstructor.class;
      }
    });
    when(et.hasCompoundKey()).thenReturn(true);
    cut = new JPAInstanceCreator<>(odata, et);
    final Optional<Object> constructor = cut.createInstance(keyPredicates);
    assertFalse(constructor.isPresent());
  }

  @Test
  void testCreateInstanceRethrowsException() throws ODataJPAProcessorException, ODataJPAModelException {

    final List<JPAAttribute> keys = Arrays.asList(fillOneKey("key", UUID.class, EdmPrimitiveTypeKind.Guid));
    buildTypeWithSingleKey(keys);
    final UUID value = UUID.randomUUID();
    final UriParameter keyParam1 = fillUriParameter("key", value.toString());
    final List<UriParameter> keyPredicates = Arrays.asList(keyParam1);
    when(et.getPath("key")).thenThrow(ODataJPAModelException.class);
    cut = new JPAInstanceCreator<>(odata, et);

    assertThrows(ODataJPAProcessorException.class, () -> cut.createInstance(keyPredicates));
  }

  @Test
  void testCreateInstanceReturnsEmptyIdClassNoConstructor() throws ODataJPAProcessorException, ODataJPAModelException {
    fillCompoundKey();
    buildTypeWithCompoundKey(ClassWithIdClassWithoutConstructor.class, ClassWithMultipleKeysConstructor.class);
    final UriParameter keyParam1 = fillUriParameter("id1", "'Test'");
    final UriParameter keyParam2 = fillUriParameter("id2", "12");
    final UriParameter keyParam3 = fillUriParameter("id3", "'654645'");
    final List<UriParameter> keyPredicates = Arrays.asList(keyParam1, keyParam2, keyParam3);
    cut = new JPAInstanceCreator<>(odata, et);

    final Optional<Object> instance = cut.createInstance(keyPredicates);

    assertNotNull(instance);
    assertFalse(instance.isPresent());
  }

  private UriParameter fillUriParameter(final String name, final String value) {
    final UriParameter keyParam1 = mock(UriParameter.class);
    when(keyParam1.getName()).thenReturn(name);
    when(keyParam1.getText()).thenReturn(value);
    return keyParam1;
  }

  private void buildTypeWithCompoundKey(final Class<?> typeClazz) {
    buildTypeWithCompoundKey(typeClazz, ClassWithMultipleKeysSetter.class);
  }

  private void buildTypeWithCompoundKey(final Class<?> typeClazz, final Class<?> idClass) {

    when(et.getTypeClass()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return typeClazz;
      }
    });
    when(et.getKeyType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return idClass;
      }
    });
    when(et.hasCompoundKey()).thenReturn(true);
  }

  private void fillCompoundKey() throws ODataJPAModelException {
    final JPAAttribute id1 = fillOneKey("id1", String.class, EdmPrimitiveTypeKind.String);
    final JPAAttribute id2 = fillOneKey("id2", Integer.class, EdmPrimitiveTypeKind.Int32);
    final JPAAttribute id3 = fillOneKey("id3", String.class, EdmPrimitiveTypeKind.String);

    final List<JPAAttribute> keys = Arrays.asList(id1, id2, id3);
    when(et.getKey()).thenReturn(keys);
  }

  private JPAAttribute fillOneKey(final String name, final Class<?> type, final EdmPrimitiveTypeKind edmType) throws ODataJPAModelException {
    final JPAAttribute id1 = mock(JPAAttribute.class);
    final JPAPath id1Path = mock(JPAPath.class);
    final CsdlProperty id1Property = mock(CsdlProperty.class);
    when(id1.getInternalName()).thenReturn(name);
    when(id1.getJavaType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return type;
      }
    });
    when(id1.getType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return type;
      }
    });
    when(id1.getEdmType()).thenReturn(edmType);
    when(et.getPath(name)).thenReturn(id1Path);
    when(et.getAttribute(name)).thenReturn(Optional.of(id1));
    when(id1Path.getLeaf()).thenReturn(id1);

    when(id1Property.isNullable()).thenReturn(Boolean.FALSE);
    when(id1Property.getPrecision()).thenReturn(null);
    when(id1Property.getScale()).thenReturn(null);
    if (type == String.class)
      when(id1Property.getMaxLength()).thenReturn(255);
    when(id1.getProperty()).thenReturn(id1Property);
    return id1;
  }

  private void buildTypeWithSingleKey(final List<JPAAttribute> keys) throws ODataJPAModelException {
    when(et.getKey()).thenReturn(keys);
    when(et.getTypeClass()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return ClassWithOneKeyAndEmptyConstructor.class;
      }
    });
    when(et.getKeyType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return UUID.class;
      }
    });
    when(et.hasCompoundKey()).thenReturn(false);
  }
}
