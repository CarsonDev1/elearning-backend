@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(Arrays.asList(
                    new Server().url("https://backendlearning.xyz").description("HTTPS Server")
                ))
                .info(new Info()
                        .title("Japanese Learning API")
                        .version("1.0")
                        .description("API Documentation"));
    }
}
