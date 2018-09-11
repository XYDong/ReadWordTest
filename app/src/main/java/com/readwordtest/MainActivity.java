package com.readwordtest;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.FrameLayout;

import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_open)
    Button btnOpen;
    @BindView(R.id.fl)
    FrameLayout fl;

    private static final int CHOOSE_REQUEST_CODE = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_open)
    public void onViewClicked() {
        chooseFile();
    }

     /**
       * @description ：选择文件
       * @project name：MainActivity
       * @author : Administrator
       * @param :
       * @return :
       * @creation date:  2018/9/10 21:26
       * @version 1.0
       */
    private void chooseFile() {
        Intent it = new Intent(Intent.ACTION_GET_CONTENT);
        it.setType("*/*");
        it.addCategory(Intent.CATEGORY_OPENABLE);
        Intent chooser = Intent.createChooser(it, "请选择要打开的文件");
        startActivityForResult(chooser,CHOOSE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            if(resultCode == RESULT_OK){
                switch (requestCode) {
                    case CHOOSE_REQUEST_CODE:
                        Uri uri = data.getData();
//                        String path = getPathFromUri(this,uri);
                        String path = getRealFilePath(this,uri);
                        openFile(path);
                        break;
                }
            }
        }
    }

     /**
       * @description ：打开文件夹
       * @project name：MainActivity
       * @author : Administrator
       * @param :
       * @return :
       * @creation date:  2018/9/10 21:43
       * @version 1.0
       */
    private void openFile(String path) {
        TbsReaderView tbsReaderView = new TbsReaderView(this, new TbsReaderView.ReaderCallback() {
            @Override
            public void onCallBackAction(Integer integer, Object o, Object o1) {

            }
        });

        //通过bundle把文件传递给x5，打开的过程交由x5处理
        Bundle bundle = new Bundle();
        bundle.putString("filePath",path);
        //加载插件保存的路径
        bundle.putString("tempPath", Environment.getExternalStorageDirectory() + File.separator + "temp");
        //加载文件的初始化工作，加载支持不同格式的插件
        boolean b = tbsReaderView.preOpen(getFileType(path), false);
        if (b) {
            tbsReaderView.openFile(bundle);
        }
        fl.addView(tbsReaderView);
    }

     /**
       * @description ：获取文件的格式
       * @project name：MainActivity
       * @author : Administrator
       * @param :
       * @return :
       * @creation date:  2018/9/10 21:48
       * @version 1.0
       */
    private String getFileType(String path) {
        String str = "";
        if (TextUtils.isEmpty(path)) {
            return str;
        }
        int i = path.lastIndexOf('.');
        if (i <= -1) {
            return str;
        }
        str = path.substring(i + 1);
        return str;
    }

    /**
     * uri转path
     */
    public static String getRealFilePath(final Context context, final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
       * @description ：通过Uri获取文件地址
       * @project name：MainActivity
       * @author : Administrator
       * @param :
       * @param context
      * @return :
       * @creation date:  2018/9/10 21:31
       * @version 1.0
       */
    private String getPathFromUri(Context context, Uri uri) {
        //选择图片路径
        String selectPath = null;

        String uriScheme = uri.getScheme();
        if(uri != null && uriScheme != null){
            if (uriScheme.equals(ContentResolver.SCHEME_CONTENT)) {
                //content://开头的uri
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndexOrThrow = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    //取出文件路径
                    selectPath  = cursor.getString(columnIndexOrThrow);
                    if (!selectPath.startsWith("/storage") && !selectPath.startsWith("/mnt")) {
                        //检查是否有"/mnt" 前缀
                        selectPath = "/mnt" + selectPath;
                    }
                    //关闭游标
                    cursor.close();
                }
            }else if(uriScheme.equals(ContentResolver.SCHEME_FILE)){// file://开头的文件
                selectPath = uri.toString().replace("file://", "");
                int index = selectPath.indexOf("/sdcard");
                selectPath = index == -1 ? selectPath : selectPath.substring(index);
                if (!selectPath.startsWith("/mnt")) {
                    selectPath += "/mnt";
                }
            }
        }


        return selectPath;
    }
}
