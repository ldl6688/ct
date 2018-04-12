package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @ Autheor:ldl
 * @Description:
 * @Date: 2018/3/20 22:06
 * @Modified By:
 */

public class ScanRowKeyUtil {

    private String telephone;
    private String startDateString;
    private String stopDateString;
    List<String[]> list = null;

    int index = 0;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");


    public ScanRowKeyUtil(String telephone, String startDateString, String stopDateString) {
        this.telephone = telephone;
        this.startDateString = startDateString;
        this.stopDateString = stopDateString;
        list = new ArrayList<>();

        genRowKey();
    }

    public void genRowKey() {
        //获取分区数
        int regions = Integer.valueOf(PropertiesUtil.getProperty("hbase.calllog.regions"));

        //把传入的时间字符串转换成日期对象
        try {
            Date startDate = sdf.parse(startDateString);
            Date stopDate = sdf.parse(stopDateString);

            //获取当系统时间,设置当前一个月的开始时间和结束时间
            Calendar currentStartCalendar = Calendar.getInstance();
            currentStartCalendar.setTimeInMillis(startDate.getTime());

            Calendar currentStopCalendar = Calendar.getInstance();
            currentStopCalendar.setTimeInMillis(startDate.getTime());
            currentStopCalendar.add(Calendar.MONTH, 1);


            //循环获取要查询的时间段内的通话信息
            while (currentStopCalendar.getTimeInMillis() <= stopDate.getTime()) {
                String regionCode = HBaseUtil.genRowKeyPartitionCode(telephone, sdf2.format(new Date(currentStartCalendar.getTimeInMillis())), regions);
                // 01_15837312345_201711
                //组装要查询的开始时间rowKey和结束时间rowKey
                String startRowKey = regionCode + "_" + telephone + "_" + sdf2.format(new Date(currentStartCalendar.getTimeInMillis()));
                String stopRowKey = regionCode + "_" + telephone + "_" + sdf2.format(new Date(currentStopCalendar.getTimeInMillis()));

                String[] rowkeys = {startRowKey, stopRowKey};
                list.add(rowkeys);
                currentStartCalendar.add(Calendar.MONTH, 1);
                currentStopCalendar.add(Calendar.MONTH, 1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    /**
     * 判断list集合中是否还有下一组rowkey
     * @return
     */
    public boolean hasNext() {
        if(index < list.size()){
            return true;
        }else{
            return false;
        }
    }


    /**
     *  取出list集合中存放的下一组rowkey
     * @return
     */
    public String[] next() {
        String[] rowkeys = list.get(index);
        index++;
        return rowkeys;
    }
}
