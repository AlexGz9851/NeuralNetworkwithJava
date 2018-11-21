// Luis Iván Morett Arévalo		   A01634417
// Jesús Alejandro González Sánchez A00820225
// BitmapManager
// Profesor: Gerardo Salinas

package com.datastructuresproyect.numberrecognizer.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.io.ByteArrayOutputStream;

public class BitmapManager {


    private Bitmap bmp;

    /**
     * Constructor that receives the bitmap as a Bitmap and sets it
     * @param bmp
     */
    public BitmapManager(Bitmap bmp){
        setBitmap(bmp);
    }

    /**
     * Constructor that receives the bitmap as a Byte array and sets it as a bitmap
     * @param bytes
     */
    public BitmapManager(byte[] bytes){
        setBitmap(bytes);
    }

    /**
     * Transforms new bitmap to a grayscale image
     * @return new Bitmap
     */
    public Bitmap toGrayScale(){
        Bitmap bmpGrayscale = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        c.drawBitmap(bmp, 0, 0, paint);
        return bmpGrayscale;
    }

    /**
     * Transforms new bitmap to the given scale
     * @param width
     * @param height
     * @return new Bitmap
     */
    public Bitmap scale(int width, int height){
        int originalWidth = bmp.getWidth();
        int originalHeight = bmp.getHeight();
        float scaleWidth = ((float) width) / originalWidth;
        float scaleHeight = ((float) height) / originalHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(
                bmp, 0, 0, originalWidth, originalHeight, matrix, false);

    }

    /**
     * Crops a new bitmap to the given scale
     * @param x the x position where the crop starts
     * @param y the y position where the crop starts
     * @param width
     * @param height
     * @return new Bitmap
     */
    public Bitmap crop(int x, int y, int width, int height){
        return Bitmap.createBitmap(bmp, x,y, width,height);
    }

    /**
     * Obtained from https://stackoverflow.com/questions/12891520/how-to-programmatically-change-contrast-of-a-bitmap-in-android
     * Second answer
     * @param contrast 0..10 1 is default
     * @param brightness -255..255 0 is default
     * @return new bitmap
     */
    public Bitmap changeContrastBrightness(float contrast, float brightness)
    {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }

    public void setBitmap(Bitmap bmp){
        this.bmp=bmp;
    }

    public void setBitmap(byte[] bytes){
        this.bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Gets the bitmap as bytes
     * @return Byte[]
     */
    public byte[] getBytes(){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    /**
     *
     * @return Bitmap
     */
    public Bitmap getBitmap() {
        return bmp;
    }

    public int getWidth(){
        return this.bmp.getWidth();
    }

    public int getHeight(){
        return this.bmp.getHeight();
    }

    public int[] getPixelArray() {
        return this.getPixelArray(this.bmp);
    }

    /**
     * Returns a pixel array of the given Bitmap
     * @param bmp Bitmap
     * @return int[]
     */
    public int[] getPixelArray(Bitmap bmp){
        int[] pixelArray = new int[28*28];
        for(int x = 0; x<28; x++){
            for(int y = 0; y < 28; y++){
                pixelArray[x*28 + y] = 255 - (bmp.getPixel(x,y) & 0x000000ff);
            }
        }
        return pixelArray;
    }
}
