package com.je_chen.ocr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateRemoteModel;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import okhttp3.Callback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener, MessageCallBack{

    private Callback messageCallBack;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_GET_SPEECH = 2;
    static final int TTS_CHECK_CODE = 3;
    static final int WRITE_EXTERNAL_STORAGE = 4;
    static final String DEFAULT_LANGUAGE = "eng";
    static final String CHINESE_LANGUAGE = "chi_tra";
    static String TESSBASE_PATH = "";
    public Download_File Downloader;
    Zip_Process Zip = new Zip_Process();
    String mCurrentPhotoPath;
    Bitmap_Process Bitmap_C;
    int Picture_H;
    int Picture_W;
    TextToSpeech tts;

    FirebaseTranslator English_to_Zh;
    ImageView imageView;
    TextView txtResult;
    Button voice, translate, take_picture, instructions;
    //圖片名稱
    private String filename;

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    public String ocrWithEnglish() {
        String resString = "";

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 6;
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, options);
        //bitmap=Bitmap_C.Re_Scale_Bitmap(bitmap,Picture_W,Picture_H);
        Log.d("OnEEE", String.valueOf(bitmap.getHeight()));
        Log.d("OnEEE", String.valueOf(bitmap.getWidth()));
        imageView.setImageBitmap(bitmap);

        TessBaseAPI ocrApi = new TessBaseAPI();

        ocrApi.init(TESSBASE_PATH, CHINESE_LANGUAGE);
        ocrApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);
        ocrApi.setImage(bitmap);
        resString = ocrApi.getUTF8Text();
        ocrApi.clear();
        ocrApi.end();


        return resString;
    }

    public void run() {
        String ocrResult = ocrWithEnglish();
        Log.d("TAG-RE", ocrResult);
        txtResult.setText(ocrResult);
        File file = new File(mCurrentPhotoPath);
        Log.d("RRRR_ME", String.valueOf(file.exists()));
        Log.d("RRRR_ME", String.valueOf(file.getParent()));
        Log.d("RRRR_ME", file.getName());
        if (file.exists())
            file.delete();
    }

    public String Translate(FirebaseTranslator a, final String text) {
        a.translate(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                // Translation successful.
                                Log.d("Hello", translatedText);
                                Toast.makeText(getApplicationContext(), translatedText, Toast.LENGTH_LONG).show();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error.
                                // ...
                            }
                        });
        return text;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void prc_camera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.je_chen.ocr.fileprovider",
                        photoFile);
                Log.d("Photo_URL", String.valueOf(photoURI));
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    public void onInit(int status) {
        // TODO Auto-generated method stub
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.TAIWAN);    //設定語言為中文
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                tts.setPitch(1);    //語調(1為正常語調；0.5比正常語調低一倍；2比正常語調高一倍)
                tts.setSpeechRate(1);    //速度(1為正常速度；0.5比正常速度慢一倍；2比正常速度快一倍)
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_CHECK_CODE);

        Bitmap_C = new Bitmap_Process(getApplicationContext());
        Picture_H = (int) Bitmap_Process.PX_Form_DP(getApplicationContext(), 800);
        Picture_W = (int) Bitmap_Process.DP_From_PX(getApplicationContext(), 800);

        imageView = findViewById(R.id.imageView);
        txtResult = findViewById(R.id.txtResult);

        voice = findViewById(R.id.voice);
        voice.setOnClickListener(this);

        take_picture = findViewById(R.id.take_picture);
        take_picture.setOnClickListener(this);

        translate = findViewById(R.id.translate);
        translate.setOnClickListener(this);
        instructions = findViewById(R.id.instructions);
        instructions.setOnClickListener(this);

        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
        }


        if(!new File(getApplicationContext().getFilesDir() + "/data.zip").exists()) {
            Download_File Downloader = new Download_File(4);
            Downloader.download("https://drive.google.com/u/0/uc?id=1Y2F1S0-pRUewkIv_hHG-oL-IZ2rm7_QM&export=download", getFilesDir().getPath(), "data.zip", this);
        }


        Log.d("File_exists", String.valueOf(new File(getApplicationContext().getFilesDir() + "/data.zip").exists()));

        try {
            Zip.upZipFile(new File(getApplicationContext().getFilesDir()+ "/data.zip"),getApplicationContext().getFilesDir().getPath()+"/tessdata");
        } catch (IOException e) {
            e.printStackTrace();
        }



        TESSBASE_PATH = getApplicationContext().getFilesDir().getPath() + "/";

        FirebaseModelManager modelManager = FirebaseModelManager.getInstance();
        // Create an English-German translator:
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(FirebaseTranslateLanguage.EN)
                        .setTargetLanguage(FirebaseTranslateLanguage.ZH)
                        .build();

        English_to_Zh = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        English_to_Zh.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                // Model downloaded successfully. Okay to start translating.
                                // (Set a flag, unhide the translation UI, etc.)
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                // ...
                            }
                        });

        // Get translation models stored on the device.
        modelManager.getDownloadedModels(FirebaseTranslateRemoteModel.class)
                .addOnSuccessListener(new OnSuccessListener<Set<FirebaseTranslateRemoteModel>>() {
                    @Override
                    public void onSuccess(Set<FirebaseTranslateRemoteModel> models) {
                        // ...
                        for (FirebaseTranslateRemoteModel a : models) {
                            Log.d("Model", String.valueOf(a));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error.
                    }
                });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // shutdown tts
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.take_picture:
                prc_camera();
                break;

            case R.id.voice:
                onPause();
                new AlertDialog.Builder(MainActivity.this)
                        .setCancelable(false)
                        .setTitle(getString(R.string.Voice_Choice))
                        .setMessage(getString(R.string.Voice_Choice))
                        .setNeutralButton(getString(R.string.Voice_Input), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onResume();
                                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請說～");
                                try {
                                    startActivityForResult(intent, REQUEST_GET_SPEECH);
                                } catch (ActivityNotFoundException a) {
                                    Toast.makeText(getApplicationContext(), "Intent problem", Toast.LENGTH_SHORT).show();
                                }

                            }
                        })
                        .setPositiveButton(getString(R.string.Voice_Output), new DialogInterface.OnClickListener() {
                            @SuppressLint("NewApi")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onResume();
                                if (!txtResult.getText().toString().equals("")) {
                                    tts.speak(txtResult.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null); //發音
                                    Toast.makeText(getApplicationContext(), "正在唸\n" + txtResult.getText().toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }).show();
                break;

            case R.id.translate:
                if (txtResult.getText().toString() != "")
                    Translate(English_to_Zh, txtResult.getText().toString());
                break;

            case R.id.instructions:
                onPause();
                new AlertDialog.Builder(MainActivity.this)
                        .setCancelable(false)
                        .setTitle(getString(R.string.instruction_title))
                        .setMessage(getString(R.string.instruction_content))
                        .setNeutralButton(getString(R.string.instruction_button_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onResume();
                            }
                        })
                        .show();
                break;

        }
    }


    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //拍照
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File file = new File(mCurrentPhotoPath);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getBaseContext().getContentResolver(), Uri.fromFile(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bitmap != null) {

                ExifInterface ei = null;
                try {
                    ei = new ExifInterface(file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                Log.d("REEE", String.valueOf(orientation));


                switch (orientation) {

                    case ExifInterface.ORIENTATION_ROTATE_90:
                        Log.d("ORJE", String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
                        Log.d("ORJE", "ORIENTATION_ROTATE_90");
                        bitmap = rotateImage(bitmap, 90);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        Log.d("ORJE", String.valueOf(ExifInterface.ORIENTATION_ROTATE_180));
                        Log.d("ORJE", "ORIENTATION_ROTATE_180");
                        bitmap = rotateImage(bitmap, 180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        Log.d("ORJE", String.valueOf(ExifInterface.ORIENTATION_ROTATE_270));
                        Log.d("ORJE", "ORIENTATION_ROTATE_270");
                        bitmap = rotateImage(bitmap, 270);
                        break;

                    case ExifInterface.ORIENTATION_NORMAL:
                        Log.d("ORJE", "ORIENTATION_NORMAL");
                    default:
                        break;
                }
                //bitmap=Bitmap_C.Re_Scale_Bitmap(bitmap,Picture_W,Picture_H);
                //imageView.setImageBitmap(bitmap);
                Log.d("Reee", String.valueOf(bitmap.getHeight()));
                Log.d("Reee", String.valueOf(bitmap.getWidth()));
                File f = new File(mCurrentPhotoPath);
                try (FileOutputStream out = new FileOutputStream(f)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                run();
            }
        }
        //文字語音輸入
        if (requestCode == REQUEST_GET_SPEECH) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                //txtResult.setText(result.get(0));
                if (result.get(0).equals("拍照")) {
                    prc_camera();
                } else if (result.get(0).equals("翻譯")) {
                    Translate(English_to_Zh, txtResult.getText().toString());
                } else if (result.get(0).equals("唸出來")) {
                    if (!txtResult.getText().toString().equals(""))
                        tts.speak(txtResult.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null); //發音
                    Toast.makeText(getApplicationContext(), "正在唸\n" + txtResult.getText().toString(), Toast.LENGTH_LONG).show();
                }
            }
        }
        //文字語音輸出
        if (requestCode == TTS_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                tts = new TextToSpeech(this, this);
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onDownloadStarted(long totalLength) {
        Log.d("totalLength", String.valueOf(totalLength));
    }

    @Override
    public void onDownloadProgress(long downloadedLength) {
        Log.d("downloadedLength", String.valueOf(downloadedLength));
    }

    @Override
    public void onDownloadComplete(File file) {
        Log.d("DownloadComplete",file.getName());
    }

    @Override
    public void onDownloadError(Exception e) {
        Log.d("Exception",e.toString());
    }

}
