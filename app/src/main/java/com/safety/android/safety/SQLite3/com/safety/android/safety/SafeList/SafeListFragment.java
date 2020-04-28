package com.safety.android.safety.SQLite3.com.safety.android.safety.SafeList;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.safety.android.safety.SQLite3.com.safety.android.safety.SQLite3.SafeInfo;
import com.safety.android.safety.SQLite3.com.safety.android.safety.SQLite3.SafeLab;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Created by WangJing on 2017/5/28.
 */

public class SafeListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE="subtitle";

    private static final int REQUEST_CRIME=1;

    private RecyclerView mCrimeRecyclerView;
    private SafeAdapter mAdapter;
    private boolean mSubtitleVisible;
    private Callbacks mCallbacks;

    public interface Callbacks{
        void onSafeSelected(SafeInfo safeInfo);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mCallbacks= (Callbacks) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private List<SafeInfo> mSafeInfos;
    protected int notify_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.fragment_safe_list,container,false);

        mCrimeRecyclerView= (RecyclerView) view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if(savedInstanceState!=null){
            mSubtitleVisible=savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();

        return  view;
    }

    @Override
    public void onResume(){
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE,mSubtitleVisible);
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mCallbacks=null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.fragment_safe_list,menu);

        MenuItem subtitleItem=menu.findItem(R.id.menu_item_show_subtitle);
        if(mSubtitleVisible){
            subtitleItem.setTitle(R.string.hide_subtitle);
        }else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_item_new_crime:
                SafeInfo safeInfo=new SafeInfo();
                SafeLab.get(getActivity()).addSafeInfo(safeInfo);
              //  Intent intent=CrimePagerActivity.newIntent(getActivity(),crime.getId());
              //  startActivity(intent);
                updateUI();
                mCallbacks.onSafeSelected(safeInfo);
                return true;
            case R.id.menu_item_show_subtitle:
                mSubtitleVisible=!mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void updateUI() {
        SafeLab safeLab = SafeLab.get(getActivity());
        List<SafeInfo> safeInfos = safeLab.getSafeInfo();
        mSafeInfos = safeInfos;
        int crimesCount = mSafeInfos.size();

        if (crimesCount != 0) {
            if (mAdapter == null) {
                mAdapter = new SafeAdapter(mSafeInfos);
                mCrimeRecyclerView.setAdapter(mAdapter);
             } else {
            // mAdapter.notifyItemChanged(notify_id);
                mAdapter.setmSafes(mSafeInfos);
               mAdapter.notifyDataSetChanged();
            }
        }else {
          NewPageAdapter newPageAdapter=new NewPageAdapter();
           mCrimeRecyclerView.setAdapter(newPageAdapter);
        }
       updateSubtitle();

    }

    private void updateSubtitle(){
        SafeLab crimeLab= SafeLab.get(getActivity());
        int crimeSize=crimeLab.getSafeInfo().size();
        //String subtitle=getString(R.string.subtitle_format,crimeCount);
      //  String subtitle=getResources().getQuantityString(crimeSize,crimeSize);

        if(!mSubtitleVisible){
      //      subtitle=null;
        }

      //  AppCompatActivity activity= (AppCompatActivity) getActivity();
     //   activity.getSupportActionBar().setSubtitle(subtitle);
    }

    private class SafeHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener{
        private SafeInfo mSafeInfo;
       // public TextView mTitleTextView;
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private CheckBox mSolvedCheckBox;

        public SafeHolder(View itemView){
            super(itemView);
            itemView.setOnClickListener(this);

          //  mTitleTextView= (TextView) itemView;
            mTitleTextView= (TextView) itemView.findViewById(R.id.list_item_safe_title_text_view);
            mDateTextView= (TextView) itemView.findViewById(R.id.list_item_safe_date_text_view);
            mSolvedCheckBox= (CheckBox) itemView.findViewById(R.id.list_item_safe_solved_check_box);

            mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
                    SafeLab safeLab= SafeLab.get(getActivity());
                    safeLab.updateCrime(mSafeInfo);
                    mCallbacks.onSafeSelected(mSafeInfo);

                }
            });
        }

        public void bindSafe(SafeInfo safeInfo){
            mSafeInfo=safeInfo;

            Calendar calendar= Calendar.getInstance();
            calendar.setTime(mSafeInfo.getmDate());
            String week=swicthWeek(calendar.get(Calendar.DAY_OF_WEEK)-1);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 星期"+week +" HH时mm分ss秒");
            String date=sdf.format(mSafeInfo.getmDate());

            mTitleTextView.setText(mSafeInfo.getTitle());
            mDateTextView.setText(date);
            mSolvedCheckBox.setChecked(mSafeInfo.ismSolved());
        }

        String swicthWeek(int i){
            String week="";
            switch (i){
                case 0:week="天";break;
                case 1:week="一";break;
                case 2:week="二";break;
                case 3:week="三";break;
                case 4:week="四";break;
                case 5:week="五";break;
                case 6:week="六";break;
            }
            return week;
        }

        @Override
        public void onClick(View view) {
            //Intent intent=SafeActivity.newIntent(getActivity(),mSafe.getId());
           // Intent intent=SafePagerActivity.newIntent(getActivity(),mSafe.getId());
            UUID uuid=mSafeInfo.getmId();
            for(int i=0;i<mSafeInfos.size();i++){
                if(mSafeInfos.get(i).getmId().equals(uuid)){
                    notify_id=i;
                    break;
                }
            }

           // startActivityForResult(intent,REQUEST_Safe);
            mCallbacks.onSafeSelected(mSafeInfo);
        }


    }

    private class SafeAdapter extends RecyclerView.Adapter<SafeHolder>
     {
        private List<SafeInfo> mSafeInfos;

        public SafeAdapter(List<SafeInfo> safeInfos){
            mSafeInfos=safeInfos;
        }

        @Override
        public SafeHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater layoutInflater= LayoutInflater.from(getActivity());
            View view=layoutInflater
                    .inflate(R.layout.list_item_safe,parent,false);
            return new SafeHolder(view);
        }

        @Override
        public void onBindViewHolder(SafeHolder holder,int position){
            SafeInfo mSafeInfo=mSafeInfos.get(position);
            holder.bindSafe(mSafeInfo);
        }

        @Override
        public int getItemCount(){
            return mSafeInfos.size();
        }

        public void setmSafes(List<SafeInfo> safeInfos){
            mSafeInfos=safeInfos;
        }
    }

    private class NewPagerHoler extends RecyclerView.ViewHolder{
        private Button mSafeNew;

        public NewPagerHoler(View itemView) {
            super(itemView);
            mSafeNew= (Button) itemView.findViewById(R.id.safe_new);

            mSafeNew.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SafeInfo mSafeInfo=new SafeInfo();
                    SafeLab.get(getActivity()).addSafeInfo(mSafeInfo);
                    Intent intent= SafePagerActivity.newIntent(getActivity(),mSafeInfo.getmId());
                    startActivity(intent);
                }
            });
        }
    }

    private class NewPageAdapter extends RecyclerView.Adapter<NewPagerHoler>{


        @Override
        public NewPagerHoler onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater= LayoutInflater.from(getActivity());
            View view=layoutInflater.inflate(R.layout.fragment_null,parent,false);
            return new NewPagerHoler(view);
        }

        @Override
        public void onBindViewHolder(NewPagerHoler holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 1;         //生成adapter的数量
        }
    }

}
