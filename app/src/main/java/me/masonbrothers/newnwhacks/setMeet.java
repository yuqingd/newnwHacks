package me.masonbrothers.newnwhacks;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.os.Bundle;
import android.widget.EditText;
import android.view.View;
import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.app.Activity;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter ;


public class setMeet extends Activity{
    String[] zegroup = {"Cinny", "Yifei", "Calvin", "Mason",
            "Jerry", "Suzanne", "Yo-mama", "Mr.PoopyButtHole", "Schleem"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_meet);

        ListAdapter happyAdaptor = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, zegroup);
        ListView happyListView = (ListView) findViewById(R.id.listView);
        happyListView.setAdapter(happyAdaptor);

    }
    public void onClick(View view){
        Intent i = new Intent(this, MainActivity.class);
        i.setAction("me.masonbrothers.nwhacks.chosenContacts");
        final EditText eventplan = (EditText)findViewById(R.id.eventplan);
        String e_planner =eventplan.getText().toString();
        String[] newGroup = new String[zegroup.length + 1];
        newGroup[0] = e_planner;
        for (int j = 1; j < zegroup.length + 1; j++)
            newGroup[j] = zegroup[j-1];
        i.putExtra("Event->Data", newGroup);
        startActivity(i);
    }



}
