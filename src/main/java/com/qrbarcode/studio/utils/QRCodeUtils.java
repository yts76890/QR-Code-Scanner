package com.qrbarcode.studio.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class QRCodeUtils {

    public static Bitmap generateQRCode(String content, int size, int fgColor, int bgColor) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? fgColor : bgColor);
            }
        }
        return bitmap;
    }

    public static Bitmap generateBarcode(String content, BarcodeFormat format, int width, int height)
            throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 1);

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix bitMatrix = writer.encode(content, format, width, height, hints);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }

    public static Bitmap overlayLogo(Bitmap qrBitmap, Bitmap logo) {
        int qrSize = qrBitmap.getWidth();
        int logoSize = qrSize / 5;

        Bitmap result = Bitmap.createBitmap(qrSize, qrSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(qrBitmap, 0, 0, null);

        Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, logoSize, logoSize, true);
        int left = (qrSize - logoSize) / 2;
        int top = (qrSize - logoSize) / 2;

        // White background behind logo
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawRect(left - 8, top - 8, left + logoSize + 8, top + logoSize + 8, paint);
        canvas.drawBitmap(scaledLogo, left, top, null);

        return result;
    }

    public static Uri saveImageToGallery(Context context, Bitmap bitmap, String displayName) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/QRBarcodeStudio");

        Uri uri = context.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                }
            }
        }
        return uri;
    }

    public static String buildWifiContent(String ssid, String password, String encryption) {
        return "WIFI:T:" + encryption + ";S:" + ssid + ";P:" + password + ";;";
    }

    public static String buildVCardContent(String name, String phone, String email, String org) {
        return "BEGIN:VCARD\nVERSION:3.0\nFN:" + name + "\nTEL:" + phone +
                "\nEMAIL:" + email + "\nORG:" + org + "\nEND:VCARD";
    }

    public static String buildEmailContent(String to, String subject, String body) {
        return "mailto:" + to + "?subject=" + subject + "&body=" + body;
    }

    public static String buildSmsContent(String phone, String message) {
        return "smsto:" + phone + ":" + message;
    }
}
