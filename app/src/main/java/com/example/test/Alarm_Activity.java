package com.example.test;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import com.naver.maps.geometry.LatLng;

public class Alarm_Activity extends AppCompatActivity {

    private ImageView img_knh_back;

    // View Object 관련
    private SwipeMenuListView hisview;
    private MyAdapter myAdapter = new MyAdapter();
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_layout);

        img_knh_back = findViewById(R.id.img_knh_back);
        hisview = findViewById(R.id.hisview);

        dataSetting();

        //swipe
        hisview.setMenuCreator(creator);
        //스와이프 리스너 등록
        hisview.setOnSwipeListener(swipeListener);
        //swipe
        //클릭 리스너 등록
        hisview.setOnMenuItemClickListener(itmeClickListener);
    }

    private void dataSetting() {
        for (int i = 0; i < MainActivity.alertDTOS.size(); i++) {
            if (MainActivity.Cal_distance(
                    new LatLng(MainActivity.alertDTOS.get(i).getLatitude(),
                            MainActivity.alertDTOS.get(i).getLongitude()),MainActivity.Last_coord) < 1000);
            myAdapter.addItem(MainActivity.alertDTOS.get(i));
        }

        /* 리스트뷰에 어댑터 등록 */
        hisview.setAdapter(myAdapter);
    }

    public void backClick(View V) {
        finish();
    }

    //swipe
    SwipeMenuCreator creator = new SwipeMenuCreator() {

        @Override
        public void create(SwipeMenu menu) {
            // create "Close" item
            SwipeMenuItem openItem = new SwipeMenuItem(
                    getApplicationContext());
            // set item background
            openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                    0xCE)));
            // set item width
            openItem.setWidth(300);
            // set item title
            openItem.setTitle("Close");
            // set item title fontsize
            openItem.setTitleSize(15);
            // set item title font color
            openItem.setTitleColor(Color.WHITE);
            // add to menu
            menu.addMenuItem(openItem);

            // create "delete" item
            SwipeMenuItem deleteItem = new SwipeMenuItem(
                    getApplicationContext());
            // set item background
            deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                    0x3F, 0x25)));
            // set item width
            deleteItem.setWidth(300);
            // set a icon
            deleteItem.setIcon(R.drawable.ic_del);
            // add to menu
            menu.addMenuItem(deleteItem);
        }
    };
    //swipe
    //swipeMenuListView 리스너 등록
    SwipeMenuListView.OnSwipeListener swipeListener = new SwipeMenuListView.OnSwipeListener() {
        @Override
        public void onSwipeStart(int position) {
            hisview.smoothOpenMenu(position);
        }

        @Override
        public void onSwipeEnd(int position) {
            hisview.smoothOpenMenu(position);
        }
    };
    //swipe
    SwipeMenuListView.OnMenuItemClickListener itmeClickListener = new SwipeMenuListView.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
            switch (index) {
                case 0:
                    // open
                    break;
                case 1:
                    // delete
                    if (position >= 0 && position < myAdapter.getCount()) {
                        long selectedId = myAdapter.getItemId(position);
                        int deleteId = (int) selectedId;
                        myAdapter.removeItem(deleteId);
                        myAdapter.notifyDataSetChanged();
                        MainActivity.del_list(deleteId);
                    }
                    break;
            }
            return false;
        }
    };
}

