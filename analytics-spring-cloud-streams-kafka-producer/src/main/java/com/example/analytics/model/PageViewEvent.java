package com.example.analytics.model;

import java.util.StringJoiner;

public class PageViewEvent {

  private String userId;

  private String page;

  private long duration;

  public PageViewEvent(String userId, String page, long duration) {
    this.userId = userId;
    this.page = page;
    this.duration = duration;
  }

  public PageViewEvent() {}

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

  @Override
  public String toString() {
    return new StringJoiner(", ", PageViewEvent.class.getSimpleName() + "[", "]")
        .add("userId='" + userId + "'")
        .add("page='" + page + "'")
        .add("duration=" + duration)
        .toString();
  }
}
