package storm.magicspace.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import storm.commonlib.common.base.BaseASyncTask;
import storm.commonlib.common.base.BaseFragment;
import storm.magicspace.R;
import storm.magicspace.activity.EggGameInfoActivity;
import storm.magicspace.activity.EggGamePreviewActivity;
import storm.magicspace.adapter.EggAdapter;
import storm.magicspace.bean.EggInfo;
import storm.magicspace.http.HTTPManager;
import storm.magicspace.http.reponse.EggHttpResponse;

/**
 * Created by gdq on 16/6/15.
 */
public class EggFragment extends BaseFragment {
    private ListView lv_egg;
    private List<EggInfo> egginfoList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_egg, null);
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        lv_egg = this.findItemEventView(view, R.id.lv_egg);


    }

    @Override
    public void initData() {
        super.initData();
        new getEggTask().execute();
    }

    @Override
    public void onLocalItemClicked(AdapterView<?> parent, View view, int position, long id) {
        super.onLocalItemClicked(parent, view, position, id);
        goToNext(EggGameInfoActivity.class);
    }

    private class getEggTask extends BaseASyncTask<Void, EggHttpResponse> {
        @Override
        public EggHttpResponse doRequest(Void param) {
            return HTTPManager.getEggList();
        }

        @Override
        public void onSuccess(EggHttpResponse response) {
            super.onSuccess(response);
            egginfoList.clear();
            egginfoList.addAll(response.data);
            EggAdapter adapter = new EggAdapter(getActivity(), egginfoList);
            lv_egg.setAdapter(adapter);
        }

        @Override
        public void onFailed() {
            super.onFailed();
        }
    }


}
