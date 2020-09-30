/*
 * Copyright (c) 2018.JE-Chen
 */

package com.je_chen.je_ocr;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;



    public class Download_File extends AsyncTask<String, Double, Boolean> {

        public static final String TAG = "Download_File";

        private String Url;

        private Context Activity_Context;

        private long FileSize;

        private long TotalReadSize;

        //構造方法,建構子
        public Download_File(Context context) {
            Activity_Context = context;
        }


        // 執行耗時操作,params[0]為URL，params[1]為文件名（空則null）
        @Override
        protected Boolean doInBackground(String... params) {

            try {
                Url = params[0];
                //建立連結
                URLConnection connection = new URL(Url).openConnection();
                //獲取文件大小
                FileSize = connection.getContentLength();
                Log.d(TAG, "the count of the url content length is : " + FileSize);

                //獲得輸入流
                InputStream is = connection.getInputStream();
                //先建立資料夾(如果不存在 )
                File fold = new File(getFolderPath());
                if (!fold.exists()) {
                    fold.mkdirs();
                }

                String fileName = "";
                //判斷文件名，自定義或ＵＲＬ獲得
                if (params[1] != null) {
                    fileName = params[1];
                } else {
                    fileName = getFileName(params[0]);
                }
                //文件输出流 (依照文件夾路徑+傳入的檔案名稱)
                FileOutputStream fos = new FileOutputStream(new File(getFolderPath() + fileName));

                byte[] buff = new byte[1024];
                int len;
                while ((len = is.read(buff)) != -1) {
                    TotalReadSize += len;
                    fos.write(buff, 0, len);
                }
                fos.flush();
                fos.close();

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        //由URL獲得文件名
        private String getFileName(String string) {
            return string.substring(string.lastIndexOf("/") + 1);
        }

        //下載文件夾路徑
        private String getFolderPath() {
            Log.d("File_Path",Activity_Context.getFilesDir().getPath());
            return Activity_Context.getFilesDir().getPath()+"/"; }


        // doInBackground方法之前调用，初始化UI
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // 在doInBackground方法之後调用
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
               //成功
            } else {
                //失敗
            }
        }

        @Override
        protected void onProgressUpdate(Double... values) {
            super.onProgressUpdate(values);
        }


    }
