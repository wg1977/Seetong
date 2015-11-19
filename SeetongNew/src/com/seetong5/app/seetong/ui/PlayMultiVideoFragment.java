package com.seetong5.app.seetong.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.FloatMath;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.audio.AudioPlayer;
import com.android.opengles.OpenglesRender;
import com.android.opengles.OpenglesView;
import com.custom.etc.EtcInfo;
import com.seetong5.app.seetong.Config;
import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import com.seetong5.app.seetong.comm.Define;
import com.seetong5.app.seetong.sdk.impl.ConstantImpl;
import com.seetong5.app.seetong.sdk.impl.LibImpl;
import com.seetong5.app.seetong.sdk.impl.PlayerDevice;
import com.seetong5.app.seetong.ui.aid.MarqueeTextView;
import ipc.android.sdk.com.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by gmk on 2015/9/18.
 */
public class PlayMultiVideoFragment extends BaseFragment {
    private String TAG = PlayMultiVideoFragment.class.getName();
    private View fragmentView;
    private LinearLayout mainLayout;
    private PlayerDevice playerDevice;
    private PlayerDevice chosenPlayerDevice;
    private int[] location = new int[4];
    List<PlayerDevice> deviceList = new LinkedList<>();
    private GestureDetector gestureDetector;
    private int currentIndex = 0;
    public static final int FLING_MOVEMENT_THRESHOLD = 100;
    public static final int MAX_WINDOW = 4;
    public static final int MAX_WINDOW_BY_ROW = 2;
    private static boolean bFullScreen = false;
    private Animation animation;
    private Map<Integer, LinearLayout> rowLayoutMap = new HashMap<>();
    private Map<Integer, RelativeLayout> layoutMap = new HashMap<>();
    private Map<Integer, OpenglesRender> renderMap = new HashMap<>();
    private PointF prePoint = new PointF();
    private PointF curPoint = new PointF();

    public static PlayMultiVideoFragment newInstance(PlayerDevice playerDevice, int index) {
        return new PlayMultiVideoFragment(playerDevice, index);
    }

    public PlayMultiVideoFragment() {}

    public PlayMultiVideoFragment(PlayerDevice playerDevice, int index) {
        this.playerDevice = getPlayerDeviceByIndex(playerDevice, index);
        this.deviceList = getDeviceList(this.playerDevice);
        this.chosenPlayerDevice = this.deviceList.get(index);
        this.currentIndex = index;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "PlayMultiVideoFragment onCreateView...");
        PlayerActivity.m_this.setPlayMultiVideoFragment(this);
        Global.m_audioManage.setMode(AudioManager.MODE_NORMAL);
        fragmentView = inflater.inflate(R.layout.play_multi_video, container, false);
        mainLayout = (LinearLayout) fragmentView.findViewById(R.id.play_multi_video_layout);
        gestureDetector = new GestureDetector(fragmentView.getContext(), new MyOnGestureListener());
        gestureDetector.setOnDoubleTapListener(new OnDoubleClick());
        initView();

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startPlayList();
    }

    class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) {
                return false;
            }

            /* 如果GestureDetector检测到用户向左滑动，则显示上一个设备的视频 */
            if ((e2.getX() - e1.getX()) > FLING_MOVEMENT_THRESHOLD) {
                showPreviousDeviceListVideo(playerDevice);
            }

            /* 如果GestureDetector检测到用户向右滑动，这显示下一个设备的视频 */
            if ((e1.getX() - e2.getX()) > FLING_MOVEMENT_THRESHOLD) {
                showNextDeviceListVideo(playerDevice);
            }

            return false;
        }
    }

    class OnDoubleClick extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!bFullScreen) {
                setCurrentWindow(e);
            }
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            setCurrentWindow(e);
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (bFullScreen) {
                resetCurrentWindow();
                bFullScreen = false;
            } else {
                if ((Math.abs(curPoint.x - prePoint.x) < 20f) && (Math.abs(curPoint.y - prePoint.y) < 20f)) {
                    setCurrentWindow(e);
                    fullCurrentWindow();
                    bFullScreen = true;
                }
            }
            return true;
        }
    }

    private void setCurrentWindow(MotionEvent e) {
        /* 需要获取精确的每个图形绘制窗口的宽高，在这里获取的原因是为了确保横屏和竖屏时都能获得正确的location */
        location = PlayerActivity.m_this.getFragmentLocation();
        float x = e.getRawX();
        float y = e.getRawY();
        float w = location[1] - location[0];
        float h = location[3] - location[2];

        //Log.d(TAG, "l0 " + location[0] + " l1 " + location[1] + " l2 " + location[2] + " l3 " + location[3]);
        //Log.d(TAG, "x:" + x + " y:" + y + " w:" + w + " h:" + h);

        layoutMap.get(currentIndex).setBackgroundColor(getResources().getColor(R.color.video_view_normal_border));

        if ((x >= location[0]) && (x < location[0] + w / 2)
                && (y >= location[2]) && (y < location[2] + h / 2)) {
            chosenPlayerDevice = deviceList.get(0);
            currentIndex = 0;
        }

        if ((x >= location[0] + w / 2) && (x < location[0] + w)
                && (y >= location[2]) && (y < location[2] + h / 2)) {
            chosenPlayerDevice = deviceList.get(1);
            currentIndex = 1;
        }

        if ((x >= location[0]) && (x < location[0] + w / 2)
                && (y >= location[2] + h / 2) && (y < location[2] + h)) {
            chosenPlayerDevice = deviceList.get(2);
            currentIndex = 2;
        }

        if ((x >= location[0] + w / 2) && (x < location[0] + w)
                && (y >= location[2] + h / 2) && (y < location[2] + h)) {
            chosenPlayerDevice = deviceList.get(3);
            currentIndex = 3;
        }

        layoutMap.get(currentIndex).setBackgroundColor(getResources().getColor(R.color.video_view_focus_border));
        stopAllVoice();
        stopAllTalk();

        /* 多画面选择不同的窗口时，PlayerActivity的设备Id也要随着变换 */
        PlayerActivity.m_this.setCurrentDeviceId(this.chosenPlayerDevice.m_devId);
    }

    private void fullCurrentWindow() {
        for (int i = 0; i < MAX_WINDOW; i++) {
            if (i != currentIndex) {
                renderMap.get(i).getSurface().setVisibility(View.GONE);
                layoutMap.get(i).setVisibility(View.GONE);
            }
        }

        for (int i = 0; i < rowLayoutMap.size(); i++) {
            if (i == (currentIndex / MAX_WINDOW_BY_ROW)) {
                rowLayoutMap.get(i).setVisibility(View.VISIBLE);
            } else {
                rowLayoutMap.get(i).setVisibility(View.GONE);
            }
        }

        layoutMap.get(currentIndex).setVisibility(View.VISIBLE);
        renderMap.get(currentIndex).getSurface().setVisibility(View.VISIBLE);
    }

    private void resetCurrentWindow() {
        for (int i = 0; i < rowLayoutMap.size(); i++) {
            rowLayoutMap.get(i).setVisibility(View.VISIBLE);
        }

        for (int i = 0; i < MAX_WINDOW; i++) {
            layoutMap.get(i).setVisibility(View.VISIBLE);
            renderMap.get(i).getSurface().setVisibility(View.VISIBLE);
        }
    }

    class TouchListener implements View.OnTouchListener {
        private int mode = 0;
        private static final int DRAG = 1;
        private static final int ZOOM = 2;
        private static final float MAX_SCALE = 4.0f;
        private static final float MIN_SCALE = 0.25f;
        private static final float MIN_SPCE = 10f;
        private float preScale;
        private float oldDist = 1f;
        private PointF start = new PointF();
        private PointF mid = new PointF();
        PointF startOffset = new PointF();

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (null == chosenPlayerDevice) {
                return gestureDetector.onTouchEvent(event);
            }
            OpenglesRender render = chosenPlayerDevice.m_video;
            if (null == render) {
                return gestureDetector.onTouchEvent(event);
            }
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    prePoint.set(curPoint.x, curPoint.y);
                    curPoint.set(event.getRawX(), event.getRawY());
                    mode = DRAG;
                    preScale = render.bitmapScale;
                    start.set(event.getX(), event.getY());
                    startOffset.set(render.mStartX, render.mStartY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        if (render.bitmapScale > 1.0) {
                            float dtX = event.getX() - start.x;
                            float dtY = event.getY() - start.y;
                            start.set(event.getX(), event.getY());
                            render.mStartX += dtX;
                            render.mStartY -= dtY;

                            //向右拖动
                            if (dtX > 0) {
                                if (render.mScaleBitmapW < render.mViewWidth) {
                                    if (render.mViewWidth - (render.mTargetX + render.mStartX + render.mScaleBitmapW) < 10)
                                        render.mStartX = render.mViewWidth - (int) (render.mTargetX + render.mScaleBitmapW) - 10;
                                } else {
                                    int w = render.mScaleBitmapW - render.mViewWidth + 10;
                                    if ((render.mViewWidth + w) - (render.mTargetX + render.mStartX + render.mScaleBitmapW) < 10)
                                        render.mStartX = (render.mViewWidth + w) - (int) (render.mTargetX + render.mScaleBitmapW);
                                }
                            } else {
                                if (render.mScaleBitmapW < render.mViewWidth) {
                                    if (render.mTargetX - Math.abs(render.mStartX) < 10)
                                        render.mStartX = (int) -(render.mTargetX - 10);
                                } else {
                                    int w = render.mScaleBitmapW - render.mViewWidth + 10;
                                    if ((render.mTargetX + w) - Math.abs(render.mStartX) < 0)
                                        render.mStartX = (int) -(render.mTargetX + w);
                                }
                            }

                            //向下拖动
                            if (dtY > 0) {
                                if (render.mScaleBitmapH < render.mViewHeight) {
                                    if (render.mTargetY - Math.abs(render.mStartY) < 10)
                                        render.mStartY = (int) -(render.mTargetY - 10);
                                } else {
                                    int h = render.mScaleBitmapH - render.mViewHeight + 10;
                                    if ((render.mTargetY + h) - Math.abs(render.mStartY) < 0)
                                        render.mStartY = (int) -(render.mTargetY + h);
                                }
                            } else {
                                if (render.mScaleBitmapH < render.mViewHeight) {
                                    if (render.mViewHeight - (render.mTargetY + render.mStartY + render.mScaleBitmapH) < 10)
                                        render.mStartY = render.mViewHeight - (int) (render.mTargetY + render.mScaleBitmapH) - 10;
                                } else {
                                    int h = render.mScaleBitmapH - render.mViewHeight + 10;
                                    if ((render.mViewHeight + h) - (render.mTargetY + render.mStartY + render.mScaleBitmapH) < 10)
                                        render.mStartY = (render.mViewHeight + h) - (int) (render.mTargetY + render.mScaleBitmapH);
                                }
                            }
                        }
                    } else if (mode == ZOOM) {
                        float newDist = spacing(event);
                        if (newDist > MIN_SPCE) {
                            float scale = (newDist / oldDist) * preScale;
                            scale = (scale >= MAX_SCALE) ? MAX_SCALE : scale;
                            scale = (scale <= MIN_SCALE) ? MIN_SCALE : scale;
                            render.bitmapScale = scale;

                            float dtX = (scale - preScale) * render.mSrcBitmapW / 2;
                            float dtY = (scale - preScale) * render.mSrcBitmapH / 2;
                            render.mStartX = (int) (startOffset.x - dtX);
                            render.mStartY = (int) (startOffset.y - dtY);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mode = 0;
                    if (render.bitmapScale <= 1.0) {
                        render.resetScaleInfo();
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = 0;
                    if (render.bitmapScale <= 1.0) {
                        render.resetScaleInfo();
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = this.spacing(event);
                    if (oldDist > MIN_SPCE) {
                        midPoint(mid, event);
                        mode = ZOOM;
                    }
                    break;
            }
            return gestureDetector.onTouchEvent(event);
        }

        public float spacing(MotionEvent event) {//两点的距离
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return FloatMath.sqrt(x * x + y * y);
        }

        public void midPoint(PointF point, MotionEvent event) {//中点坐标
            float x = event.getX(0) + event.getX(1);
            float y = event.getY(0) + event.getY(1);
            point.set(x / 2, y / 2);
        }
    }


    private void initView() {
        LayoutInflater layoutInflater = LayoutInflater.from(fragmentView.getContext());
        LinearLayout row = new LinearLayout(mainLayout.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < MAX_WINDOW; i++) {
            RelativeLayout layout = layoutMap.get(i);
            if (null == layout) {
                layout = (RelativeLayout) layoutInflater.inflate(R.layout.play_multi_video_item, null);
                layout.setTag(i);
                layoutMap.put(i, layout);
            }

            layoutMap.get(i).setBackgroundColor(getResources().getColor(R.color.video_view_normal_border));

            OpenglesView openglesView = (OpenglesView) layout.findViewById(R.id.liveVideoView);
            OpenglesRender openglesRender = renderMap.get(i);
            if (null == openglesRender) {
                openglesRender = new OpenglesRender(openglesView, i);
                openglesRender.setVideoMode(OpenglesRender.VIDEO_MODE_CUSTOM);
                openglesView.setRenderer(openglesRender);
                openglesView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                openglesView.setTag(i);
                openglesView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                openglesView.setLongClickable(true);
                openglesView.setOnTouchListener(new TouchListener());

                openglesRender.addCheckCallback(new OpenglesRender.CheckCallback() {
                    @Override
                    public void recvDataTimout(int branch, OpenglesRender rander) {
                        if (null == rander) {
                            setVideoInfo(branch, T(R.string.tv_video_play_tip));
                            return;
                        }
                        setVideoInfo(branch, T(R.string.tv_video_wait_video_stream_tip));
                    }
                });

                renderMap.put(i, openglesRender);
            }

            row.addView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            if ((i + 1) % MAX_WINDOW_BY_ROW != 0) {
                continue;
            }

            mainLayout.addView(row, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            rowLayoutMap.put((i / MAX_WINDOW_BY_ROW), row);
            row = new LinearLayout(mainLayout.getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
        }

        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_switch_next_video);
    }

    private List<PlayerDevice> getDeviceList(PlayerDevice device) {
        List<PlayerDevice> list = new LinkedList<>();
        List<PlayerDevice> currentList = new LinkedList<>();
        list.addAll(Global.getSelfDeviceList());

        if (list.size() < MAX_WINDOW) {
            for (int i = 0; i < MAX_WINDOW; i++) {
                if (i < list.size()) {
                    currentList.add(i, list.get(i));
                } else {
                    if ((i - list.size()) < list.size()) {
                        currentList.add(i, list.get(i - list.size()));
                    } else {
                        currentList.add(i, list.get(0));
                    }
                }
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                if (device.equals(list.get(i))) {
                    for (int j = 0; j < MAX_WINDOW; j++) {
                        if ((i + j) < list.size()) {
                            currentList.add(j, list.get(i + j));
                        } else {
                            currentList.add(j, list.get(i + j - list.size()));
                        }
                    }
                    break;
                }
            }
        }
        return currentList;
    }

    public void startPlayList() {
        startPlay(deviceList);
    }

    public void startChoosenPlay(PlayerDevice dev) {
        stopCurrentPlay();
        chosenPlayerDevice = dev;
        modifyDeviceList(dev);
        startSinglePlay();
    }

    private void modifyDeviceList(PlayerDevice dev) {
        deviceList.set(currentIndex, dev);
    }

    private void stopCurrentPlay() {
        PlayerActivity.m_this.resetWidget();
        stopVideoRecord();
        stopVideoSound();
        LibImpl.stopPlay(currentIndex, deviceList.get(currentIndex));
        deviceList.get(currentIndex).m_video.mIsStopVideo = true;
        deviceList.get(currentIndex).m_video = null;
    }

    public void autoCyclePlay() {
        showNextDeviceListVideo(this.playerDevice);
    }

    public void startSpeak() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        stopAllVoice();
        stopAllTalk();

        if (null == chosenPlayerDevice || !chosenPlayerDevice.m_playing) {
            toast(R.string.before_open_video_preview);
            return;
        }

        TPS_AddWachtRsp rsp = chosenPlayerDevice.m_add_watch_rsp;
        if (null == rsp) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        if (!rsp.hasAudio()) {
            toast(R.string.tv_talk_fail_invalid_audio_device);
            return;
        }

        String audio_encoder = new String(rsp.getAudioParam().getAudio_encoder()).trim();
        if (!LibImpl.isValidAudioFormat(audio_encoder)) {
            toast(R.string.tv_talk_fail_illegal_format_tip);
            return;
        }

        if (chosenPlayerDevice.m_talk) return;

        int ret = LibImpl.getInstance().getFuncLib().StartTalkAgent(chosenPlayerDevice.m_dev.getDevId());
        if (0 != ret) {
            toast(R.string.tv_talk_fail_tip);
            return;
        }

        Global.m_audioManage.setMode(AudioManager.MODE_NORMAL);
        chosenPlayerDevice.m_audio.startTalk();
        chosenPlayerDevice.m_talk = true;
    }

    public void stopSpeak() {
        if (null == chosenPlayerDevice) return;
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        LibImpl.getInstance().getFuncLib().StopTalkAgent(chosenPlayerDevice.m_dev.getDevId());
        if (Config.m_in_call_mode) Global.m_audioManage.setMode(AudioManager.MODE_IN_CALL);
        if (!chosenPlayerDevice.m_talk) return;
        chosenPlayerDevice.m_audio.stopTalk();
        chosenPlayerDevice.m_talk = false;
    }

    private void stopAllTalk() {
        for (int i = 0; i < MAX_WINDOW; i++) {
            if (null == deviceList.get(i) || null == deviceList.get(i).m_audio) continue;
            LibImpl.getInstance().getFuncLib().StopTalkAgent(deviceList.get(i).m_dev.getDevId());
            deviceList.get(i).m_audio.stopTalk();
            deviceList.get(i).m_talk = false;
        }
    }

    public void videoCapture() {
        TPS_AddWachtRsp rsp = chosenPlayerDevice.m_add_watch_rsp;
        if (null == rsp) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        String strDate = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        String fileName = Global.getImageDir() + "/" + chosenPlayerDevice.m_dev.getDevId() + "_" + strDate + ".jpg";
        boolean bShotOk = chosenPlayerDevice.m_video.startShot(fileName);
        if (bShotOk) {
            toast(R.string.snapshot_succeed);
            final ImageView screenShotView = (ImageView) layoutMap.get(currentIndex).findViewById(R.id.screenShotFlash);
            screenShotView.setVisibility(View.VISIBLE);
            Bitmap bitmap = BitmapFactory.decodeFile(Global.getSnapshotDir() + "/" + chosenPlayerDevice.m_dev.getDevId() + ".jpg");
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.camera);
            }
            screenShotView.setImageBitmap(bitmap);
            RotateAnimation fade = new RotateAnimation(0, -60);
            fade.setDuration(500);
            fade.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation anim) {
                    screenShotView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            screenShotView.startAnimation(fade);
        } else {
            toast(R.string.snapshot_failed);
        }
    }

    public boolean startHighDefinition() {
        if (null == chosenPlayerDevice) {
            return false;
        }
        LibImpl.stopPlay(0, chosenPlayerDevice);
        chosenPlayerDevice.m_stream_type = Define.MAIN_STREAM_TYPE;
        LibImpl.startPlay(currentIndex, chosenPlayerDevice, chosenPlayerDevice.m_stream_type, chosenPlayerDevice.m_frame_type);
        return true;
    }

    public void stopHighDefinition() {
        if (null == chosenPlayerDevice) {
            return;
        }
        LibImpl.stopPlay(0, chosenPlayerDevice);
        chosenPlayerDevice.m_stream_type = Define.SUB_STREAM_TYPE;
        LibImpl.startPlay(currentIndex, chosenPlayerDevice, chosenPlayerDevice.m_stream_type, chosenPlayerDevice.m_frame_type);
    }

    public boolean startVideoRecord() {
        if (null == chosenPlayerDevice || !chosenPlayerDevice.m_playing) {
            toast(R.string.before_open_video_preview);
            return false;
        }

        TPS_AddWachtRsp rsp = chosenPlayerDevice.m_add_watch_rsp;
        if (null == rsp) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return false;
        }

        String filePath = Global.getVideoDir() + "/" + chosenPlayerDevice.m_dev.getDevId();
        File dir = new File(filePath);
        if (!(dir.exists())) dir.mkdirs();
        int ret = LibImpl.getInstance().getFuncLib().StartRecordAgent(chosenPlayerDevice.m_dev.getDevId(), filePath, EtcInfo.PER_RECORD_TIME_LENGTH);
        Log.d(TAG, "video record ret is : " + ret);
        if (0 != ret) {
            toast(R.string.fvu_tip_start_record_failed);
            return false;
        }

        chosenPlayerDevice.m_record = true;
        showRecordIcon(chosenPlayerDevice.m_devId, true);
        return true;
    }

    public void stopVideoRecord() {
        if (null == chosenPlayerDevice || !chosenPlayerDevice.m_playing) {
            //toast(R.string.before_open_video_preview);
            return;
        }

        TPS_AddWachtRsp rsp = chosenPlayerDevice.m_add_watch_rsp;
        if (null == rsp) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        LibImpl.getInstance().getFuncLib().StopRecordAgent(chosenPlayerDevice.m_dev.getDevId());
        chosenPlayerDevice.m_record = false;
        showRecordIcon(chosenPlayerDevice.m_devId, false);

        /*for (int i = 0; i < MAX_WINDOW; i++) {
            if (null == this.deviceList.get(i) || !this.deviceList.get(i).m_playing) {
                //toast(R.string.before_open_video_preview);
                return;
            }

            TPS_AddWachtRsp rsp = this.deviceList.get(i).m_add_watch_rsp;
            if (null == rsp) {
                toast(R.string.tv_video_wait_video_stream_tip);
                return;
            }

            LibImpl.getInstance().getFuncLib().StopRecordAgent(this.deviceList.get(i).m_dev.getDevId());
            this.deviceList.get(i).m_record = false;
            showRecordIcon(chosenPlayerDevice.m_devId, false);
        }*/
    }

    public void showRecordIcon(String devId, boolean bShow) {
        PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(devId);
        if (null == dev || !dev.m_playing) return;

        if (dev.m_view_id < 0) return;
        ImageView imageView = (ImageView) layoutMap.get(currentIndex).findViewById(R.id.imgRecord);
        imageView.setVisibility(bShow ? View.VISIBLE : View.INVISIBLE);
    }

    public void hideAllRecordIcon() {
        for (int i = 0; i < MAX_WINDOW; i++) {
            ImageView imageView = (ImageView) layoutMap.get(i).findViewById(R.id.imgRecord);
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    private void stopAllVoice() {
        for (int i = 0; i < MAX_WINDOW; i++) {
            if (null == deviceList.get(i) || null == deviceList.get(i).m_audio) continue;
            deviceList.get(i).m_audio.stopOutAudio();
            deviceList.get(i).m_voice = false;
        }
    }

    public void startRecordPlayBack() {
        if (null == chosenPlayerDevice) return;
        chosenPlayerDevice.m_capacity_set = LibImpl.getInstance().getCapacitySet(chosenPlayerDevice);
        if(chosenPlayerDevice.isNVR() && chosenPlayerDevice.is_p2p_replay()) {
            /* 开启回放前先关闭正在播放的设备 */
            stopCurrentPlayList();
            Intent it = new Intent(this.getActivity(), NvrRecord.class);
            it.putExtra(Constant.EXTRA_DEVICE_ID, chosenPlayerDevice.m_dev.getDevId());
            this.startActivity(it);
            return;
        }

        stopCurrentPlayList();
        if (!chosenPlayerDevice.is_p2p_replay()) {
            toast(R.string.tv_not_support_front_end_record);
            return;
        }

        Intent it = new Intent(this.getActivity(), FrontEndRecord.class);
        it.putExtra(Constant.EXTRA_DEVICE_ID, chosenPlayerDevice.m_dev.getDevId());
        this.startActivity(it);
    }

    public boolean startVideoSound() {
        if (null == chosenPlayerDevice || !chosenPlayerDevice.m_playing) {
            toast(R.string.before_open_video_preview);
            return false;
        }

        TPS_AddWachtRsp rsp = chosenPlayerDevice.m_add_watch_rsp;
        if (null == rsp) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return false;
        }

        if (!rsp.hasAudio()) {
            toast(R.string.fvu_tip_open_voice_fail_invalid_audio_device);
            return false;
        }

        String audio_encoder = new String(rsp.getAudioParam().getAudio_encoder()).trim();
        if (!LibImpl.isValidAudioFormat(audio_encoder)) {
            toast(R.string.fvu_tip_open_voice_fail_illegal_format_tip);
            return false;
        }

        if (null == chosenPlayerDevice.m_add_watch_rsp || !chosenPlayerDevice.m_add_watch_rsp.hasAudio()) {
            toast(R.string.tv_video_wait_video_stream_tip);
            return false;
        }

        stopAllVoice();
        if (Config.m_in_call_mode) Global.m_audioManage.setMode(AudioManager.MODE_IN_CALL);
        chosenPlayerDevice.m_audio.startOutAudio();
        chosenPlayerDevice.m_voice = true;
        toast(R.string.fvu_tip_open_voice);
        PlayerActivity.m_this.setVideoSoundWidget();

        return true;
    }

    public void stopVideoSound() {
        if (null == chosenPlayerDevice || !chosenPlayerDevice.m_playing) {
            //toast(R.string.before_open_video_preview);
            return;
        }

        TPS_AddWachtRsp rsp = chosenPlayerDevice.m_add_watch_rsp;
        if (null == rsp) {
            //toast(R.string.tv_video_wait_video_stream_tip);
            return;
        }

        if (!rsp.hasAudio()) {
            //toast(R.string.fvu_tip_open_voice_fail_invalid_audio_device);
            return;
        }

        String audio_encoder = new String(rsp.getAudioParam().getAudio_encoder()).trim();
        if (!LibImpl.isValidAudioFormat(audio_encoder)) {
            toast(R.string.fvu_tip_open_voice_fail_illegal_format_tip);
            return;
        }

        if (!chosenPlayerDevice.m_voice) return;
        Global.m_audioManage.setMode(AudioManager.MODE_NORMAL);
        chosenPlayerDevice.m_audio.stopOutAudio();
        chosenPlayerDevice.m_voice = false;
        toast(R.string.fvu_tip_close_voice);
    }

    private void startSinglePlay() {
        RelativeLayout layout = layoutMap.get(currentIndex);
        deviceList.get(currentIndex).m_device_play_count++;
        deviceList.get(currentIndex).m_video = renderMap.get(currentIndex);
        deviceList.get(currentIndex).m_video.mIsStopVideo = false;

        deviceList.get(currentIndex).m_device_play_count++;
        deviceList.get(currentIndex).m_video = renderMap.get(currentIndex);
        deviceList.get(currentIndex).m_video.mIsStopVideo = false;

        if (!deviceList.get(currentIndex).m_play) {
            int ret = LibImpl.startPlay(currentIndex, deviceList.get(currentIndex), deviceList.get(currentIndex).m_stream_type, deviceList.get(currentIndex).m_frame_type);
            if (ret == 0) {
                deviceList.get(currentIndex).m_online = true;
                deviceList.get(currentIndex).m_playing = false;
                setVideoInfo(currentIndex, T(R.string.tv_video_req_tip));
            } else {
                String selfID = "";
                if (LibImpl.mDeviceNotifyInfo.get(LibImpl.getRightDeviceID(deviceList.get(currentIndex).m_dev.getDevId())) != null) {
                    selfID = LibImpl.mDeviceNotifyInfo.get(LibImpl.getRightDeviceID(deviceList.get(currentIndex).m_dev.getDevId())).getNotifyStr();
                }

                Log.i("DeviceNotifyInfo", "DeviceNotifyInfo ary:" + LibImpl.mDeviceNotifyInfo + ".");
                selfID = (isNullStr(selfID)) ? "" : ("(" + selfID + ")");
                setVideoInfo(currentIndex, ConstantImpl.getTPSErrText(ret, false) + selfID);
                toast(ConstantImpl.getTPSErrText(ret, false) + selfID);;
            }
        } else {
            setVideoInfo(currentIndex, deviceList.get(currentIndex).m_tipInfo);
            setVideoInfo2(currentIndex, deviceList.get(currentIndex).m_tipTinfo2);
        }
        deviceList.get(currentIndex).m_play = true;
        deviceList.get(currentIndex).m_view_id = currentIndex;

        View view = layout.findViewById(R.id.tvLiveInfo);
        view.setVisibility(View.VISIBLE);
        view = deviceList.get(currentIndex).m_video.getSurface();
        view.setBackgroundColor(Color.TRANSPARENT);
        view.setVisibility(View.VISIBLE);

        deviceList.get(currentIndex).m_audio = new AudioPlayer(currentIndex);
        deviceList.get(currentIndex).m_audio.mIsAecm = false;
        deviceList.get(currentIndex).m_audio.mIsNoiseReduction = false;
        deviceList.get(currentIndex).m_audio.addRecordCallback(new AudioPlayer.MyRecordCallback() {
            @Override
            public void recvRecordData(byte[] data, int length, int reserver) {
                if (reserver >= 0) {
                    PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(reserver);
                    if (null == dev || null == dev.m_dev) return;
                    LibImpl.getInstance().recvRecordData(data, length, dev.m_dev.getDevId(), reserver);
                }
            }
        });
    }

    private boolean startPlay(List<PlayerDevice> devList) {
        RelativeLayout layout;
        if (null == devList) {
            Log.e(TAG, "the device list is null!!!");
            return false;
        }

        for (int i = 0; i < MAX_WINDOW; i++) {
            layout = layoutMap.get(i);

            devList.get(i).m_device_play_count++;
            devList.get(i).m_video = renderMap.get(i);
            devList.get(i).m_video.mIsStopVideo = false;

            if (!devList.get(i).m_play) {
                int ret = LibImpl.startPlay(i, devList.get(i), devList.get(i).m_stream_type, devList.get(i).m_frame_type);
                if (ret == 0) {
                    devList.get(i).m_online = true;
                    devList.get(i).m_playing = false;
                    setVideoInfo(i, T(R.string.tv_video_req_tip));
                } else {
                    String selfID = "";
                    if (LibImpl.mDeviceNotifyInfo.get(LibImpl.getRightDeviceID(devList.get(i).m_dev.getDevId())) != null) {
                        selfID = LibImpl.mDeviceNotifyInfo.get(LibImpl.getRightDeviceID(devList.get(i).m_dev.getDevId())).getNotifyStr();
                    }

                    Log.i("DeviceNotifyInfo", "DeviceNotifyInfo ary:" + LibImpl.mDeviceNotifyInfo + ".");
                    selfID = (isNullStr(selfID)) ? "" : ("(" + selfID + ")");
                    setVideoInfo(i, ConstantImpl.getTPSErrText(ret, false) + selfID);
                    toast(ConstantImpl.getTPSErrText(ret, false) + selfID);
                }
            } else {
                setVideoInfo(i, devList.get(i).m_tipInfo);
                setVideoInfo2(i, devList.get(i).m_tipTinfo2);
            }
            devList.get(i).m_play = true;
            devList.get(i).m_view_id = i;

            if (bFullScreen) {
                if (i == currentIndex) {
                    View view = layout.findViewById(R.id.tvLiveInfo);
                    view.setVisibility(View.VISIBLE);
                    view = devList.get(i).m_video.getSurface();
                    view.setBackgroundColor(Color.TRANSPARENT);
                    view.setVisibility(View.VISIBLE);

                    devList.get(i).m_audio = new AudioPlayer(i);
                    devList.get(i).m_audio.mIsAecm = false;
                    devList.get(i).m_audio.mIsNoiseReduction = false;
                    devList.get(i).m_audio.addRecordCallback(new AudioPlayer.MyRecordCallback() {
                        @Override
                        public void recvRecordData(byte[] data, int length, int reserver) {
                            if (reserver >= 0) {
                                PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(reserver);
                                if (null == dev || null == dev.m_dev) return;
                                LibImpl.getInstance().recvRecordData(data, length, dev.m_dev.getDevId(), reserver);
                            }
                        }
                    });
                }
            } else {
                View view = layout.findViewById(R.id.tvLiveInfo);
                view.setVisibility(View.VISIBLE);
                view = devList.get(i).m_video.getSurface();
                view.setBackgroundColor(Color.TRANSPARENT);
                view.setVisibility(View.VISIBLE);

                devList.get(i).m_audio = new AudioPlayer(i);
                devList.get(i).m_audio.mIsAecm = false;
                devList.get(i).m_audio.mIsNoiseReduction = false;
                devList.get(i).m_audio.addRecordCallback(new AudioPlayer.MyRecordCallback() {
                    @Override
                    public void recvRecordData(byte[] data, int length, int reserver) {
                        if (reserver >= 0) {
                            PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(reserver);
                            if (null == dev || null == dev.m_dev) return;
                            LibImpl.getInstance().recvRecordData(data, length, dev.m_dev.getDevId(), reserver);
                        }
                    }
                });
            }
        }

        fragmentView.setAnimation(animation);
        layoutMap.get(currentIndex).setBackgroundColor(getResources().getColor(R.color.video_view_focus_border));

        return true;
    }

    public void stopPlayList() {
        RelativeLayout layout;
        View view;
        stopVideoRecord();
        hideAllRecordIcon();
        stopVideoSound();
        stopHighDefinition();
        for (int i = 0; i< MAX_WINDOW; i++) {
            LibImpl.stopPlay(i, this.deviceList.get(i));
            layout = layoutMap.get(i);
            view = layout.findViewById(R.id.liveVideoView);
            view.setBackgroundColor(Color.BLACK);
            setVideoInfo(i, T(R.string.tv_video_stop_tip));
            setVideoInfo2(i, "");
            this.deviceList.get(i).m_video.mIsStopVideo = true;
            layout.invalidate();
        }
    }

    private void stopCurrentPlayList() {
        PlayerActivity.m_this.resetWidget();
        stopVideoRecord();
        stopVideoSound();
        for (int i = 0; i < MAX_WINDOW; i++) {
            LibImpl.stopPlay(i, this.deviceList.get(i));
        }
    }

    private void setCurrentDevice(PlayerDevice device) {
        this.playerDevice = device;
    }

    public PlayerDevice getChoosenDevice() {
        return this.chosenPlayerDevice;
    }

    private PlayerDevice getPlayerDeviceByIndex(PlayerDevice device, int index) {
        List<PlayerDevice> list = new LinkedList<>();
        list.addAll(Global.getSelfDeviceList());

        if (list.size() < MAX_WINDOW) {
            for (int i= 0; i < list.size(); i++) {
                if (device.equals(list.get(i))) {
                    return list.get(i);
                }
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                if (device.equals(list.get(i))) {
                    if (i - index >= 0) {
                        return list.get(i - index);
                    } else {
                        return list.get(i + list.size() - index);
                    }
                }
            }
        }

        Log.e(TAG, "the device is not in the device list err!!!");
        return device;
    }

    private void showPreviousDeviceListVideo(PlayerDevice device) {
        List<PlayerDevice> list = new LinkedList<>();
        list.addAll(Global.getSelfDeviceList());

        if (list.size() < MAX_WINDOW) {
            return;
        }

        /* 恢复PlayerActivity的button状态 */
        PlayerActivity.m_this.resetWidget();

        /* 停止所有正在录制的视频 */
        stopVideoRecord();
        hideAllRecordIcon();
        stopVideoSound();
        stopHighDefinition();

        /* 停止当前正在播放的设备列表 */
        RelativeLayout layout;
        for (int i = 0; i < MAX_WINDOW; i++) {
            LibImpl.stopPlay(i, this.deviceList.get(i));
            layout = layoutMap.get(i);
            layout.findViewById(R.id.liveVideoView).setVisibility(View.GONE);
            this.deviceList.get(i).m_video.mIsStopVideo = true;
            this.deviceList.get(i).m_video = null;
        }

        for (int i = 0; i < list.size(); i++) {
            if (device.equals(list.get(i))) {
                if (bFullScreen) {
                    if ((i - 1) >= 0) {
                        setCurrentDevice(list.get(i - 1));
                    } else {
                        setCurrentDevice(list.get(list.size() - 1));
                    }
                } else {
                    if ((i - MAX_WINDOW) >= 0) {
                        setCurrentDevice(list.get(i - MAX_WINDOW));
                    } else {
                        setCurrentDevice(list.get(i + list.size() - MAX_WINDOW));
                    }
                }
                break;
            }
        }

        this.deviceList = getDeviceList(this.playerDevice);
        chosenPlayerDevice = this.deviceList.get(currentIndex);
        /* 切换之后设置PlayerActivity当前播放的设备ID */
        PlayerActivity.m_this.setCurrentDeviceId(this.chosenPlayerDevice.m_devId);
        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_switch_prev_video);
        Boolean bRet = startPlay(this.deviceList);
        if (!bRet) {
            Log.e(TAG, "Start previous device err!!!");
        }
    }

    private void showNextDeviceListVideo(PlayerDevice device) {
        List<PlayerDevice> list = new LinkedList<>();
        list.addAll(Global.getSelfDeviceList());

        if (list.size() < MAX_WINDOW) {
            return;
        }

        /* 恢复PlayerActivity的button状态 */
        PlayerActivity.m_this.resetWidget();

        /* 停止所有正在录制的视频 */
        stopVideoRecord();
        hideAllRecordIcon();
        stopVideoSound();
        stopHighDefinition();

        /* 停止当前正在播放的设备列表 */
        RelativeLayout layout;
        for (int i = 0; i < MAX_WINDOW; i++) {
            LibImpl.stopPlay(i, this.deviceList.get(i));
            layout = layoutMap.get(i);
            layout.findViewById(R.id.liveVideoView).setVisibility(View.GONE);
            this.deviceList.get(i).m_video.mIsStopVideo = true;
            this.deviceList.get(i).m_video = null;
        }

        for (int i = list.size() - 1; i >= 0; i--) {
            if (device.equals(list.get(i))) {
                if (bFullScreen) {
                    if (i < list.size() - 1) {
                        setCurrentDevice(list.get(i + 1));
                    } else {
                        setCurrentDevice(list.get(0));
                    }
                } else {
                    if ((i + MAX_WINDOW) <= (list.size() - 1)) {
                        setCurrentDevice(list.get(i + MAX_WINDOW));
                    } else {
                        setCurrentDevice(list.get(i + MAX_WINDOW - list.size()));
                    }
                }
                break;
            }
        }

        this.deviceList = getDeviceList(this.playerDevice);
        chosenPlayerDevice = this.deviceList.get(currentIndex);
        /* 切换之后设置PlayerActivity当前播放的设备ID */
        PlayerActivity.m_this.setCurrentDeviceId(this.chosenPlayerDevice.m_devId);
        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_switch_next_video);
        Boolean bRet = startPlay(this.deviceList);
        if (!bRet) {
            Log.e(TAG, "Start next device err!!!");
        }
    }


    public void setVideoInfo(String devID, final String msg) {
        Log.d("setTipText", devID + "," + msg);
        int index = LibImpl.getInstance().getIndexByDeviceID(devID);
        if ((index < 0) || (index > MAX_WINDOW - 1)) {
            return;
        }
        setVideoInfo(index, msg);
    }

    public void setVideoInfo(final int index, final String msg) {
        RelativeLayout layout = layoutMap.get(index);
        MarqueeTextView v = (MarqueeTextView) layout.findViewById(R.id.tvLiveInfo);
        String dev_type = "";

        if (null == msg) {
            v.setVisibility(View.GONE);
        } else if (T(R.string.tv_video_play_tip).compareToIgnoreCase(msg) == 0) { //playing
            StringBuilder msgBuf = new StringBuilder();
            String _devName = deviceList.get(index).getDeviceName();
            msgBuf.append("[").append(_devName).append(dev_type).append("]");
            String _msg = msgBuf.toString();
            v.setText(_msg);
        } else {
            StringBuilder msgBuf = new StringBuilder();
            if (!isNullStr(deviceList.get(index).m_dev.getDevId())) {
                //加载别名
                String _devName = deviceList.get(index).getDeviceName();
                msgBuf.append("[").append(_devName).append(dev_type).append("]");
                if (!TextUtils.isEmpty(msg)) msgBuf.append(":").append(msg).append(".");
            } else {
                msgBuf.append(msg);
                if (!"".equals(msg)) msgBuf.append(".");
            }

            deviceList.get(index).m_tipInfo = msg;
            String _msg = msgBuf.toString();
            v.setText(_msg);
        }
    }

    public void setVideoInfo2(String devID, final String msg) {
        int index = LibImpl.getInstance().getIndexByDeviceID(devID);
        if ((index < 0) || (index > MAX_WINDOW - 1)) {
            return;
        }
        setVideoInfo2(index, msg);
    }

    public void setVideoInfo2(final int index, final String msg) {
        MarqueeTextView v = (MarqueeTextView) layoutMap.get(index).findViewById(R.id.tvMsgInfo);
        v.setVisibility(Config.m_show_video_info ? View.VISIBLE : View.GONE);
        deviceList.get(index).m_tipTinfo2 = msg;
        if (msg.equals("")) {
            v.setText(msg);
        } else {
            v.setText(Global.m_mobile_net_type_2 + msg);
        }
    }

    public boolean handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case SDK_CONSTANT.TPS_MSG_RSP_TALK://TPS_TALKRsp
                LibImpl.MsgObject msgObj = (LibImpl.MsgObject) msg.obj;
                TPS_TALKRsp ts = (TPS_TALKRsp) msgObj.recvObj;
                String devID = new String(ts.getSzDevId()).trim();
                PlayerDevice dev = LibImpl.getInstance().getPlayerDevice(devID);
                TPS_AUDIO_PARAM audioParm = ts.getAudioParam();
                if (audioParm == null) {
                    toast(R.string.tv_talk_fail_param_error_tip);
                    stopSpeak();
                    return true;
                }

                if (ts.getnResult() != 0) {
                    toast(R.string.tv_talk_fail_tip, ts.getnResult());
                    stopSpeak();
                }

                if (SDK_CONSTANT.AUDIO_TYPE_G711.compareToIgnoreCase(new String(audioParm.getAudio_encoder()).trim()) != 0) {
                    toast(R.string.tv_talk_fail_illegal_format_tip);
                    stopSpeak();
                }

                toast(R.string.tv_talk_success_tip);
                //startTalk(dev, true);
                return true;
            case Global.MSG_ADD_ALARM_DATA:
                TPS_AlarmInfo ta = (TPS_AlarmInfo) msg.obj;
                //showAlarmIcon(new String(ta.getSzDevId()).trim(), true);
                return true;
            case SDK_CONSTANT.TPS_MSG_NOTIFY_LOGIN_FAILED:
                //onLoginFailed((PlayerDevice) msg.obj);
                return true;
            case LibImpl.MSG_VIDEO_SET_STATUS_INFO:
                onSetStatusInfo(msg);
                return true;
            case SDK_CONSTANT.TPS_MSG_RSP_PTZREQ:
                String data = (String) msg.obj;
                //onPtzReqResp(data);
                return true;
            case SDK_CONSTANT.TPS_MSG_RSP_ADDWATCH:
                TPS_AddWachtRsp tas = (TPS_AddWachtRsp) msg.obj;
                onAddWatchResp(tas);
                return true;
            case NetSDK_CMD_TYPE.CMD_GET_SYSTEM_USER_CONFIG:
                msgObj = (LibImpl.MsgObject) msg.obj;
                List<NetSDK_UserAccount> lst = (List<NetSDK_UserAccount>) msgObj.recvObj;
                //CheckDefaultUserPwd(LibImpl.getInstance().getPlayerDevice(msgObj.devID));
                return false;
            case SDK_CONSTANT.TPS_MSG_NOTIFY_DISP_INFO:
                TPS_NotifyInfo tni = (TPS_NotifyInfo) msg.obj;
                onNotifyDispInfo(tni);
                return true;
            case Define.MSG_RECEIVER_MEDIA_FIRST_FRAME:
                onRecvFirstFrame((PlayerDevice) msg.obj);
                return true;
        }

        return false;
    }

    private void onSetStatusInfo(android.os.Message msg) {
        if (msg.arg1 == 0) {
            LibImpl.MsgObject msgObj = (LibImpl.MsgObject) msg.obj;
            if (null != msgObj.reserveObj) {
                setTipText(msgObj.devID, msgObj.recvObj, (String) msgObj.reserveObj);
            } else {
                setTipText(msgObj.devID, msgObj.recvObj);
            }
        } else if (msg.arg1 == 1) {
            LibImpl.MsgObject msgObj = (LibImpl.MsgObject) msg.obj;
            setVideoInfo2(msgObj.devID, (String)msgObj.recvObj);
        }
    }

    private void onNotifyDispInfo(TPS_NotifyInfo tni) {
        String devId = new String(tni.getSzDevId()).trim();
        String msg = new String(tni.getSzInfo()).trim();
        PlayerDevice dev = LibImpl.findDeviceByID(devId);
        if (null != dev) {
            setVideoInfo2(dev.m_devId, msg);
        } else {
            List<PlayerDevice> lst = Global.getDeviceByName(devId);
            if (null != lst) {
                for (PlayerDevice d : lst) {
                    setVideoInfo2(d.m_devId, msg);
                }
            }
        }
    }

    private void onRecvFirstFrame(PlayerDevice dev) {
        if (null == dev) return;
        setTipText(dev.m_devId, "");
    }

    private void onAddWatchResp(TPS_AddWachtRsp ts) {
        final String devId = new String(ts.getSzDevId()).trim();
        int result = ts.getnResult();
        if (result == 0) {//视频请求成功
            PlayerDevice dev = LibImpl.findDeviceByID(devId);
            if (dev.m_online && !dev.m_play) {
                setTipText(devId, R.string.tv_video_req_succeed_tip);
            }
        } else {
            /*if (-1 == result) {
                setTipText(devId, R.string.dlg_login_fail_user_pwd_incorrect_tip);
            } else*/ if (-2 == result) {
                setTipText(devId, R.string.err_illegal_channel_id);
            } else if (-3 == result) {
                setTipText(devId, R.string.err_illegal_stream_id);
            } else if (-4 == result) {
                setTipText(devId, R.string.err_illegal_audio_enable);
            } else if (-7 == ts.getnResult()) {
                setTipText(devId, R.string.err_all_stream_full);
            } else if (-8 == ts.getnResult()) {
                setTipText(devId, R.string.err_session_stream_full);
            } else if (-9 == ts.getnResult()) {
                setTipText(devId, R.string.err_get_video_cfg_fail);
            } else if (-10 == ts.getnResult()) {
                setTipText(devId, R.string.err_get_stream_fail);
            } else {
                setTipText(devId, R.string.tv_video_req_fail_tip, ts.getnResult() + "");
            }
        }
    }

    public void setTipText(String devID, Object msg) {
        setTipText(devID, msg, "");
    }

    public void setTipText(String devID, Object msg, String reserver) {
        String _msg = T(msg);
        if (!TextUtils.isEmpty(reserver)) _msg += "(" + reserver + ")";
        setVideoInfo(devID, _msg);
    }
}
