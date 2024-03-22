package net.scrayos.xenos.client.data

import com.google.protobuf.ByteString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.resource.resourceAsBytes
import io.kotest.matchers.shouldBe
import net.scrayos.xenos.client.shouldBeOfEqualDimensions
import net.scrayos.xenos.client.shouldHaveEqualPixels
import net.scrayos.xenos.client.utility.toImage
import scrayosnet.xenos.skinResponse
import java.time.Instant

class SkinInfoTest : ShouldSpec(
    {
        context("#toResult") {
            should("contain original data") {
                val response = skinResponse {
                    bytes = ByteString.copyFrom(resourceAsBytes("/image_test.png"))
                    timestamp = 60
                    default = true
                }
                val image = ByteString.copyFrom(resourceAsBytes("/image_test.png")).toImage()
                val result = response.toResult()

                result.retrievedAt shouldBe Instant.EPOCH.plusSeconds(60)
                result.texture shouldBeOfEqualDimensions image
                result.texture shouldHaveEqualPixels image
                result.default shouldBe true
            }
        }
    },
)
