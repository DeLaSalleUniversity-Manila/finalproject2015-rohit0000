package ph.edu.dlsu.texto;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import java.util.ArrayList;
import java.util.List;


public class ListofSavedImages extends AppCompatActivity {

    private SQLiteDatabase db;
    private TexToDatabaseHelper dbHelper;

    ArrayList<String> names;
    ArrayList<String> imagepath;

    String[] namesArray;
    String[] imagepathArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listof_saved_images);
        readDatabase();
        initializeList();

    }

    public void initializeList(){
        namesArray = names.toArray(new String[names.size()]);
        imagepathArray = imagepath.toArray(new String[imagepath.size()]);

        CustomList adapter = new CustomList(this, namesArray, imagepathArray);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, imagepathArray);
        ListView list = (ListView) findViewById(R.id.photolistView);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent toDisplaySelectedImage = new Intent(ListofSavedImages.this, DisplaySelectedImage.class);
                //Intent toGet = getIntent();

                toDisplaySelectedImage.putExtra("passImagePath", imagepathArray[position]);
                toDisplaySelectedImage.putExtra("passName", namesArray[position]);

                //toDisplaySelectedImage.putExtra("passlangdatapath",toGet.getStringExtra("passdatapath"));
                startActivity(toDisplaySelectedImage);
            }
        });
    }

    public void displayList(){
        namesArray = names.toArray(new String[names.size()]);
        imagepathArray = imagepath.toArray(new String[imagepath.size()]);

        CustomList adapter = new CustomList(this, namesArray, imagepathArray);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, imagepathArray);
        ListView list = (ListView) findViewById(R.id.photolistView);
        list.setAdapter(adapter);
    }
    public void readDatabase(){

        names = new ArrayList<String>();
        imagepath = new ArrayList<String>();
        try{
            dbHelper = new TexToDatabaseHelper(this);
            db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(dbHelper.PhotosTable,null,null,null,null,null,null);

            if(cursor != null){
                while(cursor.moveToNext()){
                    names.add(cursor.getString(cursor.getColumnIndex("NAME")));
                    imagepath.add(cursor.getString(cursor.getColumnIndex("IMAGE_PATH")));
                }
            }
            cursor.close();
            db.close();
        }catch(SQLiteException e){
            Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_listof_saved_images, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete) {
            namesArray = names.toArray(new String[names.size()]);
            imagepathArray = imagepath.toArray(new String[imagepath.size()]);

            final CustomList adapter = new CustomList(this, namesArray, imagepathArray);
            //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, imagepathArray);
            ListView list = (ListView) findViewById(R.id.photolistView);
            list.setAdapter(adapter);
            Toast.makeText(ListofSavedImages.this, "Select entry to delete", Toast.LENGTH_SHORT).show();
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Object obj = adapter.getItem(position);
                    String value = obj.toString();
                    dbHelper.delete(value);
                    Toast.makeText(ListofSavedImages.this, "Deleted: " + value, Toast.LENGTH_SHORT).show();
                    readDatabase();
                    displayList();
                }
            });


            return true;
        }

        if (id == R.id.deleteAll){
            dbHelper.deleteAll();
            Toast.makeText(ListofSavedImages.this, "Deleted all", Toast.LENGTH_SHORT).show();
            Intent toMainSelection = new Intent(ListofSavedImages.this, MainSelection.class);
            startActivity(toMainSelection);
        }

        return super.onOptionsItemSelected(item);
    }
}
