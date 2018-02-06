package com.imagepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhang on 2017/6/13.
 */

public class ImageUtils {
    public static Bitmap getSmallBitmap(String filePath) {
        return getSmallBitmap(filePath, 960, 1280);
    }

    /**
     * 通过减少options.inSampleSize图片的采样率，降低图片像素，来压缩图片
     * @param filePath 图片的路径
     * @param maxWidth 图片最大宽度
     * @param maxHeight 图片最大高度
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath,int maxWidth, int maxHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        if(bitmap == null){
            return  null;
        }
        int degree = readPictureDegree(filePath);
        return rotateBitmap(bitmap,degree);
    }

    public static ByteArrayOutputStream calculateQuality(Bitmap bitmap,int maxSize){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
        while (baos.toByteArray().length / 1024 > maxSize && options >= 20) {
            baos.reset();
            options -= 10;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        return baos;
    }

    public static void compressFile(String oldFilePath,String newFilePath){
        compressFile(oldFilePath, newFilePath, 100);
    }

    /**
     * 不减少图片像素，压缩图片的质量。
     * @param oldFilePath
     * @param newFilePath
     * @param maxSize 默认是100k
     */
    public static void compressFile(String oldFilePath,String newFilePath, int maxSize){
        Bitmap bitmap = getSmallBitmap(oldFilePath);
        ByteArrayOutputStream baos = calculateQuality(bitmap, maxSize);
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(newFilePath);
            fos.write(baos.toByteArray());
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                if(fos != null)
                    fos.close() ;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void saveBitmapToPath(Bitmap bitmap, String path) {
        saveBitmapToFile(bitmap, new File(path));
    }
    public static void saveBitmapToFile(Bitmap bitmap, File file) {
        if (bitmap == null)
            return;
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算采样大小
     *
     * @param options   选项
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return 采样大小
     */
//    private static int calculateInSampleSize(BitmapFactory.Options options, int maxWidth, int maxHeight) {
//        if (maxWidth == 0 || maxHeight == 0)
//            return 1;
//        int height = options.outHeight;
//        int width = options.outWidth;
//        int inSampleSize = 1;
//        while ((height >>= 1) >= maxHeight && (width >>= 1) >= maxWidth) {
//            inSampleSize <<= 1;
//        }
//        return inSampleSize;
//    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            //使用需要的宽高的最大值来计算比率
            int suitedValue = reqHeight > reqWidth ? reqHeight : reqWidth;
            int heightRatio = Math.round((float) height / (float) suitedValue);
            int widthRatio = Math.round((float) width / (float) suitedValue);

            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;//用最大
        }

        return inSampleSize;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int rotate){
        if(bitmap == null)
            return null ;

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        // Setting post rotate to 90
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    /**
     * 读取图片属性：旋转的角度
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree  = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }
}
