package com.mynews.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mynews.R;
import com.mynews.model.News;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/12/28.
 */

public class CollectNewsDetailActivity extends Activity {

    private String TAG = CollectNewsDetailActivity.class.getSimpleName();
    private News news;
    private WebView collectNewsDetailLlWV;
    private ProgressBar collectNewsDetailLlPB;
    private TextView collectNewsDetailRlTvCategory;
    private Button collectNewsDetailRlBtnComment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collect_news_detail);
        collectNewsDetailLlWV = findViewById(R.id.collect_news_detail_ll_wv);
        collectNewsDetailLlPB = findViewById(R.id.collect_news_detail_ll_pb);
        collectNewsDetailRlTvCategory = findViewById(R.id.collect_news_detail_rl_tv_category);
        collectNewsDetailRlBtnComment = findViewById(R.id.collect_news_detail_rl_btn_comment);

        //-------------------------------加载所有跟帖详情-------------------------------->
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = getResources().getString(R.string.issue) + "findNewsById?newsdetailId=" + getIntent().getStringExtra("newsdetailId");
                    addComment(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        //<------------------------------------------------------------------------------------

        //-------------------------------WebView加载事件-------------------------------->
        collectNewsDetailLlWV.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //Log.i(TAG, "onPageStarted: 页面开始加载");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //Log.i(TAG, "onPageFinished: 页面加载完成");
                collectNewsDetailLlPB.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                //加载出现失败
                super.onReceivedError(view, errorCode, description, failingUrl);
                //Log.i(TAG, "onReceivedError: 加载失败 errorCode:" + errorCode);
            }
        });
        collectNewsDetailLlWV.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int progress) {
                //Log.i(TAG, "onProgressChanged: 正在加载中 progress:" + progress);
                //加载过程回调，progress是接受到的数据的百分比
            }

        });
        //<------------------------------------------------------------------------------------


        //-------------------------------点击跳转到跟帖-------------------------------->
        collectNewsDetailRlBtnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(CollectNewsDetailActivity.this, CommentActivity.class);
                //Log.i(TAG, "点击跳转到跟帖: newsdetailId-->" + news.getNEWSDETAIL_ID());
                intent1.putExtra("newsdetailId", news.getNEWSDETAIL_ID());
                intent1.putExtra("comment", news.getCOMMENT());
                startActivity(intent1);
            }
        });
        //<------------------------------------------------------------------------------------

    }

    /**
     * 添加跟帖
     *
     * @param path 路径
     */
    private void addComment(String path) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(path);
        HttpResponse httpResponse;
        try {
            httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity, "utf-8");
                JSONObject jsonObject = new JSONObject(result).getJSONObject("pd");

                News news = new News();
                news.setNEWSDETAIL_ID(jsonObject.getString("NEWSDETAIL_ID"));
                news.setUNIQUEKEY(jsonObject.getString("UNIQUEKEY"));
                news.setTITLE(jsonObject.getString("TITLE"));
                news.setURL(jsonObject.getString("URL"));
                news.setDATE(jsonObject.getString("DATE"));
                news.setPIC1(jsonObject.getString("PIC1"));
                news.setCATEGORY(jsonObject.getString("CATEGORY"));
                news.setCOMMENT(jsonObject.getString("COMMENT"));

                Message message1 = Message.obtain();
                message1.obj = news;
                message1.what = 1;
                handler.sendMessage(message1);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                news = (News) msg.obj;
                collectNewsDetailLlWV.loadUrl(news.getURL());
                collectNewsDetailRlTvCategory.setText(news.getCATEGORY());
                collectNewsDetailRlBtnComment.setText(news.getCOMMENT() + "跟帖");
            }
        }
    };
}
