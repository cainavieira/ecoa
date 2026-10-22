package ecoa.config;

import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final BloqueioInterceptor bloqueio;

  public WebConfig(BloqueioInterceptor bloqueio) {
    this.bloqueio = bloqueio;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registro) {
    // O bloqueado continua vendo a própria conta, e entrando e saindo (endpoints.md)
    registro.addInterceptor(bloqueio).excludePathPatterns("/eu", "/auth/**");
  }

  @Override
  public void addFormatters(FormatterRegistry registro) {
    // O enum na URL vem em minúsculas, como sai no JSON: ?status=pendente (endpoints.md)
    ApplicationConversionService.addApplicationConverters(registro);
  }
}
