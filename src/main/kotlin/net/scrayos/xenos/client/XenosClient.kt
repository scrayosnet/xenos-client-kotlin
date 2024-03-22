package net.scrayos.xenos.client

import kotlinx.coroutines.flow.Flow
import net.scrayos.xenos.client.data.CapeInfo
import net.scrayos.xenos.client.data.HeadInfo
import net.scrayos.xenos.client.data.ProfileInfo
import net.scrayos.xenos.client.data.SkinInfo
import net.scrayos.xenos.client.data.UuidInfo
import java.util.UUID

/**
 * A [XenosClient] represents the interface to Xenos, through which user and profile data regarding specific accounts
 * may be retrieved. This client may be used to interact with a specific Xenos instance and its datastore. No internal
 * caching is performed, as the latency to Xenos should already be pretty low and caching twice could introduce more
 * stale data into the lookups.
 *
 * All lookups are executed asynchronously (through coroutines) and return their results after the response from the
 * Client has been recorded. Endpoints that work with streams of data are also executed asynchronously and are wrapped
 * into [flows][Flow] to be compatible with the coroutines. Errors will always be returned immediately if they are
 * discovered and the operation will only be automatically retried, if the returned condition can be recovered from.
 *
 * The signatures of the protobuf RPCs have been slightly adjusted to unify their naming scheme and to better fit into
 * the Kotlin/Java ecosystem. Generally speaking, no hidden logic is embedded into the calls and the values are
 * therefore relayed to Xenos without further changes. The Client is always kept compatible with the official
 * recommendations and should be used directly whenever possible, as the individual steps are atomic.
 *
 * @see <a href="https://github.com/scrayosnet/xenos">Xenos Documentation</a>
 */
interface XenosClient : AutoCloseable {

    /**
     * Retrieves the [UUID][UuidInfo] for the supplied [name] from Xenos. The result includes the moment when the data
     * was fetched from the Mojang API, so that stale data can be identified. If the player cannot be found, `null` will
     * be returned. The username does not have to be in the correct case and the canonical name will be returned within
     * the result.
     */
    suspend fun getUuid(name: String): UuidInfo?

    /**
     * Retrieves the [UUIDs][UuidInfo] of the supplied [names] from Xenos. Duplicates are filtered from the result
     * and names that could not be found are discarded from the result. Each result includes the moment when the data
     * was fetched from the Mojang API, so that stale data can be identified. The usernames do not have to be in the
     * correct case and the canonical names will also be returned within the result.
     *
     * The keys of the result map are all [lowercase][String.lowercase] to prevent duplicate entries. Therefore, to
     * retrieve or check the results, all keys have to be submitted in [lowercase][String.lowercase]. That warrants that
     * every name will only be present once.
     */
    suspend fun getUuids(names: Collection<String>): Map<String, UuidInfo>

    /**
     * Retrieves the [profile][ProfileInfo] for the supplied [userId] from Xenos. The result includes the moment
     * when the data was fetched from the Mojang API, so that stale data can be identified. If the player cannot be
     * found, `null` will be returned. Whether profiles contain signatures can be configured within Xenos.
     */
    suspend fun getProfile(userId: UUID): ProfileInfo?

    /**
     * Retrieves the current [skin][SkinInfo] of the supplied [userId] from Xenos. The result includes the moment when
     * the data was fetched from the Mojang API, to that stale data can be identified. If the player cannot be found,
     * `null` will be returned. The texture is guaranteed to be present within the memory and therefore easily
     * accessible.
     */
    suspend fun getSkin(userId: UUID): SkinInfo?

    /**
     * Retrieves the current [cape][CapeInfo] of the supplied [userId] from Xenos. The result includes the moment when
     * the data was fetched from the Mojang API, to that stale data can be identified. If the player cannot be found or
     * has no cape, `null` will be returned. The texture is guaranteed to be present within the memory and therefore
     * easily accessible.
     */
    suspend fun getCape(userId: UUID): CapeInfo?

    /**
     * Retrieves the current [head][HeadInfo] of the supplied [userId] from Xenos. The result includes the moment when
     * the data was fetched from the Mojang API, to that stale data can be identified. The overlay skin layer may
     * [be included][includeOverlay]. If the player cannot be found, `null` will be returned. The texture is guaranteed
     * to be present within the memory and therefore easily accessible.
     */
    suspend fun getHead(userId: UUID, includeOverlay: Boolean = false): HeadInfo?

    /**
     * Closes all open resources that are associated with the [Xenos Client][XenosClient]. After this operation, this
     * instance of the [Xenos Client][XenosClient] may no longer be used, as all connections are no longer usable. This
     * method is idempotent and can therefore be called any number of times without changing its behaviour. It is
     * guaranteed, that after this call all open connections and allocated resources will be closed or released.
     * Although not all implementations have such resources, the method should still always be called (for example,
     * within a Try-With-Resources block or a use method) to cleanly terminate resource usage.
     *
     * **Implementation Note**: Closing the open connections and releasing the allocated resources must happen blocking
     * so that the main thread can be safely shut down after this method was called. The difference of this method to
     * [AutoCloseable.close] is, that it is not permitted to trigger any exceptions within this method.
     */
    override fun close()
}
