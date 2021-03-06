package storm.magicspace.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import storm.commonlib.common.CommonConstants;
import storm.commonlib.common.util.JsonUtil;
import storm.commonlib.common.util.SharedPreferencesUtil;
import storm.magicspace.R;
import storm.magicspace.activity.album.AlbumInfoActivity;
import storm.magicspace.bean.Album;
import storm.magicspace.view.AlbumPicView;

import static storm.commonlib.common.CommonConstants.FROM;

/**
 * Created by gdq on 16/6/22.
 */
public class OnlineRVAdapter extends RecyclerView.Adapter<OnlineRVAdapter.ViewHolder> {
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_NORMAL = 2;
    private static final int TYPE_FOOTER = 3;
    private List<Album> list;
    private Context context;
    private OnRecyclerViewClickListener onRecyclerViewClickListener;
    private boolean isLimit = false;
    private View mHeaderView = null;
    private View mFooterView = null;
    private int limit;

    public OnlineRVAdapter(List<Album> list, Context context) {
        this.list = list;
        this.context = context;
        saveContentIds(list);
    }

    public void setLimit(int limit) {
        isLimit = true;
        this.limit = limit;
    }

    public void setHeaderView(View header) {
        this.mHeaderView = header;
    }

    public void setFooterView(View footer) {
        this.mFooterView = footer;
        hideFooter();
    }

    public void showFooter() {
        mFooterView.setVisibility(View.VISIBLE);
    }

    public void hideFooter() {
        mFooterView.setVisibility(View.GONE);
    }

    public boolean isHeader(int position) {
        return getItemViewType(position) == TYPE_HEADER;
    }

    public boolean isFooter(int position) {
        return getItemViewType(position) == TYPE_FOOTER;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mHeaderView != null) return TYPE_HEADER;
        else if (position == getItemCount() - 1 && mFooterView != null) return TYPE_FOOTER;
        else return TYPE_NORMAL;
    }

    @Override
    public OnlineRVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderView != null && viewType == TYPE_HEADER) return new ViewHolder(mHeaderView);
        if (mFooterView != null && viewType == TYPE_FOOTER) return new ViewHolder(mFooterView);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_album_online, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (isHeader(position)) {
            return;
        }
        if (isFooter(position)) {
            return;
        }
        final int pos;
        if (mHeaderView == null) {
            pos = position;
        } else {
            pos = position - 1;
        }
        final Album item = list.get(pos);
        Picasso.with(context).load(item.getThumbImageUrl()).into(holder.albumPicView.getBgIv());
        holder.descTv.setText(list.get(pos).getDescription());
        holder.nameTv.setText(list.get(pos).getTitle());
        holder.albumPicView.setSupportTimes(item.getAppreciateCount());
        holder.albumPicView.setCollectTimes(item.getCommentCount());
        holder.father.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AlbumInfoActivity.class);
                intent.putExtra("album", list.get(pos));
                intent.putExtra(FROM, CommonConstants.TOPIC);
                context.startActivity(intent);
                onRecyclerViewClickListener.onItemClick(pos);
            }
        });
//
        holder.downloadIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onRecyclerViewClickListener.onBtnClick(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
//        return  20;
        if (list == null) return 0;
        if (isLimit) {
            if (list.size() > limit) {
                return getItemCountByLimit(limit);
            } else {
                return getItemCountByLimit(list.size());
            }
        } else {
            return getItemCountByLimit(list.size());
        }
//        return list == null ? 0 : (isLimit ? (mHeaderView == null ? (list.size() <= 6 ? list.size() : 6) : (list.size() <= 6 ? list.size() + 1 : 7)) : (mHeaderView == null ? list.size() : list.size() + 1));
    }

    private int getItemCountByLimit(int count) {
        if (mHeaderView == null && mFooterView == null)
            return count;
        else if ((mHeaderView == null && mFooterView != null) || (mHeaderView != null && mFooterView == null))
            return count + 1;
        else return count + 2;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        AlbumPicView albumPicView;
        private TextView nameTv;
        private TextView descTv;
        private ImageView downloadIv;
        private ImageView imageView;
        private LinearLayout father;

        public ViewHolder(View itemView) {
            super(itemView);
            if (itemView == mHeaderView || itemView == mFooterView) return;
            albumPicView = (AlbumPicView) itemView.findViewById(R.id.picview);
            nameTv = (TextView) itemView.findViewById(R.id.name);
            descTv = (TextView) itemView.findViewById(R.id.desc);
            downloadIv = (ImageView) itemView.findViewById(R.id.iv_down_load);
            father = (LinearLayout) itemView.findViewById(R.id.father);
        }
    }

    public void update(List<Album> albumList) {
        list.clear();
        list.addAll(albumList);
        saveContentIds(albumList);
        notifyDataSetChanged();
    }

    private void saveContentIds(List<Album> albumList) {
        if (albumList != null && albumList.size() > 0) {
            ArrayList<String> contentIds = new ArrayList<>();
            int size = albumList.size();
            for (int i = 0; i < size; i++) {
                Album album = albumList.get(i);
                String contentId = album.getContentId();
                contentIds.add(contentId);
            }
            if (contentIds.size() > 0) {
                SharedPreferencesUtil.saveJsonInSharedPreferences(context,
                        CommonConstants.CONTEND_IDS, JsonUtil.toJson(contentIds));
            }
        }
    }

    public void setOnRecyclerViewClickListener(OnRecyclerViewClickListener onRecyclerViewClickListener) {
        this.onRecyclerViewClickListener = onRecyclerViewClickListener;
    }

    public interface OnRecyclerViewClickListener {
        void onItemClick(int position);

        void onBtnClick(int position);
    }
}

