package ph.edu.dlsu.texto;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainSelection extends AppCompatActivity {

    SQLiteDatabase db;
    TexToDatabaseHelper dbHelper;

    private static final int REQUEST_IMAGE_CAPTURE = 0;
    private static final int REQUEST_SELECT_IMAGE = 1;

    public String imagePath;
    public String name;

    TextView text;
    EditText nametext;
    ImageView picture;
    Intent goTakePicture;
    Uri fileUri;
    File folder;
    File imagefile;

    int increment = 0;

    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/TexTo_storage/";
    private static final String lang = "eng";
    private static final String TAG = "MainSelection.java";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_selection);
        dbHelper = new TexToDatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
        toFile();
    }

    public void selectOption(View view){
        final CharSequence[] options = {"Take a Photo", "Choose Photo from Gallery", "Cancel"};
        AlertDialog.Builder alert = new AlertDialog.Builder(MainSelection.this);
        alert.setTitle("Select Option");
        alert.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options[which].equals("Take a Photo")) {
                    goTakePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = getFile();
                    fileUri = Uri.fromFile(file);
                    goTakePicture.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(goTakePicture, REQUEST_IMAGE_CAPTURE);
                } else if (options[which].equals("Choose Photo from Gallery")) {
                    Intent goSelectPicture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(goSelectPicture, REQUEST_SELECT_IMAGE);
                } else if (options[which].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        EditText n = (EditText) findViewById(R.id.name_edittext);
        n.setText("");
        alert.show();
    }

    public void getText(View view){
        try {
            if(!imagePath.equals("")) {
                Intent toDirectTextRecog = new Intent(MainSelection.this, text_recognition.class);
                toDirectTextRecog.putExtra("passImagePathtotextrecog", imagePath);
                toDirectTextRecog.putExtra("passNametotextrecog", "");
                startActivity(toDirectTextRecog);
            }
            else
                Toast.makeText(this, "Cannot load text recognition", Toast.LENGTH_SHORT).show();

        }
        catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Cannot load text recognition", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveimage(View view){
        picture = (ImageView) findViewById(R.id.viewpicture);
        nametext =  (EditText) findViewById(R.id.name_edittext);
        name = nametext.getText().toString();
//        Toast.makeText(this, "image Path:" + imagePath, Toast.LENGTH_SHORT).show();
        try {
            if (!nametext.getText().toString().equals("") && !imagePath.equals("")) {
                text = (TextView) findViewById(R.id.imagetext);
                text.setText(name + " Located at " + imagePath);

                // Add to database when click the saved button
                Toast.makeText(this, "Image Saved in the Database", Toast.LENGTH_SHORT).show();
                dbHelper.insertPhotos(db, name, imagePath);

                // After Saving.. Clears all
                name = "";
                imagePath = "";
                nametext.setText("");
                picture.setImageResource(R.drawable.noimage);
            }
            else {
                Toast.makeText(this, "Please input name and picture", Toast.LENGTH_SHORT).show();
            }
            }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Cannot save", Toast.LENGTH_SHORT).show();
        }
    }


    public void checklist(View view){
        Intent tolistofsavedimages = new Intent(MainSelection.this, ListofSavedImages.class);
        startActivity(tolistofsavedimages);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            imagePath = DATA_PATH+"image"+increment+".jpg";
            Toast.makeText(this, "Saved at " + imagePath, Toast.LENGTH_LONG).show();
            picture = (ImageView) findViewById(R.id.viewpicture);
            picture.setImageDrawable(Drawable.createFromPath(imagePath));
            //getOCR(imagePath);
        }
        else if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK){
            //Read picked image
            Uri imageUri = data.getData();
            imagePath = getRealPath(imageUri);
            Toast.makeText(this, "Located at " + imagePath, Toast.LENGTH_LONG).show();
            picture = (ImageView) findViewById(R.id.viewpicture);
            picture.setImageDrawable(Drawable.createFromPath(imagePath));
            //getOCR(imagePath);
        }
    }

    // For creating the file after taking a photo
    private File getFile(){
        do{
            increment++;
            imagefile = new File(folder, "image"+ increment + ".jpg");
        }while(imagefile.exists());
        return imagefile;
    }

    public void toFile(){

        folder = new File("sdcard/TexTo_storage");

        if(!folder.exists()){
            folder.mkdir();
        }

        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }
        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }
    }

    // For Getting the File path from file selected from gallery
    public String getRealPath(Uri image_Uri){
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(image_Uri, filePathColumn, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.help) {
            try{
                Intent toHelp = new Intent(MainSelection.this, Help.class);
                startActivity(toHelp);

                return true;}
            catch (Exception e){
                e.printStackTrace();
            }
        }

        return super.onOptionsItemSelected(item);
    }

}

