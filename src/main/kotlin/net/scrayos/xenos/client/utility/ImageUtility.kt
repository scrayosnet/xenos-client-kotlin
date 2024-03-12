package net.scrayos.xenos.client.utility

import com.google.protobuf.ByteString
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/**
 * Converts the bytes of this [ByteString] into an image, regardless of the format that was used to generate the bytes.
 * If this [ByteString] [isEmpty], an [IllegalStateException] will be thrown. This method supports the alpha layer and
 * therefore preserves opacity. The type of [BufferedImage] that will be created by this method depends on the bytes
 * that are contained within this [ByteString].
 */
fun ByteString.toImage(): BufferedImage {
    check(!isEmpty) {
        "Image cannot be created from empty ByteString!"
    }

    return ImageIO.read(newInput())
}
