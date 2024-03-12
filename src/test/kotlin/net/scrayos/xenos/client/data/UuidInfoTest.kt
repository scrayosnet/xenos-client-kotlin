package net.scrayos.xenos.client.data

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.maps.shouldContainKeys
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import scrayosnet.xenos.uuidData
import scrayosnet.xenos.uuidResponse
import scrayosnet.xenos.uuidResult
import java.time.Instant
import java.util.UUID

class UuidInfoTest : ShouldSpec(
    {
        context("#toResult") {
            should("contain original data") {
                val scrayosUuid = UUID.randomUUID()
                val hydrofinUuid = UUID.randomUUID()
                val response = uuidResponse {
                    resolved.putAll(
                        mapOf(
                            "scrayos" to uuidResult {
                                data = uuidData {
                                    uuid = scrayosUuid.toString()
                                    username = "Scrayos"
                                }
                                timestamp = 65
                            },
                            "hydrofin" to uuidResult {
                                data = uuidData {
                                    uuid = hydrofinUuid.toString()
                                    username = "Hydrofin"
                                }
                                timestamp = 70
                            },
                            "nonexisting" to uuidResult {
                                timestamp = 0
                            },
                        ),
                    )
                }
                val result = response.toResult()

                result.shouldContainKeys("scrayos", "hydrofin", "nonexisting")

                result["nonexisting"].shouldBeNull()

                val scrayos = result["scrayos"]
                scrayos.shouldNotBeNull()
                scrayos.id shouldBe scrayosUuid
                scrayos.username shouldBe "Scrayos"
                scrayos.retrievedAt shouldBe Instant.EPOCH.plusSeconds(65)

                val hydrofin = result["hydrofin"]
                hydrofin.shouldNotBeNull()
                hydrofin.id shouldBe hydrofinUuid
                hydrofin.username shouldBe "Hydrofin"
                hydrofin.retrievedAt shouldBe Instant.EPOCH.plusSeconds(70)
            }
        }
    },
)
