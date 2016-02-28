package me.masonbrothers.newnwhacks;

import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.FreeBusyCalendar;
import com.google.api.services.calendar.model.FreeBusyRequest;
import com.google.api.services.calendar.model.FreeBusyRequestItem;
import com.google.api.services.calendar.model.FreeBusyResponse;
import com.google.api.services.calendar.model.TimePeriod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Yuqing on 2/28/2016.
 */
public class AsyncAddEvent extends EventAsyncTask{

    private String summary;
    private DateTime startDateTime;
    private DateTime endDateTime;
    private EventAttendee[] attendees;

    public AsyncAddEvent(MainActivity activity, String summary, DateTime startDateTime, DateTime endDateTime, EventAttendee[] attendees){
        super(activity);

        this.summary = summary;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.attendees = attendees;
    }

    @Override
    protected void doInBackground() throws IOException {
        createEventAndSendInvites(summary, startDateTime, endDateTime, attendees);
    }

    static void run(MainActivity calendarSample, String summary, DateTime startDateTime, DateTime endDateTime, EventAttendee[] attendees) {
        new AsyncAddEvent(calendarSample, summary, startDateTime, endDateTime, attendees).execute();
    }

    /*
     * Creates a new event and sends out notifications to others requesting them to accept the event
     *
     *  EventAttendee[] attendees = new EventAttendee[] {
                new EventAttendee().setEmail("lpage@example.com"),
                new EventAttendee().setEmail("sbrin@example.com"),
        };
     */
    void createEventAndSendInvites(String summary, DateTime startDateTime, DateTime endDateTime, EventAttendee[] attendees ){
        Event event = new Event()
                .setSummary(summary)
                .setLocation("800 Howard St., San Francisco, CA 94103")
                .setDescription("A chance to hear more about Google's developer products.");

        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("America/Los_Angeles");
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("America/Los_Angeles");
        event.setEnd(end);

        event.setAttendees(Arrays.asList(attendees));

        String calendarId = "primary";
        try {
            event = client.events().insert(calendarId, event).execute();
        } catch(Exception e){
            System.out.println("Error: IO Exception, was not able to create Event!");
        }

        System.out.printf("Event created: %s\n", event.getHtmlLink());
    }

}
