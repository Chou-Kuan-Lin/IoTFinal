package com.example.lin.iotfinal;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.provider.ContactsContract.Data;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;

public class MainActivity2 extends Activity {

    Button bt_run;
    CheckBox cb_sd, cb_server;
    ListView listView;

    String data = "";
    String url = "http://203.145.202.150/test.php";
    String name = "";
    String number = "";

    ContentResolver cr;
    Cursor cur, pCur;
    ArrayList<String> ar;
    String[] ar_server;
    File file;
    BufferedReader br;
    File root;
    File gpxfile;
    FileWriter writer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        cb_sd = (CheckBox) findViewById(R.id.cb_sd);
        cb_server = (CheckBox) findViewById(R.id.cb_server);
        bt_run = (Button) findViewById(R.id.bt_run);
        listView = (ListView) findViewById(R.id.listview);

        ar = new ArrayList<String>();

        //顯示資料
        if(MainActivity.control==true)  // 備份
        {
            bt_run.setText("備份");
            // 手機聯絡簿
            cr = getContentResolver();
            cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        pCur = cr.query(  ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", new String[]{cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))}, null );
                        while (pCur.moveToNext()) {
                            number = filterDate(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                        }
                        pCur.close();
                    }
                    ar.add(name+"\n"+number);
                }
            }

            // 聯絡人資料數0
            if(cur.getCount()==0)
                Toast.makeText(MainActivity2.this, "沒有聯絡人資料", Toast.LENGTH_SHORT).show();
        }
        else // 還原
        {
            bt_run.setText("還原");
            // 伺服器
            if(isConnected(MainActivity2.this)) {
                String temp = getUrlContents(url);
                ar_server = temp.split("_");
                for (int i = 0; i < ar_server.length - 1; i++)
                    ar.add("(伺服器)"+ar_server[i]);
            }else
                Toast.makeText(MainActivity2.this, "目前沒連上網際網路\n無法取得伺服器資料", Toast.LENGTH_SHORT).show();

            // SD
            file = new File(Environment.getExternalStorageDirectory().getPath(),"Contact.txt");
            StringBuilder text = new StringBuilder();

            try {
                br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            }
            catch (IOException e) {
                Toast.makeText(MainActivity2.this, "記憶卡中無指定檔案\n無法取得聯絡人資料", Toast.LENGTH_SHORT).show();
            }
            ar_server = text.toString().split("_");
            for(int i=0;i<ar_server.length-1;i++)
                ar.add("(記憶卡)"+ar_server[i]);

        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity2.this, android.R.layout.simple_list_item_1, android.R.id.text1, ar);
        listView.setAdapter(adapter);

        bt_run.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN)
                    Toast.makeText(MainActivity2.this, "程序處理中", Toast.LENGTH_SHORT).show();
                else if(event.getAction()==MotionEvent.ACTION_UP)
                {
                    if(MainActivity.control==true)  // 備份
                    {
                        if(cb_sd.isChecked())
                        {
                            // SD備份
                            try {
                                root = new File(Environment.getExternalStorageDirectory().getPath());
                                if (!root.exists()) {
                                    root.mkdirs();
                                }
                                gpxfile = new File(root, "Contact.txt");
                                writer = new FileWriter(gpxfile);

                                cr = getContentResolver();
                                cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                                if (cur.getCount() > 0) {
                                    while (cur.moveToNext()) {
                                        name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                                        if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                                            pCur = cr.query(  ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", new String[]{cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))}, null );
                                            while (pCur.moveToNext()) {
                                                number = filterDate(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                                            }
                                            pCur.close();
                                        }
                                        if(number!="")
                                            writer.append(name+"\n"+number+"_");
                                    }
                                }

                                writer.close();
                                //Toast.makeText(MainActivity2.this, "存入記憶卡成功", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Toast.makeText(MainActivity2.this, "存入記憶卡失敗", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }

                        if(cb_server.isChecked())
                        {
                            // Server備份
                            if(isConnected(MainActivity2.this))
                            {
                                runUrl(url+"?name=delete&number=delete");  // 先刪除原有資料，避免重複!!
                                cr = getContentResolver();
                                cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                                if (cur.getCount() > 0) {
                                    while (cur.moveToNext()) {
                                        name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                                        if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                                            pCur = cr.query(  ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", new String[]{cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))}, null );
                                            while (pCur.moveToNext()) {
                                                number = filterDate(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                                            }
                                            pCur.close();
                                        }
                                        runUrl(url+"?name="+name+"&number="+number);
                                    }
                                }

                                //Toast.makeText(MainActivity2.this, "存入伺服器成功", Toast.LENGTH_SHORT).show();
                            }else
                                Toast.makeText(MainActivity2.this, "目前沒有連上網際網路\n無法儲存資料至伺服器", Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(MainActivity2.this, "程序處理完畢", Toast.LENGTH_SHORT).show();

                    }else   // 還原
                    {
                        if(cb_sd.isChecked())
                        {
                            // SD還原
                            file = new File(Environment.getExternalStorageDirectory().getPath(),"Contact.txt");
                            StringBuilder text = new StringBuilder();

                            try {
                                br = new BufferedReader(new FileReader(file));
                                String line;

                                while ((line = br.readLine()) != null) {
                                    text.append(line);
                                    text.append('\n');
                                }
                                br.close();
                            }
                            catch (IOException e) {
                                Toast.makeText(MainActivity2.this, "記憶卡中無指定檔案\n無法取得聯絡人資料", Toast.LENGTH_SHORT).show();
                            }

                            ar_server = text.toString().split("_"); // name,number
                            String temp = "";
                            String[] ar_temp, ar_temp2;

                            for(int i=0;i<ar_server.length-1;i++) {
                                temp = ar_server[i].toString();
                                ar_temp = temp.split(",");
                                ar_temp2 = ar_temp[0].toString().split("\n");
                                addContact(ar_temp2[0].toString(), ar_temp2[1].toString());
                            }
                            // ********記憶卡->手機聯絡簿*********

                            //Toast.makeText(MainActivity2.this, "取得記憶卡資料成功", Toast.LENGTH_SHORT).show();
                        }

                        if(cb_server.isChecked())
                        {
                            // Server還原
                            if(isConnected(MainActivity2.this))
                            {
                                //Toast.makeText(MainActivity2.this, "取得伺服器資料成功", Toast.LENGTH_SHORT).show();
                                if(isConnected(MainActivity2.this)) {
                                    ar_server = getUrlContents(url).split("_");
                                    String temp = "";
                                    String[] ar_temp, ar_temp2;

                                    for(int i=0;i<ar_server.length-1;i++) {
                                        temp = ar_server[i].toString();
                                        ar_temp = temp.split(",");
                                        ar_temp2 = ar_temp[0].toString().split("\n");
                                        addContact(ar_temp2[0].toString(), ar_temp2[1].toString());
                                    }
                                }else
                                    Toast.makeText(MainActivity2.this, "目前沒連上網際網路\n無法取得伺服器資料", Toast.LENGTH_SHORT).show();
                            }else
                                Toast.makeText(MainActivity2.this, "目前沒有連上網際網路\n無法取得資料至伺服器", Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(MainActivity2.this, "程序處理完畢", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });
    }



    //取得網頁字串
    public static String getUrlContents(String u)
    {
        final String theUrl = u;
        Thread thread;
        final StringBuilder content = new StringBuilder();

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    // create a url object
                    final URL url = new URL(theUrl);

                    // create a urlconnection object
                    URLConnection urlConnection = url.openConnection();

                    // wrap the urlconnection in a bufferedreader
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    String line;

                    // read from the urlconnection via the bufferedreader
                    while ((line = bufferedReader.readLine()) != null)
                    {
                        content.append(line + "\n");
                    }

                    bufferedReader.close();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try{
            thread.sleep(100);
        }catch (Exception e){};
        thread.interrupt();

        return content.toString();
    }


    // 執行網頁
    public static void runUrl(String ru)
    {
        final String theRunUrl = ru;
        Thread thread;
        final StringBuilder content = new StringBuilder();

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    // create a url object
                    final URL url = new URL(theRunUrl);

                    // create a urlconnection object
                    URLConnection urlConnection = url.openConnection();

                    // wrap the urlconnection in a bufferedreader
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try{
            thread.sleep(100);
        }catch (Exception e){};
        thread.interrupt();
    }

    public static String filterDate(String Str) {
        String filter = "[^0-9]"; // 指定要過濾的字元
        Pattern p = Pattern.compile(filter);
        Matcher m = p.matcher(Str);
        return m.replaceAll("").trim(); // 將非上列所設定的字元全部replace 掉
    }

    //  確認連線狀況
    public static boolean isConnected(Activity activity){
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    // 新增聯絡簿
    public void addContact(String Name, String Number)
    {
        ContentValues values = new ContentValues();
        // 首先向RawContacts.CONTENT_URI 執行一個空值插入(raw_contacts 表), 為了建立聯繫人 ID
        Uri rawContactUri = getContentResolver().insert( ContactsContract.RawContacts.CONTENT_URI, values);
        // 然後取得系統返回的rawContactId ， 就是新加入的這個聯繫人的 ID
        long rawContactId = ContentUris.parseId(rawContactUri);
        // 往data 表輸入姓名資料
        values.clear();
        // raw_contacts_id 欄位，是 raw_contacts 表格 id 的外部鍵，用於說明此記錄屬於哪一個聯繫人
        values.put(Data.RAW_CONTACT_ID, rawContactId);
        // Data.MIMETYPE 欄位，用於描述此資料的類型，電話號碼？Email？....

        //*********姓*********
        values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        values.put(StructuredName.FAMILY_NAME, Name);
        getContentResolver().insert(
                android.provider.ContactsContract.Data.CONTENT_URI, values);
        // 往data 表輸入電話資料
        values.clear();
        values.put(Data.RAW_CONTACT_ID, rawContactId);

        //*********號碼*********
        values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        values.put(Phone.NUMBER, Number);
        values.put(Phone.TYPE, Phone.TYPE_MOBILE);
        getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
    }
}
