package com.example.mov;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Himchan Song
 */

public class VoiceTTS extends Thread {

    public static final String TAG = "VoiceTTS";

    private Context mContext;

    private static final String SET_URL = "https://openapi.naver.com/v1/voice/tts.bin";
    private static final String clientID = "FwTO_BsdJUIv6yafZQkr";
    private static final String clientSecret = "dT5hiitdnP";
    private HttpURLConnection huc;

    private String text;
    private String lang;
    private String voiceActor;
    private int vSpeed;

    public VoiceTTS(Context mContext){
        this(mContext, null);
    }

    public VoiceTTS(Context mContext, String Text){
        this.mContext = mContext;
        setText(Text);
    }

    public void setText(String text){
        this.text = text;
    }

    public void setLang(String lang){
        this.lang = lang;
    }

    public void setSpeed(int vSpeed){
        this.vSpeed = (vSpeed-5)*(-1);
    }

    @Override
    public void run() {
        super.run();

        startTTS();
    }

    private void startTTS(){
        try {
            // 접속 및 설정
            URL url = new URL(SET_URL);
            huc = (HttpURLConnection)url.openConnection();
            huc.setRequestMethod("POST");
            huc.setRequestProperty("X-Naver-Client-Id", clientID);
            huc.setRequestProperty("X-Naver-Client-Secret", clientSecret);

            // 데이터 주기
            checkLang(lang);
            String postParams = "speaker=" + voiceActor + "&speed=" + vSpeed +"&text=" + URLEncoder.encode(text, "UTF-8");
            huc.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(huc.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();

            Log.e(TAG, huc.getResponseCode() + "");

            //MP3파일 만들기
            checkNetWork(huc.getResponseCode());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //결과 확인후 MP3파일로 만들기
    private void checkNetWork(int resultvalue){
        if (resultvalue == 200){
            try {
                InputStream is = huc.getInputStream();
                int read = 0;
                byte[] buf = new byte[1024];
                File f = new File(mContext.getExternalCacheDir(), "ttstemp.mp3");
                f.createNewFile();
                OutputStream os = new FileOutputStream(f);

                while ((read = is.read(buf)) != -1){
                    os.write(buf, 0, read);
                }

                os.close();
                is.close();

                MediaPlayer audioPlay = new MediaPlayer();
                audioPlay.setDataSource(f.getPath());
                audioPlay.prepare();
                audioPlay.start();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //성우 설정
    private void checkLang(String langCode) {
        switch(langCode) {
            case "ko":
                voiceActor = "mijin";
                break;

            case "en":
                voiceActor = "clara";
                break;

            case "zh-CN":
                voiceActor = "meimei";
                break;
        }
        return;
    }
}