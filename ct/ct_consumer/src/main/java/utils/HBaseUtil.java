package utils;

import contants.ContantsUtil;
import coprocessor.CalleeWriteObserver;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * @ Autheor:ldl
 * @Description: HBase工具类
 * @Date: 2018/3/20 15:51
 * @Modified By:
 */

public class HBaseUtil {

//    public static Configuration configuration = HBaseConfiguration.create();

    /**
     * 判断表是否存在
     */
    public static boolean isTableExist(String tableName) throws IOException {

        Connection connection = ConnectionFactory.createConnection(ContantsUtil.conf);
        Admin admin = connection.getAdmin();
        boolean isExist = admin.tableExists(TableName.valueOf(tableName));
        close(connection, admin, null);

        return isExist;
    }

    /**
     * 初始化命名空间
     */
    public static void initNameSpace(String nameSpace) throws IOException {
        Connection connection = ConnectionFactory.createConnection(ContantsUtil.conf);
        Admin admin = connection.getAdmin();

        NamespaceDescriptor ns = NamespaceDescriptor.create(nameSpace)
                .addConfiguration("creator", "Admin")
                .addConfiguration("create_ts", String.valueOf(System.currentTimeMillis()))
                .build();

        admin.createNamespace(ns);

        close(connection, admin, null);
    }

    /**
     * 创建电信客服所用的表
     */
    public static void createTable(String tableName, String... cfs) throws IOException {
        Connection connection = ConnectionFactory.createConnection(ContantsUtil.conf);
        Admin admin = connection.getAdmin();

        if (isTableExist(tableName)) {
            close(connection, admin, null);
            return;
        }

        HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));

        hTableDescriptor.addCoprocessor("coprocessor.CalleeWriteObserver");


        for (String cf : cfs) {
            HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(cf);
            hColumnDescriptor.setMinVersions(1).setMaxVersions(1);
            hTableDescriptor.addFamily(hColumnDescriptor);
        }

        Integer regions = Integer.valueOf(PropertiesUtil.getProperty("hbase.calllog.regions"));

        admin.createTable(hTableDescriptor, genSplitKeys(regions));

        close(connection, admin, null);
    }

    /**
     * 生成预分区所用的键
     */
    public static byte[][] genSplitKeys(int regions) {

        String[] keys = new String[regions];
        DecimalFormat decimalFormat = new DecimalFormat("00");

        for (int i = 0; i < keys.length; i++) {
            keys[i] = decimalFormat.format(i) + "|";
        }

        System.out.println(Arrays.asList(keys));

        byte[][] splitKeys = new byte[keys.length][];

        TreeSet<byte[]> treeSet = new TreeSet<>(Bytes.BYTES_COMPARATOR);

        for (int i = 0; i < keys.length; i++) {
            treeSet.add(Bytes.toBytes(keys[i]));
        }

        Iterator<byte[]> iterator = treeSet.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            byte[] temp = iterator.next();
            iterator.remove();
            splitKeys[i++] = temp;
        }
        return splitKeys;
    }

    /**
     * 生成rowkey
     */
    public static String genRowKey(String regionHash,
                                   String call1,
                                   String dateTime,
                                   String call2,
                                   String flag,
                                   String duration) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(regionHash)
                .append("_")
                .append(call1)
                .append("_")
                .append(dateTime)
                .append("_")
                .append(call2)
                .append("_")
                .append(flag)
                .append("_")
                .append(duration);
        return stringBuilder.toString();
    }


    /**
     * 生成rowKey的分区号
     */
    public static String genRowKeyPartitionCode(String call1, String dataTime, int regions) {
        int length = call1.length();
        String last4Num = call1.substring(length - 4);
        String first4Num = dataTime.replaceAll("-", "").substring(0, 6);
        int hashPartitionCode = (Integer.valueOf(last4Num) ^ Integer.valueOf(first4Num)) % regions;

        return new DecimalFormat("00").format(hashPartitionCode);
    }

    /**
     * 关闭连接释放资源
     */
    public static void close(Connection connection, Admin admin, Table... tables) throws IOException {
        if (tables != null) {
            for (Table table : tables) {
                table.close();
            }
        }

        if (admin != null)
            admin.close();

        if (connection != null && !connection.isClosed())
            connection.close();
    }

}
