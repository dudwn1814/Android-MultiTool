package com.example.listviewver;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, MediaStoreAdapter.OnClickThumbListener {

    TabHost tabHost;

    //phone
    ListView list;

    //Gallery
    private final static int READ_EXTERNAL_STORAGE_PERMMISSION_RESULT = 0;
    private final static int MEDIASTORE_LOADER_ID = 0;
    private RecyclerView mThumbnailRecyclerView;
    private MediaStoreAdapter mMediaStoreAdapter;

    //trans
    private EditText translationText;
    private Button translationButton;
    private TextView resultText;
    private String result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //tabHost 생성과 설정
        tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec tabSpecPhone = tabHost.newTabSpec("PHONE").setIndicator("", getResources().getDrawable(R.drawable.contact));
       tabSpecPhone.setContent(R.id.tabPop1);
        tabHost.addTab(tabSpecPhone);

        TabHost.TabSpec tabSpecGallery = tabHost.newTabSpec("GALLERY").setIndicator("", getResources().getDrawable(R.drawable.photo));
        tabSpecGallery.setContent(R.id.tabPop2);
        tabHost.addTab(tabSpecGallery);

        TabHost.TabSpec tabSpecMy = tabHost.newTabSpec("MY").setIndicator("", getResources().getDrawable(R.drawable.translate));
        tabSpecMy.setContent(R.id.tabPop3);
        tabHost.addTab(tabSpecMy);

        //맨 처음 보여지는 탭
        tabHost.setCurrentTab(0);


        //-----------------------------------Tab1----------------------------------------------
        list = (ListView) findViewById(R.id.listView1);

        //전화번호부 불러오기
        LoadContactsAyscn lca = new LoadContactsAyscn();
        lca.execute();

        //-----------------------------------Tab2----------------------------------------------
        mThumbnailRecyclerView = (RecyclerView) findViewById(R.id.thumbnailRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        mThumbnailRecyclerView.setLayoutManager(gridLayoutManager);
        mMediaStoreAdapter = new MediaStoreAdapter(this);
        mThumbnailRecyclerView.setAdapter(mMediaStoreAdapter);

        checkReadExternalStoragePermission();

        //-----------------------------------Tab3----------------------------------------------
        translationText = (EditText)findViewById(R.id.translationText);
        translationButton = (Button)findViewById(R.id.translationButton);
        resultText = (TextView)findViewById(R.id.resultText);

        //번역결과 스크롤 가능
        resultText.setMovementMethod(new ScrollingMovementMethod());

        //번역 버튼 클릭 -> 번역 시작
        translationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                new BackgroundTask().execute();
            }
        });
    }

    //주소록 함수
    class LoadContactsAyscn extends AsyncTask<Void, Void, ArrayList<Contact>>{
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = ProgressDialog.show(MainActivity.this, "Loading Contacts","Please Wait");
        }

        @Override
        protected ArrayList<Contact> doInBackground(Void... params) {
            ArrayList<Contact> contacts = new ArrayList<Contact>();
            Contact temp;

            Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, "upper("+ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");

            while (c.moveToNext()) {

                String contactName =  c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                temp = new Contact(contactName, phNumber);
                contacts.add(temp);
            }

            //contacts.sort(contactName);
            c.close();

            return contacts;
        }

        @Override
        protected void onPostExecute(final ArrayList<Contact> contacts) {
            super.onPostExecute(contacts);

            pd.cancel();


          ContactsAdapter adapter = new ContactsAdapter(getApplicationContext(), R.layout.text, contacts);

            list.setAdapter(adapter);
        }
    }

    //갤러리 함수
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case READ_EXTERNAL_STORAGE_PERMMISSION_RESULT:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSupportLoaderManager().initLoader(MEDIASTORE_LOADER_ID, null, this);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void checkReadExternalStoragePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Start cursor loader
                getSupportLoaderManager().initLoader(MEDIASTORE_LOADER_ID, null,  this);
            } else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "App needs to view thumbnails", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_PERMMISSION_RESULT);
            }
        } else {
            // Start cursor loader
            getSupportLoaderManager().initLoader(MEDIASTORE_LOADER_ID, null,  this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MEDIA_TYPE
        };
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        return new CursorLoader(
                this,
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMediaStoreAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMediaStoreAdapter.changeCursor(null);
    }

    @Override
    public void OnClickImage(Uri imageUri) {
        Intent fullScreenIntent = new Intent(this, FullScreenImageActivity.class);
        fullScreenIntent.setData(imageUri);
        startActivity(fullScreenIntent);
    }

    @Override
    public void OnClickVideo(Uri videoUri) {
        Intent videoPlayIntent = new Intent(this, VideoPlayActivity.class);
        videoPlayIntent.setData(videoUri);
        startActivity(videoPlayIntent);

    }

    //번역기 BackgroudTask 함수
    class BackgroundTask extends AsyncTask<Integer, Integer, Integer> {
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(Integer... integers) {

            StringBuilder output = new StringBuilder();
            String clientId = "0zo_SFJW5xDZcsN_Vu3g";//애플리케이션 클라이언트 아이디값";
            String clientSecret = "e2IOTKW4X4";//애플리케이션 클라이언트 시크릿값";
            try {
                String text = URLEncoder.encode(translationText.getText().toString(), "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/papago/n2mt";

                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

                // post request
                String postParams = "source=ko&target=en&text=" + text;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    output.append(inputLine);
                }
                br.close();
            } catch (Exception e) {
                Log.e("SampleHTTP", "Exception in processing response.", e);
                e.printStackTrace();
            }
            result = output.toString();
            return null;
        }

        protected void onPostExecute(Integer a){
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);
            if(element.getAsJsonObject().get("errorMessage") != null){
                Log.e("번역 오류", "번역 오류가 발생했습니다. " + "[오류 코드: " +
                        element.getAsJsonObject().get("errorCode").getAsString() + "]");
            }else if(element.getAsJsonObject().get("message") != null){
                resultText.setText(element.getAsJsonObject().get("message").getAsJsonObject().get("result").getAsJsonObject().
                        get("translatedText").getAsString());
            }
        }
    }
}