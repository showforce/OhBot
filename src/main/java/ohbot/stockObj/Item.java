package ohbot.stockObj;

/**
 * Created by lambertyang on 2017/2/23.
 */
public class Item {

    //收盤日
    private String vFLD_YMD;
    //收盤價
    private String vFLD_CLOSE;
    //漲跌
    private String vFLD_UP_DN;
    //漲跌幅
    private String vFLD_UP_DN_RATE;
    //近52週最高
    private String v52_WEEK_HIGH_PRICE;
    //近52週最低
    private String v52_WEEK_LOW_PRICE;

    //營收月份
    private String vGET_MONEY_DATE;
    //營收
    private String vGET_MONEY;
    //毛利率季
    private String vFLD_PRCQ_YMD;
    //毛利率
    private String vFLD_PROFIT;
    //每股盈餘（EPS)
    private String vFLD_EPS;
    //本益比(PER)
    private String vFLD_PER;
    //每股淨值
    private String vSTK_VALUE;
    //股價淨值比(PBR)
    private String vFLD_PBR;
    //股東權益報酬率(ROE)
    private String vFLD_ROE;
    //K 值
    private String vFLD_K9_UPDNRATE;
    //D 值
    private String vFLD_D9_UPDNRATE;
    //MACD值
    private String vMACD;



    public String getVFLD_UP_DN ()
    {
        return vFLD_UP_DN;
    }

    public void setVFLD_UP_DN (String vFLD_UP_DN)
    {
        this.vFLD_UP_DN = vFLD_UP_DN;
    }

    public String getVFLD_YMD ()
    {
        return vFLD_YMD;
    }

    public void setVFLD_YMD (String vFLD_YMD)
    {
        this.vFLD_YMD = vFLD_YMD;
    }

    public String getVFLD_UP_DN_RATE ()
    {
        return vFLD_UP_DN_RATE;
    }

    public void setVFLD_UP_DN_RATE (String vFLD_UP_DN_RATE)
    {
        this.vFLD_UP_DN_RATE = vFLD_UP_DN_RATE;
    }

    public String getVFLD_CLOSE ()
    {
        return vFLD_CLOSE;
    }

    public void setVFLD_CLOSE (String vFLD_CLOSE)
    {
        this.vFLD_CLOSE = vFLD_CLOSE;
    }


    public String getVGET_MONEY_DATE ()
    {
        return vGET_MONEY_DATE;
    }

    public void setVGET_MONEY_DATE (String vGET_MONEY_DATE)
    {
        this.vGET_MONEY_DATE = vGET_MONEY_DATE;
    }

    public String getVFLD_EPS ()
    {
        return vFLD_EPS;
    }

    public void setVFLD_EPS (String vFLD_EPS)
    {
        this.vFLD_EPS = vFLD_EPS;
    }

    public String getVFLD_PER ()
    {
        return vFLD_PER;
    }

    public void setVFLD_PER (String vFLD_PER)
    {
        this.vFLD_PER = vFLD_PER;
    }

    public String getVGET_MONEY ()
    {
        return vGET_MONEY;
    }

    public void setVGET_MONEY (String vGET_MONEY)
    {
        this.vGET_MONEY = vGET_MONEY;
    }

    public String getVFLD_ROE ()
    {
        return vFLD_ROE;
    }

    public void setVFLD_ROE (String vFLD_ROE)
    {
        this.vFLD_ROE = vFLD_ROE;
    }

    public String getVFLD_K9_UPDNRATE ()
    {
        return vFLD_K9_UPDNRATE;
    }

    public void setVFLD_K9_UPDNRATE (String vFLD_K9_UPDNRATE)
    {
        this.vFLD_K9_UPDNRATE = vFLD_K9_UPDNRATE;
    }

    public String getV52_WEEK_HIGH_PRICE ()
    {
        return v52_WEEK_HIGH_PRICE;
    }

    public void setV52_WEEK_HIGH_PRICE (String v52_WEEK_HIGH_PRICE)
    {
        this.v52_WEEK_HIGH_PRICE = v52_WEEK_HIGH_PRICE;
    }

    public String getVSTK_VALUE ()
    {
        return vSTK_VALUE;
    }

    public void setVSTK_VALUE (String vSTK_VALUE)
    {
        this.vSTK_VALUE = vSTK_VALUE;
    }

    public String getV52_WEEK_LOW_PRICE ()
    {
        return v52_WEEK_LOW_PRICE;
    }

    public void setV52_WEEK_LOW_PRICE (String v52_WEEK_LOW_PRICE)
    {
        this.v52_WEEK_LOW_PRICE = v52_WEEK_LOW_PRICE;
    }

    public String getVFLD_PROFIT ()
    {
        return vFLD_PROFIT;
    }

    public void setVFLD_PROFIT (String vFLD_PROFIT)
    {
        this.vFLD_PROFIT = vFLD_PROFIT;
    }

    public String getVFLD_D9_UPDNRATE ()
    {
        return vFLD_D9_UPDNRATE;
    }

    public void setVFLD_D9_UPDNRATE (String vFLD_D9_UPDNRATE)
    {
        this.vFLD_D9_UPDNRATE = vFLD_D9_UPDNRATE;
    }

    public String getVMACD ()
    {
        return vMACD;
    }

    public void setVMACD (String vMACD)
    {
        this.vMACD = vMACD;
    }

    public String getVFLD_PRCQ_YMD ()
    {
        return vFLD_PRCQ_YMD;
    }

    public void setVFLD_PRCQ_YMD (String vFLD_PRCQ_YMD)
    {
        this.vFLD_PRCQ_YMD = vFLD_PRCQ_YMD;
    }

    public String getVFLD_PBR ()
    {
        return vFLD_PBR;
    }

    public void setVFLD_PBR (String vFLD_PBR)
    {
        this.vFLD_PBR = vFLD_PBR;
    }

}
