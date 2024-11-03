![The official Logo of Xenos Client](.github/images/logo.png "Xenos Client")

![A visual badge for the latest release](https://img.shields.io/github/v/release/scrayosnet/xenos-client-kotlin "Latest Release")
![A visual badge for the workflow status](https://img.shields.io/github/actions/workflow/status/scrayosnet/xenos-client-kotlin/gradle.yml "Workflow Status")
![A visual badge for the license](https://img.shields.io/github/license/scrayosnet/xenos "License")
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=scrayosnet_xenos-client-kotlin&metric=coverage)](https://sonarcloud.io/summary/new_code?id=scrayosnet_xenos-client-kotlin "Coverage")
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=scrayosnet_xenos-client-kotlin&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=scrayosnet_xenos-client-kotlin "Reliability")
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=scrayosnet_xenos-client-kotlin&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=scrayosnet_xenos-client-kotlin "Maintainability")
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=scrayosnet_xenos-client-kotlin&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=scrayosnet_xenos-client-kotlin "Security")

Xenos Client for Kotlin is a [gRPC][grpc-docs] client/binding that wraps the raw protobuf stubs and communication with
[Xenos][xenos-project] in an easy-to-use, polished API. It is possible to add the client as a dependency in Maven and
Gradle, simplifying the process of supporting [Xenos][xenos-project] within projects.

The bindings are automatically created and then documented and equipped with sane defaults to assist developers in
quickly adapting the API and removing the need to understand the inner workings of Protobuf and [gRPC][grpc-docs].

## Motivation

For the original motivation regarding the development of Xenos, visit the original project [here][xenos-project]. Xenos
Client for Kotlin was developed to simplify the embedding into existing Minecraft infrastructure while still enabling
advances syntax sugar for Kotlin based setups. While we do our best to support plain Java projects, this client is
developed with a "Kotlin first" approach.

This is reflected in the heavy use of suspended functions. They are the ideal language feature to reflect asynchronous
communication with a service, so they had to be included into our client. While they are strongly embedded into the
foundation of Kotlin, and the use within plain Java projects is, admittedly, a little cumbersome, there are still a few
[options to invoke them][suspend-from-java] from within Java code. But we recommend calling the API from Kotlin instead
and switching to a hybrid project.

We strive to optimize this client with efficient defaults and still allow users to tweak the client to their liking or
infrastructural needs. This way, new developers don't need to meddle with things like gRPC pipeline configuration or
the decision of which stub to use, but can focus on developing their applications instead.

## Major Features

* Communicate with your local or remote [Xenos][xenos-project] instance through [gRPC][grpc-docs] with blazing fast
  response times.
* Treat the request of Minecraft profile information like local data and never think about rate limits or performance
  impact ever again.
* Get cached responses if the Mojang API is currently unavailable to continue working even while there is an outage.
* Use pooled connections to [Xenos][xenos-project] to benefit from the HA capabilities.
* Ignore the inner workings of the protocol transfer and just think about the data. Xenos-Client will handle the rest
  for you!
* Invoke simplified methods to retrieve the data that you really care about, without any boilerplate code on your end.

## Getting started

In order to use this client, your application needs to have access to some instance of [Xenos][xenos-project] and this
instance needs to have [gRPC][grpc-docs] enabled. For information on how to deploy your own instance and preparing it
for connections from your clients, visit [this documentation][xenos-project].

### Dependency

After your instance is up and running, you have to add Xenos Client for Kotlin to your dependencies:

```kotlin
dependencies {
    // make sure to specify the latest version
    api("net.scrayos", "xenos-client", "0.8.0")

    // choose your own gRPC runtime or use an existing one
    runtimeOnly("io.grpc", "grpc-netty", "1.68.1")
}
```

Instead of `grpc-netty` you may also use `grpc-netty-shaded` (which hides the transitive Netty dependency) or any other
compatible gRPC runtime. We recommend sticking with the default runtime but in theory, Xenos Client should be compatible
with either runtime.

Since Xenos Client is deployed to the official Maven Central repository, you don't need to include any special repositories:

```kotlin
repositories {
    mavenCentral()
}
```

After reloading your Gradle project, you should now be able to retrieve all related classes.

### Usage

Below you can find a guide on obtaining a `XenosClient`, the central interface to perform lookups through
[Xenos][xenos-project]. Please use the version, appropriate for your language (Java/Kotlin).

#### Kotlin

```kotlin
// host and port could be supplied by an environment variable or flag
val client = GrpcXenosClient("127.0.0.1", 50051)

// any request can be performed on the client while it is open
val profile = client.getProfile(UUID.randomUUID())
```

#### Java

```java
// host and port could be supplied by an environment variable or flag
XenosClient client = new GrpcXenosClient("127.0.0.1", 50051);

// any request can be performed on the client while it is open
ProfileResult profile = client.getProfile(UUID.randomUUID());
```

## Reporting Security Issues

To report a security issue for this project, please note our [Security Policy][security-policy].

## Code of Conduct

Participation in this project comes under the [Contributor Covenant Code of Conduct][code-of-conduct].

## How to contribute

Thanks for considering contributing to this project! In order to submit a Pull Request, please read
our [contributing][contributing-guide] guide. This project is in active development, and we're always happy to receive
new contributions!

## License

This project is developed and distributed under the MIT License. See [this explanation][mit-license-doc] for a rundown
on what that means.

[xenos-project]: https://github.com/scrayosnet/xenos

[grpc-docs]: https://grpc.io/

[suspend-from-java]: https://www.baeldung.com/kotlin/suspend-functions-from-java

[semver-docs]: https://semver.org/lang/de/

[security-policy]: SECURITY.md

[code-of-conduct]: CODE_OF_CONDUCT.md

[contributing-guide]: CONTRIBUTING.md

[mit-license-doc]: https://choosealicense.com/licenses/mit/
