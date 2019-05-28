package com.mynews.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.mynews.R;
import com.mynews.dao.CollectDao;
import com.mynews.model.Collect;
import com.mynews.model.News;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.List;

/**
 * Created by Administrator on 2017/12/28.
 */

public class NewsDetailActivity extends Activity implements View.OnClickListener{

    private String TAG = NewsDetailActivity.class.getSimpleName();
    private Intent intent;  //意图对象
    private int COUNT = 0;  //记录页数
    private List<News> newsList;  //传递过来的list集合
    private TextView tvCategory;  //头部显示新闻类别的文本控件
    private Button btnComment;    //头部的跟帖按钮
    private ViewFlipper viewFlipper;  //view容器
    private LayoutInflater layoutInflater;
    private ProgressBar newsDetailBodyPB;  //主体部分的进度条
    private int mStartX = 0;

    private Button newsDetailHandRelativeBtnLeft; //上一篇按钮
    private Button newsDetailHandRelativeBtnRight;  //下一篇按钮
    private ImageButton newsDetailBottomLl02IbWrite;  //图片按钮
    private Button newsDetailBottomLl01BtnSend;  //发送跟帖按钮
    private Button newsDetailBottomLl02BtnCollect;  //收藏按钮

    private long downTime;  //收藏按钮按下的时间
    private long upTime;  //收藏按钮按下的记录时间
    private volatile boolean onBtnTouch = false;  //是否按下按钮

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_detail);

        //-----------------------------------获取MainActivity传递的参数------------------------------------->
        intent = getIntent();  //获取意图对象
        Bundle bundle = intent.getBundleExtra("list");  //获取新闻集合
        newsList = (List<News>) bundle.getSerializable("list");  //新闻集合
        COUNT = intent.getIntExtra("i", 0);  //获取MainActivity点击的item的位置

        //设置头部的新闻类别
        tvCategory = findViewById(R.id.news_detail_hand_relative_tv_category);
        tvCategory.setText(newsList.get(COUNT).getCATEGORY());

        //设置头部的跟帖数
        btnComment = findViewById(R.id.news_detail_hand_relative_btn_comment);
        btnComment.setText(newsList.get(COUNT).getCOMMENT() + "跟帖");

        //设置显示新闻主体
        viewFlipper = findViewById(R.id.news_detail_body_flipper);
        layoutInflater = getLayoutInflater();

        //使用打气筒构造内容主体body
        View view = layoutInflater.inflate(R.layout.news_detail_body, null);

        //设置进度条按钮显示
        newsDetailBodyPB = view.findViewById(R.id.news_detail_body_pb);
        newsDetailBodyPB.setVisibility(View.VISIBLE);

        //viewFlipper.removeAllViews();
        for (int i = 0; i < newsList.size(); i++) {
            View inflate = layoutInflater.inflate(R.layout.news_detail_body, null);
            WebView newsDetailBodyWV = inflate.findViewById(R.id.news_detail_body_wv);
            newsDetailBodyWV.loadUrl(newsList.get(i).getURL());  //设置webView加载URL内容
            viewFlipper.addView(inflate, i);  //把构造的主体内容加入到viewFlipper容器中

            //绑定主体的触摸监听事件
            newsDetailBodyWV.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:  //手指按下触发
                            //按下时的横坐标
                            mStartX = (int) motionEvent.getX();
                            break;
                        case MotionEvent.ACTION_UP:  //手指抬起触发
                            //像左滑动
                            if (motionEvent.getX() < mStartX) {
                                nextView();
                            } else if (motionEvent.getX() > mStartX) {  //向右滑动
                                previousView();
                            }
                            break;
                    }
                    return true;
                }
            });
        }
        viewFlipper.setDisplayedChild(COUNT);  //设置第几个view显示
        newsDetailBodyPB.setVisibility(View.GONE);  //进度条隐藏
        //<--------------------------------------------------------------------------------------------------

        //---------------------------------------------设置点击事件---------------------------------------------------->
        newsDetailHandRelativeBtnLeft = findViewById(R.id.news_detail_hand_relative_btn_left);//上一页
        newsDetailHandRelativeBtnRight = findViewById(R.id.news_detail_hand_relative_btn_right);//下一页
        newsDetailBottomLl02IbWrite = findViewById(R.id.news_detail_bottom_ll_02_ib_write);//写跟帖
        newsDetailBottomLl01BtnSend = findViewById(R.id.news_detail_bottom_ll_01_btn_send);//发表
        newsDetailHandRelativeBtnLeft.setOnClickListener(this);
        newsDetailHandRelativeBtnRight.setOnClickListener(this);
        newsDetailBottomLl02IbWrite.setOnClickListener(this);
        newsDetailBottomLl01BtnSend.setOnClickListener(this);
        //------------------------------------------------------------------------------------------------->

        //---------------------------------------------设置收藏点击事件---------------------------------------------------->
        newsDetailBottomLl02BtnCollect = findViewById(R.id.news_detail_bottom_ll_02_btn_collect); //收藏 按钮是否被按下
        newsDetailBottomLl02BtnCollect.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    downTime = System.currentTimeMillis();
                    onBtnTouch = true;
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            while (onBtnTouch) {
                                upTime = System.currentTimeMillis();
                                try {
                                    Thread.sleep(100);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    t.start();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    onBtnTouch = false;
                    if (upTime - downTime > 500) {
                        handler.sendEmptyMessage(3);  //按下按钮超过0.5秒
                    } else {
                        handler.sendEmptyMessage(2);
                    }
                } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {  //手指按下移开时触发事件
                    onBtnTouch = false;
                }

                return true;

            }
        });
        //------------------------------------------------------------------------------------------------->

        //---------------------------------------------设置收藏点击事件---------------------------------------------------->
        //点击跳转到跟帖
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(NewsDetailActivity.this, CommentActivity.class);
                intent1.putExtra("newsdetailId", newsList.get(COUNT).getNEWSDETAIL_ID());
                intent1.putExtra("comment", newsList.get(COUNT).getCOMMENT());
                startActivity(intent1);
            }
        });
        //------------------------------------------------------------------------------------------------->


    }

    @Override
    public void onClick(View view) {
        //点击的是上一页
        if (view.getId() == newsDetailHandRelativeBtnLeft.getId()) {
            previousView();
        } else if (view.getId() == newsDetailHandRelativeBtnRight.getId()) {  //点击的是下一页
            nextView();
        } else if (view.getId() == newsDetailBottomLl02IbWrite.getId()) {  //点击的是写跟贴
            LinearLayout ll01 = findViewById(R.id.news_detail_bottom_ll_01);
            ll01.setVisibility(View.VISIBLE);
            LinearLayout ll02 = findViewById(R.id.news_detail_bottom_ll_02);
            ll02.setVisibility(View.GONE);
        } else if (view.getId() == newsDetailBottomLl01BtnSend.getId()) {  //点击的是发表

            final EditText etComment = findViewById(R.id.news_detail_bottom_ll_01_et_comment);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        StringBuffer sb = new StringBuffer();
                        sb.append("content=");
                        sb.append(URLEncoder.encode(etComment.getText().toString().trim(), "UTF-8"));
                        sb.append("&newsdetailId=");
                        sb.append(newsList.get(COUNT).getNEWSDETAIL_ID());
                        String path = getResources().getString(R.string.issue) + "addComment?" + sb.toString();
                        addComment(path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            Toast.makeText(this, "保存成功！", Toast.LENGTH_SHORT).show();

            LinearLayout ll01 = findViewById(R.id.news_detail_bottom_ll_01);
            ll01.setVisibility(View.GONE);
            LinearLayout ll02 = findViewById(R.id.news_detail_bottom_ll_02);
            ll02.setVisibility(View.VISIBLE);
        }
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
                // String status = new JSONObject(result).getString("message");
                // String message = new JSONObject(result).getString("status");
                String count = new JSONObject(result).getString("count");

                Message message1 = Message.obtain();
                message1.obj = count;
                message1.what = 1;
                handler.sendMessage(message1);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //点击的是下一页
    private void nextView() {
        COUNT++;
        Log.i(TAG, "nextView: count-->" + COUNT);
        if (COUNT <= newsList.size() - 1) {
            showNext();
        } else {
            COUNT = newsList.size() - 1;
            Toast.makeText(this, "已经是最后一条啦！", Toast.LENGTH_SHORT).show();
        }


    }

    //上一页
    private void previousView() {
        COUNT--;
        Log.i(TAG, "previousView: count-->" + COUNT);
        if (COUNT >= 0) {
            showPrevious();
        } else {
            COUNT = 0;
            Toast.makeText(this, "已经是第一条啦！", Toast.LENGTH_SHORT).show();
        }
    }

    //上一条
    private void showPrevious() {
        btnComment.setText(newsList.get(COUNT).getCOMMENT() + "跟帖");  //设置每条新闻对应的跟帖数量
        //设置动画效果
        viewFlipper.setInAnimation(NewsDetailActivity.this, R.anim.push_right_in);
        viewFlipper.setOutAnimation(NewsDetailActivity.this, R.anim.push_right_out);
        viewFlipper.showPrevious();  //调用viewFlipper的上一条方法
    }

    //下一条
    private void showNext() {
        btnComment.setText(newsList.get(COUNT).getCOMMENT() + "跟帖"); //设置每条新闻对应的跟帖数量
        //设置动画效果
        viewFlipper.setInAnimation(NewsDetailActivity.this, R.anim.push_left_in);
        viewFlipper.setOutAnimation(NewsDetailActivity.this, R.anim.push_left_out);
        viewFlipper.showNext();//调用viewFlipper的下一条方法
    }


    /**
     * 创建一个列表弹窗
     */
    private void createListDialog(final List<Collect> collects) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("收藏列表：");
        builder.setAdapter(new CollectAdapter(collects), new DialogInterface.OnClickListener() {
            /**
             *
             * @param dialogInterface 当前的对话框
             * @param i 当前点击的是列表的第几个 item
             */
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(NewsDetailActivity.this, CollectNewsDetailActivity.class);
                intent.putExtra("newsdetailId", collects.get(i).getNewsdetailId());
                startActivity(intent);
            }
        });
        builder.setCancelable(true);//允许被某些方式取消,比如按对话框之外的区域或者是返回键
        builder.show();
    }

    class CollectAdapter extends BaseAdapter {
        private List<Collect> collects;
        public CollectAdapter(List<Collect> collects) {
            this.collects = collects;
        }
        @Override
        public int getCount() {
            return collects.size();
        }
        @Override
        public Object getItem(int i) {
            return i;
        }
        @Override
        public long getItemId(int i) {
            return 0;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            CollectDialog collectDialog;
            if (view == null) {
                collectDialog = new CollectDialog();
                view = getLayoutInflater().inflate(R.layout.news_detail_collect_dialog, viewGroup, false);
                collectDialog.tv1 = view.findViewById(R.id.btn_collect_dialog_title);
                collectDialog.tv2 = view.findViewById(R.id.btn_collect_dialog_date);
                view.setTag(collectDialog);
            } else {
                collectDialog = (CollectDialog) view.getTag();
            }
            collectDialog.tv1.setText(collects.get(i).getTitle());
            collectDialog.tv2.setText(collects.get(i).getDate());
            return view;
        }
    }

    class CollectDialog {
        TextView tv1;
        TextView tv2;
    }

    private Handler handler = new Handler(){
        CollectDao collectDao = new CollectDao(NewsDetailActivity.this);

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                String count = (String) msg.obj;
                btnComment.setText(count + "跟帖");
            } else if (msg.what == 2) {
                List<Collect> collects = collectDao.queryBuilder1(newsList.get(COUNT).getNEWSDETAIL_ID());
                if (collects.size() <= 0) {
                    Collect collect = new Collect();
                    collect.setNewsdetailId(newsList.get(COUNT).getNEWSDETAIL_ID());
                    collect.setTitle(newsList.get(COUNT).getTITLE());
                    collect.setDate(newsList.get(COUNT).getDATE());
                    collectDao.addCollect(collect);
                    Toast.makeText(NewsDetailActivity.this, "收藏成功，长按可查看收藏哦！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NewsDetailActivity.this, "已收藏该文章，长按可查看收藏哦！", Toast.LENGTH_SHORT).show();
                }
            } else if (msg.what == 3) {
                createListDialog(collectDao.listAll());
            }
        }
    };
}
