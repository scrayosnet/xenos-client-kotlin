package net.scrayos.xenos.client

import io.kotest.assertions.print.Printed
import io.kotest.assertions.print.print
import io.kotest.assertions.withClue
import io.kotest.matchers.DiffableMatcherResult
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResultBuilder
import io.kotest.matchers.compose.all
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import java.awt.image.BufferedImage

fun haveWidth(width: Int) = Matcher<BufferedImage> { value ->
    MatcherResultBuilder.create(value.width == width)
        .withFailureMessage { "image had width ${value.width} but we expected width $width" }
        .withNegatedFailureMessage { "image should not have width $width" }
        .withValues({ value.width.print() }, { width.print() })
        .build()
}

fun haveHeight(height: Int) = Matcher<BufferedImage> { value ->
    MatcherResultBuilder.create(value.height == height)
        .withFailureMessage { "image had height ${value.height} but we expected height $height" }
        .withNegatedFailureMessage { "image should not have height $height" }
        .withValues({ value.height.print() }, { height.print() })
        .build()
}

infix fun BufferedImage.shouldBeOfEqualDimensions(template: BufferedImage) = this should haveDimensions(template)

fun haveDimensions(template: BufferedImage) = Matcher.all(
    haveWidth(template.width),
    haveHeight(template.height),
)

infix fun BufferedImage.shouldHaveEqualPixels(template: BufferedImage) {
    for (x in 0 until template.width) {
        for (y in 0 until template.height) {
            withClue("Pixel at ($x, $y) does not match expected pixel") {
                this.getRGB(x, y) shouldBe template.getRGB(x, y)
            }
        }
    }
}
