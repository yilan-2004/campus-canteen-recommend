package com.example.shitang.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 配置
 *
 * 访问:
 *  - http://localhost:8080/doc.html (Knife4j 增强 UI,推荐)
 *  - http://localhost:8080/swagger-ui.html (原生 UI)
 *  - http://localhost:8080/v3/api-docs (OpenAPI JSON)
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer";

    @Bean
    public OpenAPI shitangOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园食堂个性化推荐系统 API")
                        .description("第五阶段:含认证、基础 CRUD、学生端交互、推荐系统(热门/高分/个性化/混合/场景)及 A/B 评估")
                        .version("1.0.0")
                        .contact(new Contact().name("以蓝").email("yilan@peihua.edu.cn"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("在请求头中携带 JWT,格式: Bearer {token}")));
    }
}
