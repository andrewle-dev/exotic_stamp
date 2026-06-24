package metro.ExoticStamp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Marks legacy {@code /api/v1/collections/**} responses as deprecated for API consumers.
 */
@Component
public class DeprecatedLegacyCollectionsInterceptor implements HandlerInterceptor {

    static final String DEPRECATION_HEADER = "Deprecation";
    static final String LINK_HEADER = "Link";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/api/v1/collections")) {
            response.setHeader(DEPRECATION_HEADER, "true");
            response.setHeader(LINK_HEADER, "</api/v1/collection>; rel=\"successor-version\"");
        }
        return true;
    }
}
