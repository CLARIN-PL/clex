package pl.clarin.pwr.g419;


import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@Import( {AppConfig.class})
public class App {

  public static void main(final String[] args) throws Exception {
    new SpringApplicationBuilder(AppConfig.class).run(args);
  }

}
