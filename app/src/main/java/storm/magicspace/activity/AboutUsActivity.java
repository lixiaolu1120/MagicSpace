package storm.magicspace.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import storm.commonlib.common.CommonConstants;
import storm.commonlib.common.base.BaseASyncTask;
import storm.commonlib.common.base.BaseActivity;
import storm.magicspace.R;
import storm.magicspace.http.HTTPManager;
import storm.magicspace.http.URLConstant;
import storm.magicspace.http.reponse.ShareUrlResponse;

/**
 * Created by py on 2016/6/16.
 */
public class AboutUsActivity extends BaseActivity {
    private ImageView iv_Avatar;
    private RelativeLayout rl_share, rl_assessment;
    private TextView tv_version;

    public AboutUsActivity() {
        super(R.layout.activity_about_us, CommonConstants.ACTIVITY_STYLE_WITH_TITLE_BAR);
    }

    @Override
    public void initView() {
        super.initView();
        iv_Avatar = findView(R.id.iv_Avatar);
        tv_version = findView(R.id.tv_version);
        rl_assessment = findEventView(R.id.rl_assessment);
        rl_share = findEventView(R.id.rl_share);
        setActivityTitle(getResources().getString(R.string.about_us));
        setTitleRightBtVisibility(View.GONE);
        tv_version.setText("魔fun version " + getVersion());
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, AboutUsActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onLocalClicked(int resId) {
        super.onLocalClicked(resId);
        switch (resId) {
            case R.id.rl_share:
//                showShare();
//                setShare();
                new getUrl().execute();
                break;


            case R.id.rl_assessment:

                break;

            default:
                break;
        }
    }

    private void showShare() {
        ShareSDK.initSDK(AboutUsActivity.this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

// 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle("魔fun全景挖彩蛋");
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl(URLConstant.SHARE_US_URL);
        // text是分享文本，所有平台都需要这个字段
        oks.setText(getString(R.string.shared_content));
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        //oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(URLConstant.SHARE_US_URL);

// 启动分享GUI
        oks.show(AboutUsActivity.this);
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "获取版本号失败";
        }
    }

    public void setShare(String url) {

        final SHARE_MEDIA[] displaylist = new SHARE_MEDIA[]
                {
                        SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.SINA,
                        SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE
                };
        new ShareAction(this).setDisplayList(displaylist)
                .withText(getString(R.string.shared_content))
                .withTitle("魔fun全景挖彩蛋")
//                .withTargetUrl("http://app.stemmind.com/vr/a/tour.html")
                .withTargetUrl(url)
                .open();
    }

    private class getUrl extends BaseASyncTask<Void, ShareUrlResponse> {
        @Override
        public ShareUrlResponse doRequest(Void param) {
            return HTTPManager.getUrl();
        }

        @Override
        public void onSuccess(ShareUrlResponse response) {
            super.onSuccess(response);
            setShare(response.data.url == null ? "" : response.data.url);
        }

        @Override
        public void onFailed() {
            super.onFailed();
        }
    }
}
