package storm.magicspace.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import storm.commonlib.common.BaseApplication;
import storm.commonlib.common.base.BaseASyncTask;
import storm.commonlib.common.base.BaseFragment;
import storm.commonlib.common.view.RoundedImageView;
import storm.magicspace.R;
import storm.magicspace.activity.FreshHelpActivity;
import storm.magicspace.activity.mine.MyCollectionActivity;
import storm.magicspace.activity.mine.MyWorksActivity;
import storm.magicspace.bean.CirclePic;
import storm.magicspace.bean.UserInfo;
import storm.magicspace.bean.httpBean.UserInfoResponse;
import storm.magicspace.http.Conpon;
import storm.magicspace.http.HTTPManager;
import storm.magicspace.http.reponse.ConponResponse;
import storm.magicspace.util.LocalSPUtil;
import storm.magicspace.view.MineShowView;

public class MyFragment extends BaseFragment implements ViewPager.OnPageChangeListener {

    private EditText nameEt;
    private RoundedImageView advator;
    private TextView money;
    private TextView level;
    private TextView editNickNameTv;
    private ViewPager showPage;

    private List<CirclePic> circlePicList = new ArrayList<>();
    private List<MineShowView> imageViews = new ArrayList<>();
    private Gallery gallery;
    private List<Conpon> conponList;
    private GalleryAdapter adapter;
    private List<Conpon> tempConponList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mine, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gallery = findView(view, R.id.mine_gallery);
        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedPosition(position);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void initData() {
        super.initData();


        new GetcouponListTask().execute();
//        new CirclePicTask().execute();
        GetAccountInfoTask task = new GetAccountInfoTask(getActivity());
        task.execute();
    }


    @Override
    public void initView(View view) {
        super.initView(view);

        editNickNameTv = findEventView(view, R.id.edit_my_account);
        findEventView(view, R.id.my_siv_wroks);
        findEventView(view, R.id.my_siv_collection);
        findEventView(view, R.id.my_siv_fresh_help);
        nameEt = findView(view, R.id.mine_tv_name);
        advator = findView(view, R.id.mine_ri_avatar);
        money = findView(view, R.id.money);
        level = findView(view, R.id.level);
        showPage = findView(view, R.id.show_view_page);
        showPage.setOnPageChangeListener(this);


    }


    @Override
    public void onLocalClicked(int resId) {
        super.onLocalClicked(resId);

        switch (resId) {
            case R.id.my_siv_wroks:
                goToNext(MyWorksActivity.class);
                break;

            case R.id.my_siv_collection:
                goToNext(MyCollectionActivity.class);
                break;

            case R.id.edit_my_account:
                nameEt.setEnabled(true);
                break;

            case R.id.my_siv_fresh_help:
                goToNext(FreshHelpActivity.class);
                break;
        }
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class GetAccountInfoTask extends BaseASyncTask<Void, UserInfoResponse> {

        public GetAccountInfoTask(Context context) {
            super(context, true);
        }

        @Override
        public UserInfoResponse doRequest(Void param) {
            return HTTPManager.getAccountInfo();
        }

        @Override
        public void onSuccess(UserInfoResponse userInfoResponse) {
            super.onSuccess(userInfoResponse);

            UserInfo data = userInfoResponse.data;
            if (data == null) {
                Toast.makeText(BaseApplication.getApplication(), "参数返回错误！", Toast.LENGTH_SHORT).show();
                return;
            }

            money.setText(data.getTotalCredit());
            level.setText(data.getTotalCredit());

            Picasso.with(getActivity()).load(data.getPortraitImage()).into(advator);
            nameEt.setText(LocalSPUtil.getAccountInfo().getUser_name());
        }
    }

    private class GetcouponListTask extends BaseASyncTask<Void, ConponResponse> {
        @Override
        public ConponResponse doRequest(Void param) {
            return HTTPManager.GetcouponList();
        }

        @Override
        public void onSuccess(ConponResponse conponResponse) {
            super.onSuccess(conponResponse);
            imageViews.clear();
//            title.setText(circlePicList.get(0).getTitle());
            conponList = conponResponse.data;
            tempConponList = conponList;
            if (conponList == null) return;
            adapter = new GalleryAdapter(getActivity(), conponList, conponList.size() / 2);
            gallery.setAdapter(adapter);
        }
    }

}
