package com.watermelon.wt_zxing;

import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;


import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.model.FunctionConfig;
import com.luck.picture.lib.model.FunctionOptions;
import com.luck.picture.lib.model.PictureConfig;
import com.watermelon.wt_zinglib.core.QRCodeView;
import com.watermelon.wt_zinglib.zxing.QRCodeDecoder;
import com.watermelon.wt_zinglib.zxing.ZXingView;

import java.util.ArrayList;
import java.util.List;


public class ZxingActivity extends AppCompatActivity implements QRCodeView.Delegate{


    private static final String TAG = ZxingActivity.class.getSimpleName();

    private QRCodeView mQRCodeView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_zxing);

        //初始化
        mQRCodeView = (ZXingView) findViewById(R.id.zxingview);
        mQRCodeView.setDelegate(this);
        //二维码扫描设置
        mQRCodeView.startSpot();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mQRCodeView.startCamera();
//        mQRCodeView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);//前置摄像头

        mQRCodeView.showScanRect();//显示扫描框
    }

    @Override
    protected void onStop() {
        mQRCodeView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mQRCodeView.onDestroy();
        super.onDestroy();
    }

    /**
     * 手机震动
     */
    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.i(TAG, "result:" + result);
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        vibrate();
        mQRCodeView.startSpot();
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错");
    }

    public void xiangce(View view){
        FunctionOptions options = new FunctionOptions.Builder()
                .setType(FunctionConfig.TYPE_IMAGE) // 图片or视频 FunctionConfig.TYPE_IMAGE  TYPE_VIDEO
                .setCompress(false) //是否压缩
                .setMaxSelectNum(1) // 可选择图片的数量
                .setSelectMode(FunctionConfig.MODE_SINGLE) // 单选 or 多选  FunctionConfig.MODE_SINGLE FunctionConfig.MODE_MULTIPLE
                .setShowCamera(false) //是否显示拍照选项 这里自动根据type 启动拍照或录视频
                .setEnablePreview(true) // 是否打开预览选项
                .setGif(false)// 是否显示gif图片，默认不显示
                .setNumComplete(false) // 0/9 完成  样式
                .create();
        // 先初始化参数配置，在启动相册
        PictureConfig.getInstance().init(options).openPhoto(ZxingActivity.this, resultCallback);
    }
    private List<LocalMedia> selectMedia;
    private PictureConfig.OnSelectResultCallback resultCallback = new PictureConfig.OnSelectResultCallback() {

        @Override
        public void onSelectSuccess(List<LocalMedia> list) {}
        @Override//单选
        public void onSelectSuccess(LocalMedia media) {
            mQRCodeView.showScanRect();
            // 单选回调
            selectMedia=new ArrayList();
            selectMedia.add(media);
            final String picturePath=selectMedia.get(0).getPath();
            if (selectMedia != null) {
                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        return QRCodeDecoder.syncDecodeQRCode(picturePath);
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        if (TextUtils.isEmpty(result)) {
                            Toast.makeText(ZxingActivity.this,"未发现二维码",Toast.LENGTH_SHORT).show();
                        } else {
                            vibrate();
                            Toast.makeText(ZxingActivity.this,result,Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute();
            }
        }
    };

}
