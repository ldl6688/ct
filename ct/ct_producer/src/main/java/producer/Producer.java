package producer;


import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ Autheor:ldl
 * @Description:
 * @Date: 2018/3/20 10:26
 * @Modified By:
 */

public class Producer {

    //存放联系人电话与姓名的映射
    public Map<String,String> contacts = null;

    //存放联系人电话号码的集合
    public List<String> phoneList = null;

    public static Random random = new Random();

//    public String filePath = "C:\\Users\\Administrator\\Desktop";

    //联系人信息初始化方法
    public void initContacts() {
        contacts = new HashMap<>();
        phoneList = new ArrayList<>();

        phoneList.add("15369468720");
        phoneList.add("19920860202");
        phoneList.add("18411925860");
        phoneList.add("14473548449");
        phoneList.add("18749966182");
        phoneList.add("19379884788");
        phoneList.add("19335715448");
        phoneList.add("18503558939");
        phoneList.add("13407209608");
        phoneList.add("15596505995");
        phoneList.add("17519874292");
        phoneList.add("15178485516");
        phoneList.add("19877232369");
        phoneList.add("18706287692");
        phoneList.add("18944239644");
        phoneList.add("17325302007");
        phoneList.add("18839074540");
        phoneList.add("19879419704");
        phoneList.add("16480981069");
        phoneList.add("18674257265");


        contacts.put("15369468720", "李雁");
        contacts.put("19920860202", "卫艺");
        contacts.put("18411925860", "仰莉");
        contacts.put("14473548449", "陶欣悦");
        contacts.put("18749966182", "施梅梅");
        contacts.put("19379884788", "金虹霖");
        contacts.put("19335715448", "魏明艳");
        contacts.put("18503558939", "华贞");
        contacts.put("13407209608", "华啟倩");
        contacts.put("15596505995", "仲采绿");
        contacts.put("17519874292", "卫丹");
        contacts.put("15178485516", "戚丽红");
        contacts.put("19877232369", "何翠柔");
        contacts.put("18706287692", "钱溶艳");
        contacts.put("18944239644", "钱琳");
        contacts.put("17325302007", "缪静欣");
        contacts.put("18839074540", "焦秋菊");
        contacts.put("19879419704", "吕访琴");
        contacts.put("16480981069", "沈丹");
        contacts.put("18674257265", "褚美丽");
    }


    //随机生成通话时间方法
    public Calendar randomDate(String startDate, String endDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = simpleDateFormat.parse(startDate);
            Date end = simpleDateFormat.parse(endDate);

            if (start.getTime() > end.getTime()) return null;

            long resultTime = start.getTime() +(long) (Math.random() *(end.getTime() - start.getTime()));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(resultTime);

            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }


    //生产一条日志的方法
    /**
     * 随机抽取两个电话号码，
     * 随机产生通话建立时间，
     * 随机通话时长，
     * 将这几个字段拼接成一个字符串，
     * 然后return，
     * 便可以产生一条通话的记录。
     * 需要注意的是，如果随机出的两个电话号码一样，需要重新随机
     * （随机过程可优化，但并非此次重点）。
     * 通话时长的随机为20分钟以内，即：60秒 * 20，并格式化为4位数字，例如：0600(10分钟)
     */
    public String prductLog() {
        int call1Index = random.nextInt(phoneList.size());
        int call2Inedx = -1;
        String call1 = phoneList.get(call1Index);
        String call2 = null;
        while (true) {
            call2Inedx = random.nextInt(phoneList.size());
            call2 = phoneList.get(call2Inedx);
            if (!call2.equals(call1)) break;
        }

        //随即产生通话时长,并格式化为4位
        int duration = (int) (Math.random() * (20 * 60)) + 1;
        String durationString = new DecimalFormat("0000").format(duration);

        //随即产生通话建立时间
        Calendar buildTime = randomDate("2019-01-01", "2020-01-01");
        String buildTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(buildTime.getTime());

        //主叫和被叫人姓名
        String callName = contacts.get(call1);
        String call2Name = contacts.get(call2);

        //两个联系人之间通话日志拼接
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(call1 + ",")
                .append(call2 + ",")
                .append(buildTimeString + ",")
                .append(durationString);

        System.out.println(logBuilder);


        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return logBuilder.toString();

    }

    //把产生的日志写入本地文件的方法
    public void writeLog(String filePath) {
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(filePath, true),"UTF-8");

            while (true) {
                String log = prductLog();
                osw.write(log + "\n");
                osw.flush();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                osw.flush();
                osw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //主函数测试方法
    public static void main(String[] args) {


       /* if (args != null || args.length <= 0) {
            System.out.println("no arguments!");
            System.exit(1);

        }*/

        Producer producer = new Producer();
        producer.initContacts();
        producer.writeLog(args[0]);

    }
}
