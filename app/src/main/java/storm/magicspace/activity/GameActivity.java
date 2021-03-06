package storm.magicspace.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.KeyEvent;
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
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import storm.commonlib.common.CommonConstants;
import storm.commonlib.common.base.BaseASyncTask;
import storm.commonlib.common.util.DialogUtils;
import storm.commonlib.common.util.JsonUtil;
import storm.commonlib.common.util.SharedPreferencesUtil;
import storm.magicspace.R;
import storm.magicspace.adapter.EggsAdapter;
import storm.magicspace.bean.IssueUCGContent;
import storm.magicspace.bean.UGCItem;
import storm.magicspace.bean.UGCScene;
import storm.magicspace.bean.httpBean.EggImage;
import storm.magicspace.bean.httpBean.EggImageListResponse;
import storm.magicspace.bean.httpBean.IssueUCGContentResponse;
import storm.magicspace.bean.httpBean.UpdateUGCContentScenesResponse;
import storm.magicspace.event.GameEvent;
import storm.magicspace.event.PublishEvent;
import storm.magicspace.fragment.EggImageFragment;
import storm.magicspace.http.HTTPManager;
import storm.magicspace.http.URLConstant;
import storm.magicspace.util.LocalSPUtil;
import storm.magicspace.view.FloatView;
import storm.magicspace.view.FloatView.FloatInfo;

import static storm.commonlib.common.CommonConstants.CONTEND_IDS;
import static storm.magicspace.view.FloatView.DEFAULT_ALPHA;

public class GameActivity extends FragmentActivity {

    ///////////////////////////////////////////////////////////////////////////
    // CONSTANT
    ///////////////////////////////////////////////////////////////////////////
    private static final String TAG = GameActivity.class.getSimpleName();
    private static final String RULE_BOTTOM = "bottom";
    private static final String RULE_ABOVE_EGG = "above_eggs";
    private static final String DEFAULT_CONTENT_ID = "3403";
    private static final int EGG_INIT_COUNT = 0;
    private static final int EGG_MIN_COUNT = 3;
    private static final int EGG_MAX_COUNT = 10;
    private static final boolean DEBUG = true;
    private static final boolean USE_TEST_URL = false;


    ///////////////////////////////////////////////////////////////////////////
    // VIEW
    ///////////////////////////////////////////////////////////////////////////
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

    ///////////////////////////////////////////////////////////////////////////
    // FIELD
    ///////////////////////////////////////////////////////////////////////////
    private float mAlphaVal = 1.0f;
    private int mEggsCount = 1;
    private boolean mAlphaBarShowing = false;
    private String mUrl;
    private String mContentId;
    private List<EggImage> mEggImageList;
    private Map<String, UGCItem> mEggInfos;
    private UGCScene mUCGScene;
    private List<UGCItem> mUGCItems;
    private UGCItem mCurrentItem;
    private int mCurrentState = 1;
    private boolean mWebViewInit = false;
    private String mEggKey;
    private boolean mFromEdit = false;
    private IssueUCGContent mUCGContent;
    private String mFrom;
    private boolean mCoinError;
    private boolean mNeedUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_game);
        initData();
        initView();
        initEvent();
    }

    private void initData() {
        mContentId = getRandomContentId();
        setContentIdIfNecessary();
        mFrom = configFrom();
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

    private void initEvent() {
        handleEditGameEvent();
        handleConfirmEvent();
        handleShowEggEvent();
        handleBackEvent();
        handleDeleteEvent();
    }

    private void initAlphaController() {
        positionAlphaController(RULE_ABOVE_EGG);
        mAlphaBar.setProgress(100);
        mAlphaBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAlphaVal = progress * 1f / 100;
                mFloatView.setFloatAlpha(mAlphaVal);
                float alpha = (DEFAULT_ALPHA + (1 - DEFAULT_ALPHA) * mAlphaVal);
                //mCurrentItem.setTransparency(String.valueOf(alpha));
                log("alpha = %s", alpha);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void handleConfirmEvent() {
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mUrl)) {
                    toastHint(R.string.add_egg_hint);
                    return;
                }
                if (mEggsCount >= EGG_MAX_COUNT) {
                    toastHint(R.string.add_egg_over_hint);
                    return;
                }
                //new UpdateUGCContentTask(GameActivity.this, true).execute();

                if (!mFromEdit) {
                    mEggKey = String.valueOf(System.currentTimeMillis());
                }

                createEgg();
            }
        });
    }

    private void createEgg() {
        // dropItem('contentId', 'itemId', 'url', 'alpha', 'scale', 'rotate')
        String itemId = mEggKey;
        float alpha = Float.parseFloat(mCurrentItem.getTransparency());
        float scale = Float.parseFloat(mCurrentItem.getScalex());
        BigDecimal testScale = BigDecimal.valueOf(Double.valueOf(mCurrentItem.getScalex()));
        float rotate = -Float.parseFloat(mCurrentItem.getRotatez());

        log("[JS dropItem] >>> contentId = %s, itemId = %s, url = %s, alpha = %s, scale = %s, " +
                "rotate = %s", mContentId, itemId, mUrl, alpha, scale, rotate);

        mWebView.loadUrl("javascript:dropItem('"
                + mContentId + "' ,'"
                + itemId + "' ,'"
                + mUrl + "' ,'"
                + alpha + "' ,'"
                + scale + "' ,'"
                + rotate + "')");
    }

    private class ContainerView {

        @JavascriptInterface
        public void dropItemCallBack(String msg) {
            log("[JS dropItemCallBack] >>> msg = %s ", msg);
            FloatInfo floatInfo = JsonUtil.fromJson(msg, FloatInfo.class);
            if (floatInfo == null) return;
            float x = floatInfo.getX();
            float y = floatInfo.getY();
            mCurrentItem.setX(String.valueOf(x));
            mCurrentItem.setY(String.valueOf(y));

            float scale = floatInfo.getScale();
            mCurrentItem.setScalex(String.valueOf(scale));
            log("floatInfo x =  %s, y = %s", x, y);
            if (mUCGScene == null) return;
            // if key exist, should't generate new key
            if (mFromEdit) {
                for (UGCItem ugcItem : mUGCItems) {
                    if (ugcItem != null && ugcItem.getItemId() != null
                            && ugcItem.getItemId().equals(mEggKey)) {
                        // sometime item dismiss ?
                        mUGCItems.remove(ugcItem);
                        break;
                    }
                }
                mFromEdit = false;
            } else {
                //mEggKey = String.valueOf(System.currentTimeMillis());
            }
            mCurrentItem.setItemId(mEggKey);
            mUGCItems.add(mCurrentItem);
            mUCGScene.setItems(mUGCItems);
            mUCGScene.setItemsCount(String.valueOf(mEggsCount + 1));
            mEggInfos.put(mEggKey, mCurrentItem);

            new UpdateUGCContentTask(GameActivity.this, true).execute();
        }

        @JavascriptInterface
        public void editItem(String contentId, String sceneId, String itemId) {
            log("[JS editItem] >>> contentId = %s, sceneId =  %s, itemId = %s"
                    , contentId, sceneId, itemId);
            mEggsCount = mEggsCount - 1;
            mEggsCount = mEggsCount >= 0 ? mEggsCount : 0;
            UGCItem ugcItem = mEggInfos.get(itemId);


            if (ugcItem == null) return;

            ugcItem.setScalex("1.0");
            mCurrentItem = ugcItem;
            mEggKey = ugcItem.getItemId();
            mFromEdit = true;
            mUrl = ugcItem.getItemMediaUrl();
            float rotate = Float.parseFloat(ugcItem.getRotatez());
            float scale = Float.parseFloat(ugcItem.getScalex());
            float alpha = Float.parseFloat(ugcItem.getTransparency());
            createAndShowBitmap(mUrl, scale, rotate, alpha);
            updateCountHint();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // update request
    ///////////////////////////////////////////////////////////////////////////
    private class UpdateUGCContentTask extends BaseASyncTask<Void, UpdateUGCContentScenesResponse> {
        public UpdateUGCContentTask(Context context, boolean hasLoadingDialog) {
            super(context, hasLoadingDialog);
        }

        @Override
        public UpdateUGCContentScenesResponse doRequest(Void param) {
            super.doRequest(param);

            return HTTPManager.updateUGCContentScenes(mUCGContent.getContentId(),
                    JsonUtil.toJson(mUCGScene));
        }

        @Override
        public void onSuccess(UpdateUGCContentScenesResponse response) {
            super.onSuccess(response);
            toastHint(R.string.update_egg_success);
            //createEgg();
            resetFloatView();
            resetAlphaController();
            mEggsCount = mEggsCount + 1;
            updateEggsCountHint(mEggsCount);
        }

        @Override
        public void onFailed() {
            super.onFailed();
            if (mCoinError) {
                toastHint(R.string.update_egg_failed_no_coin);
            } else {
                toastHint(R.string.update_egg_failed);
            }
            if (mUGCItems != null) {
                mUGCItems.remove(mCurrentItem);
            }
            mEggInfos.remove(mEggKey);
        }
    }

    private void resetAlphaController() {
        mAlphaBar.setProgress(100);
        mAlphaBar.setVisibility(View.INVISIBLE);
        mAlphaVal = 1.0f;
    }

    private String getRandomContentId() {
        String contentJson = SharedPreferencesUtil.getJsonFromSharedPreferences(this,
                CONTEND_IDS);
        if (contentJson == null) return DEFAULT_CONTENT_ID;
        ArrayList contentList = JsonUtil.fromJson(contentJson, ArrayList.class);
        if (contentList == null || contentList.size() == 0) return DEFAULT_CONTENT_ID;
        Random random = new Random();
        int id = random.nextInt(contentList.size());
        return (String) contentList.get(id);
    }

    private void setContentIdIfNecessary() {
        Intent intent = getIntent();
        if (intent == null) return;
        String contentId = intent.getStringExtra(CommonConstants.CONTENT_ID);
        if (!TextUtils.isEmpty(contentId)) {
            mContentId = contentId;
        }
    }

    private String configFrom() {
        Intent intent = getIntent();
        if (intent == null) return CommonConstants.GAME;
        return intent.getStringExtra(CommonConstants.FROM);
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

    private void initFloatView() {
        mFloatView.setVisibility(View.VISIBLE);
        mFloatView.setOnFloatListener(new FloatView.FloatListener() {
            @Override
            public void clickLeftTop() {
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
                if (mCurrentItem == null) {
                    mCurrentItem = new UGCItem();
                }
                mCurrentItem.setScalex(String.valueOf(floatInfo.getScale()));
                mCurrentItem.setRotatez(String.valueOf(floatInfo.getRotate()));
                mCurrentItem.setTransparency(String.valueOf(floatInfo.getAlpha()));
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

    @SuppressLint("JavascriptInterface")
    private void initWebView() {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDefaultTextEncodingName("gb2312");
        String webUrl = getUrl();
        if (USE_TEST_URL) {
            webUrl = URLConstant.URL_112 + mContentId;
        }
        mWebView.loadUrl(webUrl);
        ContainerView containerView = new ContainerView();
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.addJavascriptInterface(containerView, "containerView");
        mWebViewInit = true;
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

    private void initEggs() {
        updateEggsCountHint(mEggsCount = EGG_INIT_COUNT);
        new GetEggImageListTask().execute();
        if (CommonConstants.GAME.equals(mFrom)) {
            new IssueUGCContentWithGameTask().execute();
        } else {
            new IssueUGCContentTask().execute();
        }
    }

    private void initGuide() {
        boolean showedGuide = LocalSPUtil.getGuide();
        if (showedGuide) {
            mGuide.setVisibility(View.GONE);
        } else {
            mGuide.setVisibility(View.VISIBLE);
            LocalSPUtil.saveGuide(true);
        }
    }

    private void reportEditorState() {
        if (mWebViewInit) {
            mWebView.loadUrl("javascript:setEditorState('" + mCurrentState + "')");
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // issue request
    ///////////////////////////////////////////////////////////////////////////
    private class IssueUGCContentTask extends BaseASyncTask<Void, IssueUCGContentResponse> {
        @Override
        public IssueUCGContentResponse doRequest(Void param) {
            return HTTPManager.issueUCCContent("", "", mContentId);
        }

        @Override
        public void onSuccess(IssueUCGContentResponse response) {
            super.onSuccess(response);
            issueSuccess(response);
        }

        @Override
        public void onFailed(IssueUCGContentResponse response) {
            super.onFailed(response);
            issueFailed(response);
        }
    }

    private class IssueUGCContentWithGameTask extends BaseASyncTask<Void, IssueUCGContentResponse> {
        @Override
        public IssueUCGContentResponse doRequest(Void param) {
            return HTTPManager.issueUCCContent("", "", mContentId, "game");
        }

        @Override
        public void onSuccess(IssueUCGContentResponse response) {
            super.onSuccess(response);
            issueSuccess(response);
        }

        @Override
        public void onFailed(IssueUCGContentResponse response) {
            super.onFailed(response);
            issueFailed(response);
        }
    }

    private void issueFailed(IssueUCGContentResponse response) {
        float status = response.getStatus();
        if (status == 1001) {
            mCoinError = true;
        }
    }

    private void issueSuccess(IssueUCGContentResponse response) {
        IssueUCGContent data = response.getData();
        if (data == null) return;
        mUCGContent = data;
        List<UGCScene> scenes = mUCGContent.getScenes();
        if (scenes == null) return;
        mUCGScene = scenes.get(0);
        mUGCItems = mUCGScene.getItems();
        if (mUGCItems == null) return;
        for (UGCItem ugcItem : mUGCItems) {
            mEggInfos.put(ugcItem.getItemId(), ugcItem);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // egg image request
    ///////////////////////////////////////////////////////////////////////////
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

    private void setEggImageListener(EggImageFragment fragment) {
        fragment.setOnEggClickListener(new EggsAdapter.ClickInterface() {
            @Override
            public void onClick(int position, String url, Bitmap bitmap) {
                log("egg image, position = %s, url = %s", position, url);
                mFloatView.useExtraMatrix(mFromEdit);
                //mFloatView.setImageBitmap(null);
                mFloatView.setImageBitmap(bitmap);
                if (!mFromEdit | mNeedUpdate) {
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
                mUrl = url;
                //initFloatView();
                mFloatView.setVisibility(View.VISIBLE);
                syncFloatView(true);
            }
        });
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

    private void fillTabLayout(final int pos) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final BitmapDrawable bitmapDrawable = getDrawableWithBitmap(pos);
                    final TabLayout.Tab tab = mEggTab.getTabAt(pos);
                    final View view = View.inflate(GameActivity.this, R.layout.item_tab, null);
                    ImageView iv = (ImageView) view.findViewById(R.id.iv_egg);
                    iv.setImageDrawable(bitmapDrawable);
                    if (tab != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tab.setCustomView(view);
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

    public List<EggImage> getEggImageList() {
        return mEggImageList;
    }

    private void handleDeleteEvent() {
        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFloatView();
            }
        });
    }

    private void handleBackEvent() {
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBackDialog();
            }
        });
    }

    private void showBackDialog() {
        DialogUtils.showContentDialog(GameActivity.this, getString(R.string.game_quit_hint),
                getString(R.string.cancel), getString(R.string.confirm),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        GameActivity.this.finish();
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    private void handleShowEggEvent() {
        mShowEggBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEggsContainer.setVisibility(View.VISIBLE);
                positionAlphaController(RULE_ABOVE_EGG);
            }
        });
    }

    private void handleEditGameEvent() {
        mSharedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCoinError) {
                    toastHint(R.string.coin_error_hint);
                    return;
                }
                if (mEggsCount < EGG_MIN_COUNT) {
                    toastHint(R.string.add_egg_less_hint);
                    return;
                }
                Intent intent = new Intent(GameActivity.this, GameEditDetailActivity.class);
                intent.putExtra(CommonConstants.CONTENT_ID, mUCGContent.getContentId());
                startActivity(intent);
                sendUCGData();
            }
        });
    }

    private void sendUCGData() {
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

    private void resetFloatView() {
        mFloatView.setImageBitmap(null);
        syncFloatView(false);
        mUrl = null;
    }

    private void updateEggsCountHint(int count) {
        mShowEggBtn.setText(String.format(getResources().getString(R.string.eggs_count), count));
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

    private String getUrl() {
        if (mFrom.equals(CommonConstants.GAME))
            return URLConstant.URL_WEBVIEW_GAME + mContentId;
        else
            return URLConstant.URL_WEBVIEW_TOPIC + mContentId;
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
                            final float alpha) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFloatView.setImageBitmap(null);
                mFloatView.setFloatView(bitmap, scale, rotate);
                mFloatView.setFloatAlpha(alpha);
                mAlphaBar.setProgress((int) (alpha * 100));
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PublishEvent event) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void log(String log, Object... msg) {
        try {
            if (DEBUG) {
                String result = String.format(log, msg);
                Log.d(TAG, result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toastHint(int resId) {
        Toast.makeText(GameActivity.this, resId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showBackDialog();
        }
        return false;
    }
}

//    js 调app 用editItem('contentId','sceneId','order')
//    app 调 js 用 dropItem('contentId' , 'itemId','trans'  ) 返回 array ('x', 'y', 'scale')
