package com.example.demo;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryXmlConfig;
import com.hazelcast.core.HazelcastInstance;
import org.apache.camel.component.hazelcast.HazelcastUtil;
import org.apache.camel.component.hazelcast.policy.HazelcastRoutePolicy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Configuration
public class HazelcastConfiguration {
    @Value("classpath:hazelcast-local.xml")
    private Resource resource;

    @Bean(name = "hazelcastConfig")
    public Config hazelcastConfig() {
        String hazelcastConfiguration = getHazelcastConfiguration();
        return new InMemoryXmlConfig(hazelcastConfiguration);
    }

    @Bean(name = "hazelcastInstance")
    public HazelcastInstance hazelcastInstance(@Qualifier("hazelcastConfig") Config config) {
        return HazelcastUtil.newInstance(config);
    }

    @Bean(name = "hazelcastRoutePolicy")
    public HazelcastRoutePolicy hazelcastRoutePolicy(@Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance) {
        final HazelcastRoutePolicy hazelcastRoutePolicy = new HazelcastRoutePolicy(hazelcastInstance);

        hazelcastRoutePolicy.setLockMapName("my_lock_map");
        hazelcastRoutePolicy.setLockKey("my_leader_lock");
        hazelcastRoutePolicy.setLockValue("my_leader_lock_value");

        return hazelcastRoutePolicy;
    }

    private String getHazelcastConfiguration() {
        try {
            return Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
