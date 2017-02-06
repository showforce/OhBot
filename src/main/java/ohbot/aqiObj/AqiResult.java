package ohbot.aqiObj;

import java.util.List;

/**
 * Created by lambertyang on 2017/2/6.
 */
public class AqiResult {
    private String result;
    private List<Datum> data = null;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }
}
