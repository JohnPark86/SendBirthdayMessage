package com.example.sendbirthdaymessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TextView;

public class BirthdayMessage extends Activity
{
    TextView tv1,tv2;
    SQLiteDatabase db;
    ContentValues cv;
    String[] lines;
    Cursor c;
    DateFormat date;
    ArrayList<String> bDayNames = new ArrayList<String>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birthday_message);
        tv1 =(TextView)findViewById(R.id.tv1);
        tv2= (TextView)findViewById(R.id.tv2);
        createTable();
        fileRead();
        checkBirthday();
        db.execSQL("drop table if exists Contacts;");
    }

    
    protected void fileRead()
    {
      
        try 
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.contacts)));
        String line;
        
            while((line=br.readLine())!=null)
            {
                lines=line.split("/");
                populateTable(lines);
            }
           
            br.close();
        }
        catch  (IOException e) 
        {  
            e.printStackTrace();
        }
    }
    
    protected void createTable() 
    {
        try
        {
            db = openOrCreateDatabase("Contacts", SQLiteDatabase.CREATE_IF_NECESSARY, null);
            db.setLockingEnabled(true);
            db.setVersion(1);
           
            String sqlcmd = "create table if not exists Contacts(id INTEGER PRIMARY KEY, Name TEXT, Birthday DATETIME,"
                                + "PhoneNumber TEXT, Email TEXT);"; 
            db.execSQL(sqlcmd);
        } catch (SQLException e)
        {
           Log.d("error",e.getMessage());
        }
    }
    
    protected void populateTable(String[] lines)
    {
        try
        {
            cv = new ContentValues();
            cv.put("Name",lines[0]);
            cv.put("Birthday",lines[1]);
            cv.put("PhoneNumber", lines[2]);
            cv.put("Email",lines[3]);
            long recnum = db.insert("Contacts",null,cv);
            lines=new String[lines.length];
  
        } catch (Exception e)
        {
            Log.d("error",e.getMessage());
        }
    }
    
    public void checkBirthday()
    {
        date = new DateFormat();
        String currDate = new SimpleDateFormat("MM-dd").format(new Date());
        c = db.query("Contacts",null,null,new String[]{},null,null,null,null);
        if (c != null)
        {
            c.moveToFirst();
        }
        do
        {
            String string1 = c.getString(2);
            string1 = string1.substring(5);
            
            if(currDate.equals(string1))
            {
                String name = c.getString(1);
                String phone = c.getString(3);
                String message = birthdayMessage(name);
                bDayNames.add(name);
                sendSMS(message,phone);
            }  
        }while(c.moveToNext());
        String birthdays = "";
        for (int i = 0; i < bDayNames.size(); i++)
        {
            if(i==bDayNames.size()-1)
                birthdays+="and " + bDayNames.get(i);
            else    
                birthdays+=bDayNames.get(i)+", ";
            
        }
        tv2.setText(birthdays);
    }

    /**
     * @param message
     */
    public void sendSMS(String message,String number)
    {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage("5556", null, message, null, null);
    }

    /**
     * @throws NotFoundException
     */
    public String birthdayMessage(String name) throws NotFoundException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.birthday_wish)));
        String message = "";
        String line;
        try
        {
            while ((line=br.readLine())!=null)    
            {
                if(line!=null)
                    message+=line;
                    message+=" ";
            }
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        message = message.replace("XXX", name);
        Log.d("message",""+ message);
        return message;
    }
}

