package com.zll.FJSP.Data;

import com.zll.FJSP.GA.CaculateFitness;

import java.util.Objects;

public class Time {
    public int start;
    public int end;
    public int type;// 0为工作,1为空闲。
    public int jobNo;
    public int operNo;

    public Time(int s, int e, int t) {
        this.start = s;
        this.end = e;
        this.type = t;
        this.jobNo = -1;
        this.operNo = -1;
    }

    public Time(int s, int e, int t, int j, int o) {
        this.start = s;
        this.end = e;
        this.type = t;
        this.jobNo = j;
        this.operNo = o;
    }

    public Time(Time t) {
        this.start = t.start;
        this.end = t.end;
        this.type = t.type;
        this.jobNo = t.jobNo;
        this.operNo = t.operNo;
    }

    public Time(int j, int o) {
        this.start = -1;
        this.end = -1;
        this.type = -1;
        this.jobNo = j;
        this.operNo = o;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Time)) return false;
        Time time = (Time) o;
        return jobNo == time.jobNo && operNo == time.operNo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobNo, operNo);
    }
}
