package net.scrayos.xenos.client

import com.google.protobuf.ByteString
import io.grpc.Status
import io.grpc.StatusException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.extensions.testcontainers.ContainerExtension
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.resource.resourceAsBytes
import io.kotest.matchers.shouldBe
import net.scrayos.xenos.client.data.ProfileInfo
import net.scrayos.xenos.client.utility.toImage
import org.testcontainers.containers.GenericContainer
import java.util.UUID

class GrpcXenosClientTest : ShouldSpec() {
    init {
        val container = GenericContainer<Nothing>("ghcr.io/scrayosnet/xenos:0.6.1-testing").apply {
            withExposedPorts(50051)
        }
        val xenos = install(ContainerExtension(container))
        lateinit var client: XenosClient

        val scrayosId = UUID.fromString("9c09eef4-f68d-4387-9751-72bbff53d5a0")
        val scrayosSkin = ByteString.copyFrom(resourceAsBytes("/scrayos_skin.png")).toImage()
        val scrayosHead = ByteString.copyFrom(resourceAsBytes("/scrayos_head.png")).toImage()
        val scrayosHeadOverlay = ByteString.copyFrom(resourceAsBytes("/scrayos_head_overlay.png")).toImage()
        val herbertId = UUID.fromString("1119fff4-f68d-4388-8751-72bbff53d5a0")

        beforeEach {
            client = GrpcXenosClient(
                xenos.host,
                xenos.getMappedPort(50051),
            )
        }

        afterEach {
            client.close()
        }

        context("#getUuid") {
            should("resolve for an existing name") {
                val result = client.getUuid("scrayos")
                result.shouldNotBeNull()
                result.username shouldBe "Scrayos"
                result.id shouldBe UUID.fromString("9c09eef4-f68d-4387-9751-72bbff53d5a0")
            }

            should("return null for invalid name") {
                val result = client.getUuid("#")
                result.shouldBeNull()
            }

            should("return null for a missing name") {
                val result = client.getUuid("missingname")
                result.shouldBeNull()
            }

            should("return correct capitalization of the name") {
                val result = client.getUuid("scrayos")
                result.shouldNotBeNull()
                result.username shouldBe "Scrayos"
            }
        }

        context("#getUuids") {
            should("return nothing for empty requests") {
                val result = client.getUuids(listOf())
                result.shouldBeEmpty()
            }

            should("resolve a single name") {
                val result = client.getUuids(listOf("scrayos"))
                result.size shouldBe 1
            }

            should("return correct capitalization of username") {
                val result = client.getUuids(listOf("scrayos"))
                val res = result["scrayos"]
                res.shouldNotBeNull()
                res.username shouldBe "Scrayos"
            }

            should("return correct user id") {
                val result = client.getUuids(listOf("scrayos"))
                val res = result["scrayos"]
                res.shouldNotBeNull()
                res.id shouldBe scrayosId
            }

            should("resolve multiple names") {
                val result = client.getUuids(listOf("scrayos", "hydrofin"))
                result.size shouldBe 2
            }

            should("discard missing UUID for request") {
                val result = client.getUuids(listOf("scrayos", "hydrofin", "missingname"))
                result.size shouldBe 2
            }

            should("discard missing UUID for otherwise empty request") {
                val result = client.getUuids(listOf("missingname"))
                result.shouldBeEmpty()
            }

            should("contain only lowercase names") {
                val result = client.getUuids(listOf("ScRaYoS"))
                result.keys shouldContainExactly setOf(
                    "scrayos",
                )
            }

            should("deduplicate requested names") {
                val result = client.getUuids(listOf("scrayos", "scrayos"))
                result.size shouldBe 1
            }

            should("deduplicate different casing") {
                val result = client.getUuids(listOf("scrayos", "sCrAyOs"))
                result.size shouldBe 1
            }

            should("discard invalid names") {
                val result = client.getUuids(listOf("ä"))
                result.size shouldBe 0
            }
        }

        context("#getProfile") {
            should("resolve for an existing UUID") {
                val result = client.getProfile(scrayosId)
                result.shouldNotBeNull()
            }

            should("return null for a missing UUID") {
                val result = client.getProfile(UUID(0, 0))
                result.shouldBeNull()
            }

            should("return correct id") {
                val result = client.getProfile(scrayosId)
                result.shouldNotBeNull()
                result.id shouldBe scrayosId
            }

            should("return correct name") {
                val result = client.getProfile(scrayosId)
                result.shouldNotBeNull()
                result.name shouldBe "Scrayos"
            }

            should("return correct properties") {
                val result = client.getProfile(scrayosId)
                result.shouldNotBeNull()
                result.properties shouldContainExactly setOf(
                    ProfileInfo.Property(
                        "textures",
                        "eyJ0aW1lc3RhbXAiOjAsInByb2ZpbGVJZCI6IjljMDllZWY0LWY2OGQtNDM4Ny05NzUxLTcyYmJmZjUzZD" +
                            "VhMCIsInByb2ZpbGVOYW1lIjoiU2NyYXlvcyIsInNpZ25hdHVyZVJlcXVpcmVkIjpudWxsLCJ0ZXh0dXJlcy" +
                            "I6eyJTS0lOIjp7InVybCI6InNraW5fOWMwOWVlZjQtZjY4ZC00Mzg3LTk3NTEtNzJiYmZmNTNkNWEwIiwibW" +
                            "V0YWRhdGEiOm51bGx9LCJDQVBFIjpudWxsfX0=",
                        null,
                    ),
                )
            }

            should("return correct actions") {
                val result = client.getProfile(scrayosId)
                result.shouldNotBeNull()
                result.actions shouldContainExactly setOf()
            }
        }

        context("#getSkin") {

            should("resolve for an existing UUID") {
                val result = client.getSkin(scrayosId)
                result.shouldNotBeNull()
            }

            should("return null for a missing UUID") {
                val result = client.getSkin(UUID(0, 0))
                result.shouldBeNull()
            }

            should("return default for standard skin") {
                val result = client.getSkin(herbertId)
                result.shouldNotBeNull()
                result.default shouldBe true
            }

            should("return correct image") {
                val result = client.getSkin(scrayosId)
                result.shouldNotBeNull()
                result.texture shouldBeOfEqualDimensions scrayosSkin
                result.texture shouldHaveEqualPixels scrayosSkin
                result.default shouldBe false
            }
        }

        context("#getCape") {

            should("return null for a missing UUID") {
                val result = client.getCape(UUID(0, 0))
                result.shouldBeNull()
            }

            should("return null for an existing UUID without cape") {
                val result = client.getCape(scrayosId)
                result.shouldBeNull()
            }
        }

        context("#getHead") {

            should("resolve for an existing UUID") {
                val result = client.getHead(scrayosId)
                result.shouldNotBeNull()
            }

            should("return null for a missing UUID") {
                val result = client.getHead(UUID(0, 0))
                result.shouldBeNull()
            }

            should("return default for standard skin") {
                val result = client.getHead(herbertId)
                result.shouldNotBeNull()
                result.default shouldBe true
            }

            should("return correct image (no overlay)") {
                val result = client.getHead(scrayosId, false)
                result.shouldNotBeNull()
                result.texture shouldBeOfEqualDimensions scrayosHead
                result.texture shouldHaveEqualPixels scrayosHead
                result.default shouldBe false
            }

            should("return correct image (overlay)") {
                val result = client.getHead(scrayosId, true)
                result.shouldNotBeNull()
                result.texture shouldBeOfEqualDimensions scrayosHeadOverlay
                result.texture shouldHaveEqualPixels scrayosHeadOverlay
                result.default shouldBe false
            }
        }

        context("Closing") {
            should("be idempotent") {
                client.close()
                client.close()
            }

            should("throw if channel is closed") {
                client.close()

                val exception = shouldThrow<StatusException> {
                    client.getUuids(listOf("a"))
                }
                exception.status.code shouldBe Status.Code.UNAVAILABLE
                exception.status.description shouldBe "Channel shutdown invoked"
            }

            should("refresh interrupted flag") {
                Thread.currentThread().interrupt()
                client.close()
                Thread.interrupted().shouldBeTrue()
            }
        }
    }
}
