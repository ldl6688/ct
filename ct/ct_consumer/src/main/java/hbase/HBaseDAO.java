package hbase;


import contants.ContantsUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import utils.ConnectionInstance;
import utils.HBaseUtil;
import utils.PropertiesUtil;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * @ Autheor:ldl
 * @Description:
 * @Date: 2018/3/20 15:49
 * @Modified By:
 */

public class HBaseDAO {

    private int regions;
    private String nameSpace;
    private String tableName;
    private HTable hTable;
    private SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddHHmmss");
    private Connection connection;
    public static Configuration configuration;

    private ArrayList<Put> cacheList = new ArrayList<>();

    static {
        configuration = HBaseConfiguration.create();
    }

    public HBaseDAO() throws IOException {
        regions = Integer.valueOf(PropertiesUtil.getProperty("hbase.calllog.regions"));
        nameSpace = PropertiesUtil.getProperty("hbase.calllog.namespace");
        tableName = PropertiesUtil.getProperty("hbase.calllog.tablename");

        if (!HBaseUtil.isTableExist(tableName)) {
            HBaseUtil.initNameSpace(nameSpace);
            HBaseUtil.createTable(tableName, "f1", "f2");
        }
    }

    public void put(String ori) throws IOException, ParseException {

        if (cacheList.size() == 0) {
            connection = ConnectionInstance.getConenction();
            hTable = (HTable) connection.getTable(TableName.valueOf(tableName));
            hTable.setAutoFlushTo(false);
            hTable.setWriteBufferSize(2 * 1024 * 1024);
        }

        //  13098309585,18246233914,2019-11-18 01:04:34,1200
        //rowkey样式：01_18576581848_20170814133831_17269452013_1_1761
        String[] splitOri = ori.split(",");
        String caller = splitOri[0];
        String callee = splitOri[1];
        String buildTime = splitOri[2];
        String duration = splitOri[3];
        String regionHashCode = HBaseUtil.genRowKeyPartitionCode(caller, buildTime, regions);
        String buildTimeString = simpleDateFormat2.format(simpleDateFormat1.parse(buildTime));
        String buildTimeTs = String.valueOf(simpleDateFormat1.parse(buildTime).getTime());

        String rowKey = HBaseUtil.genRowKey(regionHashCode, caller, buildTimeString, callee, "1", duration);

        //向表中插入一条数据
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("call1"), Bytes.toBytes(caller));
        put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("call2"), Bytes.toBytes(callee));
        put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("build_time"), Bytes.toBytes(buildTimeString));
        put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("build_time_ts"), Bytes.toBytes(buildTimeTs));
        put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("flag"), Bytes.toBytes("1"));
        put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("duration"), Bytes.toBytes(duration));

        cacheList.add(put);

        if (cacheList.size() >= 30) {
            hTable.put(cacheList);
            hTable.flushCommits();

            hTable.close();
            cacheList.clear();
        }
    }
}
