package com.safety.android.tools;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.R;
import com.unnamed.b.atv.model.TreeNode;

public class MyHolder extends TreeNode.BaseNodeViewHolder<MyHolder.IconTreeItem> {

    public MyHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, IconTreeItem value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.layout_profile_node, null, false);
        LinearLayout linearLayout=view.findViewById(R.id.pIcon);
        TextView tvValue = (TextView) view.findViewById(R.id.node_value);
        tvValue.setText(value.text);
        ImageView imageView=view.findViewById(R.id.icon);
        Drawable icon= context.getResources().getDrawable(value.icon);
        imageView.setImageDrawable(icon);
        linearLayout.setPadding(40,0,0,0);
        return view;
    }

    public static class IconTreeItem {
        private int icon;
        private String text;

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}