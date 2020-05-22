package com.fc;

import com.fc.configCentre.annotation.EnableConfigurationCenterClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author  配置中心
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableConfigurationCenterClient
public class ConfigurationEntreServerApplication {

    public static void main(String[] args) {

        SpringApplication.run(ConfigurationEntreServerApplication.class,args);

    }
}
