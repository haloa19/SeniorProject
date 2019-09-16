package com.dteviot.epubviewer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * Created by hyejin on 2017-04-01.
 */

public class MainActivity_first extends Activity implements TextToSpeech.OnInitListener {
    Button startBtn;
    RelativeLayout rLayout;
    private TextToSpeech mTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        startBtn = (Button) findViewById(R.id.startBtn);
        rLayout = (RelativeLayout)findViewById(R.id.activity_main1);
        mTTS = new TextToSpeech(getApplicationContext(), this);

        /*버튼 클릭시 Main2Activity로 이동*/
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test","one");
                Intent intent = new Intent(MainActivity_first.this, Main2Activity.class);
                startActivity(intent);
            }
        });

        /*화면 아무곳이나 클릭시 Main2Activity로 이동*/
        rLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity_first.this, Main2Activity.class);
                startActivity(intent);
            }
        });
    }

    /*처음 화면이 뜨면 다음의 글이 음성 지원 됨*/
    public void onInit(int i){
        mTTS.speak("전자책 목록으로 이동하시려면 화면의 아무곳이나 터치해주세요", TextToSpeech.QUEUE_FLUSH, null);
    }
}
