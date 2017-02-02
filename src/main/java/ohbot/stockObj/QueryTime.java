
package ohbot.stockObj;


public class QueryTime {

    private String sysTime;
    private Integer sessionLatestTime;
    private String sysDate;
    private String sessionKey;
    private Integer sessionFromTime;
    private Integer stockInfoItem;
    private Boolean showChart;
    private String sessionStr;
    private Integer stockInfo;

    public String getSysTime() {
        return sysTime;
    }

    public void setSysTime(String sysTime) {
        this.sysTime = sysTime;
    }

    public Integer getSessionLatestTime() {
        return sessionLatestTime;
    }

    public void setSessionLatestTime(Integer sessionLatestTime) {
        this.sessionLatestTime = sessionLatestTime;
    }

    public String getSysDate() {
        return sysDate;
    }

    public void setSysDate(String sysDate) {
        this.sysDate = sysDate;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public Integer getSessionFromTime() {
        return sessionFromTime;
    }

    public void setSessionFromTime(Integer sessionFromTime) {
        this.sessionFromTime = sessionFromTime;
    }

    public Integer getStockInfoItem() {
        return stockInfoItem;
    }

    public void setStockInfoItem(Integer stockInfoItem) {
        this.stockInfoItem = stockInfoItem;
    }

    public Boolean getShowChart() {
        return showChart;
    }

    public void setShowChart(Boolean showChart) {
        this.showChart = showChart;
    }

    public String getSessionStr() {
        return sessionStr;
    }

    public void setSessionStr(String sessionStr) {
        this.sessionStr = sessionStr;
    }

    public Integer getStockInfo() {
        return stockInfo;
    }

    public void setStockInfo(Integer stockInfo) {
        this.stockInfo = stockInfo;
    }

}
