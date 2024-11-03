package net.scrayos.xenos.client.data

import net.scrayos.xenos.client.utility.toImage
import scrayosnet.xenos.ProfileOuterClass.HeadResponse
import java.awt.image.BufferedImage
import java.time.Instant

/**
 * A [HeadInfo] is the response to the request of the head texture of a specific player. The result includes the
 * moment when this information was freshly retrieved from Mojang and may be stale or outdated, if Xenos is configured
 * to return those values. The texture is optimized for fast retrieval and all data is stored in memory.
 */
data class HeadInfo(
    /** The loaded texture of the head that was requested for the supplied player. */
    val texture: BufferedImage,
    /** The moment when this data was originally fetched from the MojangAPI and when it was considered fresh. */
    val retrievedAt: Instant,
    /** Whether this includes the default skin, as the supplied player did not have a custom skin configured. */
    val default: Boolean,
)

/**
 * Converts the raw response from gRPC into a more user-friendly data class, that encapsulates the contained data in an
 * easy-to-use format. No information is dropped or lost through the conversion. The wrapped response drops all
 * information that is related to the gRPC origin and is therefore independent of the client implementation, that was
 * used to retrieve the data from Xenos.
 */
internal fun HeadResponse.toResult(): HeadInfo = HeadInfo(
    bytes.toImage(),
    Instant.ofEpochSecond(timestamp),
    default,
)
