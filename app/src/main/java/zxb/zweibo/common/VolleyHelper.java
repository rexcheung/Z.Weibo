package zxb.zweibo.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import zxb.zweibo.bean.PicUrls;

/**
 * Created by rex on 15-8-4.
 */
public class VolleyHelper {
    private static RequestQueue mQueue = null;

    private Context mContext;

    private String TAG;

    public VolleyHelper(Context context){
        this.mContext = context;
        if(mContext!=null){
            this.mQueue = Volley.newRequestQueue(mContext);
        }

        TAG = getClass().getSimpleName();
    }

    private LruCache<String, Bitmap> mMemoryCache;
    public void loadImg(Context context, final ImageView imgView, final String picUrl) {
        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(context);
        }

        if(mMemoryCache == null){
            // 系统分配给APP的最大内存
            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            Log.i(TAG, "Maxmemory = "+maxMemory+" KB");
            // 使用最大可用内存值的1/8作为缓存的大小。
            int cacheSize = maxMemory / 20;
            Log.i(TAG, "CacheSize = "+cacheSize+" KB");
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // 重写此方法来衡量每张图片的大小，默认返回图片数量。
                    return bitmap.getByteCount() / 1024;
                }
            };
        }

        Bitmap cache = getBitmapFromMemCache(picUrl);
        if (cache == null) {
            imageRequest(imgView, picUrl);
        } else if (!cache.isRecycled()){
            imgView.setImageBitmap(cache);
        } else {
            imageRequest(imgView, picUrl);
        }

    }

    private void imageRequest(final ImageView imgView, final String picUrl) {
        ImageRequest imageRequest = new ImageRequest(
                picUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        imgView.setImageBitmap(response);
                        addBitmapToMemoryCache(picUrl, response);
//                            Log.i("VolleyHelper", "response = " + response.toString());
                    }
                }, 0, 0, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565,

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //...
                    }
                }
        );

        mQueue.add(imageRequest);
    }

    public void loadMultiImg(Context context, List<ImageView> imgList, PicUrls[] urlList) {
        for (int i = 0; i < urlList.length; i++) {
            imgList.get(i).setVisibility(View.VISIBLE);
            loadImg(context, imgList.get(i), urlList[i].getThumbnail_pic());
        }
    }

    public void clearCache(){
        if(mMemoryCache != null){
            Log.i(TAG, "清除前 MemoryCacheSize = "+mMemoryCache.size());

            mMemoryCache.evictAll();  //可以尝试这个方法释放所有缓存文件
            Log.i(TAG, "清除后 MemoryCacheSize = " + mMemoryCache.size());
        }
        mMemoryCache = null;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
}
