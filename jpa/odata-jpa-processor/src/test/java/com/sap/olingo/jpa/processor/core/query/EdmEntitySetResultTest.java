package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEntityContainer;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmNavigationPropertyBinding;
import org.apache.olingo.server.api.uri.UriParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EdmEntitySetResultTest {
  private EdmBindingTargetResult cut;
  private List<UriParameter> keys;
  private EdmEntitySet es;
  private EdmEntitySet est;

  @BeforeEach
  void setup() {
    keys = new ArrayList<>();
    es = mock(EdmEntitySet.class);
    when(es.getName()).thenReturn("Persons");
    est = mock(EdmEntitySet.class);
    when(est.getName()).thenReturn("BusinessPartnerRoles");
  }

  @Test
  void testGetEntitySetName() {
    cut = new EdmBindingTargetResult(es, keys, "");
    assertEquals("Persons", cut.getName());
  }

  @Test
  void testGetEntitySetGetKeys() {
    final UriParameter key = mock(UriParameter.class);
    when(key.getName()).thenReturn("ID");
    keys.add(key);
    cut = new EdmBindingTargetResult(es, keys, "");
    assertEquals(keys, cut.getKeyPredicates());
  }

  @Test
  void testGetEntitySetGet() {
    cut = new EdmBindingTargetResult(es, keys, "Roles");
    assertEquals("Roles", cut.getNavigationPath());
  }

  @Test
  void testDetermineTargetEntitySetWithNaviNull() {
    when(es.getNavigationPropertyBindings()).thenReturn(null);
    cut = new EdmBindingTargetResult(es, keys, null);
    assertEquals(es, cut.getTargetEdmBindingTarget());
  }

  @Test
  void testDetermineTargetEntitySetWithNaviEmpty() {
    when(es.getNavigationPropertyBindings()).thenReturn(null);
    cut = new EdmBindingTargetResult(es, keys, "");
    assertEquals(es, cut.getTargetEdmBindingTarget());
  }

// return edmEntitySet.getEntityContainer().getEntitySet(navi.getTarget());
  @Test
  void testDetermineTargetEntitySetWithNavigation() {
    final EdmEntityContainer container = mock(EdmEntityContainer.class);
    final List<EdmNavigationPropertyBinding> bindings = new ArrayList<>(2);
    EdmNavigationPropertyBinding binding = mock(EdmNavigationPropertyBinding.class);
    bindings.add(binding);
    when(binding.getPath()).thenReturn("InhouseAddress");

    binding = mock(EdmNavigationPropertyBinding.class);
    bindings.add(binding);
    when(binding.getPath()).thenReturn("Roles");
    when(binding.getTarget()).thenReturn("BusinessPartnerRoles");
    when(es.getEntityContainer()).thenReturn(container);
    when(es.getNavigationPropertyBindings()).thenReturn(bindings);
    when(container.getEntitySet("BusinessPartnerRoles")).thenReturn(est);

    cut = new EdmBindingTargetResult(es, keys, "Roles");
    assertEquals(es, cut.getEdmBindingTarget());
    assertEquals(est, cut.getTargetEdmBindingTarget());
  }
}
