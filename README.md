# Camel & Hazelcast resequencer bug proof of concept

This repository is a proof of concept for a bug identified in Camel processors library.

## Use case of Camel & Hazelcast

Camel is used in an Enterprise Integration Pattern project with Spring Boot. Hazelcast library is included to deploy the project in a cluster with active-passive deployment model. The aim is to have multiple containers running but only one of them to consume messages with a Camel route. When this leader container dies for any reason, one of the other containers in cluster will continue consuming messages seamlessly. Hazelcast provides this deployment model for us.

## Problem with batch mode resequencer

Any other Camel routes we have works with this Hazelcast setup, except a route with a batch mode resequencer processor is registered.

### Observed behavior

This route with a batch mode resequencer processor does not start after a container takes leadership. An exception of type `java.lang.IllegalThreadStateException` is observed in application logs.

### Root cause

The [Resequencer.java](https://github.com/apache/camel/blob/main/core/camel-core-processor/src/main/java/org/apache/camel/processor/Resequencer.java) file uses a raw thread named `sender`. This thread is started once the route is registered, but if we want to stop the route and restart again, this [sender.start()](https://github.com/apache/camel/blob/efec4cadf81753c79ce713c81ea9e463c114d293/core/camel-core-processor/src/main/java/org/apache/camel/processor/Resequencer.java#L343) call throws an exception. Because the thread has already been started before. [Here's the related page in javadoc](https://docs.oracle.com/javase/8/docs/api/java/lang/Thread.html#start--).

## Reproducing the bug

Clone this repository and build the project

```shell
mvn clean install package
```

Open two terminals and run these commands in one of each

```shell
mvn -Plocal-1 spring-boot:run
```

```shell
mvn -Plocal-2 spring-boot:run
```

After a while these two processes should find each other using Hazelcast and one of them should announce itself as the leader. After that, you will observe the `java.lang.IllegalThreadStateException` error message and one logger called "normal" should log a null message once every second.

If you go to [MyRouteBuilder.java](src/main/java/com/example/demo/MyRouteBuilder.java) file, you will observe two Camel routes, one with a batch resequencer processor and one with not. The route with no processor is logging to "normal" and those logs can be seen. The route with resequencer is logging to "resequencer" and those logs can not be seen because of the route restart failure.
