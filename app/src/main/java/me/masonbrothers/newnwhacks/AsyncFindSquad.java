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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Yuqing on 2/28/2016.
 */
public class AsyncFindSquad extends EventAsyncTask {

    public AsyncFindSquad(MainActivity activity){
        super(activity);
    }

    @Override
    protected void doInBackground() throws IOException {
        MainActivity.squadTimes = displayTimesWhenPeopleAreFree();

        Log.i("Result", MainActivity.squadTimes.toString());

    }

    static void run(MainActivity calendarSample) {
        new AsyncFindSquad(calendarSample).execute();
    }

    static Date toNearestWholeHour(Date d) {
        Calendar c = new GregorianCalendar();
        c.setTime(d);

        c.add(Calendar.HOUR, 1);

        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        return c.getTime();
    }

    /*
         * Gets the times when each person is busy
         *
         * SYNTAX for FreeBusyRequestItems:
         *  FreeBusyRequestItem fbd = new FreeBusyRequestItem();
            fbd.setId(email goes here);
         */
    private FreeBusyResponse getTimesWhenBusy(List<FreeBusyRequestItem> freeBusyRequestItemList, DateTime timeMin, DateTime timeMax ){
        DateTime now = new DateTime(System.currentTimeMillis());
        FreeBusyRequest freeBusyRequest = new FreeBusyRequest();
        freeBusyRequest.setItems(freeBusyRequestItemList);
        freeBusyRequest.setTimeMin(timeMin);
        freeBusyRequest.setTimeMax(timeMax);
        try{
            return client.freebusy().query(freeBusyRequest).execute();
        } catch(Exception e){
            System.out.println("Error: IO Exception Free Busy Request");
            return null;
        }
    }

    /*
     * Gets the intervals when people are free
     */
    public Map<TimePeriod, Integer> displayTimesWhenPeopleAreFree(){
        // get the list of people to check
        List<FreeBusyRequestItem> freeBusyRequestItemList = new ArrayList<FreeBusyRequestItem>();


        // loop through and add all people to freeBusyRequestItemList
        // TODO
        for(String currentVal : activity.contactsPlease().values()){
           FreeBusyRequestItem fbd = new FreeBusyRequestItem();

            if(currentVal == null){continue;}
           freeBusyRequestItemList.add(fbd.setId(currentVal));
        }
        // get the next Hour and the next day
        Date now = new Date();
        Date nearestHourDate = toNearestWholeHour(now);
        DateTime nearestHourDateTime = new DateTime(nearestHourDate);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nearestHourDate);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date nextHourDate = calendar.getTime();
        DateTime nextDateTime = new DateTime(nextHourDate);

        FreeBusyResponse timesBusy = getTimesWhenBusy(freeBusyRequestItemList, nearestHourDateTime, nextDateTime);
        Map<String,FreeBusyCalendar> timesBusyCalendarMap = timesBusy.getCalendars();
        Set<String> keys = timesBusyCalendarMap.keySet();

        // create time periods for the next 24 hours
        List<TimePeriod> timePeriods = new ArrayList<TimePeriod>();
        int i = 0;
        while(nearestHourDate.before(nextHourDate)){


            timePeriods.add(new TimePeriod());
            timePeriods.get(i).setStart(new DateTime(nearestHourDate));
            Calendar nextDate = Calendar.getInstance();
            nextDate.setTime(nearestHourDate);
            nextDate.add(Calendar.HOUR_OF_DAY, 1);
            nearestHourDate = nextDate.getTime();
            timePeriods.get(i).setEnd(new DateTime(nearestHourDate));
            i++;


        }

        Map<TimePeriod, Integer> numOfPeopleFreeAtEachTimePeriod = new HashMap<>();
        for(TimePeriod a : timePeriods){
            Integer help = 0;
            numOfPeopleFreeAtEachTimePeriod.put(a, help);
        }
        // iterate through people and find times when they're free.
        for (String strings : keys) {
            // get times when person is free
            List<TimePeriod> timesWhenBusy = timesBusyCalendarMap.get(strings).getBusy();
            for(TimePeriod current : timesWhenBusy){
                for(TimePeriod query : timePeriods){
                    if(!isOverlapping(current,query)){
                        Integer previous = numOfPeopleFreeAtEachTimePeriod.get(query);
                        numOfPeopleFreeAtEachTimePeriod.put(query, previous + 1);
                    }
                }
            }
        }

        Log.i("Result", numOfPeopleFreeAtEachTimePeriod.toString());
        return numOfPeopleFreeAtEachTimePeriod;
    }

    /*
     * Compares to see if two time periods overlap
     */
    boolean isOverlapping(TimePeriod a, TimePeriod b){
        Date aStart = new Date(a.getStart().getValue());
        Date aEnd = new Date(a.getEnd().getValue());
        Date bStart = new Date(b.getStart().getValue());
        Date bEnd = new Date(b.getEnd().getValue());

        if(aStart.before(bStart)){
            if(aEnd.before(bStart)){
                return false;
            }
            else{
                return true;
            }
        } else{
            if(aStart.after(bEnd)){
                return false;
            }
            else{
                return true;
            }
        }

    }
}
