package com.example.injuryrecoveryapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.Image;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;

public class YuvToRgbConverter {

    private final RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;

    // Create a converter using RenderScript
    public YuvToRgbConverter(Context context) {
        rs = RenderScript.create(context);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
    }

    // Convert an ImageProxy in YUV format to a Bitmap in ARGB_8888 format
    @ExperimentalGetImage
    public void yuvToRgb(ImageProxy image, Bitmap outputBitmap) {
        // Validate image format
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            throw new IllegalArgumentException("Invalid image format");
        }

        Image yuvImage = image.getImage();
        if (yuvImage == null) {
            throw new IllegalArgumentException("Image is null");
        }

        // Get the Y, U, and V planes from the image
        ByteBuffer yBuffer = yuvImage.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = yuvImage.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = yuvImage.getPlanes()[2].getBuffer(); // V

        // create a single ByteBuffer with all the data in NV21
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        // ByteBuffer for Renderscript
        // Compose the Y, U, V data together in the NV21 layout
        byte[] nv21ByteArray = new byte[ySize + uSize + vSize];
        yBuffer.get(nv21ByteArray, 0, ySize);
        vBuffer.get(nv21ByteArray, ySize, vSize);
        uBuffer.get(nv21ByteArray, ySize + vSize, uSize);

        // Initialize allocations and types if not already created
        if (yuvType == null) {
            yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21ByteArray.length);
            in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

            rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs))
                    .setX(outputBitmap.getWidth())
                    .setY(outputBitmap.getHeight());
            out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        }

        in.copyFrom(nv21ByteArray);

        // YuvToRgb Intrinsic
        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);

        // Copy to the output bitmap
        out.copyTo(outputBitmap);
    }

    public void release() {
        if (in != null) in.destroy();
        if (out != null) out.destroy();
        if (yuvToRgbIntrinsic != null) yuvToRgbIntrinsic.destroy();
        rs.destroy();
    }
}
