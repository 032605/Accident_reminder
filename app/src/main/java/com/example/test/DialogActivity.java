package com.example.test;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.naver.maps.geometry.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DialogActivity<queryString> extends AppCompatActivity {


    private final boolean D = true;
    private final String TAG = "DialogActivity";

    public Dialog dialog, etcDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sub_layout);
    }

    private void insert_DB(String comment) {

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String cur_time = sdfNow.format(date);

        if(MainActivity.Tickey.size() > 2)
        {
            if(MainActivity.Tickey.get(0).getTime() - date.getTime() < 3600*1000)
            {
                MainActivity.ShowToast();
                Toast.makeText(getApplicationContext(),"신고를 너무 많이 하셨습니다.",Toast.LENGTH_SHORT).show();
                return;
            }
            else
            {
                MainActivity.Tickey.add(date);
                MainActivity.Tickey.remove(0);
            }
        }

        FirebaseDatabase.getInstance().getReference("Alert").push().setValue(
                new AlertDTO(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        MainActivity.Click_coord.latitude,
                        MainActivity.Click_coord.longitude,
                        cur_time,
                        comment
                )
        );
    }

    private Dialog makeDialog(String title, String type, boolean etc) {
        dialog = new Dialog(DialogActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RadioButton rbtn_knh_op1 = dialog.findViewById(R.id.rbtn_knh_op1);
        rbtn_knh_op1.setText(title);
        rbtn_knh_op1.setEnabled(true);

        RadioButton rbtn_knh_op2 = dialog.findViewById(R.id.rbtn_knh_op2);
        EditText et_knh_custominfo = dialog.findViewById(R.id.et_knh_custominfo);
        et_knh_custominfo.setEnabled(false);

        Button btn_knh_Confirm = dialog.findViewById(R.id.btn_knh_Confirm);
        Button btn_knh_Cancle = dialog.findViewById(R.id.btn_knh_Cancle);

        // radio button 속성 --------------------------------------------------------------

        rbtn_knh_op1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (rbtn_knh_op1.isChecked()) {

                    et_knh_custominfo.setEnabled(false);  // editText 비활성화
                    rbtn_knh_op1.setTextColor(Color.BLACK);
                    rbtn_knh_op2.setTextColor(Color.GRAY);
                }
            }
        });

        rbtn_knh_op2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (rbtn_knh_op2.isChecked()) {
                    et_knh_custominfo.setEnabled(true);  // editText 활성화

                    rbtn_knh_op2.setTextColor(Color.BLACK); // 색상
                    rbtn_knh_op1.setTextColor(Color.GRAY);
                }
            }

        });
        // -------------------------- radio button 속성 끝 -------------------------------

        // dialog 버튼


        btn_knh_Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date temp_date = new Date(System.currentTimeMillis());
                if (rbtn_knh_op1.isChecked()) {
                    dialog.dismiss();
                    if(MainActivity.compareDB.size() < 1)
                    {
                        Toast.makeText(getApplicationContext(),"신고해주셔서 감사합니다.",Toast.LENGTH_SHORT).show();
                        insert_DB(type);
                        finish();
                        return;
                    }
                    for(int i =0;i<MainActivity.compareDB.size();i++)
                    {
                        if(MainActivity.Cal_distance(
                                new LatLng(MainActivity.compareDB.get(i).getLatitude(),MainActivity.compareDB.get(i).getLongitude())
                                ,MainActivity.Click_coord) < 100)
                        {
                            Log.d("test","시간비교 : " + (temp_date.getTime() - MainActivity.StrToDate(MainActivity.compareDB.get(i).getTime()).getTime()));
                            if(temp_date.getTime() - MainActivity.StrToDate(MainActivity.compareDB.get(i).getTime()).getTime() <3600*1000)
                            {
                                Toast.makeText(getApplicationContext(),"신고해주셔서 감사합니다. 이미 접수된 사고입니다.",Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }
                        }
                    }
                    insert_DB(type);
                    finish();
                    Toast.makeText(getApplicationContext(),"신고해주셔서 감사합니다.",Toast.LENGTH_SHORT).show();
                } else if (rbtn_knh_op2.isChecked()) {

                    if (et_knh_custominfo.length() > 0 ) {
                        dialog.dismiss();
                        if (MainActivity.compareDB.size() < 1) {
                            Toast.makeText(getApplicationContext(), "신고해주셔서 감사합니다.", Toast.LENGTH_SHORT).show();
                            insert_DB(type);
                            finish();
                            return;
                        }
                        for (int i = 0; i < MainActivity.compareDB.size(); i++) {
                            if (MainActivity.Cal_distance(
                                    new LatLng(MainActivity.compareDB.get(i).getLatitude(), MainActivity.compareDB.get(i).getLongitude())
                                    , MainActivity.Click_coord) < 100) {

                                if (temp_date.getTime() - MainActivity.StrToDate(MainActivity.compareDB.get(i).getTime()).getTime() < 3600 * 1000) {
                                    Toast.makeText(getApplicationContext(), "신고해주셔서 감사합니다. 이미 접수된 사고입니다.", Toast.LENGTH_SHORT).show();
                                    finish();
                                    return;
                                }
                            }
                        }
                        insert_DB(et_knh_custominfo.getText().toString());
                        finish();
                        Toast.makeText(getApplicationContext(), "신고해주셔서 감사합니다.", Toast.LENGTH_SHORT).show();
                    } else Toast.makeText(DialogActivity.this, "값을 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(DialogActivity.this, "값을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        btn_knh_Cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DialogActivity.this, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        return dialog;
    }

    private Dialog makeDiaglog_etc() {
        etcDialog = new Dialog(DialogActivity.this);
        etcDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        etcDialog.setContentView(R.layout.etc_layout);

        etcDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText et_knh_etcinfo = etcDialog.findViewById(R.id.et_knh_etcinfo);

        Button btn_knh_Confirm = etcDialog.findViewById(R.id.btn_knh_Confirm);
        Button btn_knh_Cancle = etcDialog.findViewById(R.id.btn_knh_Cancle);


        btn_knh_Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                double x = MainActivity.Click_coord.latitude;
                double y = MainActivity.Click_coord.longitude;

                if (et_knh_etcinfo.length() > 0 ) {

                    String info = et_knh_etcinfo.getText().toString();

                    etcDialog.dismiss();

                    insert_DB(et_knh_etcinfo.getText().toString());
                    finish();
                }
                else {
                    Toast.makeText(DialogActivity.this, "값을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_knh_Cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DialogActivity.this, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                etcDialog.dismiss();
            }
        });

        return etcDialog;
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_knh_accbtn1:
                makeDialog("인근에서 교통사고 발생", "교통사고", true).show();
                break;

            case R.id.img_knh_accbtn2:
                makeDialog("인근에서 화재 발생", "화재사고", true).show();
                break;

            case R.id.img_knh_accbtn3:
                makeDialog("인근에서 공사 중", "공사 중", true).show();
                break;

            case R.id.btn_knh_accbtn4:
                makeDiaglog_etc().show();
                break;
        }
    }

    //-------------------------------------

    public void backClick(View V) {
        finish();
    }
}
