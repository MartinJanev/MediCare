package mk.ukim.finki.medicare.config;

import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Serves the Angular build from classpath:/static. A URL that is not a real file (for example
 * /referrals/42, typed straight into the browser) gets index.html, so Angular's router can handle it.
 * Anything under /api is never rewritten: an unknown API path stays a 404.
 */
@Configuration
public class SpaFallbackConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.isReadable()) {
                            return requested;
                        }
                        return resourcePath.startsWith("api/") ? null : new ClassPathResource("static/index.html");
                    }
                });
    }
}
