package com.dteviot.epubviewer;

import android.app.Activity;
import android.content.Intent;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import nl.siegmann.epublib.epub.Main;

public class Main3Activity extends Activity implements TextToSpeech.OnInitListener{
    public static final String FILENAME_EXTRA = "FILENAME_EXTRA";
    public static final String PAGE_EXTRA = "PAGE_EXTRA";


    static List<File> epubs;
    static List<String> names;
    ArrayAdapter<String> adapter;
    ArrayList<String> list1;
    Button refreshbtn;
    ListView listview;

    String File_Name = "";
    String File_extend = "";
    String Save_Path;
    String Save_folder = "/Epub";
    String bookName;
    private String mRootPath;

    private TextToSpeech mTTS;
    int position_n = 3;
    private DoubleTap doubleTap;

    private long btnPressTime = 0;

    private String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    //private String dataPath = SDPath + "/instinctcoder/zipunzip/data/" ;
    private String zipPath = SDPath + "/Download/" ;
    private String unzipPath = SDPath + "/Download/";

    final static String TAG = MainActivity.class.getName();

    //DownloadThread dThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        doubleTap = (DoubleTap) findViewById(R.id.booklist);
        listview = (ListView)findViewById(R.id.booklist);

        list1 = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list1);

        mTTS = new TextToSpeech(getApplicationContext(), this);

        Intent intent_book = getIntent();

        bookName = intent_book.getExtras().getString("BOOKNAME"); // 2Activity에서 책이름을 받아옴


        String encodeResult = null;
        try {
            encodeResult = URLEncoder.encode(bookName, "utf-8");
         } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
         }
        Intent i = new Intent(Intent.ACTION_VIEW); // Server
        Intent j = new Intent(Intent.ACTION_VIEW);
        Uri u = Uri.parse("http://computer.kevincrack.com/download.jsp?name=" + encodeResult);          //zip
        Uri u2 = Uri.parse("http://computer.kevincrack.com/download2.jsp?name=" + encodeResult);    //epub

        j.setData(u2);
        i.setData(u);

        startActivity(j);
        startActivity(i);

        /*다운로드 경로를 */
        String ext = Environment.getExternalStorageState();
        if(ext.equals(Environment.MEDIA_MOUNTED)){
            Save_Path = Environment.getExternalStorageDirectory().getAbsolutePath() + Save_folder;
        }

        if ((epubs == null) || (epubs.size() == 0)) {
            epubs = epubList(Environment.getExternalStorageDirectory());
        }


        final ListView list = (ListView)findViewById(R.id.booklist);
        names = fileNames(epubs);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (FileHelper.unzip(zipPath + bookName ,unzipPath)) {
                    Log.d("hhh","gagagaga");
                    Toast.makeText(Main3Activity.this,"Unzip successfully.",Toast.LENGTH_LONG).show();
                }

                String fileName = titleToFileName(((TextView)view).getText().toString());

                String str = fileName.substring(fileName.lastIndexOf("/")+1, fileName.indexOf("."));
                System.out.println(str);
                Gloval.setB_name(str);

                StringTokenizer tokens = new StringTokenizer(fileName, "/");

                String[] array = new String[tokens.countTokens()];
                int i=0;
                while(tokens.hasMoreElements()){
                    array[i++] = tokens.nextToken();
                }

                for(i=0; i<array.length; i++){
                    String a = array[i];
                    System.out.println(array[i]);
                }

                Intent intent = new Intent();
                intent.putExtra(FILENAME_EXTRA, fileName);

                // set page to first, because ListChaptersActivity returns page to start at
                intent.putExtra(PAGE_EXTRA, 0);
                setResult(RESULT_OK, intent);
                finish();

                Log.d("here","here4"+fileName);
            }
        });
        list.setAdapter(adapter);


/*
        doubleTap.setOnDoubleClickListener(new DoubleTap.OnDoubleClickListener() {

            @Override
            public void onDoubleClick(View view) {

                String fileName = titleToFileName(((TextView)view).getText().toString());
                System.out.println("실험1" + fileName);
                Log.d("here","here3");

                Intent intent = new Intent();
                intent.putExtra(FILENAME_EXTRA, fileName);

                intent.putExtra(PAGE_EXTRA, 0);
                setResult(RESULT_OK, intent);
                System.out.println("화면의 아무곳이나 터치해");
                finish();
                System.out.println("화면의 아무곳이나 터치해3");
                Log.d("here","here4"+fileName);

            }
        });*/

        refreshbtn = (Button)findViewById(R.id.refreshlist);
        refreshbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    mTTS.speak("책 목록 갱신", TextToSpeech.QUEUE_FLUSH, null);
                    refreshList();

            }
        });


    }


    private String titleToFileName(String title) {
        File path = Environment
                .getExternalStorageDirectory();
        mRootPath = path.toString();

        return mRootPath + "/Download/" + title+".epub";


    }


    private List<String> fileNames(List<File> files){
        List<String> res = new ArrayList<String>();
        for (int i = 0; i<files.size(); i++){
            res.add(files.get(i).getName().replace(".epub",""));
        }
        return res;
    }

    private List<File> epubList(File dir) {
        List<File> res = new ArrayList<File>();

        if(dir.isDirectory()){
            File[] f = dir.listFiles();
            if (f != null) {
                for (int i = 0; i < f.length; i++) {
                    if (f[i].isDirectory()) {
                        res.addAll(epubList(f[i]));
                    } else {

                        String aa =  f[i].toString();

                        if ( aa. startsWith("/storage/emulated/0/Download/")) {
                            if (aa.endsWith(".epub")) {
                                res.add(f[i]);
                            }
                        }
                    }
                }
            }
        }
        return res;
    }

    private void refreshList(){
        epubs = epubList(Environment.getExternalStorageDirectory());
        names.clear();
        names.addAll(fileNames(epubs));
        this.adapter.notifyDataSetChanged();
    }

    public void onInit(int i){
        mTTS.speak("다운로드된 전자책 목록 화면입니다. " +
                "오른쪽 상단의 책목록 갱신 버튼을 누르면 다운로드한 전자책이 추가됩니다.", TextToSpeech.QUEUE_FLUSH, null);
    }

}