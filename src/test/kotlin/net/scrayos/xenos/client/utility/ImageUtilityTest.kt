package net.scrayos.xenos.client.utility

import com.google.protobuf.ByteString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.resource.resourceAsBytes
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.startWith

@OptIn(ExperimentalStdlibApi::class)
class ImageUtilityTest :
    ShouldSpec(
        {
            context("ByteString.toImage") {
                should("throw ISE for empty bytes") {
                    val exception = shouldThrow<IllegalStateException> {
                        ByteString.empty().toImage()
                    }

                    exception.message?.should(startWith("Image cannot be created from empty ByteString!"))
                }

                should("return correct dimensions for image") {
                    val resourceBytes = resourceAsBytes("/image_test.png")
                    val image = ByteString.copyFrom(resourceBytes).toImage()

                    image.width shouldBe 4
                    image.height shouldBe 4
                }

                should("return correct data for image") {
                    val resourceBytes = resourceAsBytes("/image_test.png")
                    val image = ByteString.copyFrom(resourceBytes).toImage()

                    val testValues = listOf(
                        Triple(3, 0, 0xFFFFFFFF),

                        Triple(2, 0, 0xFFBCBCBC),
                        Triple(3, 1, 0xFFBCBCBC),

                        Triple(1, 0, 0xFF939393),
                        Triple(2, 1, 0xFF939393),
                        Triple(3, 2, 0xFF939393),

                        Triple(0, 0, 0xFF6B6B6B),
                        Triple(1, 1, 0xFF6B6B6B),
                        Triple(2, 2, 0xFF6B6B6B),
                        Triple(3, 3, 0xFF6B6B6B),

                        Triple(0, 1, 0xFF434343),
                        Triple(1, 2, 0xFF434343),
                        Triple(2, 3, 0xFF434343),

                        Triple(0, 2, 0xFF282828),
                        Triple(1, 3, 0xFF282828),

                        Triple(0, 3, 0xFF000000),
                    )
                    for (v in testValues) {
                        val actual = image.getRGB(v.first, v.second)
                        val expected = v.third.toInt()
                        val expectedHex = expected.toHexString(HexFormat.UpperCase)
                        withClue("Pixel at (${v.first}, ${v.second}) should be color #$expectedHex") {
                            actual shouldBe expected
                        }
                    }
                }
            }
        },
    )
