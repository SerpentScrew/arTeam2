package com.example.arteam2.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.arteam2.R;


import java.util.ArrayList;
import java.util.TreeMap;


public class SettingActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    ListView lv;
    ArrayAdapter<String> myAdapter;
    TreeMap<String,float[]> standard = new TreeMap<String, float[]>();
    ArrayList<String> items = new ArrayList<String>();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_settings);

        //세상에 이게 뭐야
        float[] temp = new float[3];
        temp[0] = 55.0f; temp[1] = 40.0f; temp[2] = 20.0f;
        standard.put("아시아나항공",temp);
        standard.put("대한한공",temp);

        temp = new float[3];
        temp[0] = 56.0f; temp[1] = 45.0f; temp[2] = 25.0f;
        standard.put("IATA 가이드 라인",temp);
        standard.put("영국항공",temp);
        standard.put("이지제트",temp);

        temp = new float[3];
        temp[0] = 56.0f; temp[1] = 45.0f; temp[2] = 25.0f;
        standard.put("라이언에어",temp);
        standard.put("토마스쿡",temp);

        temp = new float[3];
        temp[0] = 56.0f; temp[1] = 36.0f; temp[2] = 23.0f;
        standard.put("버진아틀란틱인",temp);
        standard.put("아메리칸항공",temp);

        temp = new float[3];
        temp[0] = 56.0f; temp[1] = 40.0f; temp[2] = 23.0f;
        standard.put("루프트한자",temp);

        temp = new float[3];
        temp[0] = 56.0f; temp[1] = 35.0f; temp[2] = 22.0f;
        standard.put("유나이티드항공",temp);

        temp = new float[3];
        temp[0] = 56.0f; temp[1] = 35.0f; temp[2] = 23.0f;
        standard.put("델타항공",temp);

        temp = new float[3];
        temp[0] = 55.0f; temp[1] = 35.0f; temp[2] = 25.0f;
        standard.put("에어프랑스",temp);

        temp = new float[3];
        temp[0] = 55.0f; temp[1] = 38.0f; temp[2] = 20.0f;
        standard.put("에미레이트",temp);

        temp = new float[3];
        temp[0] = 42.0f; temp[1] = 32.0f; temp[2] = 25.0f;
        standard.put("위즈에어",temp);

        temp = new float[3];
        temp[0] = 55.0f; temp[1] = 40.0f; temp[2] = 23.0f;
        standard.put("저먼윙스",temp);
        standard.put("플라이비",temp);

        temp = new float[3];
        temp[0] = 55.8f; temp[1] = 36.8f; temp[2] = 22.9f;
        standard.put("미연방 기준",temp);





        for(String text : standard.keySet())
        {
            //text = text.concat(standard.get(text)[0] + " x " + standard.get(text)[1] + " x " + standard.get(text)[2] );
            //왜 팅기냐?
            items.add(text);
        }

        myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,items);
        lv = (ListView)findViewById(R.id.lv);
        lv.setAdapter(myAdapter);
        lv.setOnItemClickListener(this);


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.putExtra("airline",items.get(position));
        intent.putExtra("size",standard.get(items.get(position)));
        setResult(RESULT_OK,intent);
        finish();
    }
}
