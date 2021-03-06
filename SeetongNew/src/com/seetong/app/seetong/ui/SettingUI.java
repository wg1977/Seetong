package com.seetong.app.seetong.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.system.MediaPlayer;
import com.seetong.app.seetong.Config;
import com.seetong.app.seetong.Global;
import com.seetong.app.seetong.R;
import com.seetong.app.seetong.comm.Define;
import com.seetong.app.seetong.sdk.impl.LibImpl;

import java.util.ArrayList;
import java.util.Collections;

public class SettingUI extends BaseActivity {
    TextView mtvPtzValue;
    SeekBar mPtzBar;
    TextView mPollingTimeValue;
    SeekBar mPollingTimeBar;
    ToggleButton mtbAutoPlay;
    RadioGroup mrgViewSelect;
    Spinner mspPicSetting;
    ToggleButton m_btnAlarm;
    Spinner m_cbxAlarmSound;
    ToggleButton m_btnInCallMode;
    ToggleButton m_tb_show_video_info;
    ToggleButton m_tb_show_alias;
    ToggleButton m_tb_show_devid;
    ToggleButton m_tb_video_preview;
    ToggleButton m_tb_enable_hardware_decode;
    RelativeLayout m_layout_hardware_decode;
    ArrayList<String> m_soundAry = new ArrayList<>();
    ArrayAdapter<String> m_adpAry;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_ui);
        initWidget();
    }

    @Override
    public void onBackPressed() {
        MainActivity2.m_this.sendMessage(Define.MSG_ENABLE_ALIAS, 0, 0, null);
        this.finish();
    }

    protected void initWidget() {
        ((TextView) findViewById(R.id.tvTitle)).setText(mResources.getString(R.string.tv_setting_title));
        mtbAutoPlay = (ToggleButton) findViewById(R.id.tbAutoPlay);
        mrgViewSelect = (RadioGroup) findViewById(R.id.rgViewSelect);
        mspPicSetting = (Spinner) findViewById(R.id.spPicSetting);
        mtvPtzValue = (TextView) findViewById(R.id.tvPtzCtrlValue);
        mPtzBar = (SeekBar) findViewById(R.id.sbPtzCtrl); // 云台步长设置
        mPtzBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() { // 云台步长拖动改变监听器
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //seekBar.setProgress((progress < 1?1:progress));
                mtvPtzValue.setText((progress < 1 ? 1 : progress) + "/" + seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mPollingTimeValue = (TextView) findViewById(R.id.tvPollingTimeValue);
        mPollingTimeBar = (SeekBar) findViewById(R.id.sbPollingTime);
        mPollingTimeBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mPollingTimeValue.setText((i < 1 ? 5 : i + 5) + "/" + (seekBar.getMax() + 5));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mspPicSetting.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        m_btnAlarm = (ToggleButton) findViewById(R.id.tb_enable_alarm);
        m_btnAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int visible = isChecked ? View.VISIBLE : View.GONE;
                if (!isChecked) {
                    MediaPlayer.stop();
                }

                findViewById(R.id.layout_alarm_sound).setVisibility(visible);
            }
        });

        m_btnInCallMode = (ToggleButton) findViewById(R.id.tb_in_call_mode);
        m_tb_show_video_info = (ToggleButton) findViewById(R.id.tb_show_video_info);
        m_tb_show_alias = (ToggleButton) findViewById(R.id.tb_show_alias);
        m_tb_show_devid = (ToggleButton) findViewById(R.id.tb_show_devid);
        m_tb_video_preview = (ToggleButton) findViewById(R.id.tb_preview_mode);
        m_tb_enable_hardware_decode = (ToggleButton) findViewById(R.id.tb_enable_hardware_decode);
        m_layout_hardware_decode = (RelativeLayout) findViewById(R.id.layout_enable_hardware_decode);
        if (LibImpl.getInstance().hasHardwareDecode()) {
            m_layout_hardware_decode.setVisibility(View.VISIBLE);
            m_tb_enable_hardware_decode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean bEnableHardwareDecode = m_tb_enable_hardware_decode.isChecked();
                    LibImpl.getInstance().enableHardwareDecode(bEnableHardwareDecode);
                }
            });
        } else {
            m_layout_hardware_decode.setVisibility(View.GONE);
        }

        String[] ls = getResources().getStringArray(R.array.string_ary_alarm_sound_name);
        Collections.addAll(m_soundAry, ls);

        m_adpAry = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, m_soundAry);
        m_adpAry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_cbxAlarmSound = (Spinner) findViewById(R.id.sp_alarm_sound);
        m_cbxAlarmSound.setAdapter(m_adpAry);
        m_cbxAlarmSound.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (Config.m_alarm_sound == position) return;
                Config.m_alarm_sound = position;
                Config.m_alarm_sound_res_id = Global.m_resSound[position];
                MediaPlayer.play();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        loadData();
        if (!Config.m_enable_alarm) {
            findViewById(R.id.layout_alarm_sound).setVisibility(View.GONE);
        }
    }

    public void loadData() {
        mPtzBar.setProgress(Config.m_ptz_step);
        mtvPtzValue.setText(mPtzBar.getProgress() + "/" + mPtzBar.getMax());
        mtbAutoPlay.setChecked(Config.m_auto_play);
//		mrgViewSelect.check((Config.m_view_num == 0)? R.id.rbOneView : R.id.rbFourView);
        int pos = 1;
        if (Config.m_view_num == 1) {
            pos = 0;
        }else if (Config.m_view_num >= 4) {
            pos = 1;
        } /*else if (Config.m_view_num == 9) {
            pos = 2;
        } else if (Config.m_view_num == 16) {
            pos = 3;
        }*/

        mspPicSetting.setSelection(pos);
        m_btnAlarm.setChecked(Config.m_enable_alarm);
        m_cbxAlarmSound.setSelection(Config.m_alarm_sound);
        m_btnInCallMode.setChecked(Config.m_in_call_mode);
        m_tb_show_video_info.setChecked(Config.m_show_video_info);
        m_tb_show_alias.setChecked(Config.m_show_alias);
        m_tb_show_devid.setChecked(Config.m_show_devid);
        m_tb_video_preview.setChecked(Config.m_video_fill_preview);
        m_tb_enable_hardware_decode.setChecked(Config.m_enable_hardware_decode);
        if (LibImpl.getInstance().hasHardwareDecode()) {
            LibImpl.getInstance().enableHardwareDecode(Config.m_enable_hardware_decode);
        }
        mPollingTimeBar.setProgress(Config.m_polling_time);
        mPollingTimeValue.setText((mPollingTimeBar.getProgress() + 5) + "/" + (mPollingTimeBar.getMax() + 5));
    }

    public void saveData() {
        Config.m_ptz_step = (mPtzBar.getProgress() < 1)? 1: mPtzBar.getProgress();
        int pos = mspPicSetting.getSelectedItemPosition();
        String view_nums[] = getResources().getStringArray(R.array.t_view_types);
        Config.m_view_num = Integer.valueOf(view_nums[pos]);
        Config.m_auto_play = mtbAutoPlay.isChecked();
        Config.m_enable_alarm = m_btnAlarm.isChecked();
        Config.m_alarm_sound = m_cbxAlarmSound.getSelectedItemPosition();
        Config.m_in_call_mode = m_btnInCallMode.isChecked();
        Config.m_show_video_info = m_tb_show_video_info.isChecked();
        Config.m_show_alias = m_tb_show_alias.isChecked();
        Config.m_show_devid = m_tb_show_devid.isChecked();
        Config.m_video_fill_preview = m_tb_video_preview.isChecked();
        Config.m_enable_hardware_decode = m_tb_enable_hardware_decode.isChecked();
        Config.m_polling_time = (mPollingTimeBar.getProgress() < 1) ? 1 : mPollingTimeBar.getProgress();
        Config.saveData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaPlayer.stop();
        saveData();
    }
}