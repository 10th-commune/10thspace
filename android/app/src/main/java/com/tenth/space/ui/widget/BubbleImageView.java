package com.tenth.space.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.tenth.space.R;
import com.tenth.space.app.IMApplication;
import com.tenth.space.utils.ImageLoaderUtil;
import com.tenth.space.utils.Logger;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * Created by zhujian on 15/2/13.
 */
public class BubbleImageView extends ImageView {
    /**
     * 图片设置相关
     */
    protected String imageUrl = null;
    protected boolean isAttachedOnWindow = false;
    protected int defaultImageRes = R.drawable.tt_message_image_default;

    protected ImageLoaddingCallback imageLoaddingCallback;

    private Logger logger = Logger.getLogger(BubbleImageView.class);

    public BubbleImageView(Context context) {
        super(context);
    }

    public BubbleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BubbleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* 图片设置相关 */

    public void setImageLoaddingCallback(ImageLoaddingCallback callback) {
        this.imageLoaddingCallback = callback;
    }

    public void setImageUrl(final String url) {
        this.imageUrl = url;
        if (isAttachedOnWindow) {
            final BubbleImageView view = this;
            if (!TextUtils.isEmpty(this.imageUrl)) {
                ImageAware imageAware = new ImageViewAware(this, false);
                //logger.i("neil 11" + this.imageUrl);
                ImageLoaderUtil.instance().displayImage(IMApplication.app.UrlFormat(this.imageUrl), imageAware, new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .showImageOnLoading(R.drawable.tt_message_image_default)
                        .showImageOnFail(R.drawable.tt_message_image_error)
                        .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                        .bitmapConfig(Bitmap.Config.RGB_565)
                        .delayBeforeLoading(100)
                        .build(), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);

                        if (imageLoaddingCallback != null) {
                            String cachePath = ImageLoaderUtil.instance().getDiskCache().get(imageUri).getPath();//这个路径其实已不再更新
                            imageLoaddingCallback.onLoadingComplete(cachePath, view, loadedImage);
                        }
                    }

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        super.onLoadingStarted(imageUri, view);
                        if (imageLoaddingCallback != null) {
                            imageLoaddingCallback.onLoadingStarted(imageUri, view);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        super.onLoadingCancelled(imageUri, view);
                        if (imageLoaddingCallback != null) {
                            imageLoaddingCallback.onLoadingCanceled(imageUri, view);
                        }
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        super.onLoadingFailed(imageUri, view, failReason);
                        if (imageLoaddingCallback != null) {
                            imageLoaddingCallback.onLoadingFailed(imageUri, view);
                        }
                    }
                });
            }
        } else {
            this.setImageResource(R.drawable.tt_message_image_default);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedOnWindow = true;
        setImageUrl(this.imageUrl);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.isAttachedOnWindow = false;
        ImageLoaderUtil.instance().cancelDisplayTask(this);
    }

    public interface ImageLoaddingCallback {
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage);

        public void onLoadingStarted(String imageUri, View view);

        public void onLoadingCanceled(String imageUri, View view);

        public void onLoadingFailed(String imageUri, View view);
    }


}
