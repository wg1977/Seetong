package com.seetong.app.seetong.gui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;

import com.seetong.app.seetong.R;

public class MainActivity extends FragmentActivity {
    private TabHost tabHost;

    private View deviceTabView;
    private View mediaTabView;
    private View moreTabView;
    private Fragment currentFragment;
    private DeviceFragment deviceFragment;
    private MediaFragment mediaFragment;
    private MoreFragment moreFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initWidget();
    }

    private void initWidget() {
        tabHost = (TabHost) findViewById(R.id.main_tab_host);
        tabHost.setup();

        deviceTabView = getLayoutInflater().inflate(R.layout.tab_widget, null);
        deviceTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_list_on);
        ((TextView) deviceTabView.findViewById(R.id.id_text_view)).setText(R.string.main_device);
        ((TextView) deviceTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.green));

        TabHost.TabSpec device = tabHost.newTabSpec("device");
        device.setContent(R.id.fragment_device);
        device.setIndicator(deviceTabView);
        tabHost.addTab(device);

        mediaTabView = getLayoutInflater().inflate(R.layout.tab_widget, null);
        mediaTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_media_off);
        ((TextView) mediaTabView.findViewById(R.id.id_text_view)).setText(R.string.main_media);
        ((TextView) mediaTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.gray));

        TabHost.TabSpec media = tabHost.newTabSpec("media");
        media.setContent(R.id.fragment_media);
        media.setIndicator(mediaTabView);
        tabHost.addTab(media);

        moreTabView = getLayoutInflater().inflate(R.layout.tab_widget, null);
        moreTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_more_off);
        ((TextView) moreTabView.findViewById(R.id.id_text_view)).setText(R.string.main_more);
        ((TextView) moreTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.gray));

        TabHost.TabSpec more = tabHost.newTabSpec("more");
        more.setContent(R.id.fragment_more);
        more.setIndicator(moreTabView);
        tabHost.addTab(more);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                setCurrentFragment(tabId);
                //View v = tabHost.getCurrentTabView();
                switch (tabId) {
                    case "device":
                        deviceTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_list_on);
                        ((TextView) deviceTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.green));
                        mediaTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_media_off);
                        ((TextView) mediaTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.gray));
                        moreTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_more_off);
                        ((TextView) moreTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.gray));
                        break;
                    case "media":
                        deviceTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_list_off);
                        ((TextView) deviceTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.gray));
                        mediaTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_media_on);
                        ((TextView) mediaTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.green));
                        moreTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_more_off);
                        ((TextView) moreTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.gray));
                        break;
                    case "more":
                        deviceTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_list_off);
                        ((TextView) deviceTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.gray));
                        mediaTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_media_off);
                        ((TextView) mediaTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.gray));
                        moreTabView.findViewById(R.id.id_image_view).setBackgroundResource(R.drawable.tps_tab_more_on);
                        ((TextView) moreTabView.findViewById(R.id.id_text_view)).setTextColor(getResources().getColor(R.color.green));
                        break;
                }
            }
        });
    }

    private void setCurrentFragment(String name) {
        switch (name) {
            case "device":
                currentFragment = deviceFragment;
                break;
            case "media":
                currentFragment = mediaFragment;
                break;
            case "more":
                currentFragment = moreFragment;
                break;
        }
    }
}
