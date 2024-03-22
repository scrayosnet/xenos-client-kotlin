package net.scrayos.xenos.client.data

import com.google.protobuf.ByteString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.resource.resourceAsBytes
import io.kotest.matchers.shouldBe
import net.scrayos.xenos.client.shouldBeOfEqualDimensions
import net.scrayos.xenos.client.shouldHaveEqualPixels
import net.scrayos.xenos.client.utility.toImage
import scrayosnet.xenos.capeResponse
import java.time.Instant

class CapeInfoTest : ShouldSpec(
    {
        context("#toResult") {
            should("contain original data") {
                val response = capeResponse {
                    bytes = ByteString.copyFrom(resourceAsBytes("/image_test.png"))
                    timestamp = 60
                }
                val image = ByteString.copyFrom(resourceAsBytes("/image_test.png")).toImage()
                val result = response.toResult()

                result.retrievedAt shouldBe Instant.EPOCH.plusSeconds(60)
                result.texture shouldBeOfEqualDimensions image
                result.texture shouldHaveEqualPixels image
            }
        }
    },
)
