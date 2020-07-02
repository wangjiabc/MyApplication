package com.example.myapplication;

import com.safety.android.tools.MyTestUtil;

        import org.junit.Test;

        import java.util.Arrays;
        import java.util.List;

public class quickSort {

    static  void quickSort(List<Integer> arr, int begin, int end)
    {
        //如果区间不只一个数
        if(begin < end)
        {
            int temp = arr.get(begin); //将区间的第一个数作为基准数
            int i = begin; //从左到右进行查找时的“指针”，指示当前左位置
            int j = end; //从右到左进行查找时的“指针”，指示当前右位置
            //不重复遍历
            while(i < j)
            {
                //当右边的数大于基准数时，略过，继续向左查找
                //不满足条件时跳出循环，此时的j对应的元素是小于基准元素的
                while(i<j && arr.get(j) > temp)
                    j--;
                //将右边小于等于基准元素的数填入右边相应位置
                arr.set(i,arr.get(j));
                //当左边的数小于等于基准数时，略过，继续向右查找
                //(重复的基准元素集合到左区间)
                //不满足条件时跳出循环，此时的i对应的元素是大于等于基准元素的
                while(i<j && arr.get(i) <= temp)
                    i++;
                //将左边大于基准元素的数填入左边相应位置
                arr.set(j,arr.get(i));
            }
            //将基准元素填入相应位置
            arr.set(i,temp);
            //此时的i即为基准元素的位置
            //对基准元素的左边子区间进行相似的快速排序
            quickSort(arr,begin,i-1);
            //对基准元素的右边子区间进行相似的快速排序
            quickSort(arr,i+1,end);
        }
        //如果区间只有一个数，则返回
        else
            return;
    }
    @Test
    public static void main(String[] args){
        int num[] = {23,45,17,11,13,89,72,26,3,17,11,13};
        List list= Arrays.asList(num);
        MyTestUtil.print(list);
        quickSort(list,0,list.size());
        MyTestUtil.print(list);
    }

}
