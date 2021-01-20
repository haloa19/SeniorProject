package com.dteviot.epubviewer;

import android.os.Handler;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;

/**
 * Created by namju on 2017-03-24.
 */

public class EpubXMLParser extends XMLParser implements Runnable {
    private ArrayList<EpubDatas> mDataList;
    private Handler mHandler;

    // 생성자에 handler가 필요한 이유: thread를 사용했기 때문에 파싱이 끝난 후에 Activity에 끝났다고 전달하기 위함
    public EpubXMLParser(String addr, Handler handler) {
        super(addr);
        mHandler = handler;
    }

    // 메소드
    public void startParsing() {
        XmlPullParser parser = getXMLParser("utf-8"); // 깨지면 euc-kr // 부모클래스의 getXMLParser을 이용해서 XmlPullParser객체를 받아옴

        if (parser == null) {
            mDataList = null;
            Log.d("EpubXMLParser", "Parser Object is null");
        }
        else {
            mDataList = new ArrayList<EpubDatas>();
            // mDataList.add(new EpubDatas(Parcel in));
            // mDataList.add(new EpubDatas("1I'm epub"));
            // mDataList.add(new EpubDatas("2I'm epub"));
            // mDataList.add(new EpubDatas("3I'm epub"));

            String epubCode = "";
            String tag = "";

            try {
                // getEventType메소드를 사용해서 현재 사건인지를 조사
                // get하는 정보는 START_DOCUMENT, END_DOCUMENT, START_TAG, TEXT 중 하나
                int parserEvent = parser.getEventType();
                int tagIdentifier = 0; // 각 태그를 식별하고 해당 태그의 데이터를 가져오기 위한 변수

                while(parserEvent != XmlPullParser.END_DOCUMENT) {
                    switch (parserEvent) {
                        case XmlPullParser.START_DOCUMENT:    // case1. XmlPullParser.START_DOCUMENT
                            break;
                        case XmlPullParser.END_DOCUMENT:      // case2. XmlPullParser.END_DOCUMENT
                            break;
                        case XmlPullParser.START_TAG:
                            tag = parser.getName(); // getName()메소드: 태그를 가져옴
                            // tag.equals("태그이름")
                            if (tag.equals("storename")) {
                                tagIdentifier = 1;
                            }
                            else if (tag.equals("rank")) {
                                break;
                            }
                            break;
                        case XmlPullParser.END_TAG:             // case3. XmlPullParser.END_TAG
                            break;
                        case XmlPullParser.TEXT:                // case4. XmlPullParser.TEXT
                            if (tagIdentifier == 1) {
                                epubCode = parser.getText().trim(); // getText()메소드: 데이터를 가져옴
                                EpubDatas data = new EpubDatas(epubCode);
                                mDataList.add(data);
                            }
                            else {
                                // 예외처리 !!!
                            }
                            tagIdentifier = 0;
                            break;
                    }
                    parserEvent = parser.next(); // next()메소드: 다음 사건을 조사하면서 문서를 처음부터 순회
                }
            } catch (Exception e) {
                Log.d("EpubXMLParser", e.getMessage());
            }
        }
    }

    // 2. getResult()메소드 실행 시, mDataList값이 없음....
    public ArrayList<EpubDatas> getResult(){
        return mDataList;
    }
    public void run() {
        startParsing();
        mHandler.sendEmptyMessage(0); // parsing이 끝나면 handler에게 empty message를 보냄
    }
}




/*
이 클래스는 XMLParser에서 가져온 정보를 이용해서,
원하는 값을 EpubDatas객체에 저장해서 리스트를 만든다.

생성자로는 handler를 받는데,
thread를 사용했기 때문에 파싱이 끝난 후에 Activity에 끝났다고 전달하기 위함

부모 클래스 getXMLParser("utf-8")을 이용해서,
XmlPullParser객체를 받아오고 있다.

getEventType()를 사용해서 "현재 사건인지"를 조사
여기서 얻은 정보는
 - START_DOCUMENT (문서 처음)
 - END_DOCUMENT (문서 끝)
 - START_TAG (태그의 시작(<tag>), END_TAG(</tag>),
 - TEXT (데이터)
 중 하나임!

tagIdentifier는 각 태그를 식별하고 해당 태그의 데이터를 가져오기 위한 변수
next메소드를 사용해서 다음 사건을 조사하면서 문서를 처음부터 순회
getName메소드는 태그를 가져옴
getText메소드는 데이터를 가져옴
parsing이 끝나면 handler에게 empty message를 보냄
*/
