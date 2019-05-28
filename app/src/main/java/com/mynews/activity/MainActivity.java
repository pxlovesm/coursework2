package com.mynews.activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mynews.R;
import com.mynews.dao.VersionDao;
import com.mynews.model.News;
import com.mynews.utils.DensityUtil;
import com.mynews.utils.GetPinyin;
import com.mynews.utils.PicassoUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private String CURRENTCATEGORY = "toutiao";  //点击的类别的拼音
    private int COUNT = 1;  //分页的记录值
    private List<News> newsList = new ArrayList<>();  //新闻的集合
    private ListView mainBodyList;   //列表listView
    private View mainBodyListBottom;  //listView底部
    private TextView mainBodyListBottomTV;  //listView底部的TextView
    private ProgressBar mainBodyListBottomPB;  //listView底部的进度条
    private NewsAdapter newsAdapter;  //新闻适配器
    private Map<String, String> map;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView mainHandRefrech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainBodyList = findViewById(R.id.main_body_list);
        mainHandRefrech = findViewById(R.id.main_hand_refresh);  //头部的刷新图片

        //<-----------------------------------设置点击按钮滚动标题栏---------------------------------------
        final HorizontalScrollView category_scrollview = findViewById(R.id.main_category_scroll);
        Button main_category_button = findViewById(R.id.main_category_button);
        main_category_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                category_scrollview.fling(DensityUtil.px2dip(MainActivity.this, 1500)); //滚动方法
            }
        });
        //--------------------------------------------------------------------------------------->

        //-----------------------------------设置点击按钮滚动标题栏--------------------------------------->
        int columnWidth = 400;  //分类间隔距离 px
        String[] categoryArray = getResources().getStringArray(R.array.main_category_item);  //分类信息
        List<Map<String, String>> categorys = new ArrayList<>();
        for (int i = 0; i < categoryArray.length; i++) {
            Map<String, String> map = new HashMap<>();
            map.put("main_category_item", categoryArray[i]);
            categorys.add(map);
        }
        SimpleAdapter categoryAdapter = new SimpleAdapter(this, categorys, R.layout.main_category_item, new String[]{"main_category_item"}, new int[]{R.id.main_category_item});
        //创建网格视图
        GridView gridView = new GridView(this);
        gridView.setNumColumns(GridView.AUTO_FIT);  //列数设置为自动
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));  //设置选中颜色为透明色
        gridView.setColumnWidth(DensityUtil.px2dip(this, columnWidth));  //设置列宽，DensityUtil.px2dip(）转换px为dip的工具类方法
        gridView.setGravity(Gravity.CENTER);  //设置对齐方式
        int dipColumnWidth = DensityUtil.px2dip(this, columnWidth) * categorys.size();
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(dipColumnWidth, ActionBar.LayoutParams.WRAP_CONTENT);
        gridView.setLayoutParams(params);
        gridView.setAdapter(categoryAdapter);
        LinearLayout categoryLinearlayout = findViewById(R.id.main_category_linear);
        categoryLinearlayout.addView(gridView);
        //<---------------------------------------------------------------------------------------

        //-----------------------------------item的点击事件监听--------------------------------------->
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView categoryTitle;
                //这个循环主要作用清空所有item的背景，并设置初始颜色
                for (int j = 0; j < adapterView.getCount(); j++) {
                    ((TextView) adapterView.getChildAt(j)).setTextColor(getResources().getColor(R.color.color_ADB2AD));
                    adapterView.getChildAt(j).setBackgroundDrawable(null);
                }
                CURRENTCATEGORY = GetPinyin.getPingYin(((TextView) view).getText().toString());  //设置点击后的类别拼音，此处的工具类需要导包 pinyin4j-2.5.0.jar
                COUNT = 1; //分页的记录值
                categoryTitle = (TextView) view;
                categoryTitle.setTextColor(getResources().getColor(R.color.color_FFFFFF));
                categoryTitle.setBackgroundResource(R.drawable.main_category_item_selector);

                newsList = new ArrayList<>();  //清空新闻list
                mainBodyList.removeFooterView(mainBodyListBottom);  //清除底部view
                new NewsTask().execute(getResources().getString(R.string.issue) + "news?type=" + CURRENTCATEGORY);  //异步请求数据 R.string.issue=http://10.0.2.2:8080/news/api/

            }
        });
        //<---------------------------------------------------------------------------------------

        //-----------------------------------listVIew的拖动监听事件监听--------------------------------------->
        mainBodyList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {  //停止滑动状态
                    if (view.getLastVisiblePosition() == (view.getCount() - 1)) {   //滑动到listView的最后
                        mainBodyListBottomTV.setVisibility(View.GONE);  //隐藏
                        mainBodyListBottomPB.setVisibility(View.VISIBLE);  //显示
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    addData(getResources().getString(R.string.issue) + "news?type=" + CURRENTCATEGORY, COUNT);
                                    handler.sendEmptyMessage(2);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        //<---------------------------------------------------------------------------------------

        //-----------------------------------listView的item点击事件--------------------------------------->
        mainBodyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, NewsDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("list", (Serializable) newsList);//序列化,要注意转化(Serializable)
                intent.putExtra("list", bundle);
                intent.putExtra("i", i);
                startActivity(intent);
            }
        });
        //<---------------------------------------------------------------------------------------

        //--------------------------请求新版本------------------------------------------>
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = getResources().getString(R.string.issue) + "findVersion";
                    findVersion(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        //<----------------------------------------------------------------------------------

        //------------------------------设置下拉刷新界面数据------------------------------------>
        swipeRefreshLayout = findViewById(R.id.main_body_swipe);
        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // 设置下拉进度的主题颜色
        swipeRefreshLayout.setColorSchemeResources(R.color.color_FE0000, R.color.color_FFFF00, R.color.color_0166FF);

        // 下拉时触发SwipeRefreshLayout的下拉动画，动画完毕之后就会回调这个方法
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 开始刷新，设置当前为刷新状态
                swipeRefreshLayout.setRefreshing(true);

                newsList = new ArrayList<>();
                mainBodyList.removeFooterView(mainBodyListBottom);
                new NewsTask().execute(getResources().getString(R.string.issue) + "news?type=" + CURRENTCATEGORY);
                Toast.makeText(MainActivity.this, "更新成功", Toast.LENGTH_SHORT).show();

                // 加载完数据设置为不刷新状态，将下拉进度收起来
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        //<----------------------------------------------------------------------------------

        new NewsTask().execute(getResources().getString(R.string.issue) + "news?type=" + CURRENTCATEGORY);  //初始化加载

    }

    class NewsTask extends AsyncTask<String, Void, List<News>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<News> doInBackground(String... strings) {
            //使用httpclient需要在app目录下的build.gradle文件中的Android里面加上useLibrary 'org.apache.http.legacy' /*加载 HttpClient*/
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(strings[0]);
            try {
                HttpResponse httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String json = EntityUtils.toString(entity, "utf-8");
                    JSONArray jsonArray = new JSONObject(json).getJSONArray("pdList");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject element = jsonArray.getJSONObject(i);
                        News news = new News();
                        news.setNEWSDETAIL_ID(element.getString("NEWSDETAIL_ID"));
                        news.setUNIQUEKEY(element.getString("UNIQUEKEY"));
                        news.setTITLE(element.getString("TITLE"));
                        news.setURL(element.getString("URL"));
                        news.setDATE(element.getString("DATE"));
                        news.setPIC1(element.getString("PIC1"));
                        news.setCATEGORY(element.getString("CATEGORY"));
                        news.setCOMMENT(element.getString("COMMENT"));
                        newsList.add(news);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return newsList;
        }

        @Override
        protected void onPostExecute(List<News> newsList) {
            super.onPostExecute(newsList);
            mainBodyListBottom = getLayoutInflater().inflate(R.layout.main_body_list_bottom, null);
            mainBodyListBottomTV = mainBodyListBottom.findViewById(R.id.main_body_list_bottom_tv);
            mainBodyListBottomPB = mainBodyListBottom.findViewById(R.id.main_body_list_bottom_pb);
            if (newsList.size() == 0) {
                mainBodyListBottomTV.setText("暂无新闻");
            }
            mainBodyList.addFooterView(mainBodyListBottom);
            mainBodyList.setAdapter(newsAdapter = new NewsAdapter(newsList));

            handler.sendEmptyMessage(1);
        }
    }

    class NewsAdapter extends BaseAdapter {
        public List<News> data;

        public NewsAdapter(List<News> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
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
            NewsList newsList;
            if (view == null) {
                newsList = new NewsList();
                view = getLayoutInflater().inflate(R.layout.main_body_list_item, viewGroup, false);
                newsList.imageView = view.findViewById(R.id.imageView);
                newsList.textView1 = view.findViewById(R.id.tv_title);
                newsList.textView2 = view.findViewById(R.id.tv_below);
                view.setTag(newsList);
            } else {
                newsList = (NewsList) view.getTag();
            }
            newsList.textView1.setText(data.get(i).getTITLE());
            newsList.textView2.setText(data.get(i).getDATE());
            PicassoUtil.loadImageWithHodler(MainActivity.this, data.get(i).getPIC1(), R.drawable.loading, newsList.imageView);  //使用picasso加载图片资源
            return view;
        }
    }

    class NewsList {
        ImageView imageView;
        TextView textView1;
        TextView textView2;
    }

    /**
     * * 加载分页数据
     *
     * @param count 第几页
     * @param path  地址
     */
    private void addData(String path, int count) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(path + "&count=" + count);
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = httpResponse.getEntity();
                String json = EntityUtils.toString(entity, "utf-8");
                JSONArray jsonArray = new JSONObject(json).getJSONArray("pdList");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject element = jsonArray.getJSONObject(i);
                    News news = new News();
                    news.setNEWSDETAIL_ID(element.getString("NEWSDETAIL_ID"));
                    news.setUNIQUEKEY(element.getString("UNIQUEKEY"));
                    news.setTITLE(element.getString("TITLE"));
                    news.setURL(element.getString("URL"));
                    news.setDATE(element.getString("DATE"));
                    news.setPIC1(element.getString("PIC1"));
                    news.setCATEGORY(element.getString("CATEGORY"));
                    news.setCOMMENT(element.getString("COMMENT"));
                    newsList.add(news);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //点击顶部刷新按钮时加载动画
    public void refreshClick(View v) {
        //显示刷新动画
        Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.refresh);
        //设置重复模式  Defines what this animation should do when it reaches the end
        animation.setRepeatMode(Animation.RESTART);
        //设置重复次数
        animation.setRepeatCount(Animation.INFINITE);
        //使用ImageView 显示旋转动画
        mainHandRefrech.startAnimation(animation);

        COUNT = 1;
        newsList = new ArrayList<>();
        mainBodyList.removeFooterView(mainBodyListBottom);
        new NewsTask().execute(getResources().getString(R.string.issue) + "news?type=" + CURRENTCATEGORY);
        handler.sendEmptyMessage(3);

    }

    /**
     * 查询版本信息
     *
     * @param path
     */
    private void findVersion(String path) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(path);
        HttpResponse httpResponse;
        try {
            httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity, "utf-8");
                JSONObject jsonObject = new JSONObject(result).getJSONObject("pd");
                map = new HashMap<>();
                map.put("VERSION_ID", jsonObject.getString("VERSION_ID"));
                map.put("NUMBER", jsonObject.getString("NUMBER"));
                map.put("CONTENT", jsonObject.getString("CONTENT"));
                map.put("DATE", jsonObject.getString("DATE"));
                map.put("STATUS", jsonObject.getString("STATUS"));
                map.put("PATH", jsonObject.getString("PATH"));
                handler.sendEmptyMessage(4);  //数据请求加载完毕发送消息让handler处理
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {

            } else if (msg.what == 2) {  //数据加载完毕
                mainBodyListBottomPB.setVisibility(View.GONE);  //隐藏进度条
                mainBodyListBottomTV.setVisibility(View.VISIBLE);  //显示底部textView
                newsAdapter.notifyDataSetChanged();//刷新listview
                COUNT++;
            } else if (msg.what == 3) {
                mainHandRefrech.clearAnimation();//停止动画
            } else if (msg.what == 4) {  //版本更新的逻辑
                VersionDao versionDao = new VersionDao(MainActivity.this, map);
                versionDao.checkUpdate();  //更新操作
            }
        }
    };
}
