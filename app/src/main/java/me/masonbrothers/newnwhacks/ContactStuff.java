package me.masonbrothers.newnwhacks;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by mason on 28/02/16.
 */
public class ContactStuff extends AppCompatActivity {
    public Map<String, String> contactsPlease()
    {
        Map<String, String> contacts = new HashMap<String, String>() {};
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, null, null, null);

        while (cur.moveToNext())
        {
            String name = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME_PRIMARY));
            String email = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
            contacts.put(name,email);
        }


        return contacts;
    }

    public void testContacts(Map<String, String> contacts)
    {
        Set<String> keys = contacts.keySet();
        for(String keyName : keys){
            Log.i("TESTO", String.valueOf(keys.size()));
            Log.i("TESTO", keyName + " " + contacts.get(keyName).toString());
        }

    }

    public String[] dennisContacts()
    {
        Set<String> keys = contactsPlease().keySet();
        String[] dennis = new String[keys.size()];
        int i = 0;
        for(String keyName : keys){
            dennis[i] = keyName;
            i++;
        }
        return dennis;
    }
}