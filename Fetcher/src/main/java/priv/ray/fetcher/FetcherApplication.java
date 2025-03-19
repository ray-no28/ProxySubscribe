package priv.ray.fetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "priv.ray.fetcher")
@EnableScheduling // 启用定时任务
public class FetcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(FetcherApplication.class, args);
    }

}
