/* Licensed under Apache-2.0 2025 */
package com.example.analytics.model;

public class EnrichedPageView {
    private String userId;
    private String userName;
    private String userCountry;
    private String pageName;
    private long duration;

    public EnrichedPageView() {}

    public EnrichedPageView(
            String userId, String userName, String userCountry, String pageName, long duration) {
        this.userId = userId;
        this.userName = userName;
        this.userCountry = userCountry;
        this.pageName = pageName;
        this.duration = duration;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserCountry() {
        return userCountry;
    }

    public void setUserCountry(String userCountry) {
        this.userCountry = userCountry;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnrichedPageView that = (EnrichedPageView) o;

        if (duration != that.duration) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null)
            return false;
        if (userCountry != null ? !userCountry.equals(that.userCountry) : that.userCountry != null)
            return false;
        return pageName != null ? pageName.equals(that.pageName) : that.pageName == null;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (userCountry != null ? userCountry.hashCode() : 0);
        result = 31 * result + (pageName != null ? pageName.hashCode() : 0);
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "EnrichedPageView{"
                + "userId='"
                + userId
                + '\''
                + ", userName='"
                + userName
                + '\''
                + ", userCountry='"
                + userCountry
                + '\''
                + ", pageName='"
                + pageName
                + '\''
                + ", duration="
                + duration
                + '}';
    }
}
