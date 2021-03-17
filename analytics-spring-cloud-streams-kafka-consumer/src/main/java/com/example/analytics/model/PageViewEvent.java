package com.example.analytics.model;

public class PageViewEvent {
    private String userId;
    private String page;
    private long duration;

    public PageViewEvent() {}

    public PageViewEvent(String userId, String page, long duration) {
        this.userId = userId;
        this.page = page;
        this.duration = duration;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
