package metro.ExoticStamp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        tags = {
                @Tag(
                        name = "Auth",
                        description = "Authentication and session lifecycle APIs"
                ),
                @Tag(
                        name = "User",
                        description = "User profile management APIs"
                ),
                @Tag(
                        name = "RBAC",
                        description = "Role and permission management APIs"
                ),
                @Tag(
                        name = "Collection Runtime",
                        description = "Canonical MVP collection APIs at /api/v1/collection (collect, stamp book, my stamps)"
                ),
                @Tag(
                        name = "Mobile App Config",
                        description = "Mobile client update and maintenance policy (not installed binary version)"
                )
        },
        info = @Info(
                title = "Exotic Stamp API",
                version = "v1",
                description = "OpenAPI specification for Exotic Stamp backend services. Use this document for contract-first integration and manual API testing.",
                contact = @Contact(
                        name = "Exotic Stamp Backend Team",
                        email = "dev.andrewle@gmail.com",
                        url = "https://www.linkedin.com/in/d%C5%A9ng-l%C3%AA-v%C4%83n-7a48a6259/"
                ),
                license = @License(
                        name = "Apache 2.0 License",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                ),
                termsOfService = "https://www.termsfeed.com/live/de90fe36-021f-4bef-92df-c1130e800ef2"
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Local development server"
                ),
                @Server(
                        url = "https://backend.exoticstamp.com",
                        description = "Production Server"
                )
        },
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Authorization header using the Bearer scheme",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT"
)
@Configuration
public class OpenApiConfig {
}
