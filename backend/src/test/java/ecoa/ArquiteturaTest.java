package ecoa;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.time.LocalDate;
import java.time.ZoneId;

/** A regra de dependência, conferida em vez de prometida (ADR-0012, arquitetura.md). */
@AnalyzeClasses(packages = "ecoa", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

  @ArchTest
  static final ArchRule regrasSoImportamABibliotecaPadrao =
      classes()
          .that()
          .resideInAPackage("ecoa.regras..")
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage("java..", "ecoa.regras..");

  @ArchTest
  static final ArchRule controllerNaoUsaRepository =
      noClasses()
          .that()
          .resideInAPackage("ecoa.controller..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("ecoa.repository..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule hojeSoPeloRelogio =
      noClasses()
          .should()
          .callMethod(LocalDate.class, "now")
          .orShould()
          .callMethod(LocalDate.class, "now", ZoneId.class)
          .because("o hoje vem do relógio do sistema, no horário de Brasília (ADR-0014)");
}
