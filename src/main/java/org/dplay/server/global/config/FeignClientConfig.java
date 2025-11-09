package org.dplay.server.global.config;

import org.dplay.server.DPlayServerApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackageClasses = DPlayServerApplication.class)
public class FeignClientConfig {
}
