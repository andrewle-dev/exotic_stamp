package metro.ExoticStamp;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureBoundaryTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("metro.ExoticStamp");
    }

    @Test
    void domainMustNotDependOnPresentationOrInfrastructure() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..presentation..", "..infrastructure..")
                .check(classes);
    }

    @Test
    void applicationMustNotDependOnPresentation() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..presentation..")
                .check(classes);
    }

    @Test
    void applicationMustNotUseJpaRepositoryDirectly() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().areAssignableTo(JpaRepository.class)
                .check(classes);
    }

    @Test
    void presentationMustNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..presentation..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(classes);
    }

    @Test
    void presentationMustNotDependOnDomain() {
        noClasses()
                .that().resideInAPackage("..presentation..")
                .should().dependOnClassesThat().resideInAPackage("..domain..")
                .check(classes);
    }
}
