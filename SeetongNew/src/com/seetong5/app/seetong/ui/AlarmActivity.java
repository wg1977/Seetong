package com.seetong5.app.seetong.ui;

import android.os.Bundle;
import android.view.Window;

import com.seetong5.app.seetong.R;

/**
 * AlarmActivity ��Ҫ��������ʾ Seetong �ı�����Ϣ����Щ��Ϣͨ������豸��
 * Ϣ�ӷ������˻�ȡ. �������� Tab ѡ����� MoreFragment���� MoreFragment
 * �����е���������ǵ� Button ������ת���� Activity���� Activity Ӧ��������
 * �����ʾҳ�棬�����ڰ����˼�ʱӦ�� finish �� Activity���˻ص���һ���� MainActivity.
 *
 * Created by gmk on 2015/9/12.
 */
public class AlarmActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_alarm);
        initWidget();
    }

    /**
     *  ��ʼ���� Activity �Ļ������.
     */
    private void initWidget() {}
}
