package com.mynews.activity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mynews.R;
import com.mynews.model.Comment;
import com.mynews.utils.DateUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/28.
 */

public class CommentActivity extends Activity {

    private EditText commentListBottomET;  //底部的输入框控件
    private Button commentListBottomBTN;  //底部的发表按钮
    private List<Comment> commentList;  //跟帖集合
    private CommentAdapter commentAdapter;  //跟帖的适配器
    private ListView commentListView;  //跟帖的listView
    private TextView commentListHandTV;
    private Button commentListHandBTN;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_list);

        commentListHandTV = findViewById(R.id.comment_list_hand_TV);
        commentListView = findViewById(R.id.comment_list_body);

        //-----------------------------------原文按钮点击事件----------------------------------------->
        commentListHandBTN = findViewById(R.id.comment_list_hand_BTN);
        commentListHandBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //<-------------------------------------------------------------------------------------

        //-----------------------------------发表按钮点击事件----------------------------------------->
        commentListBottomBTN = findViewById(R.id.comment_list_bottom_BTN); //获取发表按钮
        commentListBottomET = findViewById(R.id.comment_list_bottom_ET);   //获取editText按钮
        commentListBottomBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String commentContent = commentListBottomET.getText().toString().trim();
                            StringBuffer sb = new StringBuffer();
                            sb.append("content=");
                            sb.append(URLEncoder.encode(commentContent, "UTF-8"));
                            sb.append("&newsdetailId=");
                            sb.append(getIntent().getStringExtra("newsdetailId"));
                            String path = getResources().getString(R.string.issue) + "addComment?" + sb.toString();
                            addComment(path);

                            Comment comment = new Comment();
                            comment.setDATE(DateUtil.getTime());
                            comment.setCONTENT(commentContent);
                            commentList.add(comment);
                            handler.sendEmptyMessage(2);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        new CommentTask().execute(getResources().getString(R.string.issue) + "commentList?newsdetailId=" + getIntent().getStringExtra("newsdetailId"));
    }

    class CommentAdapter extends BaseAdapter {

        private List<Comment> commentList;

        public CommentAdapter(List<Comment> commentList) {
            this.commentList = commentList;
        }

        @Override
        public int getCount() {
            return commentList.size();
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
            Comment1 comment1;
            if (view == null) {
                comment1 = new Comment1();
                view = getLayoutInflater().inflate(R.layout.comment_list_item, viewGroup, false);
                comment1.tv1 = view.findViewById(R.id.comment_list_item_tv1);
                comment1.tv2 = view.findViewById(R.id.comment_list_item_tv2);
                comment1.tv3 = view.findViewById(R.id.comment_list_item_tv3);
                view.setTag(comment1);
            } else {
                comment1 = (Comment1) view.getTag();
            }
            comment1.tv1.setText(commentList.get(i).getDATE());
            comment1.tv2.setText(commentList.get(i).getCOMMENTID());
            comment1.tv3.setText(commentList.get(i).getCONTENT());
            return view;
        }
    }

    class Comment1 {
        TextView tv1;
        TextView tv2;
        TextView tv3;
    }

    class CommentTask extends AsyncTask<String, Void, List<Comment>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Comment> doInBackground(String... strings) {
            commentList = new ArrayList<>();
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
                        Comment comment = new Comment();
                        comment.setCOMMENTID(element.getString("COMMENT_ID"));
                        comment.setDATE(element.getString("DATE"));
                        comment.setCONTENT(element.getString("CONTENT"));
                        commentList.add(comment);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return commentList;
        }

        @Override
        protected void onPostExecute(List<Comment> commentList) {
            super.onPostExecute(commentList);
            commentListView.setAdapter(commentAdapter = new CommentAdapter(commentList));
        }
    }

    /**
     * 添加跟帖
     *
     * @param path 路径
     */
    private void addComment(String path) throws Exception {
        //Log.i(TAG, "addComment: path------>>" + path);
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(path);
        HttpResponse httpResponse;
        try {
            httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            //httpGet.addHeader("charset", HTTP.UTF_8);
            httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity, "utf-8");
                String status = new JSONObject(result).getString("message");
                String message = new JSONObject(result).getString("status");
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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                String count = (String) msg.obj;
                commentListHandTV.setText("跟帖 " + count + "条");
                Toast.makeText(CommentActivity.this, "发帖成功!", Toast.LENGTH_LONG).show();
            } else if (msg.what == 2) {
                commentAdapter.notifyDataSetChanged();//刷新listview
                commentListBottomET.setText("");  //清空文本
                commentListBottomET.clearFocus();  //清除焦点
                //软键盘隐藏
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    };

}
