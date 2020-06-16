package com.safety.android.Food;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;
import com.safety.android.http.FlickrFetch;
import com.safety.android.http.OKHttpFetch2;
import com.safety.android.tools.MyHolder;
import com.safety.android.tools.MyTestUtil;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;

public class FoodClassifyActivity extends AppCompatActivity {

    private View rootView;

    private ViewGroup containerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        LayoutInflater inflater = getLayoutInflater();

        rootView = inflater.inflate(R.layout.fragment_default, null, false);
        containerView = (ViewGroup) rootView.findViewById(R.id.container);

        new FetchItemsTask().execute();

        setContentView(rootView);


    }


    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {

            return new OKHttpFetch2(getApplicationContext()).get(FlickrFetch.base + "/tree/tree/selectTree");

        }


        @Override
        protected void onPostExecute(String json) {


            try {

                    JSONObject jsonObject = new JSONObject(json);

                    TreeNode root = TreeNode.root();

                    TreeNode parent = addTree(jsonObject,0);


              /*  MyHolder.IconTreeItem nodeItem = new MyHolder.IconTreeItem();
                nodeItem.setText("aaaaaaaa");
                nodeItem.setIcon(R.drawable.qmui_icon_popup_close);
                TreeNode parent = new TreeNode(nodeItem).setViewHolder(new MyHolder(getApplicationContext()));*/

                    root.addChild(parent);

                    AndroidTreeView tView = new AndroidTreeView(getApplicationContext(), root);

                    containerView.addView(tView.getView());



            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    TreeNode addTree(JSONObject jsonObject,int indent){

        indent++;

        String name = null;
        String childrenString;
        JSONArray children = null;
        Integer id=null;
        try {
            name = (String) jsonObject.get("name");

            id=jsonObject.getInt("id");

            childrenString = jsonObject.getString("children");

            children = new JSONArray(childrenString);
        }catch (Exception e) {
            //e.printStackTrace();
        }

        MyTestUtil.print(jsonObject);

            MyHolder.IconTreeItem nodeItem = new MyHolder.IconTreeItem();
            nodeItem.setId(id);
            nodeItem.setText(name);
            nodeItem.setIcon(android.R.drawable.arrow_down_float);
            nodeItem.setIndent(indent);
            nodeItem.setMore(true);

            TreeNode parent = new TreeNode(nodeItem).setViewHolder(new MyHolder(getApplicationContext()));

            if(children!=null){

                for(int i=0;i<children.length();i++){
                    JSONObject jsonObject2 = null;
                    JSONArray children2 = null;
                    TreeNode child = null;
                    String name2=null;
                    Integer id2=null;
                    try {
                        jsonObject2=children.getJSONObject(i);
                        name2=jsonObject2.getString("name");
                        id2=jsonObject2.getInt("id");
                        children2 = jsonObject2.getJSONArray("children");

                    }catch (Exception e){
                        //e.printStackTrace();
                    }

                    if (children2 != null) {
                        child = addTree(jsonObject2,indent);
                        parent.addChild(child);
                    }else{
                        MyHolder.IconTreeItem cNodeItem = new MyHolder.IconTreeItem();

                        cNodeItem.setText(name2);
                        cNodeItem.setId(id2);
                        cNodeItem.setIndent(indent);
                        cNodeItem.setMore(false);
                        child = new TreeNode(cNodeItem).setViewHolder(new MyHolder(getApplicationContext()));

                        child.setClickListener(new TreeNode.TreeNodeClickListener() {
                            @Override
                            public void onClick(TreeNode node, Object value) {
                                //System.out.println("id======"+node.getId()+"      "+node.getLevel());
                                // MyTestUtil.print(value);
                                MyHolder.IconTreeItem iconTreeItem = (MyHolder.IconTreeItem) value;
                                System.out.println("id======" + iconTreeItem.getId() + "      " + iconTreeItem.getText());

                                JSONObject json = new JSONObject();

                                try {
                                    json.put("catalog",iconTreeItem.getId());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Intent intent = new Intent(getApplicationContext(), FoodListActivity.class);
                                intent.putExtra("jsonString", json.toString());
                                startActivityForResult(intent, 1);
                            }

                        });

                        parent.addChild(child);
                    }

                }

            }

            return parent;

        
    }

}

