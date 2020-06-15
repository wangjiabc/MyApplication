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

    private  View view;

    private boolean more;

    @Override
    public View createNodeView(TreeNode node, IconTreeItem value) {
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.layout_profile_node, null, false);
        LinearLayout linearLayout=view.findViewById(R.id.pIcon);
        TextView tvValue = (TextView) view.findViewById(R.id.node_value);
        tvValue.setText(value.text);
        ImageView imageView=view.findViewById(R.id.icon);
        if(value.icon!=null) {
            Drawable icon = context.getResources().getDrawable(value.icon);
            imageView.setImageDrawable(icon);
        }
        linearLayout.setPadding(40*(value.indent-1),0,0,0);
        more=value.more;
        return view;
    }

    @Override
    public void toggle(boolean active) {
        if(more) {
            if (active) {
                ImageView imageView = view.findViewById(R.id.icon);
                Drawable icon = context.getResources().getDrawable(android.R.drawable.arrow_up_float);
                imageView.setImageDrawable(icon);
            } else {
                ImageView imageView = view.findViewById(R.id.icon);
                Drawable icon = context.getResources().getDrawable(android.R.drawable.arrow_down_float);
                imageView.setImageDrawable(icon);
            }
        }
    }

    public static class IconTreeItem {

        private int id;
        private Integer icon;
        private String text;
        private int  indent;
        private boolean more;

        public IconTreeItem() {
        }

        public int getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public int getIndent() {
            return indent;
        }

        public Integer getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setIndent(int indent) {
            this.indent = indent;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setMore(boolean more) {
            this.more = more;
        }

        public boolean isMore() {
            return more;
        }
    }
}