package com.wtz.expiredate.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wtz.expiredate.R;
import com.wtz.expiredate.data.GoodsItem;

import java.io.File;
import java.util.List;

public class ListAdapter extends BaseAdapter {
    private final static String TAG = ListAdapter.class.getName();

    private Context mContext;

    private List<GoodsItem> mList;

    private LayoutParams itemLayoutParams = null;

    public ListAdapter(Context context, List<GoodsItem> list) {
        mContext = context;
        mList = list;
    }

    public void update(List<GoodsItem> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public List<GoodsItem> getList() {
        return mList;
    }

    @Override
    public int getCount() {
        return (mList == null) ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return (mList == null) ? null : mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == mContext) {
            Log.d(TAG, "getView...null == mContext");
            return null;
        }

        if (null == mList || mList.isEmpty()) {
            Log.d(TAG, "getView...list isEmpty");
            return null;
        }

        ViewHolder itemLayout = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_layout, null);
            itemLayout = new ViewHolder();
            itemLayout.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            itemLayout.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            itemLayout.tvCount = (TextView) convertView.findViewById(R.id.tv_count);
            itemLayout.tvStatus = (TextView) convertView.findViewById(R.id.tv_status);
            convertView.setTag(itemLayout);
        } else {
            itemLayout = (ViewHolder) convertView.getTag();
        }

        GoodsItem item = null;
        if ((item = mList.get(position)) != null) {
            itemLayout.tvName.setText(item.getName());

            String formatCount = mContext.getString(R.string.format_count);
            itemLayout.tvCount.setText(String.format(formatCount, item.getCount()));

            if (item.isExpired()) {
                convertView.setBackgroundColor(Color.parseColor("#FFFACD"));
                int days = (int) ((System.currentTimeMillis() - item.getExpireDate()) / 86400000);
                String format = mContext.getString(R.string.format_expired);
                String result = String.format(format, days);
                itemLayout.tvStatus.setText(result);
                itemLayout.tvStatus.setTextColor(Color.parseColor("#FF0000"));
            } else {
                convertView.setBackgroundColor(Color.parseColor("#C1FFC1"));
                int days = (int) ((item.getExpireDate() - System.currentTimeMillis()) / 86400000);
                String format = mContext.getString(R.string.format_remain_days);
                String result = String.format(format, days);
                itemLayout.tvStatus.setText(result);
                itemLayout.tvStatus.setTextColor(Color.parseColor("#000000"));
            }

            if (!TextUtils.isEmpty(item.getIconPath())) {
                Picasso.with(mContext)
                        .load(new File(item.getIconPath()))
                        .placeholder(R.mipmap.unkown)
                        .into(itemLayout.ivIcon);
            } else {
                itemLayout.ivIcon.setImageResource(R.mipmap.unkown);
            }
        }

        return convertView;
    }

    class ViewHolder {
        TextView tvName;
        TextView tvCount;
        TextView tvStatus;
        ImageView ivIcon;
    }

}
