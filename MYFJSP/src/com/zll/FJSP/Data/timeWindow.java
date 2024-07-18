package com.zll.FJSP.Data;

public class timeWindow implements Comparable<timeWindow> {
    public int jobNo;
    public int startOperNo;
    public int endOperNo;
    public int waitingTime; //不包括中间工序加工时间的纯工序间等待时间

    public timeWindow(int jobNo, int startOperNo, int endOperNo, int waitingTime) {
        this.jobNo = jobNo;
        this.startOperNo = startOperNo;
        this.endOperNo = endOperNo;
        this.waitingTime = waitingTime;
    }
    public timeWindow() {
        this.jobNo = -1;
        this.startOperNo = -1;
        this.endOperNo = -1;
        this.waitingTime = -1;
    }

    @Override
    public int compareTo(timeWindow o) {
        return this.startOperNo - o.startOperNo;
    }
}
