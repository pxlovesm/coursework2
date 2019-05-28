package com.mynews.dao;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mynews.BuildConfig;
import com.mynews.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class VersionDao {

    private Context context;
    private Map<String, String> map;  //服务器端最新app信息
    private volatile boolean cancelUpdate;  //下载状态
    private ProgressBar pbNewsDeatil;
    private Dialog downloadDialog;
    private String TAG = VersionDao.class.getSimpleName();

    public VersionDao(Context context, Map<String, String> map) {
        this.context = context;
        this.map = map;
    }

    public void checkUpdate() {
        Log.i(TAG, "checkUpdate: 版本更新");
        if (isUpdate()) {
            showNoticeDialog();
        }
    }

    private boolean isUpdate() {
        //获取当前软件版本号
        String versionName = getVersionName();
        if (!versionName.equals(map.get("NUMBER").toString())) {  //版本号字符串不相等时更新
            return true;
        }
        return false;
    }

    /**
     * 当前应用版本号
     *
     * @return 版本号
     */
    private String getVersionName() {
        String versionName = null;
        try {
            //通过上下文对象获取版本号
            versionName = context.getPackageManager().getPackageInfo("com.mynews", 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    private void showNoticeDialog() {
        //构造对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("版本更新提示");
        builder.setMessage("检测到新版本，" + map.get("CONTENT").toString());
        //更新
        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();  //隐藏更新对话框
                showDownloadDialog();  //显示下载进度对话框
            }
        });

        //稍后更新
        builder.setNegativeButton("稍后更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    private void showDownloadDialog() {
        Log.i(TAG, "showDownloadDialog: 弹出更新对话框");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("更新进度:");
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.main_softupload_progress, null);
        pbNewsDeatil = view.findViewById(R.id.pb_newsDetail);
        TextView tv_version_content = view.findViewById(R.id.tv_version_content);
        tv_version_content.setText(map.get("CONTENT").toString());  //版本更新的内容
        builder.setView(view);
        //取消
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //设置取消状态
                cancelUpdate = false;
            }
        });

        downloadDialog = builder.create();
        downloadDialog.show();
        cancelUpdate = true;  //开始下载
        //下载文件
        download();

    }

    //下载文件
    private void download() {
        Log.i(TAG, "download: 下载文件中");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL(map.get("PATH").toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    int code = conn.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        File file = new File(context.getFilesDir().getPath() + "/mynews.apk");
                        FileOutputStream fos = new FileOutputStream(file);
                        float length = conn.getContentLength();
                        InputStream in = conn.getInputStream();
                        float total = 0;
                        int len = -1;
                        byte[] buffer = new byte[1024 * 1024 * 3];
                        int progress;
                        while ((len = in.read(buffer)) != -1) {
                            if (cancelUpdate) {
                                fos.write(buffer, 0, len);
                                total += len;
                                progress = (int) ((total / length) * 100);
                                pbNewsDeatil.setProgress(progress);
                            }
                        }
                        in.close();
                        fos.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 进度达到最大值后，窗口消失
                downloadDialog.cancel();
                install();
            }
        }).start();
    }

    private void install() {
        File file = new File(context.getFilesDir().getPath() + "/mynews.apk");
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
