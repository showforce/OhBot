package ohbot.stockObj;

import java.util.List;

/**
 * Created by lambertyang on 2017/2/23.
 */
public class Screener {
    private List<Item> items = null;
    private String count;
    private String totalcount;
    private String error;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getTotalcount() {
        return totalcount;
    }

    public void setTotalcount(String totalcount) {
        this.totalcount = totalcount;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
