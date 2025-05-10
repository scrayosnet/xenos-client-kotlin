package net.scrayos.xenos.client

import io.grpc.Channel
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status.Code.NOT_FOUND
import io.grpc.Status.Code.UNAVAILABLE
import io.grpc.StatusException
import net.scrayos.xenos.client.data.CapeInfo
import net.scrayos.xenos.client.data.HeadInfo
import net.scrayos.xenos.client.data.ProfileInfo
import net.scrayos.xenos.client.data.SkinInfo
import net.scrayos.xenos.client.data.UuidInfo
import net.scrayos.xenos.client.data.toResult
import org.slf4j.LoggerFactory
import scrayosnet.xenos.ProfileGrpcKt
import scrayosnet.xenos.capeRequest
import scrayosnet.xenos.headRequest
import scrayosnet.xenos.profileRequest
import scrayosnet.xenos.skinRequest
import scrayosnet.xenos.uuidRequest
import scrayosnet.xenos.uuidsRequest
import java.lang.IllegalStateException
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * A [GrpcXenosClient] represents the gRPC implementation of the [Xenos Client][XenosClient]. The implementation is
 * based on the official Protobuf definitions released with Xenos. Each platform only needs to use one implementation
 * of the Xenos Client, but is free to choose it freely. This implementation is really fast and should be preferred if
 * Xenos is deployed with gRPC.
 *
 * When creating an instance of this implementation, the corresponding [network channel][Channel] is dynamically
 * assembled for this purpose. The corresponding stubs for coroutine-based communication with the interface are
 * instantiated for the [channel][Channel] to the attached sidecar. No action is taken by creating this instance, and
 * communication with the external interface is not initiated.
 *
 * @param host The host, under which the gRPC server of Xenos can be reached and that will therefore be used to
 * establish the connection.
 * @param port The port, under which the gRPC server of Xenos can be reached and that will therefore be used to
 * establish the connection.
 *
 * @sample grpcSample
 */
class GrpcXenosClient(
    /** The host of the external gRPC interface of Xenos, that will be used to establish the connection. */
    host: String,
    /** The port of the external gRPC interface of Xenos, that will be used to establish the connection. */
    port: Int = DEFAULT_XENOS_PORT,
) : XenosClient {

    /** The [channel][ManagedChannel], that will be used for the network communication with the external interface. */
    private val channel: ManagedChannel = ManagedChannelBuilder
        .forAddress(host, port)
        .usePlaintext()
        .build()

    /** The coroutine-based stub for the communication with the gRPC interface of Xenos. */
    private val stub: ProfileGrpcKt.ProfileCoroutineStub = ProfileGrpcKt.ProfileCoroutineStub(channel)

    override suspend fun getUuid(name: String): UuidInfo? = try {
        stub.getUuid(
            uuidRequest {
                username = name
            },
        ).toResult()
    } catch (ex: StatusException) {
        when (ex.status.code) {
            UNAVAILABLE -> throw IllegalStateException("Xenos could not fetch the requested uuid")
            NOT_FOUND -> null
            else -> throw ex
        }
    }

    override suspend fun getUuids(names: Collection<String>): Map<String, UuidInfo> = stub.getUuids(
        uuidsRequest {
            usernames.addAll(names)
        },
    ).toResult()

    override suspend fun getProfile(userId: UUID): ProfileInfo? = try {
        stub.getProfile(
            profileRequest {
                uuid = userId.toString()
            },
        ).toResult()
    } catch (ex: StatusException) {
        when (ex.status.code) {
            UNAVAILABLE -> throw IllegalStateException("Xenos could not fetch the requested profile")
            NOT_FOUND -> null
            else -> throw ex
        }
    }

    override suspend fun getSkin(userId: UUID): SkinInfo? = try {
        stub.getSkin(
            skinRequest {
                uuid = userId.toString()
            },
        ).toResult()
    } catch (ex: StatusException) {
        when (ex.status.code) {
            UNAVAILABLE -> throw IllegalStateException("Xenos could not fetch the requested skin")
            NOT_FOUND -> null
            else -> throw ex
        }
    }

    override suspend fun getCape(userId: UUID): CapeInfo? = try {
        stub.getCape(
            capeRequest {
                uuid = userId.toString()
            },
        ).toResult()
    } catch (ex: StatusException) {
        when (ex.status.code) {
            UNAVAILABLE -> throw IllegalStateException("Xenos could not fetch the requested cape")
            NOT_FOUND -> null
            else -> throw ex
        }
    }

    override suspend fun getHead(userId: UUID, includeOverlay: Boolean): HeadInfo? = try {
        stub.getHead(
            headRequest {
                uuid = userId.toString()
                overlay = includeOverlay
            },
        ).toResult()
    } catch (ex: StatusException) {
        when (ex.status.code) {
            UNAVAILABLE -> throw IllegalStateException("Xenos could not fetch the requested head")
            NOT_FOUND -> null
            else -> throw ex
        }
    }

    override fun close() {
        try {
            // shutdown and wait for it to complete
            val finishedShutdown = channel
                .shutdown()
                .awaitTermination(SHUTDOWN_GRACE_PERIOD.toMillis(), TimeUnit.MILLISECONDS)

            // force shutdown if it did not terminate
            if (!finishedShutdown) {
                channel.shutdownNow()
            }
        } catch (ex: InterruptedException) {
            // log so we know the origin/reason for this interruption
            logger.debug("Thread was interrupted while waiting for the shutdown of a GrpcXenosClient: ", ex)

            // set interrupted status of this thread
            Thread.currentThread().interrupt()
        }
    }

    companion object {
        /** The logger that will be utilized to perform any logging for the methods of this class. */
        private val logger = LoggerFactory.getLogger(GrpcXenosClient::class.java)

        /** The default port, that will be used to communicate with the gRPC server of Xenos. */
        private const val DEFAULT_XENOS_PORT: Int = 50051

        /** The [duration][Duration], that will be waited at maximum for the successful shutdown of the channel. */
        private val SHUTDOWN_GRACE_PERIOD = Duration.ofSeconds(5)
    }
}
