package kv.impl;

import kv.base.BaseDimension;
import kv.impl.DateDimension;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @ Autheor:ldl
 * @Description:时间维度类
 * @Date: 2018/3/24 10:05
 * @Modified By:
 */

public class DateDimension extends BaseDimension {
    private int id;
    private int year;
    private int month;
    private int day;

    public DateDimension() {
    }

    public DateDimension(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateDimension that = (DateDimension) o;

        if (id != that.id) return false;
        if (year != that.year) return false;
        if (month != that.month) return false;
        return day == that.day;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + year;
        result = 31 * result + month;
        result = 31 * result + day;
        return result;
    }

    @Override
    public int compareTo(BaseDimension o) {
        if (this == o) return 0;
        DateDimension dateDimension = (DateDimension) o;
        int tmp = Integer.compare(this.id, dateDimension.getId());
        if (tmp != 0) return tmp;

        tmp = Integer.compare(this.year, dateDimension.getYear());
        if (tmp != 0) return tmp;

        tmp = Integer.compare(this.month, dateDimension.getMonth());
        if (tmp != 0) return tmp;

        tmp = Integer.compare(this.day, dateDimension.getDay());
        if (tmp != 0) return tmp;

        return tmp;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.id);
        dataOutput.writeInt(this.year);
        dataOutput.writeInt(this.month);
        dataOutput.writeInt(this.day);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {

        this.id = dataInput.readInt();
        this.year = dataInput.readInt();
        this.month = dataInput.readInt();
        this.day = dataInput.readInt();
    }

    @Override
    public String toString() {
        return "DateDimension{" +
                "id=" + id +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                '}';
    }
}
