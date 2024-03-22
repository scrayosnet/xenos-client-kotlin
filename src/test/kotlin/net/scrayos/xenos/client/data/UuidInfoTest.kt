package net.scrayos.xenos.client.data

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.maps.shouldContainKeys
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import scrayosnet.xenos.uuidResponse
import scrayosnet.xenos.uuidsResponse
import java.time.Instant
import java.util.UUID

class UuidInfoTest : ShouldSpec(
    {
        context("UuidResponse#toResult") {
            should("contain original data") {
                val testName = "Scrayos"
                val testUuid = UUID.randomUUID()
                val response = uuidResponse {
                    uuid = testUuid.toString()
                    username = testName
                    timestamp = 65
                }
                val result = response.toResult()
                result.id shouldBe testUuid
                result.username shouldBe testName
                result.retrievedAt shouldBe Instant.EPOCH.plusSeconds(65)
            }
        }

        context("UuidsResponse#toResult") {
            should("contain original data") {
                val scrayosUuid = UUID.randomUUID()
                val hydrofinUuid = UUID.randomUUID()
                val response = uuidsResponse {
                    resolved.putAll(
                        mapOf(
                            "scrayos" to uuidResponse {
                                uuid = scrayosUuid.toString()
                                username = "Scrayos"
                                timestamp = 65
                            },
                            "hydrofin" to uuidResponse {
                                uuid = hydrofinUuid.toString()
                                username = "Hydrofin"
                                timestamp = 70
                            },
                        ),
                    )
                }
                val result = response.toResult()

                result.shouldContainKeys("scrayos", "hydrofin")

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
