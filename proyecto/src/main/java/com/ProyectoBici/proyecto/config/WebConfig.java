package com.ProyectoBici.proyecto.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Expone la carpeta física de uploads (fotos de bicis) vía /uploads/**.
 * Sin esto, el <img th:src="@{/uploads/bicicletas/...}"> del garaje da 404
 * aunque el archivo sí se haya guardado en disco.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${bikecare.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String location = uploadPath.toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
                String uri = req.getRequestURI();
                if (uri.startsWith("/welcome") || uri.startsWith("/admin")) {
                    res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
                    res.setHeader("Pragma", "no-cache");
                    res.setDateHeader("Expires", 0);
                }
                return true;
            }
        });
    }
}
