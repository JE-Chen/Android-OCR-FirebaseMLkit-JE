package com.je_chen.je_ocr;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.widget.ImageView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Bitmap_Process {

    private Context context;

    public Bitmap_Process(Context context){
        this.context=context;
    }

    //從路徑取得Bitmap
    public static android.graphics.Bitmap Get_FitSampleBitmap(String File_name, ImageView set_bitmap_Image, String File_Path, File sd_path) {
        int width =set_bitmap_Image.getWidth();
        int height=set_bitmap_Image.getHeight();
        String file_path=(File_Path+File_name);
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if(sdCardExist) {
            String search_path = (sd_path.getPath() + File_name);
            File search_file = new File(search_path);
            if(search_file.exists()){
                file_path=search_path;
            }
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file_path, options);
        options.inSampleSize = Get_FitInSampleSize(width, height, options);
        options.inJustDecodeBounds = false;
        android.graphics.Bitmap bm =BitmapFactory.decodeFile(file_path, options);
        android.graphics.Bitmap what = android.graphics.Bitmap.createScaledBitmap(bm,width,height,true);
        bm.recycle();
        set_bitmap_Image.setImageBitmap(what);
        return what;
    }


    //從路徑取得Bitmap
    public static android.graphics.Bitmap Get_FitSampleBitmap(String File_name, String File_Path,File sd_path) {
        String file_path=(File_Path+File_name);
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if(sdCardExist) {
            String search_path = (sd_path.getPath() + File_name);
            File search_file = new File(search_path);
            if(search_file.exists()){
                file_path=search_path;
            }
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        android.graphics.Bitmap get_bm =  BitmapFactory.decodeFile(file_path, options);
        int get_width = get_bm.getWidth();
        int get_height = get_bm.getHeight();
        options.inSampleSize = Get_FitInSampleSize(get_width, get_height, options);
        options.inJustDecodeBounds = false;
        android.graphics.Bitmap bm =BitmapFactory.decodeFile(file_path, options);
        android.graphics.Bitmap what = android.graphics.Bitmap.createScaledBitmap(bm,get_width,get_height,true);
        bm.recycle();
        return what;
    }


    //從Resources 取得Bitmap
    public static android.graphics.Bitmap Get_FitSampleBitmap(Resources resources, int resId, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resId, options);
        options.inSampleSize = Get_FitInSampleSize(width, height, options);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, resId, options);
    }

    //從輸入流取得Bitmap
    public static android.graphics.Bitmap Get_FitSampleBitmap(InputStream inputStream, int width, int height) throws Exception {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        byte[] bytes = ReadStream(inputStream);
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        options.inSampleSize = Get_FitInSampleSize(width, height, options);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    public static android.graphics.Bitmap readBitmapFromFileDescriptor(String File_name,String File_Path, ImageView set_bitmap_Image, File sd_path) {
        try {
            int width =set_bitmap_Image.getWidth();
            int height=set_bitmap_Image.getHeight();
            String file_path=(File_Path+File_name);
            boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
            if(sdCardExist) {
                String search_path = (sd_path.getPath() + File_name);
                File search_file = new File(search_path);
                if(search_file.exists()){
                    file_path=search_path;
                }
            }
            FileInputStream fis = new FileInputStream(file_path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);
            float srcWidth = options.outWidth;
            float srcHeight = options.outHeight;
            int inSampleSize = 1;

            if (srcHeight > height || srcWidth > width) {
                if (srcWidth > srcHeight) {
                    inSampleSize = Math.round(srcHeight / height);
                } else {
                    inSampleSize = Math.round(srcWidth / width);
                }
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;

            return BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);
        } catch (Exception ex) {
        }
        return null;
    }

    public static byte[] ReadStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }


    public static  int Get_FitInSampleSize(int Require_Width, int Require_Height, BitmapFactory.Options options) {
        int inSampleSize = 1;
        if (options.outWidth > Require_Width || options.outHeight > Require_Height) {
            int widthRatio = Math.round((float) options.outWidth / (float) Require_Width);
            int heightRatio = Math.round((float) options.outHeight / (float) Require_Height);
            inSampleSize = Math.min(widthRatio, heightRatio);
        }
        return inSampleSize;
    }

    public android.graphics.Bitmap Re_Scale_Bitmap(android.graphics.Bitmap Need_To_Re, int Size_x, int Size_y){
        return android.graphics.Bitmap.createScaledBitmap(Need_To_Re,Size_x,Size_y,true);
    }


    public void Recycle_Bitmap(ImageView image){

        BitmapDrawable bitmapDrawable = (BitmapDrawable) image.getDrawable();

        if(!bitmapDrawable.getBitmap().isRecycled()&&bitmapDrawable.getBitmap()!=null)
        {

            bitmapDrawable.getBitmap().recycle();
        }
                image.setImageBitmap(null);
    }

    public void Recycle_Bitmap(android.graphics.Bitmap[] bitmap){

        for(android.graphics.Bitmap Need_Recycle_Bitmap : bitmap){

            if(Need_Recycle_Bitmap!=null && !Need_Recycle_Bitmap.isRecycled()) {
                Need_Recycle_Bitmap.recycle();

            }
        }

    }

    public static float DP_From_PX(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float PX_Form_DP(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

}