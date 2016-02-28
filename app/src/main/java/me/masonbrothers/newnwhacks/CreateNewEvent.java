package me.masonbrothers.newnwhacks;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.FreeBusyRequestItem;
import com.google.api.services.calendar.model.TimePeriod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CreateNewEvent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Map<TimePeriod, Integer> squadd = MainActivity.squadTimes;
        List<String> stringlist = new ArrayList<String>();
        for(TimePeriod t : squadd.keySet()){
            stringlist.add(t.getStart().toString() + " has people free: " + squadd.get(t));
        }
        setContentView(R.layout.activity_create_new_event);

        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_create_new_event,stringlist);
    }


    public void ButtonPress(View v){

//        EventAttendee[] attendees = new EventAttendee[]{
//                new EventAttendee().setEmail("yuqinggdu@gmail.com");
//                new EventAttendee().setEmail("calvinhyxu@gmail.com");
//        }

    }
}
