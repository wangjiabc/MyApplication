
package com.safety.android.ReportDetail;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.XLabels;
import com.github.mikephil.charting.utils.XLabels.XLabelPosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class AnotherBarActivity extends AppCompatActivity {

    private LineChart mLineChart;
    // private Typeface mTf;

    public BarChart barChart;

    public BarDataSet dataset ,dataset2,dataset3;

    private PieChart mChart;

    private boolean isRunning;
    private Thread thread;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barchart);

        mLineChart = (LineChart) findViewById(R.id.chart1);
        showChart(getLineData(12, 2000));


        barChart = (BarChart) findViewById(R.id.spread_line_chart);
        barChart.zoom(3.0f,1.0f,0f,0f);
        Map map=initEntriesData();  //添加Y轴数据
        ArrayList<BarEntry> entries= (ArrayList<BarEntry>) map.get("entries");
        ArrayList<BarEntry> entries2= (ArrayList<BarEntry>) map.get("entries2");
        ArrayList<BarEntry> entries3= (ArrayList<BarEntry>) map.get("entries3");
        show(entries,entries2,entries3);

        mChart = (PieChart) findViewById(R.id.chart);
        showChart(getPieData());

    }


    private void showChart(LineData lineData) {
        // 设置描述
        mLineChart.setDescription("折线图演示");
        // 设置触摸模式
        mLineChart.setTouchEnabled(true);
        // 设置图表数据
        mLineChart.setData(lineData);
    }

    /**
     * @param count 横向点个数
     * @param range 纵向变化幅度
     * @return
     */
    private LineData getLineData(int count, float range) {
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            xVals.add((i + 1) + "月");
        }

        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            float mult = range / 2f;
            float val = (float) (Math.random() * mult) + 1000;
            yVals.add(new Entry(val, i));
        }

        // 创建数据集
        LineDataSet set = new LineDataSet(yVals, "数据集");
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.YELLOW);
        set.setLineWidth(2f);
        set.setCircleSize(3f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));



        // 创建数据集列表
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set);

        // 创建折线数据对象（第二个参数可以是set）
        LineData lineData = new LineData(xVals, dataSets);

        return lineData;
    }


    public Map initEntriesData() {
        ArrayList<BarEntry> entries = new ArrayList<BarEntry>(); //(x,y1)
        ArrayList<BarEntry> entries2 = new ArrayList<BarEntry>();//(x,y2)
        ArrayList<BarEntry> entries3= new ArrayList<BarEntry>();//(x,y3)
        entries.add(new BarEntry(4f, 0));
        entries.add(new BarEntry(8f, 1));
        entries.add(new BarEntry(6f, 2));
        entries.add(new BarEntry(12f, 3));
        entries.add(new BarEntry(18f, 4));
        entries.add(new BarEntry(9f, 5));
        entries.add(new BarEntry(4f, 6));
        entries.add(new BarEntry(8f, 7));
        entries.add(new BarEntry(6f, 8));
        entries.add(new BarEntry(12f, 9));
        entries.add(new BarEntry(18f, 10));
        entries.add(new BarEntry(9f, 11));

        entries2.add(new BarEntry(5f, 0));
        entries2.add(new BarEntry(6f, 1));
        entries2.add(new BarEntry(7f, 2));
        entries2.add(new BarEntry(5f, 3));
        entries2.add(new BarEntry(13f, 4));
        entries2.add(new BarEntry(12f, 5));
        entries2.add(new BarEntry(5f, 6));
        entries2.add(new BarEntry(6f, 7));
        entries2.add(new BarEntry(7f, 8));
        entries2.add(new BarEntry(5f, 9));
        entries2.add(new BarEntry(13f, 10));
        entries2.add(new BarEntry(12f, 11));

        entries3.add(new BarEntry(8f, 0));
        entries3.add(new BarEntry(4f, 1));
        entries3.add(new BarEntry(15f, 2));
        entries3.add(new BarEntry(12f, 3));
        entries3.add(new BarEntry(12f, 4));
        entries3.add(new BarEntry(1f, 5));
        entries3.add(new BarEntry(8f, 6));
        entries3.add(new BarEntry(4f, 7));
        entries3.add(new BarEntry(15f, 8));
        entries3.add(new BarEntry(12f, 9));
        entries3.add(new BarEntry(12f, 10));
        entries3.add(new BarEntry(1f, 11));

        Map map=new HashMap();
        map.put("entries",entries);
        map.put("entries2",entries2);
        map.put("entries3",entries3);
        return  map;
    }


    public void show(ArrayList<BarEntry> entries,ArrayList<BarEntry> entries2,ArrayList<BarEntry> entries3) {
        dataset = new BarDataSet(entries, "甲");
        dataset.setColor(Color.rgb(255, 48, 48));
        dataset2 = new BarDataSet(entries2, "乙");
        dataset2.setColor(Color.rgb(0, 191, 255));
        dataset3 = new BarDataSet(entries3, "丙");
        dataset3.setColor(Color.rgb(255, 215, 0));

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>(); //坐标线的集合。
        dataSets.add(dataset);
        dataSets.add(dataset2);
        dataSets.add(dataset3);

        ArrayList<String> xVals = new ArrayList<String>(); //x轴坐标
        for(int i = 1;i<13;i++){
            xVals.add("第"+i+"次");
        }


        BarData data3 = new BarData(xVals, dataSets);
        barChart.setData(data3);
        barChart.animateY(2000);//动画效果 y轴方向，2秒


//          chart.animateXY(5000,5000);  //动画效果 xy轴方向，5秒

     //   barChart.setDescription("某某某数据直方图");
    }


    private void showChart(PieData pieData) {
        mChart.setHoleRadius(60f);  //内环半径
        mChart.setTransparentCircleRadius(64f); // 半透明圈半径
        // mChart.setHoleRadius(0);  // 实心圆
        mChart.setDescription("测试饼状图");
        mChart.setDrawCenterText(true);  //饼状图中间可以添加文字
        mChart.setCenterText("2017年季度收入");  //饼状图中间的文字
        mChart.setDrawHoleEnabled(true);
        mChart.setRotationAngle(90); // 初始旋转角度
        mChart.setRotationEnabled(true); // 可以手动旋转
        // mChart.setUsePercentValues(true);  //显示成百分比
        // 设置可触摸
        mChart.setTouchEnabled(true);
        // 设置数据
        mChart.setData(pieData);

        // 取消高亮显示
        mChart.highlightValues(null);
        mChart.invalidate();

        Legend mLegend = mChart.getLegend();  //设置比例图
        mLegend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);  //最右边显示
        mLegend.setForm(Legend.LegendForm.LINE);  //设置比例图的形状，默认是方形
        mLegend.setXEntrySpace(7f);
        mLegend.setYEntrySpace(5f);

        //设置动画
        mChart.animateXY(1000, 1000);
    }

    private PieData getPieData() {
        // xVals用来表示每个饼块上的文字
        ArrayList<String> xValues = new ArrayList<String>();

        for (int i = 0; i < 4; i++) {
            xValues.add((i + 1) + "季度");
        }

        // yVals用来表示封装每个饼块的实际数据
        ArrayList<Entry> yValues = new ArrayList<Entry>();

        // 饼图数据
        float quarterly1 = 456787;
        float quarterly2 = 534283;
        float quarterly3 = 345734;
        float quarterly4 = 658465;

        yValues.add(new Entry(quarterly1, 0));
        yValues.add(new Entry(quarterly2, 1));
        yValues.add(new Entry(quarterly3, 2));
        yValues.add(new Entry(quarterly4, 3));

        // y轴集合
        PieDataSet pieDataSet = new PieDataSet(yValues, "2017年季度收入");
        pieDataSet.setSliceSpace(0f); //设置个饼状图之间的距离

        ArrayList<Integer> colors = new ArrayList<Integer>();

        // 饼图颜色
        colors.add(Color.rgb(205, 205, 205));
        colors.add(Color.rgb(114, 188, 223));
        colors.add(Color.rgb(255, 123, 124));
        colors.add(Color.rgb(57, 135, 200));

        // 设置饼图颜色
        pieDataSet.setColors(colors);

        // 设置选中态多出的长度
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = 5 * (metrics.densityDpi / 160f);
        pieDataSet.setSelectionShift(px);

        // 创建饼图数据
        PieData pieData = new PieData(xValues, pieDataSet);

        return pieData;
    }



}
