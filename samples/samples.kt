fun grpcSample() {
    // host and port are supplied by the default environment variables
    val sdk = GrpcAgonesSdk()

    // any request can be performed on the sdk while it is open
    sdk.ready()
}
