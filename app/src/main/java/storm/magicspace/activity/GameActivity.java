package storm.magicspace.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import storm.commonlib.common.CommonConstants;
import storm.commonlib.common.base.BaseASyncTask;
import storm.commonlib.common.util.JsonUtil;
import storm.commonlib.common.util.LogUtil;
import storm.commonlib.common.util.SharedPreferencesUtil;
import storm.magicspace.R;
import storm.magicspace.adapter.EggsAdapter;
import storm.magicspace.bean.IssueUCGContent;
import storm.magicspace.bean.UGCItem;
import storm.magicspace.bean.UGCScene;
import storm.magicspace.bean.UGCUpdateContent;
import storm.magicspace.bean.httpBean.EggImage;
import storm.magicspace.bean.httpBean.EggImageListResponse;
import storm.magicspace.bean.httpBean.IssueUCGContentResponse;
import storm.magicspace.bean.httpBean.UpdateUGCContentScenesResponse;
import storm.magicspace.event.GameEvent;
import storm.magicspace.fragment.EggImageFragment;
import storm.magicspace.http.HTTPManager;
import storm.magicspace.util.LocalSPUtil;
import storm.magicspace.view.FloatView;
import storm.magicspace.view.FloatView.FloatInfo;

import static storm.magicspace.bean.UGCUpdateContent.EggInfo;

public class GameActivity extends FragmentActivity {

    public static final String TAG = GameActivity.class.getSimpleName();
    public static final String RULE_BOTTOM = "bottom";
    public static final String RULE_ABOVE_EGG = "above_eggs";
    public static final int EGG_INIT_COUNT = 0;
    public static final int EGG_MIN_COUNT = 3;
    public static final int EGG_MAX_COUNT = 10;
    public static final String DEFAULT_CONTENT_ID = "3403";

    private WebView mWebView;
    private FloatView mFloatView;

    private ImageView mBackBtn;
    private ImageView mConfirmBtn;
    private ImageView mDeleteBtn;
    private ImageView mSharedBtn;
    private TextView mShowEggBtn;

    private RelativeLayout mEggsContainer;
    private TextView mLoadingHint;
    private ViewPager mEggPager;
    private TabLayout mEggTab;

    private SeekBar mAlphaBar;
    private ImageView mGuide;

    private float mAlphaVal = 1.0f;
    private int mEggsCount = 1;
    private FloatInfo mFloatInfo;
    private boolean mAlphaBarShowing = false;
    private String mUrl;
    private String mContentId;
    private List<EggImage> mEggImageList;
    private Map<String, UGCItem> mEggInfos;
    // Get from issue, use for update
    private UGCScene mUCGScene;
    private List<UGCItem> mUGCItems;
    private UGCItem mCurrentItem;
    private int mCurrentState = 1;
    private boolean mWebViewInit = false;
    private String mEggKey;
    private boolean mFrmoEdit;
    private String mWebUrl;
    private boolean Debug = true;
    private IssueUCGContent mUCGContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        initData();
        initView();
        initEvent();
    }

    private void initData() {
        mContentId = getRandomContentId();
        mEggInfos = new HashMap();
    }

    public void initView() {
        findView();
        initFloatView();
        initWebView();
        syncFloatView(false);
        initAlphaController();
        initEggs();
        initGuide();
    }

    private void initGuide() {
        String token = LocalSPUtil.getToken();
        if (!TextUtils.isEmpty(token)) {
            mGuide.setVisibility(View.GONE);
        } else {
            mGuide.setVisibility(View.VISIBLE);
        }
    }

    private void findView() {
        mWebView = (WebView) findViewById(R.id.webview_game);
        mFloatView = (FloatView) findViewById(R.id.floatview_game);
        mAlphaBar = (SeekBar) findViewById(R.id.alpha);

        mBackBtn = (ImageView) findViewById(R.id.iv_game_back);
        mConfirmBtn = (ImageView) findViewById(R.id.iv_game_confirm);
        mDeleteBtn = (ImageView) findViewById(R.id.iv_game_delete);
        mSharedBtn = (ImageView) findViewById(R.id.iv_game_shared);
        mShowEggBtn = (TextView) findViewById(R.id.tv_game_egg);

        mGuide = (ImageView) findViewById(R.id.iv_game_guide);
        mEggsContainer = (RelativeLayout) findViewById(R.id.rl_game_eggs_container);
        mLoadingHint = (TextView) findViewById(R.id.tv_game_loading);
        mEggPager = (ViewPager) findViewById(R.id.vp_game_eggs);
        mEggTab = (TabLayout) findViewById(R.id.tab_layout_game);
    }

    /**
     * sync button status when float view status change
     */
    private void syncFloatView(boolean floatViewShowing) {
        if (floatViewShowing) {
            mCurrentState = 0;
            mConfirmBtn.setVisibility(View.VISIBLE);
            mDeleteBtn.setVisibility(View.VISIBLE);
            mSharedBtn.setVisibility(View.GONE);
        } else {
            mCurrentState = 1;
            mConfirmBtn.setVisibility(View.INVISIBLE);
            mDeleteBtn.setVisibility(View.INVISIBLE);
            mSharedBtn.setVisibility(View.VISIBLE);
        }
        reportEditorState();
    }

    private void reportEditorState() {
        if (mWebViewInit) {
            mWebView.loadUrl("javascript:setEditorState('" + mCurrentState + "')");
        }
    }


    private void initEggs() {
        updateEggsCountHint(mEggsCount = EGG_INIT_COUNT);
        new GetEggImageListTask().execute();
        new IssueUGCContentTask().execute();
    }

    private class IssueUGCContentTask extends BaseASyncTask<Void, IssueUCGContentResponse> {

        @Override
        public IssueUCGContentResponse doRequest(Void param) {
            return HTTPManager.issueUCCContent("", "", "", mContentId);
        }

        @Override
        public void onSuccess(IssueUCGContentResponse response) {
            super.onSuccess(response);
            mUCGContent = response.getData();
            if (mUCGContent == null) return;
            List<UGCScene> scenes = mUCGContent.getScenes();
            if (scenes == null) return;
            mUCGScene = scenes.get(0);
            mUGCItems = mUCGScene.getItems();
            for (UGCItem ugcItem : mUGCItems) {
                mEggInfos.put(ugcItem.getItemId(), ugcItem);
            }
        }

    }

    private String getRandomContentId() {
        String contentJson = SharedPreferencesUtil.getJsonFromSharedPreferences(this,
                CommonConstants.CONTEND_IDS);
        if (contentJson == null) return DEFAULT_CONTENT_ID;
        ArrayList contentList = JsonUtil.fromJson(contentJson, ArrayList.class);
        if (contentList == null || contentList.size() == 0) return DEFAULT_CONTENT_ID;
        Random random = new Random();
        int id = random.nextInt(contentList.size());
        return (String) contentList.get(id);
    }

    private class UpdateUGCContentTask extends BaseASyncTask<Void, UpdateUGCContentScenesResponse> {

        public UpdateUGCContentTask(Context context, boolean hasLoadingDialog) {
            super(context, hasLoadingDialog);
        }

        @Override
        public UpdateUGCContentScenesResponse doRequest(Void param) {
            super.doRequest(param);
            if (mUCGScene == null) return null;
            //UGCUpdateContent content = createUpdateBean();
            //return HTTPManager.updateUGCContentScenes("", mContentId, JsonUtil.toJson(content));
            // if key exist, should't generate new key
            if (mFrmoEdit) {
                for (UGCItem ugcItem : mUGCItems) {
                    if (ugcItem != null && ugcItem.getItemId() != null
                            && ugcItem.getItemId().equals(mEggKey)) {
                        // TODO: item dismiss
                        mUGCItems.remove(ugcItem);
                        break;
                    }
                }
                mUGCItems.add(mCurrentItem);
                mUCGScene.setItems(mUGCItems);
                mUCGScene.setItemsCount(mEggsCount+1 + "");
                mEggInfos.put(mEggKey, mCurrentItem);
                mFrmoEdit = false;
                return HTTPManager.updateUGCContentScenes("", mContentId, JsonUtil.toJson(mUCGScene));
            }
            mEggKey = System.currentTimeMillis() + "";
            mCurrentItem.setItemId(mEggKey);
            mUGCItems.add(mCurrentItem);
            mUCGScene.setItems(mUGCItems);
            mUCGScene.setItemsCount(mEggsCount+1 + "");
            mEggInfos.put(mEggKey, mCurrentItem);
            return HTTPManager.updateUGCContentScenes("", mContentId, JsonUtil.toJson(mUCGScene));
        }

        @Override
        public void onSuccess(UpdateUGCContentScenesResponse response) {
            super.onSuccess(response);
            Toast.makeText(GameActivity.this, R.string.update_egg_success, Toast.LENGTH_SHORT).show();
            createEgg();
            resetFloatView();
            resetAlphaController();
            mEggsCount = mEggsCount + 1;
            updateEggsCountHint(mEggsCount);
        }

        @Override
        public void onFailed() {
            super.onFailed();
            Toast.makeText(GameActivity.this, R.string.update_egg_failed, Toast.LENGTH_SHORT).show();
            mUGCItems.remove(mCurrentItem);
            mEggInfos.remove(mEggKey);
        }
    }

    @NonNull
    private UGCUpdateContent createUpdateBean() {

        UGCUpdateContent content = new UGCUpdateContent();

        content.setBgimageUrl(mContentId);
        content.setItemsCount(mEggsCount);
        content.setOrder(mEggsCount);
        content.setSceneId(mUCGScene == null ? "" : mUCGScene.getSceneId());
        content.setTimeLimit(120);
        content.setTips("2");

        EggInfo eggInfo = new EggInfo();

        eggInfo.setX("1");
        eggInfo.setY("2");
        eggInfo.setScalex("1.0");
        eggInfo.setRotatez("20");
        eggInfo.setTransparency("0.5");

        eggInfo.setItemId("1");//
        eggInfo.setItemMediaUrl("http://app.stemmind.com/vr/objs/08.png");
        eggInfo.setEnabled("1");

        content.setItems(eggInfo);
        return content;
    }

    private void resetAlphaController() {
        mAlphaBar.setProgress(100);
        mAlphaBar.setVisibility(View.INVISIBLE);
        mAlphaVal = 1.0f;
    }

    private class GetEggImageListTask extends BaseASyncTask<Void, EggImageListResponse> {

        @Override
        public EggImageListResponse doRequest(Void param) {

            return HTTPManager.getEggImageList();
        }

        @Override
        public void onSuccess(EggImageListResponse response) {
            super.onSuccess(response);
            mLoadingHint.setVisibility(View.INVISIBLE);
            mEggImageList = response.getData();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<EggImageFragment> fragments = new ArrayList<>();
                    int size = mEggImageList.size();
                    for (int i = 0; i < size; i++) {
                        EggImageFragment fragment = EggImageFragment.getInstance(i);
                        setEggImageListener(fragment);
                        fragments.add(fragment);
                    }
                    initViewPager(fragments, size);

                }
            }).start();
        }

        @Override
        public void onFailed() {
            super.onFailed();
            mLoadingHint.setText(R.string.loading_failed);
        }
    }

    private void fillTabLayout(final int pos) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final BitmapDrawable bitmapDrawable = getDrawableWithBitmap(pos);
                    final TabLayout.Tab tab = mEggTab.getTabAt(pos);
                    if (tab != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tab.setIcon(bitmapDrawable);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @NonNull
    private BitmapDrawable getDrawableWithBitmap(int pos) throws IOException {
        String url = mEggImageList.get(pos).getImgurl();
        Bitmap bitmap = createBitmapWithUrl(url);
        return new BitmapDrawable(getResources(), bitmap);
    }

    private Bitmap createBitmapWithUrl(String url) throws IOException {
        RequestCreator load = Picasso.with(GameActivity.this).load(url);
        return load.get();
    }

    private void initViewPager(final ArrayList<EggImageFragment> fragments, final int size) {
        mEggPager.setOffscreenPageLimit(fragments.size());
        mEggPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEggTab.setupWithViewPager(mEggPager);
                for (int i = 0; i < size; i++) {
                    // init viewpager first
                    fillTabLayout(i);
                }
            }
        });
    }

    private void setEggImageListener(EggImageFragment fragment) {
        fragment.setOnEggClickListener(new EggsAdapter.ClickInterface() {
            @Override
            public void onClick(int position, String url, Bitmap bitmap) {
                LogUtil.d(TAG, "position = " + position + ", url = " + url);
                mFloatView.useExtraMatrix(mFrmoEdit);
                //mFloatView.setImageBitmap(null);
                mFloatView.setImageBitmap(bitmap);
//                mFloatView.setImageBitmap(null);
//                mFloatView.setFloatView(bitmap, 1, 30);

                if (!mFrmoEdit) {
                    mCurrentItem = new UGCItem();
                    mCurrentItem.setX("0");
                    mCurrentItem.setY("0");
                    mCurrentItem.setScalex(1.0f + "");
                    mCurrentItem.setRotatez(0.0f + "");
                    mCurrentItem.setTransparency(1.0f + "");
                    mCurrentItem.setEnabled("1");
                }
                mCurrentItem.setItemMediaUrl(url);

                //mCurrentItem.setItemId(mEggsCount + "");

                mFloatInfo = null;
                mUrl = url;
                initFloatView();
                syncFloatView(true);
            }
        });
    }

    public List<EggImage> getEggImageList() {
        return mEggImageList;
    }

    private void createEgg() {
        String itemId = mEggKey;
//        float alpha = mAlphaVal;
//        float scale = mFloatInfo != null ? mFloatInfo.getScale() : 1.0f;
//        float rotate = mFloatInfo != null ? -mFloatInfo.getRotate() : 0.0f;

        float alpha = Float.parseFloat(mCurrentItem.getTransparency());
        float scale = Float.parseFloat(mCurrentItem.getScalex());
        float rotate = -Float.parseFloat(mCurrentItem.getRotatez());

        log("[JS dropItem] >>> contentId = %s, itemId = %s, url = %s, alpha = %s, scale = %s, rotate = %s",
                mContentId, itemId, mUrl, alpha, scale, rotate);
        // dropItem('contentId', 'itemId', 'url', 'alpha', 'scale', 'rotate')
        mWebView.loadUrl("javascript:dropItem('"
                + mContentId + "' ,'"
                + itemId + "' ,'"
                + mUrl + "' ,'"
                + alpha + "' ,'"
                + scale + "' ,'"
                + rotate + "')");
    }

    private void initEvent() {
        mSharedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEggsCount < EGG_MIN_COUNT) {
                    Toast.makeText(GameActivity.this, R.string.add_egg_less_hint, Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(GameActivity.this, GameEditDetailActivity.class);
                intent.putExtra("contentId",mContentId);
                startActivity(intent);

                final GameEvent gameEvent = new GameEvent();
                UGCScene ugcScene = mUCGContent.getScenes().get(0);
                ugcScene.setItems(mUGCItems);
                gameEvent.content = mUCGContent;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EventBus.getDefault().post(gameEvent);
                            }
                        });
                    }
                }, 200);
            }
        });

        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mUrl)) {
                    Toast.makeText(GameActivity.this, R.string.add_egg_hint, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mEggsCount >= EGG_MAX_COUNT) {
                    Toast.makeText(GameActivity.this, R.string.add_egg_over_hint, Toast.LENGTH_SHORT).show();
                    return;
                }
                new UpdateUGCContentTask(GameActivity.this, true).execute();
            }
        });

        mShowEggBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEggsContainer.setVisibility(View.VISIBLE);
                positionAlphaController(RULE_ABOVE_EGG);
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameActivity.this.finish();
            }
        });

        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFloatView();
            }
        });
    }

    private void resetFloatView() {
        mFloatView.setImageBitmap(null);
        syncFloatView(false);
        mUrl = null;
    }

    private void updateEggsCountHint(int count) {
        mShowEggBtn.setText(String.format(getResources().getString(R.string.eggs_count), count));
    }

    private void initAlphaController() {
        positionAlphaController(RULE_ABOVE_EGG);
        mAlphaBar.setProgress(100);
        mAlphaBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAlphaVal = progress * 1f / 100;
                mFloatView.setFloatAlpha(mAlphaVal);
                mCurrentItem.setTransparency(mAlphaVal + "");
                log("alpha = %s", mAlphaVal);
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
        mFloatView.setVisibility(View.VISIBLE);
        mFloatView.setOnFloatListener(new FloatView.FloatListener() {
            @Override
            public void clickLeftTop() {
                log("transparency btn clicked");
                if (!mAlphaBarShowing) {
                    mAlphaBar.setVisibility(View.VISIBLE);
                } else {
                    mAlphaBar.setVisibility(View.GONE);
                }
                mAlphaBarShowing = !mAlphaBarShowing;
            }

            @Override
            public void clickRightTop() {
                log("rotate btn clicked");
            }

            @Override
            public void clickRightBottom() {
                log("scale btn clicked");
            }

            @Override
            public void floatInfo(FloatInfo floatInfo) {
                mFloatInfo = floatInfo;
                if (mCurrentItem == null) {
                    mCurrentItem = new UGCItem();
                }
                mCurrentItem.setX("0");
                mCurrentItem.setY("0");
                mCurrentItem.setScalex(floatInfo.getScale() + "");
                mCurrentItem.setRotatez(floatInfo.getRotate() + "");
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
        if (mEggsContainer.getVisibility() == View.VISIBLE)
            mEggsContainer.setVisibility(View.INVISIBLE);
        positionAlphaController(RULE_ABOVE_EGG);
    }

    private void positionAlphaController(String type) {
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) mAlphaBar.getLayoutParams();
        if (RULE_BOTTOM.equals(type)) {
            layoutParams.removeRule(RelativeLayout.ABOVE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        } else if (RULE_ABOVE_EGG.equals(type)) {
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.ABOVE, R.id.rl_game_eggs_container);
        }
        mAlphaBar.setLayoutParams(layoutParams);
    }

    @SuppressLint("JavascriptInterface")
    private void initWebView() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDefaultTextEncodingName("gb2312");
        if (!test()) {
            mWebUrl = "http://app.stemmind.com/vr/a/vreditor.php?ua=app&c=" + mContentId;
        } else {
            mWebUrl = "http://app.stemmind.com/vr/a/test.php?ua=app&c=" + mContentId;
        }
        mWebView.loadUrl(mWebUrl);
        ContainerView containerView = new ContainerView();
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.addJavascriptInterface(containerView, "containerView");
        mWebViewInit = true;
    }

    private boolean test() {
        return false;
    }


    private class ContainerView {

        @JavascriptInterface
        public void editItem(String contentId, String sceneId, String itemId) {
            log("[JS editItem] >>> contentId = %s, sceneId =  %s, itemId = %s"
                    ,contentId ,sceneId, itemId);
            mEggsCount = mEggsCount - 1;
            mEggsCount = mEggsCount >= 0 ? mEggsCount : 0;
            UGCItem ugcItem = mEggInfos.get(itemId);
            if (ugcItem == null) return;
            mCurrentItem = ugcItem;
            mEggKey = ugcItem.getItemId();
            mFrmoEdit = true;
            //TODO: handle delete button event
            mUrl = ugcItem.getItemMediaUrl();
            float rotate = Float.parseFloat(ugcItem.getRotatez());
            float scale = Float.parseFloat(ugcItem.getScalex());
            float alpha = Float.parseFloat(ugcItem.getTransparency());
            createAndShowBitmap(mUrl, scale, rotate, alpha);
            updateCountHint();

        }

        @JavascriptInterface
        public void dropItemCallBack(String msg) {
            log("[JS dropItemCallBack] >>> msg = %s ", msg);
        }
    }

    private void updateCountHint() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateEggsCountHint(mEggsCount);
                syncFloatView(true);
            }
        });
    }

    private void createAndShowBitmap(final String url, final float scale, final float rotate,
                                     final float alpha) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap = createBitmapWithUrl(url);
                    showBitmap(bitmap, scale, rotate, alpha);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showBitmap(final Bitmap bitmap, final float scale, final float rotate,
                            float alpha) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFloatView.setImageBitmap(null);
                mFloatView.setFloatView(bitmap, scale, rotate);
            }
            //todo:
        });
    }

    private void log(String log, Object... msg) {
        try {
            if (Debug) {
                String result = String.format(log, msg);
                Log.d(TAG, result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}


//    js 调app 用editItem('contentId','sceneId','order')
//    app 调 js 用 dropItem('contentId' , 'itemId','trans'  ) 返回 array ('x', 'y', 'scale')
