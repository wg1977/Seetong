package com.seetong.app.seetong.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;

import com.seetong.app.seetong.R;

/**
 * Created by gmk on 2015/9/23.
 */
public class GalleryActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    public static GalleryActivity m_this = null;
    public int position = 0;
    private ViewPager viewPager;
    private GestureDetector gestureDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        m_this = this;
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_gallery);
        gestureDetector = new GestureDetector(this, new MyOnGestureListener());
        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
        viewPager.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        if (i == 0) {
            toast(R.string.media_first_pic);
        } else if (i == PictureFragment.mGridList.size() - 1) {
            toast(R.string.media_last_pic);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    class ViewPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            String imagePath = PictureFragment.mGridList.get(position).getPath();
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            View view = LayoutInflater.from(GalleryActivity.this).inflate(R.layout.zoom_image_layout, null);
            ZoomImageView zoomImageView = (ZoomImageView) view.findViewById(R.id.zoom_image_view);
            zoomImageView.setTag(position);
            zoomImageView.setImageBitmap(bitmap);
            zoomImageView.setOnTouchListener(new TouchListener());
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return PictureFragment.mGridList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    class TouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return gestureDetector.onTouchEvent(motionEvent);
        }
    }

    class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            GalleryActivity.this.finish();
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            ZoomImageView zoomImageView = (ZoomImageView) viewPager.findViewWithTag(viewPager.getCurrentItem());
            zoomImageView.setViewResize();
            return super.onDoubleTap(e);
        }
    }
}
