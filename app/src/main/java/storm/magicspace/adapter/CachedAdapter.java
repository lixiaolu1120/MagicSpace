package storm.magicspace.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import storm.commonlib.common.CommonConstants;
import storm.magicspace.R;
import storm.magicspace.activity.GameActivity;
import storm.magicspace.bean.Album;
import storm.magicspace.view.AlbumPicView;

/**
 * Created by gdq on 16/6/29.
 */
public class CachedAdapter extends BaseAdapter {
    List<Album> list;
    Context context;

    public CachedAdapter(List<Album> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        Log.d("zzz", " getCount 个数：" + list.size());
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.view_cached, null);
            holder = new ViewHolder();
            holder.albumPicView = (AlbumPicView) convertView.findViewById(R.id.apv_left);
            holder.nameTv = (TextView) convertView.findViewById(R.id.name);
            holder.descTv = (TextView) convertView.findViewById(R.id.desc);
            holder.countTv = (TextView) convertView.findViewById(R.id.download_cgiount);
            holder.btnTv = (TextView) convertView.findViewById(R.id.btn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (list.get(position) != null) {
            Album album = list.get(position);
            Picasso.with(context).load(album.getThumbImageUrl() == null ? "" : album.getThumbImageUrl()).into(holder.albumPicView.getBgIv());
            holder.albumPicView.setCollectTimes(album.getCommentCount());
            holder.albumPicView.setSupportTimes(album.getAppreciateCount());
            holder.nameTv.setText(album.getNickName() == null ? "" : album.getNickName());
            holder.descTv.setText(album.getDescription() == null ? "" : album.getDescription());
            holder.countTv.setText("");
            holder.btnTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(context, "跳转开发中", 1).show();
                    Intent intent = new Intent(context, GameActivity.class);
                    intent.putExtra(CommonConstants.CONTENT_ID, list.get(position).getContentId());
                    intent.putExtra(CommonConstants.FROM, CommonConstants.TOPIC);
                    context.startActivity(intent);
                }
            });
        }
        return convertView;
    }

    private class ViewHolder {
        public AlbumPicView albumPicView;
        public TextView nameTv;
        public TextView descTv;
        public TextView countTv;
        public TextView btnTv;
    }
}
