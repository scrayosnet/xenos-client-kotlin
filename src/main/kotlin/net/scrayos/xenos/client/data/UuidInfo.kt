package net.scrayos.xenos.client.data

import scrayosnet.xenos.ProfileOuterClass.UuidResponse
import java.time.Instant
import java.util.UUID

/**
 * A [UuidInfo] is the response to the request of the UUID and canonical username of a specific player. The result
 * includes the moment when this information was freshly retrieved from Mojang and may be stale or outdated, if Xenos is
 * configured to return those values. The canonical name may differ from the requested name in its capitalization but is
 * guaranteed to be equal, ignoring the case.
 */
data class UuidInfo(
    /** The unique identifier that belongs to the requested username and that unambiguously identifies the player. */
    val id: UUID,
    /** The canonical username of the requested player with the rectified capitalization as specified in the profile. */
    val username: String,
    /** The moment when this data was originally fetched from the MojangAPI and when it was considered fresh. */
    val retrievedAt: Instant,
)

/**
 * Converts the raw response from gRPC into a more user-friendly data class, that encapsulates the contained data in an
 * easy-to-use format. No information is dropped or lost through the conversion. The wrapped response drops all
 * information that is related to the gRPC origin and is therefore independent of the client implementation, that was
 * used to retrieve the data from Xenos.
 */
internal fun UuidResponse.toResult(): Map<String, UuidInfo?> {
    return resolvedMap
        .mapValues {
            val value = it.value
            if (!value.hasData()) {
                null
            } else {
                UuidInfo(
                    UUID.fromString(value.data.uuid),
                    value.data.username,
                    Instant.ofEpochSecond(value.timestamp),
                )
            }
        }
        .toMap()
}
