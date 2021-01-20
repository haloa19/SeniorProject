package com.dteviot.epubviewer;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by namju on 2017-03-24.
 */

abstract public class XMLParser {
    private String mAddr = "";

    public XMLParser(String addr) {
        mAddr = addr;
    }

    // 메소드 : URL을 이용해서 XmlPullParser객체를 반환
    XmlPullParser parser;
    public XmlPullParser getXMLParser(String type) {
        try {
            URL targetURL = new URL(mAddr);
            InputStream is = targetURL.openStream();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            parser = factory.newPullParser();

            parser.setInput(is, type);
            return parser;
        } catch (Exception e) {
            Log.d("XMLParser", e.getMessage());
            return parser; // null
        }
    }
    abstract void startParsing();
}



/*
getXMLParser메소드에서 반환되는 XmlPullParser를 가지고,
Tag와 데이터를 가지고 올 것이다.

getXMLParser메소드의 매개변수(String "type")는 XML을 가지고 올 때,
어떤 형식으로 가지고 올 것인지를 설정해주는 것이다.
대부분 'utf-8'이지만, 만약 깨진다면 euc-kr로 변경하자.
 */