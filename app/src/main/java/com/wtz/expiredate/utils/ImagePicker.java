package com.wtz.expiredate.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by WTZ on 2017/10/10.
 */

public class ImagePicker {
    private final static String TAG = ImagePicker.class.getSimpleName();

    public final static int ACTIVITY_REQUESTCODE_CAMERA = 0;
    public final static int ACTIVITY_REQUESTCODE_GALLERY = 1;
    public final static int ACTIVITY_REQUESTCODE_CROP = 2;

    public static void pickByCamera(Activity launcher, File target) {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // 对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // 通过FileProvider创建一个content类型的Uri
                uri = FileProvider.getUriForFile(launcher.getApplicationContext(), "com.test.qrcodetool.fileprovider", target);
            } else {
                uri = Uri.fromFile(target);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            launcher.startActivityForResult(intent,
                    ACTIVITY_REQUESTCODE_CAMERA);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void pickFromGallery(Activity launcher) {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            launcher.startActivityForResult(intent,
                    ACTIVITY_REQUESTCODE_GALLERY);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取uri所在的图片
     *
     * @param uri      图片对应的Uri
     * @param mContext 上下文对象
     * @return 获取图像的Bitmap
     */
    public static Bitmap getBitmapFromUri(Uri uri, Context mContext, int width, int height) {
        try {
            InputStream input = mContext.getContentResolver().openInputStream(uri);
            Bitmap bitmap = getBitmapFromStream(input, width, height);
            input.close();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param filePath 文件路径
     * @param width    实际需要展示的宽
     * @param height   实际需要展示的高
     * @return
     */
    public static Bitmap getBitmapFromFile(String filePath, int width, int height) {

        BitmapFactory.Options opts = null;
        if (width > 0 && height > 0) {
            opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, opts);
            // 计算图片缩放比例
            final int minSideLength = Math.min(width, height);
            opts.inSampleSize = computeSampleSize(opts, minSideLength, width * height);
            opts.inJustDecodeBounds = false;
            opts.inInputShareable = true;
            opts.inPurgeable = true;
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        }
        try {
            return BitmapFactory.decodeFile(filePath, opts);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmapFromStream(InputStream stream, int width, int height) {

        try {
            byte[] bytes = toByteArray(stream);
            BitmapFactory.Options opts = null;
            if (width > 0 && height > 0) {
                opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
//                BitmapFactory.decodeStream(stream, null, opts);
//                stream.reset();//报IO异常
                BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);

                // 计算图片缩放比例
                final int minSideLength = Math.min(width, height);
                opts.inSampleSize = computeSampleSize(opts, minSideLength, width * height);
                opts.inJustDecodeBounds = false;
                opts.inInputShareable = true;
                opts.inPurgeable = true;
                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            }
//            return BitmapFactory.decodeStream(stream, null, opts);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength,
                                        int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength,
                                                int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h
                / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
                Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }
}
