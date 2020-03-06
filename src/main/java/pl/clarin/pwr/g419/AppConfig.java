package pl.clarin.pwr.g419;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile("cli")
@ComponentScan({"pl.clarin.pwr.g419"})
class AppConfig {
}
