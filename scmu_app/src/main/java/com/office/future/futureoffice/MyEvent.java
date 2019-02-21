package com.office.future.futureoffice;

import java.io.Serializable;

/**
 * Created by Dooping on 24/04/2016.
 */
public class MyEvent implements Serializable{
    private static final long serialVersionUID = 0L;
    private long id;
    private long begin;
    private long end;
    private String title;
    private String location;

    public MyEvent(long id, long begin, long end, String title, String location) {
        this.id = id;
        this.begin = begin;
        this.end = end;
        this.title = title;
        this.location = location;
    }

    public long getId() {
        return id;
    }

    public long getBegin() {
        return begin;
    }

    public long getEnd() {
        return end;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }
}
