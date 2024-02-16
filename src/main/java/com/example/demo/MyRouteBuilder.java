package com.example.demo;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hazelcast.policy.HazelcastRoutePolicy;
import org.springframework.stereotype.Component;

@Component
public class MyRouteBuilder extends RouteBuilder  {
    private final HazelcastRoutePolicy hazelcastRoutePolicy;

    public MyRouteBuilder(final HazelcastRoutePolicy hazelcastRoutePolicy) {
        this.hazelcastRoutePolicy = hazelcastRoutePolicy;
    }

    @Override
    public void configure() {
        from("timer:test-normal")
                .routePolicy(hazelcastRoutePolicy)
                .to("log:normal?level=INFO");

        from("timer:test-resequencer")
                .routePolicy(hazelcastRoutePolicy)
                .resequence(header("HEADER_MESSAGE_TIMESTAMP"))
                .batch()
                .to("log:resequencer?level=INFO")
                .end();
    }
}
