package com.mynews.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

/**
 * Created by Administrator on 2017/12/18.
 */

public class PicassoUtil {

    /**
     * 根据图片大小加载图片
     *
     * @param context   上下文
     * @param path      图片路径
     * @param width     宽
     * @param height    高
     * @param imageView 图片控件
     */
    public static void loadInageWithSize(Context context, String path, int width, int height, ImageView imageView) {
        Picasso.with(context).load(path).resize(width, height).centerCrop().into(imageView);
    }

    /**
     * 加载默认图片
     *
     * @param context   上下文
     * @param path      路径
     * @param resID     备用图片资源
     * @param imageView 图片控件
     */
    public static void loadImageWithHodler(Context context, String path, int resID, ImageView imageView) {
        Picasso.with(context).load(path).fit().placeholder(resID).into(imageView);
    }

    /**
     * 实现自定义的图片裁剪
     *
     * @param context   上下文
     * @param path      图片路径
     * @param imageView 图片控件
     */
    public static void loadImageWithCrop(Context context, String path, ImageView imageView) {
        // .transform()方法需要的参数类型是Transformation这个接口，
        Picasso.with(context).load(path).transform(new CropSquareTransformation()).into(imageView);
    }

    /**
     * 重写Transformation接口的transform方法
     */
    public static class CropSquareTransformation implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int min = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - min) / 2;
            int y = (source.getHeight() - min) / 2;
            Bitmap bitmap = Bitmap.createBitmap(source, x, y, min, min);
            if (bitmap != null) {
                source.recycle();  //回收资源
            }
            return bitmap;
        }

        @Override
        public String key() {
            return "square()";
        }
    }

}

