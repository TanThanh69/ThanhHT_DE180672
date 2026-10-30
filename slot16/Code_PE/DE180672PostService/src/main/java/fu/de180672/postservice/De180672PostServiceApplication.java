package fu.de180672.postservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class De180672PostServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(De180672PostServiceApplication.class, args);
    }
}
