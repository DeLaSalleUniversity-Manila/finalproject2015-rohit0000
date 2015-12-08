package ph.edu.dlsu.texto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

public class text_recognition extends Activity {

    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/TexTo_storage/";

    public static final String lang = "eng";

    private static final String TAG = "text_recognition.java";

    TextView displayname;
    TextView displaytext;
    TextView displaytranslated;
    protected String _path;
    protected boolean _taken;

    String imagepathOCR;
    String nameOCR;

    protected static final String PHOTO_TAKEN = "photo_taken";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_recognition);

        // _image = (ImageView) findViewById(R.id.image);
        displayname = (TextView) findViewById(R.id.nametextView);
        displaytext =(TextView) findViewById(R.id.displaytextView);
        displaytranslated = (TextView) findViewById(R.id.translatedtextView);

        Intent intentfromDisplay = getIntent();
        imagepathOCR = intentfromDisplay.getStringExtra("passImagePathtotextrecog");
        nameOCR = intentfromDisplay.getStringExtra("passNametotextrecog");

        displayname.setText(nameOCR);

        //Log.v(TAG, _path+"========================================="); //DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TexTo_storage/"
        //Log.v(TAG, imagepathOCR+"========================================="); // real path of the file
        String extdatapath = imagepathOCR;

        if(imagepathOCR.startsWith("sdcard/")){
            extdatapath = "/storage/sdcard0/" + imagepathOCR.substring(7,imagepathOCR.length());
            //Log.v(TAG, "REPLACE sdcard/ to /storage/sdcard0/==============");
        }
        else{
            //Log.v(TAG, "NO CHANGE=========================================");
        }
        //Log.v(TAG, extdatapath+"======================================"); // replaced path of the file
        _path = extdatapath;
        startOCR();
    }

    public void selectLanguageTranslate(View view){

        try {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
            Translate.setClientId("ch1331");
            Translate.setClientSecret("q90KqMOnwkJYPfAIv7K5M49WLJ27w+DES+VW5KDw32w=");

            final CharSequence[] languages = {"ENGLISH", "GERMAN", "FRENCH", "CHINESE", "KOREAN", "JAPANESE", "CANCEL"};
            AlertDialog.Builder alert = new AlertDialog.Builder(text_recognition.this);
            alert.setTitle("Select Language");
            alert.setItems(languages, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String original = displaytext.getText().toString();
                    String translated = new String();
                    try {
                        if (languages[which].equals("ENGLISH")) {
                            translated = Translate.execute(original, Language.AUTO_DETECT, Language.ENGLISH);
                        } else if (languages[which].equals("GERMAN")) {
                            translated = Translate.execute(original, Language.AUTO_DETECT, Language.GERMAN);
                        } else if (languages[which].equals("FRENCH")) {
                            translated = Translate.execute(original, Language.AUTO_DETECT, Language.FRENCH);
                        } else if (languages[which].equals("CHINESE")) {
                            translated = Translate.execute(original, Language.AUTO_DETECT, Language.CHINESE_TRADITIONAL);
                        } else if (languages[which].equals("KOREAN")) {
                            translated = Translate.execute(original, Language.AUTO_DETECT, Language.KOREAN);
                        } else if (languages[which].equals("JAPANESE")) {
                            translated = Translate.execute(original, Language.AUTO_DETECT, Language.JAPANESE);
                        } else if (languages[which].equals("CANCEL")) {
                            dialog.dismiss();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    displaytranslated.setText(translated);
                }
            });
            alert.show();
        }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Cannot load translator", Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(text_recognition.PHOTO_TAKEN, _taken);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onRestoreInstanceState()");
        if (savedInstanceState.getBoolean(text_recognition.PHOTO_TAKEN)) {
            startOCR();
        }
    }

    protected void startOCR() {
        _taken = true;

        Log.v(TAG, _path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap bitmapOCR = BitmapFactory.decodeFile(_path, options);

        try {
            ExifInterface exif = new ExifInterface(_path);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Log.v(TAG, "Orient: " + exifOrientation);

            int rotate = 0;

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            Log.v(TAG, "Rotation: " + rotate);

            if (rotate != 0) {

                // Getting width & height of the given image.
                int a = bitmapOCR.getWidth();
                int b = bitmapOCR.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                // Rotating Bitmap
                bitmapOCR = Bitmap.createBitmap(bitmapOCR, 0, 0, a, b, mtx, false);
            }

            // Convert to ARGB_8888, required by tess
            bitmapOCR = bitmapOCR.copy(Bitmap.Config.ARGB_8888, true);

        } catch (IOException e) {
            Log.e(TAG, "Couldn't correct orientation: " + e.toString());
        }

        // _image.setImageBitmap( bitmap );

        Log.v(TAG, "Before baseApi");

        TessBaseAPI tessApi = new TessBaseAPI();
        tessApi.setDebug(true);
        tessApi.init(DATA_PATH, lang);
        tessApi.setImage(bitmapOCR);
        String recognizedText = tessApi.getUTF8Text();
        tessApi.end();

        Log.v(TAG, "OCRED TEXT: " + recognizedText);
        if ( lang.equalsIgnoreCase("eng") ) {
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        }

        recognizedText = recognizedText.trim();

        if ( recognizedText.length() != 0 ) {
            displaytext.setText(displaytext.getText().toString().length() == 0 ? recognizedText : displaytext.getText() + " " + recognizedText);
        }
    }
}