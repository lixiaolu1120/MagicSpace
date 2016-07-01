package storm.magicspace.activity.mine;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import storm.commonlib.common.base.BaseASyncTask;
import storm.commonlib.common.base.BaseActivity;
import storm.magicspace.R;
import storm.magicspace.adapter.CachedAdapter;
import storm.magicspace.adapter.WorksAdapter;
import storm.magicspace.bean.Album;
import storm.magicspace.bean.httpBean.MyWorksResponse;
import storm.magicspace.http.HTTPManager;

public class MyWorksActivity extends BaseActivity {
    private ListView listView;
    private WorksAdapter adapter;
    private List<Album> list = new ArrayList<>();

    public MyWorksActivity() {
        super(R.layout.activity_my_works);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityTitle("我的作品");
        setTitleLeftBtVisibility(View.VISIBLE);
        listView = (ListView) findViewById(R.id.listview);
        adapter = new WorksAdapter(list, this);
        listView.setAdapter(adapter);
        GetMyWorksTask task = new GetMyWorksTask();
        task.execute();
    }

    private class GetMyWorksTask extends BaseASyncTask<String, MyWorksResponse> {
        @Override
        public MyWorksResponse doRequest(String param) {
            return HTTPManager.getMyWorks("", "");
        }

        @Override
        protected void onPostExecute(MyWorksResponse myWorksResponse) {
            super.onPostExecute(myWorksResponse);
            list.clear();
            list.addAll(myWorksResponse.data);
            adapter.notifyDataSetChanged();
        }
    }
}
