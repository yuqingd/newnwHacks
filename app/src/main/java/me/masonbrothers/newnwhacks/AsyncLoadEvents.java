package me.masonbrothers.newnwhacks;

/**
 * Created by Yuqing on 2/28/2016.
 */

import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.List;

/**
 * Asynchronously load the calendars.
 *
 * @author Yaniv Inbar
 */
class AsyncLoadEvents extends EventAsyncTask {

    AsyncLoadEvents(MainActivity calendarSample) {
        super(calendarSample);
    }

    @Override
    protected void doInBackground() throws IOException {
        //Calendar cal = client.calendarList().list().setFields(CalendarInfo.FEED_FIELDS).execute();
        Events events = client.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(new DateTime(System.currentTimeMillis()))
                .execute();
        List<Event> items = events.getItems();
        MainActivity.userEvents = items;

        Log.i("MESSAGE", events.getItems().toString());
    }

    static void run(MainActivity calendarSample) {
        new AsyncLoadEvents(calendarSample).execute();
    }
}