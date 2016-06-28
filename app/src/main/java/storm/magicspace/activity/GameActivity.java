package storm.magicspace.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import storm.commonlib.common.base.BaseASyncTask;
import storm.commonlib.common.util.LogUtil;
import storm.magicspace.R;
import storm.magicspace.adapter.EggsAdapter;
import storm.magicspace.bean.httpBean.EggImage;
import storm.magicspace.bean.httpBean.EggImageListResponse;
import storm.magicspace.http.HTTPManager;
import storm.magicspace.view.FloatView;
import storm.magicspace.view.FloatView.FloatInfo;

public class GameActivity extends Activity {

    public static final String TAG = GameActivity.class.getSimpleName();
    public static final String ALPHA_CONTROLLER_POSITION_PARENT_BOTTOM = "bottom";
    public static final String ALPHA_CONTROLLER_POSITION_ABOVE_EGGS = "above_eggs";
    public static final int EGG_INIT_COUNT = 5;

    private WebView mWebView;
    private FloatView mFloatView;
    private Button mConfirmBtn;
    private FloatInfo mFloatInfo;
    private SeekBar mAlphaController;
    private float mAlphaVal = 1.0f;
    private int mEggsCount = 1;
    private ImageView mGuide;
    private RelativeLayout mEggsContainer;
    private RecyclerView mEggsLayout;
    private TextView mShowEggBtn;
    private boolean isAlphaControllerShowing = false;
    private TextView mEggsLoadingHint;
    private EggsAdapter mEggsAdapter;
    private ImageView mCreateEggBtn;
    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initView();
        initEvent();
    }

    public void initView() {
        mWebView = (WebView) findViewById(R.id.webview_game);
        mFloatView = (FloatView) findViewById(R.id.floatview_game);
        mAlphaController = (SeekBar) findViewById(R.id.alpha);
        mConfirmBtn = (Button) findViewById(R.id.sure);
        mGuide = (ImageView) findViewById(R.id.iv_game_guide);
        mShowEggBtn = (TextView) findViewById(R.id.tv_game_egg);
        mEggsContainer = (RelativeLayout) findViewById(R.id.rl_game_eggs_container);
        mEggsLayout = (RecyclerView) findViewById(R.id.rv_game_eggs);
        mEggsLoadingHint = (TextView) findViewById(R.id.tv_game_loading);
        mCreateEggBtn = (ImageView) findViewById(R.id.iv_game_confirm);

        initFloatView();
        initWebView();
        initAlphaController();
        initEggs();

    }

    private void initEggs() {
        //mEggsLayout.setLayoutManager(new LinearLayoutManager(this, OrientationHelper.HORIZONTAL,false));
        updateEggsCountHint(EGG_INIT_COUNT);
        mEggsLayout.setLayoutManager(new GridLayoutManager(this, 1, OrientationHelper.HORIZONTAL, false));
        new GetEggImageListTask().execute();

    }

    private class GetEggImageListTask extends BaseASyncTask<Void, EggImageListResponse> {
        @Override
        public EggImageListResponse doRequest(Void param) {
            return HTTPManager.getEggImageList();
        }

        @Override
        protected void onPostExecute(EggImageListResponse eggImageListResponse) {
            if (eggImageListResponse != null) {
                mEggsLoadingHint.setVisibility(View.INVISIBLE);
                mEggsLayout.setVisibility(View.VISIBLE);
                List<EggImage> data = eggImageListResponse.getData();
                EggImage eggImage = data.get(0);
                mEggsAdapter = new EggsAdapter(GameActivity.this, eggImage);
                mEggsLayout.setAdapter(mEggsAdapter);
                mEggsAdapter.setOnClickListener(new EggsAdapter.ClickInterface() {
                    @Override
                    public void onClick(final int position, final String url, final Bitmap bitmap) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LogUtil.d(TAG, "position = " + position + ", url = " + url);
                                mFloatView.setImageBitmap(bitmap);
                                mFloatInfo = null;
                                mUrl = url;
                                initFloatView();
                            }
                        });
                    }
                });
            } else {
                mEggsLoadingHint.setText(R.string.loading_failed);
            }
        }
    }

    private void createEgg() {
        if (mFloatInfo != null) {
            String contentId = "1";
            int itemId = mEggsCount++;
            float alpha = mAlphaVal;
            float scale = mFloatInfo.getScale();
            float rotate = -mFloatInfo.getRotate();
            // dropItem('contentId' , 'itemId', 'url' ,'alpha'  ,"scale","rotate")
            LogUtil.d(TAG, "contentId = " + contentId + ", itemId = " + itemId
                    + ", alpha = " + alpha + ", scale = " + scale
                    + ", rotate = " + rotate);
            mWebView.loadUrl("javascript:dropItem('"
                    + contentId + "' ,'"
                    + itemId + "' ,'"
                    + mUrl + "' ,'"
                    + alpha + "' ,'"
                    + scale + "' ,'"
                    + rotate + "')");
        } else {
            String contentId = "1";
            int itemId = mEggsCount++;
            float alpha = mAlphaVal;
            float scale = 1.0f;
            float rotate = 0.0f;
            mWebView.loadUrl("javascript:dropItem('"
                    + contentId + "' ,'"
                    + itemId + "' ,'"
                    + mUrl + "' ,'"
                    + alpha + "' ,'"
                    + scale + "' ,'"
                    + rotate + "')");
        }
    }

    private void initEvent() {
        mCreateEggBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mUrl)) {
                    Toast.makeText(GameActivity.this, R.string.add_egg_hint, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mEggsCount > EGG_INIT_COUNT) {
                    Toast.makeText(GameActivity.this, R.string.add_egg_over_hint, Toast.LENGTH_SHORT).show();
                    return;
                }
                updateEggsCountHint(EGG_INIT_COUNT - mEggsCount);
                createEgg();
            }
        });
        mShowEggBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEggsContainer.setVisibility(View.VISIBLE);
                positionAlphaController(ALPHA_CONTROLLER_POSITION_ABOVE_EGGS);
            }
        });
    }

    private void updateEggsCountHint(int count) {
        mShowEggBtn.setText(String.format(getResources().getString(R.string.eggs_count), count));
    }

    private void initAlphaController() {
        positionAlphaController(ALPHA_CONTROLLER_POSITION_PARENT_BOTTOM);
        mAlphaController.setProgress(100);
        mAlphaController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAlphaVal = progress * 1f / 100;
                mFloatView.setAlpha(mAlphaVal);
                Log.d(TAG, "alpha = " + mAlphaVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initFloatView() {
        // mFloatView.setImageResource(R.mipmap.surprise_egg_red);
        mFloatView.setOnFloatListener(new FloatView.FloatListener() {
            @Override
            public void clickLeftTop() {
                LogUtil.d(TAG, "left top transparent btn clicked");
                if (!isAlphaControllerShowing) {
                    mAlphaController.setVisibility(View.VISIBLE);
                } else {
                    mAlphaController.setVisibility(View.GONE);
                }
                isAlphaControllerShowing = !isAlphaControllerShowing;
            }

            @Override
            public void clickRightTop() {
                LogUtil.d(TAG, "right top rotate btn clicked");
            }

            @Override
            public void clickRightBottom() {
                LogUtil.d(TAG, "right bottom scale btn clicked");
            }

            @Override
            public void floatInfo(FloatInfo floatInfo) {
                mFloatInfo = floatInfo;
            }

        });
        mFloatView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                clearMask();
                return false;
            }
        });
    }

    private void clearMask() {
        if (mGuide.getVisibility() == View.VISIBLE) mGuide.setVisibility(View.GONE);
        if (mEggsContainer.getVisibility() == View.VISIBLE) mEggsContainer.setVisibility(View.GONE);
        positionAlphaController(ALPHA_CONTROLLER_POSITION_PARENT_BOTTOM);
    }

    private void positionAlphaController(String type) {
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) mAlphaController.getLayoutParams();
        if (ALPHA_CONTROLLER_POSITION_PARENT_BOTTOM.equals(type)) {
            layoutParams.removeRule(RelativeLayout.ABOVE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        } else if (ALPHA_CONTROLLER_POSITION_ABOVE_EGGS.equals(type)) {
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.ABOVE, R.id.rl_game_eggs_container);
        }
        mAlphaController.setLayoutParams(layoutParams);
    }

    @SuppressLint("JavascriptInterface")
    private void initWebView() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDefaultTextEncodingName("gb2312");
        mWebView.loadUrl("http://app.stemmind.com/vr/a/tour.html");
        ContainerView containerView = new ContainerView();
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.addJavascriptInterface(containerView, "containerView");
    }

    private class ContainerView {

        @JavascriptInterface
        public void editItem(String contentId, String sceneId, String order) {
            LogUtil.d(TAG, "editItem used");
        }

        @JavascriptInterface
        public void dropItemCallBack(String msg) {
            LogUtil.d(TAG, "receive msg :" + msg);
            // {"x":"0","y":"0","scale":"0.5","alpha":"0.5","rotate":"0"}
        }
    }
}


//    js 调app 用editItem('contentId','sceneId','order')
//    app 调 js 用 dropItem('contentId' , 'itemId','trans'  ) 返回 array ('x', 'y', 'scale')
