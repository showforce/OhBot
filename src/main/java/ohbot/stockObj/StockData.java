
package ohbot.stockObj;

import java.util.List;

public class StockData {

    private List<MsgArray> msgArray = null;
    private Integer userDelay;
    private String rtmessage;
    private String referer;
    private QueryTime queryTime;
    private String rtcode;

    public List<MsgArray> getMsgArray() {
        return msgArray;
    }

    public void setMsgArray(List<MsgArray> msgArray) {
        this.msgArray = msgArray;
    }

    public Integer getUserDelay() {
        return userDelay;
    }

    public void setUserDelay(Integer userDelay) {
        this.userDelay = userDelay;
    }

    public String getRtmessage() {
        return rtmessage;
    }

    public void setRtmessage(String rtmessage) {
        this.rtmessage = rtmessage;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public QueryTime getQueryTime() {
        return queryTime;
    }

    public void setQueryTime(QueryTime queryTime) {
        this.queryTime = queryTime;
    }

    public String getRtcode() {
        return rtcode;
    }

    public void setRtcode(String rtcode) {
        this.rtcode = rtcode;
    }

}
