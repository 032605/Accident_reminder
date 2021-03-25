package com.example.test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.core.graphics.ColorUtils;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationSource;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.MarkerIcons;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private boolean Data_init = false;
    private static boolean isMarker = false;
    private boolean isFollow = true;
    private boolean readyMap = false;
    private boolean DBLock = false;
    private boolean id_change = false;
    public static Context context;
    private ValueEventListener eventListener = null;
    public static ArrayList<AlertDTO> alertDTOS = new ArrayList<AlertDTO>(); //알림 DB 저장 하는 어레이
    public static ArrayList<AlertDTO> compareDB = new ArrayList<AlertDTO>(); // 컴페어 DB 메모리에 올라가있는 db 같은 역할 검색을 위해 마지막으로 찍었던 위치보다 200m 멀어지면 기존에 있던 파이어베이스를 지우고
    // 다시 생성
    public static ArrayList<Marker> al_marker = new ArrayList<Marker>(); //마커 가지고 있는 배열
    public static ArrayList<Date> Tickey = new ArrayList<Date>(); //티켓 개개인의 티켓 최대 길이 3 시간을 체크해서 가장 빠른 사람을 체크해서 데이터베이스를 덮어 씀으로써 글을 씀

    private static final int LOCATION_REQUEST_INTERVAL = 300;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static NaverMap map = null;
    public static LatLng Last_coord;
    public static LatLng Click_coord = new LatLng(37.5666102, 126.9783881);
    public static LatLng DB_coord;

    //Member Variable 선언-------------------------------------------
    private boolean D = true;
    // private String fcnToken;
    private boolean locationEnabled;
    //View Object----------------------------------------------------
    private EditText et_pgh_email;
    private EditText et_pgh_password;
    private InputMethodManager im_pgh_imm;
    private ImageView bt_kdh_range;
    private Button bt_kdh_inform;
    private Button test;
    private CircleOverlay circleOverlay = new CircleOverlay();
    private RelativeLayout rl;
    private LinearLayout ll;
    private ImageView bt_pgh_advice;
    private ImageView bt_pgh_follow;

    public static void del_list(int index) {
        alertDTOS.remove(index);
        if (al_marker.size() > 0) {
            al_marker.get(index).setMap(null);
            al_marker.remove(index);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void send_notification() {
        AlertDTO temp;
        if (alertDTOS.size() > 0) {
            temp = alertDTOS.get(alertDTOS.size() - 1);
        } else {
            return;
        }
        if (MainActivity.Cal_distance(new LatLng(temp.getLatitude(), temp.getLongitude()), Last_coord) >= 1000) {
            return;
        }

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= 26) {
            //When sdk version is larger than26
            String id = "channel_1";
            String description = "143";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(id, description, importance);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300});
            manager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(getApplicationContext(), id)
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("사고야!")
                    .setContentText(temp.getComment())
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build();
            manager.notify(1, notification);
        } else {
            //When sdk version is less than26
            Notification notification = new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle("사고야!")
                    .setContentText(temp.getComment())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            manager.notify(1, notification);
        }
    }

    private void init_DB() {
        //
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                if (Data_init == false) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!readyMap) {
                            }
                            while (FirebaseAuth.getInstance().getCurrentUser() == null) {
                            }
                            Log.d("QQQ", "검사0 : " + FirebaseAuth.getInstance().getCurrentUser().getUid() + "///" + FirebaseAuth.getInstance().getCurrentUser().getUid());

                            DBLock = true;
                            Data_init = true;
                            AlertDTO temp_alertDTO;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                temp_alertDTO = snapshot.getValue(AlertDTO.class);
                                Date temp_date = new Date(System.currentTimeMillis());
                                if ((temp_date.getTime() - StrToDate(temp_alertDTO.getTime()).getTime()) < 36000 * 1000) {

                                    if (Cal_distance(new LatLng(temp_alertDTO.getLatitude(), temp_alertDTO.getLongitude()),
                                            Last_coord) < 1000) {
                                        alertDTOS.add(temp_alertDTO);
                                    }
                                    Log.d("QQQ", "검사 : " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    if ((FirebaseAuth.getInstance().getCurrentUser().getUid().equals(temp_alertDTO.getUID())) &&
                                            (((temp_date.getTime() - StrToDate(temp_alertDTO.getTime()).getTime())<3600*1000))) {
                                        if (Tickey.size() < 3) {
                                            Tickey.add(StrToDate(temp_alertDTO.getTime()));
                                        } else {
                                            Tickey.add(StrToDate(temp_alertDTO.getTime()));
                                            Tickey.remove(0);
                                        }
                                    }
                                    Log.d("QQQ", "티켓 : " + Tickey.size());
                                    compareDB.add(temp_alertDTO);
                                }
                            }
                            DBLock = false;
                        }
                    }).start();
                } else {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (DBLock) {
                            }
                            DBLock = true;
                            AlertDTO temp_alertDTO = null;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                temp_alertDTO = snapshot.getValue(AlertDTO.class);
                            }
                            Date temp_date = new Date(System.currentTimeMillis());
                            if (Cal_distance(new LatLng(temp_alertDTO.getLatitude(), temp_alertDTO.getLongitude()),
                                    Last_coord) < 1000) {
                                alertDTOS.add(temp_alertDTO);
                                send_notification();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        create_marker();
                                    }
                                });
                            }
                            //
                            Log.d("QQQ", "검사2 : " + FirebaseAuth.getInstance().getCurrentUser().getUid() + "///" + temp_alertDTO.getUID());

                            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(temp_alertDTO.getUID())) {
                                if (Tickey.size() < 3) {
                                    Tickey.add(StrToDate(temp_alertDTO.getTime()));
                                } else {
                                    Tickey.add(StrToDate(temp_alertDTO.getTime()));
                                    Tickey.remove(0);
                                }
                            }

                            compareDB.add(temp_alertDTO);
                            DBLock = false;
                        }
                    }).start();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        // FirebaseDatabase.getInstance().getReference("Alert").removeEventListener(eventListener);
        FirebaseDatabase.getInstance().getReference("Alert").addValueEventListener(eventListener);

    }

    public static void ShowToast() {
        Toast.makeText(MainActivity.context, "신고를 너무 많이 하셨습니다.", Toast.LENGTH_SHORT).show();
    }

    private void init() {
        Data_init = false;
        isMarker = false;
        isFollow = true;
        readyMap = false;
        DBLock = false;
        alertDTOS.clear();
        compareDB.clear();
        int i = al_marker.size();
        for (int j = 0; j < i; j++) {
            al_marker.get(j).setMap(null);
        }
        al_marker.clear();
        Tickey.clear();

        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient("jit743xg27"));

        context = getApplicationContext();
        im_pgh_imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
        et_pgh_email = findViewById(R.id.et_pgh_signUpemail);
        et_pgh_password = findViewById(R.id.et_pgh_pw);
        et_pgh_password.setFilters(new InputFilter[]{filter_pw, new InputFilter.LengthFilter(20)});
        et_pgh_email.setFilters(new InputFilter[]{filter_mail, new InputFilter.LengthFilter(20)});
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        rl = findViewById(R.id.rl);
        ll = findViewById(R.id.ll);
        bt_kdh_range = findViewById(R.id.bt_kdh_range);
        bt_kdh_inform = findViewById(R.id.bt_kdh_inform);

        test = findViewById(R.id.test);

        bt_pgh_follow = findViewById(R.id.bt_pgh_follow);
        bt_pgh_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable drawableClosed = getResources().getDrawable(R.drawable.ic_locker_closed);
                Drawable drawableOpen = getResources().getDrawable(R.drawable.locker_open);
                if (isFollow == true) {

                    bt_pgh_follow.setImageDrawable(drawableOpen);
                    isFollow = false;
                } else {
                    isFollow = true;
                    bt_pgh_follow.setImageDrawable(drawableClosed);
                    map.moveCamera(CameraUpdate.scrollTo(Last_coord));
                }
            }
        });
        bt_pgh_advice = findViewById(R.id.bt_pgh_advice);
        bt_pgh_advice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
                startActivity(intent);
            }
        });
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isMarker == false) {
                    isMarker = true;
                    Drawable drawableBtn = getResources().getDrawable(R.drawable.btn_style);
                    test.setBackground(drawableBtn);
                    test.setTextColor(Color.parseColor("#F73838"));
                    if (alertDTOS.size() > 0) {
                        if (al_marker.size() > 0) {
                            int i = al_marker.size();
                            for (int j = 0; j < i; j++) {
                                al_marker.get(j).setMap(null);
                            }
                            al_marker.clear();
                        }
                        for (int i = 0; i < alertDTOS.size(); i++) {
                            Marker temp_mark = new Marker();
                            LatLng temp_latlng = new LatLng(alertDTOS.get(i).getLatitude(), alertDTOS.get(i).getLongitude());
                            temp_mark.setPosition(temp_latlng);
                            temp_mark.setIcon(MarkerIcons.RED);
                            temp_mark.setCaptionTextSize(14);
                            temp_mark.setCaptionText(alertDTOS.get(i).getComment());
                            temp_mark.setCaptionMinZoom(12);
                            temp_mark.setSubCaptionTextSize(10);
                            temp_mark.setSubCaptionColor(Color.GRAY);
                            temp_mark.setSubCaptionMinZoom(13);
                            temp_mark.setMap(MainActivity.map);
                            al_marker.add(temp_mark);
                        }
                    } else{
                        Toast.makeText(MainActivity.this, "주변에 사고가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    isMarker = false;
                    Drawable drawableBtn2 = getResources().getDrawable(R.drawable.btn_style2);
                    test.setBackground(drawableBtn2);
                    test.setTextColor(Color.WHITE);
                    for (int i = 0; i < al_marker.size(); i++) {
                        al_marker.get(i).setMap(null);
                    }
                    al_marker.clear();
                }
            }
        });
    }

    public void clickBTN(View v) {
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
        //Auth 활용 로그인 소스 코드
        login(et_pgh_email.getText().toString(), et_pgh_password.getText().toString());
        im_pgh_imm.hideSoftInputFromWindow(et_pgh_email.getWindowToken(), 0);
    }

    public void clickTXT(View v) {
        Intent intent = new Intent(MainActivity.this, Sign_Up_Activity.class);
        startActivity(intent);
    }

    //정규식
    InputFilter filter_mail = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[a-zA-Z0-9@.]+$");
            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    };
    InputFilter filter_pw = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[a-zA-Z0-9ㄱ-ㅎ가-힣`~!@#$%^&*()-_+=]+$");
            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    };

    public void login(String email, String password) {
        //
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.d("QQQ", "리스너 장착");
        } else {
            FirebaseAuth.getInstance().signOut();
        }
        Log.d("QQQ", "login");
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            init_DB();

                            rl.setVisibility(View.VISIBLE);
                            ll.setVisibility(View.GONE);

                            // 마커 찍으라고 안내 Toast start -----------------------------
                            Toast msgToast = Toast.makeText(MainActivity.this, "사건 발생 지역을 터치해주세요.", Toast.LENGTH_LONG);
                            msgToast.setGravity(Gravity.TOP, 0, 230);

                            View toastLayout = getLayoutInflater().inflate(R.layout.toast_layout, null);

                            msgToast.setView(toastLayout);
                            msgToast.show();

                            // 안내 Toast end -----------------------------

                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Toast.makeText(getApplicationContext(), "이메일 비밀번호가 맞지 않습니다.", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "잘 못 된 정보입니다.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    public void Move(View v) {
        if (!DBLock) {
            Intent move = new Intent(MainActivity.this, Alarm_Activity.class);
            startActivity(move);
        }
    }

    public static class CustomLocationSource implements LocationSource, NaverMap.OnMapClickListener {
        private OnLocationChangedListener listener;
        public static boolean isMark = false;
        public static Marker markerWithSubCaption = new Marker();

        @Override
        public void activate(@NonNull OnLocationChangedListener listener) {
            this.listener = listener;
        }

        @Override
        public void deactivate() {
            listener = null;
        }

        @Override
        public void onMapClick(@NonNull PointF point, @NonNull LatLng coord) {
            if (listener == null) {
                return;
            }

            Click_coord = coord;

            double distance = MainActivity.Cal_distance(coord, Last_coord);
            if (distance < 1000) {
                isMark = true;
                markerWithSubCaption.setPosition(coord);
                markerWithSubCaption.setIcon(MarkerIcons.BLUE);
                markerWithSubCaption.setCaptionTextSize(14);
                markerWithSubCaption.setCaptionText("사고발생지역");
                markerWithSubCaption.setCaptionMinZoom(12);
                markerWithSubCaption.setSubCaptionTextSize(10);
                markerWithSubCaption.setSubCaptionColor(Color.GRAY);
                markerWithSubCaption.setSubCaptionMinZoom(13);
                markerWithSubCaption.setMap(MainActivity.map);
            } else {
                isMark = false;
                markerWithSubCaption.setMap(null);
            }
        }
    }

    public static Date StrToDate(String str) {
        SimpleDateFormat fm = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date date = new Date(System.currentTimeMillis());
        try {
            date = fm.parse(str);
        } catch (Exception e) {
        }
        return date;
    }

    public static double Cal_distance(LatLng coord1, LatLng coord2) {
        double theta = coord1.longitude - coord2.longitude;
        double dist = Math.sin(deg2rad(coord1.latitude)) * Math.sin(deg2rad(coord2.latitude)) + Math.cos(deg2rad(coord1.latitude)) * Math.cos(deg2rad(coord2.latitude)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1609.344;
        Log.d("test", "Cal_distance 호출 : " + dist);
        return dist;
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {

        CircularProgressDrawable progressDrawable = new CircularProgressDrawable(this);
        progressDrawable.setStyle(CircularProgressDrawable.LARGE);
        progressDrawable.setColorSchemeColors(Color.WHITE);
        progressDrawable.start();
        tryEnableLocation();

        int color = Color.RED;
        circleOverlay.setRadius(1000);
        circleOverlay.setColor(ColorUtils.setAlphaComponent(color, 31));
        circleOverlay.setOutlineColor(color);
        circleOverlay.setCenter(new LatLng(37.5666102, 126.9783881));
        circleOverlay.setMap(naverMap);
        bt_kdh_range.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable drawableOn = getResources().getDrawable(R.drawable.ic_range);
                Drawable drawableOff = getResources().getDrawable(R.drawable.ic_range_null);
                if (circleOverlay.isVisible()) {
                    circleOverlay.setVisible(false);
                    bt_kdh_range.setImageDrawable(drawableOff);
                } else {
                    circleOverlay.setVisible(true);
                    bt_kdh_range.setImageDrawable(drawableOn);
                }
            }
        });
        bt_kdh_inform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CustomLocationSource.isMark == false) {

                    Toast.makeText(getApplicationContext(), "사고발생지역을 지정해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Intent intent = new Intent(MainActivity.this, DialogActivity.class);
                    startActivity(intent);
                }
            }
        });
        CustomLocationSource locationSource = new CustomLocationSource();
        naverMap.setLocationSource(locationSource);
        naverMap.setOnMapClickListener(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Face);
        naverMap.setMaxZoom(15);
        naverMap.setMinZoom(11);
        map = naverMap;
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override // 이동할때 받음
        public void onLocationResult(LocationResult locationResult) {
            if (map == null) {
                return;
            }
            Location lastLocation = locationResult.getLastLocation();
            LatLng coord = new LatLng(lastLocation);

            LocationOverlay locationOverlay = map.getLocationOverlay();
            locationOverlay.setPosition(coord);
            locationOverlay.setBearing(lastLocation.getBearing());
            if (isFollow) {
                map.moveCamera(CameraUpdate.scrollTo(coord));
            }
            locationOverlay.setVisible(true);
            circleOverlay.setCenter(coord);
            Last_coord = coord;
            if (readyMap == false) {
                DB_coord = coord;
            }
            readyMap = true;
            if (Cal_distance(coord, DB_coord) > 200) {
                DB_coord = coord;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (DBLock) {
                        }
                        DBLock = true;
                        alertDTOS.clear();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                int temp_size = al_marker.size();
                                for (int i = 0; i < temp_size; i++) {
                                    al_marker.get(0).setMap(null);
                                    al_marker.remove(0);
                                }
                            }
                        });
                        Data_init = true;
                        AlertDTO temp_alertDTO;
                        for (int j = 0; j < compareDB.size(); j++) {
                            temp_alertDTO = compareDB.get(j);
                            Date temp_date = new Date(System.currentTimeMillis());
                            if ((temp_date.getTime() - StrToDate(temp_alertDTO.getTime()).getTime()) < 36000 * 1000) {
                                if (Cal_distance(new LatLng(temp_alertDTO.getLatitude(), temp_alertDTO.getLongitude()),
                                        Last_coord) < 1000) {
                                    alertDTOS.add(temp_alertDTO);
                                }
                            }
                        }

                        //////
                        if (alertDTOS.size() > 0) {

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    create_marker();
                                }
                            });
                        }
                        DBLock = false;
                    }
                }).start();
            }
        }
    };

    public static void create_marker() {
        if (isMarker == true) {
            if (al_marker.size() > 0) {
                for (int i = 0; i < al_marker.size(); i++) {
                    al_marker.get(i).setMap(null);
                }
                al_marker.clear();
            }
            for (int i = 0; i < alertDTOS.size(); i++) {
                Marker temp_mark = new Marker();
                LatLng temp_latlng = new LatLng(alertDTOS.get(i).getLatitude(), alertDTOS.get(i).getLongitude());
                temp_mark.setPosition(temp_latlng);
                temp_mark.setIcon(MarkerIcons.RED);
                temp_mark.setCaptionTextSize(14);
                temp_mark.setCaptionText(alertDTOS.get(i).getComment());
                temp_mark.setCaptionMinZoom(12);
                temp_mark.setSubCaptionTextSize(10);
                temp_mark.setSubCaptionColor(Color.GRAY);
                temp_mark.setSubCaptionMinZoom(13);
                temp_mark.setMap(MainActivity.map);
                al_marker.add(temp_mark);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        SharedPreferences per_check = getSharedPreferences("per_check", MODE_PRIVATE);
        boolean check = per_check.getBoolean("first", true);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int grantResult : grantResults) {
                if (grantResult != PermissionChecker.PERMISSION_GRANTED) {
                    if (check == true) {
                        Toast.makeText(getApplicationContext(), "어플을 실행하려면 위치 권한이 필요합니다. 옵션에서 위치권한을 승인해주세요.", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences per_check = getSharedPreferences("per_check", MODE_PRIVATE);
                                SharedPreferences.Editor editor = per_check.edit();
                                editor.putBoolean("first", false);
                                editor.commit();
                                finish();
                            }
                        }, 700);// 0.7초 정도 딜레이를 준 후 시작
                    } else {
                        Toast.makeText(getApplicationContext(), "어플을 실행하려면 위치 권한이 필요합니다. 옵션에서 위치권한을 승인해주세요.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                        startActivity(intent);
                        finish();
                        break;
                    }
                }
            }
            enableLocation();
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStart() {
        super.onStart();
        enableLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        disableLocation();
    }

    @Override
    protected void onDestroy() {
        if(eventListener!=null)
        {
            FirebaseDatabase.getInstance().getReference("Alert").removeEventListener(eventListener);
        }
        super.onDestroy();
    }

    private void tryEnableLocation() {
        if (ContextCompat.checkSelfPermission(this, PERMISSIONS[0]) == PermissionChecker.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, PERMISSIONS[1]) == PermissionChecker.PERMISSION_GRANTED) {
            enableLocation();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
        }
    }

    private void enableLocation() {
        new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        LocationRequest locationRequest = new LocationRequest();
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
                        locationRequest.setFastestInterval(LOCATION_REQUEST_INTERVAL);

                        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                .requestLocationUpdates(locationRequest, locationCallback, null);
                        locationEnabled = true;
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .addApi(LocationServices.API)
                .build()
                .connect();
    }

    private void disableLocation() {
        if (!locationEnabled) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        locationEnabled = false;
    }
}