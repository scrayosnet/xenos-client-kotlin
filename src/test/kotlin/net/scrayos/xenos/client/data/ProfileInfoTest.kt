package net.scrayos.xenos.client.data

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import scrayosnet.xenos.profileProperty
import scrayosnet.xenos.profileResponse
import java.time.Instant
import java.util.UUID

class ProfileInfoTest : ShouldSpec() {
    init {
        context("#toResult") {
            should("contain original data") {
                val testUuid = UUID.randomUUID()
                val response = profileResponse {
                    uuid = testUuid.toString()
                    name = "Scrayos"
                    properties.addAll(
                        setOf(
                            profileProperty {
                                name = "texture"
                                value = "abc"
                                signature = "cde"
                            },
                            profileProperty {
                                name = "meta"
                                value = "data"
                            },
                        ),
                    )
                    profileActions.addAll(
                        setOf(
                            "IMPORTANT_ACTION",
                            "OTHER_ACTION",
                        ),
                    )
                    timestamp = 65
                }
                val result = response.toResult()

                result.id shouldBe testUuid
                result.name shouldBe "Scrayos"
                result.properties shouldContainExactly setOf(
                    ProfileInfo.Property(
                        "texture",
                        "abc",
                        "cde",
                    ),
                    ProfileInfo.Property(
                        "meta",
                        "data",
                        null,
                    ),
                )
                result.actions.shouldContainExactly("IMPORTANT_ACTION", "OTHER_ACTION")
                result.retrievedAt shouldBe Instant.EPOCH.plusSeconds(65)
            }
        }
    }
}
