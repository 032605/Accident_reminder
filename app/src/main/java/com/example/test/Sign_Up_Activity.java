package com.example.test;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.util.regex.Pattern;

public class Sign_Up_Activity extends AppCompatActivity {
    //Member Variable 선언-------------------------------------------
    private boolean D = true;

    //View Object----------------------------------------------------
    private EditText et_pgh_email;
    private EditText et_pgh_password;
    private EditText et_pgh_signUpwrp;
    private Button bt_pgh_createAccountBTN;
    private TextView tv_pgh_moveMainActivityTXT;
    private InputMethodManager im_pgh_imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        init();
    }

    // UI 초기화 Method------------------------------------------------
    public void init() {

        im_pgh_imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
        et_pgh_email = findViewById(R.id.et_pgh_signUpemail);
        et_pgh_password = findViewById(R.id.et_pgh_signUpw);
        et_pgh_signUpwrp = findViewById(R.id.et_pgh_signUpwrp);
        bt_pgh_createAccountBTN = findViewById(R.id.bt_pgh_createAccountBTN);
        tv_pgh_moveMainActivityTXT = findViewById(R.id.tv_pgh_LoginMoveTXT);
        et_pgh_password.setFilters(new InputFilter[]{filter_pw,new InputFilter.LengthFilter(20)});
        et_pgh_email.setFilters(new InputFilter[]{filter_mail,new InputFilter.LengthFilter(20)});
        et_pgh_signUpwrp.setFilters(new InputFilter[]{filter_pw,new InputFilter.LengthFilter(20)});
        bt_pgh_createAccountBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
        tv_pgh_moveMainActivityTXT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    //정규식 적용
    InputFilter filter_mail = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[a-zA-Z0-9@.]+$");
            if(!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    };
    InputFilter filter_pw = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[a-zA-Z0-9ㄱ-ㅎ가-힣`~!@#$%^&*()-_+=]+$");
            if(!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    };
    // createAccount Button Click Event
    public void createAccount() {

        //아이디 비밀번호 확인
        if (et_pgh_email.getText().length() <= 0) {
            Toast.makeText(this, "email을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            im_pgh_imm.hideSoftInputFromWindow(et_pgh_email.getWindowToken(), 0);
            return;
        }
        if (et_pgh_password.getText().length() <= 0) {
            Toast.makeText(this, "password를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            im_pgh_imm.hideSoftInputFromWindow(et_pgh_email.getWindowToken(), 0);
            return;
        }
        Log.d("test",et_pgh_password.getText().toString());
        Log.d("test",et_pgh_signUpwrp.getText().toString());
        if(et_pgh_password.getText().toString().equals(et_pgh_signUpwrp.getText().toString()))
        {
        im_pgh_imm.hideSoftInputFromWindow(et_pgh_email.getWindowToken(), 0);
        //firebase sign_up Check process
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(et_pgh_email.getText().toString(), et_pgh_password.getText().toString())
                .addOnCompleteListener(Sign_Up_Activity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    //이메일 인증
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        //이메일 형식이 안맞는 경우
                        if (!task.isSuccessful()) {
                            //Email fail
                            try {
                                throw task.getException();
                            }
                            catch (FirebaseAuthWeakPasswordException e) {
                                Toast.makeText(getApplicationContext(), "비밀번호는 최소 6자에서 최대 20자입니다.", Toast.LENGTH_LONG).show();
                            }
                            catch (FirebaseAuthInvalidCredentialsException e) {
                                Toast.makeText(getApplicationContext(), "이메일 형식이 맞지않습니다.", Toast.LENGTH_LONG).show();
                            }
                            catch (FirebaseAuthUserCollisionException e)
                            {
                                Toast.makeText(getApplicationContext(), "이미 존재하는 이메일입니다.", Toast.LENGTH_LONG).show();
                            }
                           catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "잘 못 된 정보입니다.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            //로그인 성공할 경우
                            Toast.makeText(Sign_Up_Activity.this, "회원가입 해주셔서 감사합니다.", Toast.LENGTH_SHORT).show();
                            //DB 넣어줌
                            finish();
                        }
                    }
                });
        }
        else
        {
            Toast.makeText(Sign_Up_Activity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}