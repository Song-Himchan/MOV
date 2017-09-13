package com.example.mov;

/**
 * Created by Himchan Song
 */

import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btTranslate;
    EditText etSource;
    TextView tvResult;
    Spinner langSpinner;
    TextView btMenu;
    ArrayList<String> spinnerList = new ArrayList<String>();

    String sourceLang = "ko";
    String targetLang = "";

    int voiceSpeed = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etSource = (EditText) findViewById(R.id.et_source);
        tvResult = (TextView) findViewById(R.id.tv_result);
        btTranslate = (Button) findViewById(R.id.bt_translate);
        langSpinner = (Spinner) findViewById(R.id.lang_spinner);
        btMenu = (TextView) findViewById(R.id.bt_menu);

        btMenu.setPaintFlags(btMenu.getPaintFlags()| Paint.FAKE_BOLD_TEXT_FLAG);

        spinnerList.add("한국어");
        spinnerList.add("영어");
        spinnerList.add("중국어");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, spinnerList);
        langSpinner.setAdapter(adapter);

        //실행버튼 클릭이벤트
        btTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //소스에 입력된 내용이 있는지 체크
                if(etSource.getText().toString().length() == 0) {
                    Toast.makeText(MainActivity.this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                    etSource.requestFocus();
                    return;
                }

                if(sourceLang == targetLang) { //한국어-한국어인 경우 번역 과정을 거치지 않고 바로 음성 합성
                    tvResult.setText(etSource.getText());

                    VoiceTTS voiceTTS = new VoiceTTS(getBaseContext());
                    voiceTTS.setText(tvResult.getText().toString().trim());
                    voiceTTS.setLang(targetLang);
                    voiceTTS.setSpeed(voiceSpeed);
                    voiceTTS.start();
                }
                else { //다른 언어간 번역인 경우 번역과정을 거침
                    NaverTranslateTask asyncTask = new NaverTranslateTask();
                    String sText = etSource.getText().toString();
                    asyncTask.execute(sText);
                }
            }
        });

        langSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(spinnerList.get(position)) {
                    case "한국어":
                        targetLang = "ko";
                        break;

                    case "영어":
                        targetLang = "en";
                        break;

                    case "중국어":
                        targetLang = "zh-CN";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){

            }
        });

        btMenu.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Intent popIntent = new Intent(MainActivity.this, Pop.class);
                popIntent.putExtra("speed", voiceSpeed);
                startActivityForResult(popIntent, 0);
            }
        });
    }

    //ASYNCTASK
    public class NaverTranslateTask extends AsyncTask<String, Void, String> {
        String clientId = "FwTO_BsdJUIv6yafZQkr";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "dT5hiitdnP";//애플리케이션 클라이언트 시크릿값";


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        //AsyncTask 메인처리
        @Override
        protected String doInBackground(String... strings) {
            String sourceText = strings[0];

            try {
                String text = URLEncoder.encode(sourceText, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                // post request
                String postParams = "source="+sourceLang+"&target="+targetLang+"&text=" + text;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if(responseCode==200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                return response.toString();

            } catch (Exception e) {
                Log.d("error", e.getMessage());
                return null;
            }
        }

        //번역된 결과를 받아서 처리
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //최종 결과 처리부
            //네이버에 보내주는 응답결과가 JSON 데이터임
            //JSON데이터를 자바객체로 변환해야 함
            //Gson을 사용

            Gson gson = new GsonBuilder().create();
            JsonParser parser = new JsonParser();
            JsonElement rootObj = parser.parse(s.toString())
                    //원하는 데이터 까지 찾아 들어감
                    .getAsJsonObject().get("message")
                    .getAsJsonObject().get("result");
            //안드로이드 객체에 담기
            TranslatedItem items = gson.fromJson(rootObj.toString(), TranslatedItem.class);
            //Log.d("result", items.getTranslatedText());
            //번역결과를 텍스트뷰에 넣음
            tvResult.setText(items.getTranslatedText());

            VoiceTTS voiceTTS = new VoiceTTS(getBaseContext());
            voiceTTS.setText(tvResult.getText().toString().trim());
            voiceTTS.setLang(targetLang);
            voiceTTS.start();
        }

        private class TranslatedItem {
            String translatedText;
            public String getTranslatedText() {
                return translatedText;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK) { voiceSpeed = data.getIntExtra("speed", 0); }
        }
    }
}