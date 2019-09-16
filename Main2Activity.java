package com.dteviot.epubviewer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hyejin on 2017-04-01.
 */

public class Main2Activity extends Activity implements TextToSpeech.OnInitListener{
    private final static int LIST_EPUB_ACTIVITY_ID = 0;

    long mStartTime, mEndTime;
    EpubXMLParser mXMLParser;

    private TextToSpeech mTTS;
    static List<File> epubs;
    ArrayAdapter<String> adapter;
    ArrayList<String> list;
    static File selected;
    ListView listview;
    private DoubleTap doubleTap;
    int position_n = 3;

    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mTTS = new TextToSpeech(getApplicationContext(), this);

        doubleTap = (DoubleTap) findViewById(R.id.booklist);
        listview = (ListView)findViewById(R.id.booklist);
        list = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);

        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            static final int REQUEST_CODE = 1111;
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String sld = list.get(position);
                position_n = position;
                mTTS.speak(sld,TextToSpeech.QUEUE_FLUSH, null);

                Intent intent = new Intent(Main2Activity.this, MainActivity.class);
                intent.putExtra("BOOKNAME", list.get(position_n));

                startActivityForResult(intent,REQUEST_CODE);
            }
        });

        mStartTime = System.currentTimeMillis();
        mXMLParser = new EpubXMLParser("http://computer.kevincrack.com/epub_download.jsp", mHandler);
        thread = new Thread(mXMLParser);
        thread.start();
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mEndTime = System.currentTimeMillis();
            Log.d("Taken Time", Long.toString((mEndTime - mStartTime) / 1000L));

            ArrayList<EpubDatas> dataList = mXMLParser.getResult();

            int dataListSize = dataList.size();

            Log.d("Data List Size", Integer.toString(dataListSize));
            for (int i=0; i<dataListSize; i++) {
                //서버에서 불러온 전자책 제목을 리스트로 보여줌
                Log.d("XML Parsing Result", dataList.get(i).getEpub());
                list.add(dataList.get(i).getEpub());
            }
            adapter.notifyDataSetChanged();
            thread.interrupt();
        }
    };

    public void onInit(int i){
        mTTS.speak("전자책 목록 화면입니다. 원하는 전자책을 선택해주세요.", TextToSpeech.QUEUE_FLUSH, null);
    }

}
