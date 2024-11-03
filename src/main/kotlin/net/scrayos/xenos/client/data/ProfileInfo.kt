package net.scrayos.xenos.client.data

import scrayosnet.xenos.ProfileOuterClass.ProfileResponse
import java.time.Instant
import java.util.UUID

/**
 * A [ProfileInfo] is the response to the request of the Profile of a specific player. The result includes the moment
 * when this information was freshly retrieved from Mojang and may be stale or outdated, if Xenos is configured to
 * return those values. The profile data may be signed and therefore have signatures and other cryptographic information
 * if Xenos is configured to request profiles as signed.
 */
data class ProfileInfo(
    /** The unique identifier that was requested and that unambiguously identifies the player. */
    val id: UUID,
    /** The canonical username of the requested player with the rectified capitalization. */
    val name: String,
    /** The individual, auxiliary properties of attached information for this profile. */
    val properties: Set<Property>,
    /** The moderative actions/sanctions that have been imposed on this profile by Mojang. */
    val actions: Set<String>,
    /** The moment when this data was originally fetched from the MojangAPI and when it was considered fresh. */
    val retrievedAt: Instant,
) {
    /**
     * A [Property] is a single dataset of auxiliary information of a specific [profile][ProfileInfo]. The data is
     * independent of the rest of the profile and no guarantees are made regarding the existence of a specific property.
     * The signature is only present if Xenos was configured to request signed profiles.
     */
    data class Property(
        /** The unique name of the property within the Minecraft Profile. */
        val name: String,
        /** The value of the property that contains the real information. */
        val value: String,
        /** The optional Yggdrasil signature to verify the authenticity and integrity. */
        val signature: String?,
    )
}

/**
 * Converts the raw response from gRPC into a more user-friendly data class, that encapsulates the contained data in an
 * easy-to-use format. No information is dropped or lost through the conversion. The wrapped response drops all
 * information that is related to the gRPC origin and is therefore independent of the client implementation, that was
 * used to retrieve the data from Xenos.
 */
internal fun ProfileResponse.toResult(): ProfileInfo = ProfileInfo(
    UUID.fromString(uuid),
    name,
    propertiesList.map {
        ProfileInfo.Property(
            it.name,
            it.value,
            if (it.hasSignature()) it.signature else null,
        )
    }.toSet(),
    profileActionsList.toSet(),
    Instant.ofEpochSecond(timestamp),
)
