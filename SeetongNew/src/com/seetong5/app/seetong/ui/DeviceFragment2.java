package com.seetong5.app.seetong.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.sdk.impl.PlayerDevice;

import java.util.List;

/**
 * DeviceFragment ��������ʾ�豸�б���ص� Fragment������ MainActivity �б����ӵ� TabHost ��.
 * Seetong ��¼������ MainActivity����Ĭ����ʾ DeviceFragment.
 * DeviceFragment ���ְ���������ͬ�� Fragment��һ���� DeviceListFragment����һ���� DeviceNoMsgFragment��
 * Seetongͨ��һ���̴߳ӷ�������ȡ��Ϣ��⵱ǰ�˺����Ƿ����豸�����������ʾ DeviceListFragment��������ʾ
 * DeviceNoMsgFragment.ע���������豸ʱ��MainActivity2֮��Ľ�����ʹ�ûص�����ʵ��.
 * ע�� Fragment Ƕ��ʹ��ʱҪʹ�� android.support.v4 ���ݰ�.
 *
 * Created by gmk on 2015/9/11.
 */
public class DeviceFragment2 extends BaseFragment {
    private View view;
    private DeviceNoMsgFragment deviceNoMsgFragment;
    private DeviceListFragment deviceListFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity2.m_this.setDeviceFragment(this);
        view = inflater.inflate(R.layout.device2, container);
        deviceNoMsgFragment = DeviceNoMsgFragment.newInstance();
        deviceListFragment = DeviceListFragment.newInstance();
        initWidget(view);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            /* �˺������� Fragment Ƕ�ף���ʱĬ����ʾ DeviceListFragment */
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.device_fragment_container, deviceNoMsgFragment)
                    .commit();
        }
    }

    /**
     * ��ʼ���� Fragment �еĻ������.
     */
    private void initWidget(final View view) {
        ImageButton deviceAddButton = (ImageButton) view.findViewById(R.id.device_add);
        deviceAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* ���������豸ҳ�� */
                Intent intent = new Intent(DeviceFragment2.this.getActivity(), AddDeviceActivity.class);
                intent.putExtra(Constant.ENTER_TYPES, 1);
                startActivityForResult(intent, Constant.ADD_DEVICE_REQ_ID);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case Constant.ADD_DEVICE_REQ_ID:
                /* TODO: �յ��˷�����Ϣ����Ҫ��̬�����豸�б� */
                final String devId = data.getStringExtra(Constant.DEVICE_INFO_KEY);
                String xml = data.getStringExtra(Constant.DEVICE_LIST_CONTENT_KEY);
                MainActivity2.m_this.onNotifyDevData(xml, new MainActivity2.ParseDevListResult() {
                    @Override
                    public void onResult(List<PlayerDevice> devices) {
                        MainActivity2.m_this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initWidget(view);
                            }
                        });
                    }
                });
                break;
            default:
                break;
        }
    }

    public void updateDeviceFragment(int listSize) {
        if (listSize > 0) {
            showDeviceListFragment();
        } else {
            showDeviceNoMsgFragment();
        }
    }

    private void showDeviceNoMsgFragment() {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.device_fragment_container, deviceNoMsgFragment)
                .commit();
    }

    private void showDeviceListFragment() {
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.device_fragment_container, deviceListFragment)
                .commit();
    }

    public void handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case Define.MSG_UPDATE_DEV_LIST:
                deviceListFragment.handleMessage(msg);
        }
    }
}
