# RSocket Sandbox

Sample codes of RSocket Java  and Spring RSocket integration.

## Docs

[Using Rsocket with Spring Boot](./docs/GUIDE.md)([medium's link](https://medium.com/@hantsy/using-rsocket-with-spring-boot-cfc67924d06a))

[Building a CRUD application with RSocket and Spring](./docs/crud.md)

## Sample Codes

* **vanilla**  rsocket-java sample

* **tcp** Spring Boot based application using TCP protocol between client and server 

* **websocket** Using WebSocket as transport protocol instead in a Webflux server.

* **security**  Spring Security RSocket integration.

* **server-requester**  Sending message to client via Server `RSocketRequster`.

* **integration**  Spring Integration RSocket inbound and outbound gateway.

  * **client** Sending messages to server via Spring Integration RSocket OutboundGateay.
  * **client-regular** Simple example using Spring Boot RSocket Starter aka  `RSocketRequestor`.
  * **server-vanilla** Handling messages via Spring Integration RSocket InboundGateway.
  * **server**  Similar with **server-vanilla**, but reuse Spring Boot RSocket Starter  and declare the `ServerRSocketConnecter`  bean through `ServerRSocketMessageHandler`.
  * **server-regular** Simple example using Spring Boot RSocket Starter aka `@Controller` and `@MessageMapping`.
* **crud** Client and server CRUD sample.
  * **client** Sending messages to server via  `RSocketRequestor` and TCP protocol.
  * **server** Running as Netty/TCP and handling request via TCP protocol(no web/http support).

## References

* [Spring Framework Reference](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/web-reactive.html#rsocket)
* [Spring Secuirty Reference](https://docs.spring.io/spring-security/site/docs/current/reference/html/rsocket.html)
* [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/2.2.4.RELEASE/reference/htmlsingle/#boot-features-rsocket)
* [Spring Integration Reference](https://docs.spring.io/spring-integration/reference/html/rsocket.html)
* [RSocket Using Spring Boot](https://www.baeldung.com/spring-boot-rsocket) by Baeldung
* [RSocket Messaging with Spring](https://www.youtube.com/watch?v=iSSrZoGtoSE)
* [Reactive Architectures with RSocket and Spring Cloud Gateway](https://www.youtube.com/watch?v=PfbycN_eqhg)

* [bclozel/spring-flights](https://github.com/bclozel/spring-flights)
* [spring-projects/spring-security/tree/5.2.2.RELEASE/samples/boot/hellorsocket](https://github.com/spring-projects/spring-security/tree/5.2.2.RELEASE/samples/boot/hellorsocket)
* [spencergibb/rsocket-routing-sample](https://github.com/spencergibb/rsocket-routing-sample)







