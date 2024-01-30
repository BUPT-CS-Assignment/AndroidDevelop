package com.bytedance.clockapplication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.clockapplication.widget.Clock;
import com.bytedance.clockapplication.widget.Digit;

public class MainActivity extends AppCompatActivity {
    private Clock mClockView;
    private Digit mDigitView;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewPager=findViewById(R.id.view_pager);

        View view = getLayoutInflater().inflate(R.layout.activity_clock,null);
        mClockView = view.findViewById(R.id.clock);
        View view2 = getLayoutInflater().inflate(R.layout.activity_digit,null);
        mDigitView = view2.findViewById(R.id.digit);


        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return view == o;
            }

            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position){
                container.addView(position == 0 ? mClockView : mDigitView);
                return position == 0 ? mClockView : mDigitView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {//必须实现，销毁
                container.removeView(position == 0 ? mClockView : mDigitView);
            }

        });
    }
}
