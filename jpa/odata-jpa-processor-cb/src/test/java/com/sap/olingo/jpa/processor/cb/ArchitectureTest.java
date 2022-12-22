package com.sap.olingo.jpa.processor.cb;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.sap.olingo.jpa.processor.cb")
class ArchitectureTest { // NOSONAR

  @ArchTest
  static final ArchRule visibilityRule = classes()
      .that()
      .resideInAPackage("..impl..")
      .should()
      .notBePublic()
      .orShould()
      .haveNameMatching(
          "com.sap.olingo.jpa.processor.cb.impl.EntityManagerWrapper");

  @ArchTest
  static final ArchRule layerRule = layeredArchitecture()
      .consideringAllDependencies()
      .layer("Api").definedBy("..api..")
      .layer("Implementation").definedBy("..impl..")
      .layer("Reuse").definedBy("..joiner..", "..exeptions..")

      .whereLayer("Api").mayNotBeAccessedByAnyLayer()
      .whereLayer("Implementation").mayOnlyBeAccessedByLayers("Api")
      .whereLayer("Reuse").mayOnlyBeAccessedByLayers("Implementation");
}
