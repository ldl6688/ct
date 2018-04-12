package coprocessor;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.HBaseUtil;
import utils.PropertiesUtil;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @ Autheor:ldl
 * @Description:
 * @Date: 2018/3/20 21:21
 * @Modified By:
 */

public class CalleeWriteObserver extends BaseRegionObserver {
    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Logger logger = LoggerFactory.getLogger(CalleeWriteObserver.class);

    @Override
    public void postPut(ObserverContext<RegionCoprocessorEnvironment> e, Put put, WALEdit edit, Durability durability) throws IOException {

        super.postPut(e, put, edit, durability);
        System.out.println("-------------CalleeWriteObserver-----------");
        //1、获取你要操作的表
        String targetTableName = PropertiesUtil.getProperty("hbase.calllog.tablename");
        logger.info("targetTableName: " + targetTableName);

        //2、获取当前操作的表
        String currentTableName = e.getEnvironment().getRegion().getRegionInfo().getTable().getNameAsString();
        logger.info("currentTableName: " + currentTableName);

        //3、判断，如果当前操作的表，不是目标表，则直接返回
        if (!StringUtils.equals(targetTableName, currentTableName)) return;

        //插入被叫数据
        //得到当前插入的数据，并重新封装
        String oriRowKey = Bytes.toString(put.getRow());
        logger.info("oriRowKey:" + oriRowKey);

        String[] splits = oriRowKey.split("_");
        String flag = "0";

        if (StringUtils.equals(splits[4], flag)) return;

        long ts = System.currentTimeMillis();
//        01_16379727058_20191120150958_14607885120_1_1178
//        01_14607885120_20191120150958_16379727058_0_1178
        String caller = splits[1];
        logger.info("caller:" + caller);
        String callee = splits[3];
        logger.info("callee:" + callee);
        String dateTime = splits[2];
        logger.info("dateTime:" + dateTime);
        String dateTimeWithoutForamt = dateTime;
        logger.info("dateTimeWithoutForamt:" + dateTimeWithoutForamt);
        String duration = splits[5];
        logger.info("duration:" + duration);
        String build_time_ts = null;
        try {
            build_time_ts = String.valueOf(sdf1.parse(dateTime).getTime());
            logger.info("build_time_ts:" + build_time_ts);
            dateTime = sdf2.format(sdf1.parse(dateTime));
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        String partitionCode = HBaseUtil.genRowKeyPartitionCode(callee, dateTime, Integer.valueOf(PropertiesUtil.getProperty("hbase.calllog.regions")));
        String rowKey = HBaseUtil.genRowKey(partitionCode, callee, dateTimeWithoutForamt, caller, flag, duration);
        logger.info("rowKey:" + rowKey);
        Put newPut = new Put(Bytes.toBytes(rowKey));

        newPut.addColumn(Bytes.toBytes("f2"), Bytes.toBytes("call1"), ts, Bytes.toBytes(callee));
        newPut.addColumn(Bytes.toBytes("f2"), Bytes.toBytes("call2"), ts, Bytes.toBytes(caller));
        newPut.addColumn(Bytes.toBytes("f2"), Bytes.toBytes("build_time"), ts, Bytes.toBytes(dateTime));
        newPut.addColumn(Bytes.toBytes("f2"), Bytes.toBytes("build_time_ts"), ts, Bytes.toBytes(build_time_ts));
        newPut.addColumn(Bytes.toBytes("f2"), Bytes.toBytes("flag"), ts, Bytes.toBytes(flag));
        newPut.addColumn(Bytes.toBytes("f2"), Bytes.toBytes("duration"), ts, Bytes.toBytes(duration));

        Table table = e.getEnvironment().getTable(TableName.valueOf(targetTableName));
        table.put(newPut);
        table.close();
    }
}
