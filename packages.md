# Module Xenos Client

Xenos Client for Kotlin is a gRPC client/binding that wraps the raw protobuf stubs and communication with Xenos in an
easy-to-use, polished API. It is possible to add the client as a dependency in Maven and Gradle, simplifying the process
of supporting Xenos within projects.

The bindings are automatically created and then documented and equipped with sane defaults to assist developers in
quickly adapting the API and removing the need to understand the inner workings of Protobuf and gRPC.

# Package net.scrayos.xenos.client

Provides the general functionality of the library with the endpoints and the overall bindings for the Xenos API. This
is the primary way that users will be interacting with the Xenos API.

# Package net.scrayos.xenos.client.data

Provides the data types that cleanly replace the raw data types of the gRPC bindings. These data types are optimized for
the usage within the applications and will not break (if possible), unlike the gRPC types.

# Package net.scrayos.xenos.client.utility

Provides utility functions that can be used throughout the application and provide shortcuts or missing functions that
simplify the work with the Xenos API. They typically are not meant to be used by the applications themselves.
