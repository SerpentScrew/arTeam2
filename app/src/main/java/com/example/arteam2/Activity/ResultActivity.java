package com.example.arteam2.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.example.arteam2.Activity.MainActivity;

import com.example.arteam2.R;
import com.example.arteam2.Utility.PointHandler;

import java.util.Arrays;

public class ResultActivity extends Activity {


    TextView standard;
    TextView result;
    TextView pass;
    MainActivity mainActivity = new MainActivity();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_result);

        //UI 객체생성
        standard = (TextView)findViewById(R.id.standard);
        result = (TextView)findViewById(R.id.result);
        pass = (TextView)findViewById(R.id.pass);

        //데이터 가져오기
        Intent intent = getIntent();
        float[] data;
        data = intent.getFloatArrayExtra("size");

        Arrays.sort(data);
        //data[2] == height
        //data[1] == width;
        //data[0] == length

        result.setText("측정 결과 : " + (int)data[2] + " x " + (int)data[1] + " x " + (int)data[0] + " cm");
        standard.setText("기준 : " + (int)mainActivity.getSize()[0] + " x " + (int)mainActivity.getSize()[1] + " x " + (int)mainActivity.getSize()[2] + " cm");

        if(data[2] < mainActivity.getSize()[0] && data[1] < mainActivity.getSize()[1] && data[0] < mainActivity.getSize()[2]) {
            pass.setText("통과!");
        }
        else
        {
            pass.setText("너무 커요!");
        }

    }

    //확인 버튼 클릭
    public void mOnClose(View v){
        /*//데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", "Close Popup");
        setResult(RESULT_OK, intent);*/

        //액티비티(팝업) 닫기
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}
