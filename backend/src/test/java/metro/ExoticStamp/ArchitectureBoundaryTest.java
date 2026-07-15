package metro.ExoticStamp;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces Spring-pragmatic DDD boundaries (ADR-001).
 * Do not add broad exclusions. Temporary waivers must document class, reason, and removal phase.
 */
class ArchitectureBoundaryTest {

    private static final String[] MODULES = {
            "auth", "user", "rbac", "metro", "collection", "reward", "community", "monetization", "mobile"
    };

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("metro.ExoticStamp");
    }

    @Test
    void domainMustNotDependOnPresentation() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..presentation..")
                .check(classes);
    }

    @Test
    void domainMustNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(classes);
    }

    @Test
    void domainMustNotDependOnApplication() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..application..")
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
    void applicationMustNotDependOnModuleInfrastructure() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("metro.ExoticStamp.modules..infrastructure..")
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
        // No approved shared domain types for presentation yet. Keep strict.
        noClasses()
                .that().resideInAPackage("..presentation..")
                .should().dependOnClassesThat().resideInAPackage("..domain..")
                .check(classes);
    }

    @Test
    void authApplicationMustNotDependOnUserDomainRepository() {
        noClasses()
                .that().resideInAPackage("metro.ExoticStamp.modules.auth.application..")
                .should().dependOnClassesThat().resideInAPackage("metro.ExoticStamp.modules.user.domain.repository..")
                .check(classes);
    }

    @Test
    void collectionDomainMustNotDependOnApplication() {
        noClasses()
                .that().resideInAPackage("metro.ExoticStamp.modules.collection.domain..")
                .should().dependOnClassesThat().resideInAPackage("metro.ExoticStamp.modules.collection.application..")
                .because("collection domain policies must not import application")
                .check(classes);
    }

    @Test
    void modulesMustNotDependOnOtherModulesInfrastructure() {
        for (String module : MODULES) {
            String foreignInfra = "metro.ExoticStamp.modules." + module + ".infrastructure..";
            String ownModule = "metro.ExoticStamp.modules." + module + "..";
            noClasses()
                    .that().resideInAPackage("metro.ExoticStamp.modules..")
                    .and().resideOutsideOfPackage(ownModule)
                    .should().dependOnClassesThat().resideInAPackage(foreignInfra)
                    .because("module boundaries must not access " + module + " infrastructure")
                    .check(classes);
        }
    }
}
