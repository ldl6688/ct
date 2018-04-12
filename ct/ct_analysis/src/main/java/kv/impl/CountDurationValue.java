package kv.impl;

import kv.base.BaseValue;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @ Autheor:ldl
 * @Description:存放某个时间某个人的通话总次数和通话时长
 * @Date: 2018/3/24 11:19
 * @Modified By:
 */

public class CountDurationValue extends BaseValue {

    /**
     * id_date_contact:复合主键
     id_date_dimension:时间维度id
     id_contact:联系人维度id
     call_sum:通话总次数
     call_duration_sum:通话总时长
    */

    private int callSum;
    private int callDurationSum;

    public CountDurationValue() {
    }

    public CountDurationValue(int callSum, int callDurationSum) {
        this.callSum = callSum;
        this.callDurationSum = callDurationSum;
    }

    public int getCallSum() {
        return callSum;
    }

    public void setCallSum(int callSum) {
        this.callSum = callSum;
    }

    public int getCallDurationSum() {
        return callDurationSum;
    }

    public void setCallDurationSum(int callDurationSum) {
        this.callDurationSum = callDurationSum;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.callSum);
        dataOutput.writeInt(this.callDurationSum);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.callSum = dataInput.readInt();
        this.callDurationSum = dataInput.readInt();
    }
}
