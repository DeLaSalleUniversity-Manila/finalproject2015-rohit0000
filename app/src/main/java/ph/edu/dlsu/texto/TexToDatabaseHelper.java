package ph.edu.dlsu.texto;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.jar.Attributes;

/**
 * Created by rybackpo on 11/19/2015.
 */
public class TexToDatabaseHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "TextTo";
    private static final int DB_VERSION = 1;
    static String PhotosTable = "PHOTO";

    TexToDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + PhotosTable + "(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "NAME TEXT, " +
                "IMAGE_PATH TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }

    public void insertPhotos(SQLiteDatabase db, String name, String image_path){
        ContentValues photosvalues = new ContentValues();
        photosvalues.put("NAME", name);
        photosvalues.put("IMAGE_PATH", image_path);
        db.insert(PhotosTable, null, photosvalues);
        Log.d("insertPhotos", "=================" + name + " " + image_path );
    }

    public void delete(String id){

        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(PhotosTable, "NAME=?", new String[]{id});
        db.close();
    }

    public void deleteAll(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(PhotosTable,null,null);
        db.close();
    }

}
