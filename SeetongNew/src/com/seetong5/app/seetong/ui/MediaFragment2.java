package com.seetong5.app.seetong.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.LinearLayout;
import com.seetong5.app.seetong.R;


/**
 * MediaFragment ��Ҫ������ʾ��ý���ļ��б��� Fragment����Ҳ�ǰ����� MainActivity �� TabHost �У�
 * ��������Ƕ�װ��������ӵ� Fragment, �ֱ�Ϊ PictureFragment �� VideoFragment ������ʾͼƬ�ļ���
 * ¼���ļ�.���û������ͬ�� Button ʱ��ͨ�� getChildFragmentManager ��ʾ��ͬ�� Fragment.
 * �� Fragment Ĭ����ʾ PictureFragment.
 *
 * Created by gmk on 2015/9/11.
 */
public class MediaFragment2 extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.media2, container);
        final LinearLayout layout = (LinearLayout) fragmentView.findViewById(R.id.media_title_layout);
        final Button picturebutton = (Button) fragmentView.findViewById(R.id.media_picture);
        final Button videoButton = (Button) fragmentView.findViewById(R.id.media_video);
        picturebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.setBackgroundResource(R.drawable.tps_media_picture);
                picturebutton.setTextColor(getResources().getColor(R.color.white));
                videoButton.setTextColor(getResources().getColor(R.color.green));
                showPictureFragment();
            }
        });


        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.setBackgroundResource(R.drawable.tps_media_video);
                picturebutton.setTextColor(getResources().getColor(R.color.green));
                videoButton.setTextColor(getResources().getColor(R.color.white));
                showVideoFragment();
            }
        });

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.media_fragment_container, PictureFragment.newInstance())
                    .commit();
        }
    }

    private void showPictureFragment() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.media_fragment_container, PictureFragment.newInstance())
                .commit();
    }

    private void showVideoFragment() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.media_fragment_container, VideoFragment2.newInstance())
                .commit();
    }
}