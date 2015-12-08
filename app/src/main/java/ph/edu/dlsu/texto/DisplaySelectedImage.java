package ph.edu.dlsu.texto;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplaySelectedImage extends AppCompatActivity {

    String displayimagepath, displayname;
    String datapath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_selected_image);
        ImageView displayimage = (ImageView) findViewById(R.id.displayimageView);
        TextView displaynametext = (TextView) findViewById(R.id.displaynameView);
        TextView displayimagepathtext = (TextView) findViewById(R.id.displayimagepathView);

        Intent intent = getIntent();
        displayimagepath = intent.getStringExtra("passImagePath");
        displayname = intent.getStringExtra("passName");
        datapath = intent.getStringExtra("passlangdatapath");

        displayimage.setImageDrawable(Drawable.createFromPath(displayimagepath));
        displaynametext.setText("Name: " + displayname);
        displayimagepathtext.setText("Located at: " + displayimagepath);
    }

    public void textrecognition(View view){
        Intent totextrecognition = new Intent(DisplaySelectedImage.this, text_recognition.class);
        totextrecognition.putExtra("passImagePathtotextrecog", displayimagepath);
        totextrecognition.putExtra("passNametotextrecog", displayname);
        startActivity(totextrecognition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_selected_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
