package ohbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import emoji4j.EmojiUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ohbot.aqiObj.AqiResult;
import ohbot.aqiObj.Datum;
import ohbot.stockObj.MsgArray;
import ohbot.stockObj.StockData;
import ohbot.stockObj.StockList;
import ohbot.stockObj.TseStock;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import retrofit2.Response;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lambertyang on 2017/1/13.
 */
@LineMessageHandler
@Slf4j
@RestController
public class OhBotController {
    @Autowired
    private LineMessagingService lineMessagingService;

    @RequestMapping("/")
    public String index() {
        Greeter greeter = new Greeter();
        return greeter.sayHello();
    }

    @RequestMapping("/greeting")
    public String greeting(@RequestParam(value = "city") String city) {
        String strResult = "";
        try {
            if (city != null) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/" + city + ".htm");
                CloseableHttpResponse response = httpClient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                strResult = EntityUtils.toString(httpEntity, "utf-8");

                String dateTime = "";
                String temperature = "";
                String comfort = "";
                String weatherConditions = "";
                String rainfallRate = "";

                strResult = strResult.substring(
                strResult.indexOf("<h3 class=\"CenterTitle\">今明預報<span class=\"Issued\">"), strResult.length());
                strResult = strResult.substring(0,strResult.indexOf("</tr><tr>"));
                Pattern pattern = Pattern.compile("<th scope=\"row\">.*?</th>");
                Matcher matcher = pattern.matcher(strResult);
                while(matcher.find()){
                    dateTime = matcher.group().replaceAll("<[^>]*>", "");
                }
                pattern = Pattern.compile("<td>.*?~.*?</td>");
                matcher = pattern.matcher(strResult);
                while(matcher.find()){
                    temperature = matcher.group().replaceAll("<[^>]*>","");
                }
                pattern = Pattern.compile("title=\".*?\"");
                matcher = pattern.matcher(strResult);
                while(matcher.find()){
                    weatherConditions = matcher.group().replace("title=\"", "").replace("\"", "");
                }
                pattern = Pattern.compile("<img.*?</td>[\\s]{0,}<td>.*?</td>");
                matcher = pattern.matcher(strResult);
                while(matcher.find()){
                    comfort = matcher.group().replaceAll("<[^>]*>", "");
                }
                pattern = Pattern.compile("<td>[\\d]{0,3} %</td>");
                matcher = pattern.matcher(strResult);
                while(matcher.find()){
                    rainfallRate = matcher.group().replaceAll("<[^>]*>", "");
                }
                strResult = "氣溫"+temperature+"\n"+dateTime+"\n天氣狀況 : "+weatherConditions+"\n舒適度 : "+comfort+"\n降雨率 : "+rainfallRate;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResult;
    }

    @RequestMapping("/test")
    public String test(@RequestParam(value = "gid") String gid,@RequestParam(value = "message") String message) {
        TextMessage textMessage = new TextMessage(message);
        PushMessage pushMessage = new PushMessage(gid,textMessage);

        Response<BotApiResponse> apiResponse = null;
        try {
            apiResponse = lineMessagingService.pushMessage(pushMessage).execute();
            return String.format("Sent messages: %s %s", apiResponse.message(), apiResponse.code());
        } catch (IOException e) {
            e.printStackTrace();
            return String.format("Error in sending messages : %s", e.toString());
        }
    }

    @RequestMapping("/stock")
    public String stock(@RequestParam(value = "stock") String stock) {
        String strResult = "";
        try {
            if (stock != null) {
                String[] otcs = StockList.otcList;
                HashMap<String, String> otcNoMap = new HashMap<>();
                HashMap<String, String> otcNameMap = new HashMap<>();
                for (String otc : otcs) {
                    String[] s = otc.split("=");
                    otcNoMap.put(s[0], s[1]);
                    otcNameMap.put(s[1], s[0]);
                }

                String[] tses = StockList.tseList;
                HashMap<String, String> tseNoMap = new HashMap<>();
                HashMap<String, String> tseNameMap = new HashMap<>();
                for (String tse : tses) {
                    String[] s = tse.split("=");
                    tseNoMap.put(s[0], s[1]);
                    tseNameMap.put(s[1], s[0]);
                }

                System.out.println(stock);
                String companyType = "";
                Pattern pattern = Pattern.compile("[\\d]{3,}");
                Matcher matcher = pattern.matcher(stock);
                if (matcher.find()) {
                    if (otcNoMap.get(stock) != null) {
                        companyType = "otc";
                    } else {
                        companyType = "tse";
                    }
                } else {
                    if (otcNameMap.get(stock) != null) {
                        companyType = "otc";
                        stock = otcNameMap.get(stock);
                    } else {
                        companyType = "tse";
                        stock = tseNameMap.get(stock);
                    }
                }

                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="http://mis.twse.com.tw/stock/index.jsp";
                log.info(url);
                HttpGet httpget = new HttpGet(url);
                httpget.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                httpget.setHeader("Accept-Encoding","gzip, deflate, sdch");
                httpget.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
                httpget.setHeader("Cache-Control", "max-age=0");
                httpget.setHeader("Connection", "keep-alive");
                httpget.setHeader("Host", "mis.twse.com.tw");
                httpget.setHeader("Upgrade-Insecure-Requests", "1");
                httpget.setHeader("User-Agent",
                                  "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
                CloseableHttpResponse response = httpClient.execute(httpget);
                log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                url = "http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=" + companyType + "_" + stock +
                      ".tw&_=" + Instant.now().toEpochMilli();
                log.info(url);
                httpget = new HttpGet(url);
                response = httpClient.execute(httpget);
                log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                strResult = "";

                Gson gson = new GsonBuilder().create();
                String s =EntityUtils.toString(httpEntity, "utf-8");
                System.out.println(s);
                StockData stockData = gson.fromJson(s, StockData.class);
                for(MsgArray msgArray:stockData.getMsgArray()){
                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
                    Double nowPrice = Double.valueOf(msgArray.getZ());
                    Double yesterday = Double.valueOf(msgArray.getY());
                    Double diff = nowPrice - yesterday;
                    String change = "";
                    String range = "";
                    if (diff == 0) {
                        change = " " + diff;
                        range = " " + "-";
                    } else if (diff > 0) {
                        change = " +" + decimalFormat.format(diff);
                        if (nowPrice == Double.parseDouble(msgArray.getU())) {
                            range = EmojiUtils.emojify(":red_circle:") + decimalFormat.format((diff / yesterday)*100) + "%";
                        }else{
                            range = EmojiUtils.emojify(":chart_with_upwards_trend:") + decimalFormat.format((diff / yesterday)*100) + "%";
                        }
                    } else {
                        change = " -" + decimalFormat.format(diff*(-1));
                        if (nowPrice == Double.parseDouble(msgArray.getW())) {
                            range = EmojiUtils.emojify(":green_circle:") + decimalFormat.format((diff / yesterday)*100) + "%";
                        }else{
                            range = EmojiUtils.emojify(":chart_with_downwards_trend:") + decimalFormat.format((diff / yesterday)*100) + "%";
                        }
                    }
                    //開盤 : "+msgArray.getO()+"\n昨收 : "+msgArray.getY()+"
                    strResult = msgArray.getC()+" "+ msgArray.getN()+" "+change+range+" \n現價 : "+msgArray.getZ()+"\n更新 : "+msgArray.getT();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResult;
    }

    @RequestMapping("/tse")
    public String tseStock() {
        String strResult = "";
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url = "http://www.tse.com.tw/api/get.php?method=home_summary";
            log.info(url);
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpget.setHeader("Accept-Encoding", "gzip, deflate, sdch");
            httpget.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
            httpget.setHeader("Cache-Control", "max-age=0");
            httpget.setHeader("Connection", "keep-alive");
            httpget.setHeader("Host", "mis.twse.com.tw");
            httpget.setHeader("Upgrade-Insecure-Requests", "1");
            httpget.setHeader("User-Agent",
                              "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
            CloseableHttpResponse response = httpClient.execute(httpget);
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            Gson gson = new GsonBuilder().create();
            strResult = EntityUtils.toString(response.getEntity(), "utf-8");
            TseStock tseStock = gson.fromJson(strResult, TseStock.class);
        }catch (IOException e) {
            e.printStackTrace();
        }
        return strResult;
    }

    @RequestMapping("/start")
    public String start(@RequestParam(value = "start") String start) {
        String strResult = "";
        try {
            if (start != null) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="http://tw.xingbar.com/cgi-bin/v5starfate2?fate=1&type="+start;
                log.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                strResult = EntityUtils.toString(httpEntity, "big5");
                strResult = strResult.substring(strResult.indexOf("<div id=\"date\">"), strResult.length());
                strResult = strResult.substring(0, strResult.indexOf("</table><div class=\"google\">"));
                strResult = strResult.replaceAll("訂閱</a></div></td>", "");
                strResult = strResult.replaceAll("<[^>]*>", "");
                strResult = strResult.replaceAll("[\\s]{2,}", "\n");
                System.out.println(strResult);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResult;
    }

    @RequestMapping("/taiwanoil")
    public String taiwanoil() {
        String strResult = "";
        try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="http://taiwanoil.org/z.php?z=oiltw";
                log.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                strResult = EntityUtils.toString(httpEntity, "utf-8");
                strResult = strResult.substring(strResult.indexOf("<table"), strResult.length());
                strResult = strResult.substring(0, strResult.indexOf("</table>\");"));
                strResult = strResult.replaceAll("</td></tr>", "\n");
                strResult = strResult.replaceAll("</td>", "：");
                strResult = strResult.replaceAll("<[^>]*>", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResult;
    }

    @RequestMapping("/aqi")
    public String aqi(@RequestParam(value = "area") String area) {
        String strResult = "";
        try {
            if (area != null) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="http://taqm.epa.gov.tw/taqm/aqs.ashx?lang=tw&act=aqi-epa";
                log.info(url);
                HttpGet httpget = new HttpGet(url);
                httpget.setHeader("Host","taqm.epa.gov.tw");
                httpget.setHeader("Connection","keep-alive");
                httpget.setHeader("Accept","*/*");
                httpget.setHeader("X-Requested-With","XMLHttpRequest");
                httpget.setHeader("User-Agent",
                                  "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
                httpget.setHeader("Referer","http://taqm.epa.gov.tw/taqm/aqi-map.aspx");
                httpget.setHeader("Accept-Encoding","gzip, deflate, sdch");
                httpget.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");

                CloseableHttpResponse response = httpClient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                strResult =  EntityUtils.toString(httpEntity, "big5").toLowerCase();
                Gson gson = new GsonBuilder().create();
                AqiResult aqiResult = gson.fromJson(strResult, AqiResult.class);
                List<Datum> areaData = new ArrayList<>();
                for(Datum datums:aqiResult.getData()){
                    if(datums.getAreakey().equals("area")){
                        areaData.add(datums);
                    }
                }
                strResult = "";
                for (Datum datums : areaData) {
                    String aqiStyle = datums.getAQI();
                    log.info(aqiStyle);
                    if (Integer.parseInt(aqiStyle) <= 50) {
                        aqiStyle = "良好";
                    } else if (Integer.parseInt(aqiStyle) >= 51 && Integer.parseInt(aqiStyle) <= 100) {
                        aqiStyle = "普通";
                    } else if (Integer.parseInt(aqiStyle) >= 101 && Integer.parseInt(aqiStyle) <= 150) {
                        aqiStyle = "對敏感族群不健康";
                    } else if (Integer.parseInt(aqiStyle) >= 151 && Integer.parseInt(aqiStyle) <= 200) {
                        aqiStyle = "對所有族群不健康";
                    } else if (Integer.parseInt(aqiStyle) >= 201 && Integer.parseInt(aqiStyle) <= 300) {
                        aqiStyle = "非常不健康";
                    } else if (Integer.parseInt(aqiStyle) >= 301 && Integer.parseInt(aqiStyle) <= 500) {
                        aqiStyle = "危害";
                    }
                    strResult = strResult + datums.getSitename() + " AQI : " + datums.getAQI() +"\n   " + aqiStyle+"\n";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResult;
    }

    @RequestMapping("/rate")
    public String rate(@RequestParam(value = "rate") String country) {
        String strResult = "";
        try {
            if (country != null) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="http://m.findrate.tw/"+country+"/";
                log.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                strResult = EntityUtils.toString(httpEntity, "utf-8");
                strResult = strResult.substring(strResult.indexOf("<td>現鈔買入</td>"), strResult.length());
                strResult = strResult.substring(0, strResult.indexOf("</table>"));
                strResult = strResult.replaceAll("</a></td>", " ");
                strResult = strResult.replaceAll("<[^>]*>", "");
                strResult = strResult.replaceAll("[\\s]{1,}", "");
                strResult = strResult.replaceAll("現鈔賣出", "\n現鈔賣出");
                strResult = strResult.replaceAll("現鈔買入", ":dollar:現鈔買入");
                System.out.println(EmojiUtils.emojify(strResult));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResult;
    }


    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        log.info("Received message(Ignored): {}", event);
    }

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws IOException {
        handleTextContent(event.getReplyToken(), event, event.getMessage());
    }

    private void handleTextContent(String replyToken, Event event, TextMessageContent content) throws IOException {
        String text = content.getText();
        log.info(text);
        if (text.endsWith("天氣?") || text.endsWith("天氣？")) {
            weatherResult(text, replyToken);
        }

        if (text.endsWith("氣象?") || text.endsWith("氣象？")) {
            weatherResult2(text, replyToken);
        }

        if (text.endsWith("座?") || text.endsWith("座？")) {
            start(text, replyToken);
        }
        if (text.endsWith("油價?") || text.endsWith("油價？")) {
            taiwanoil(text, replyToken);
        }

        if ((text.startsWith("@") && text.endsWith("?")) || (text.startsWith("@") && text.endsWith("？"))) {
            stock(text, replyToken);
        }

        if (text.endsWith("空氣?") || text.endsWith("空氣？")) {
            aqiResult(text, replyToken);
        }

        if (text.startsWith("匯率?") || text.startsWith("匯率？")) {
            rate(text, replyToken);
        }

        if (text.startsWith("呆股?") || text.startsWith("呆股？")) {
            tse(text, replyToken);
        }

        if (text.endsWith("@?") || text.endsWith("@？")) {
            help(text, replyToken);
        }
    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) throws IOException {
        log.info("Got postBack event: {}", event);
        String replyToken = event.getReplyToken();
        String data = event.getPostbackContent().getData();
        switch (data) {
            case "more:1": {
                this.replyText(replyToken, "Comming soon!");
                break;
            }
            default:
                this.replyText(replyToken, "Got postback event : " + event.getPostbackContent().getData());
        }
    }

    private void replyText(@NonNull String replyToken, @NonNull String message) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 2) + "……";
        }
        this.reply(replyToken, new TextMessage(message));
    }

    private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, Collections.singletonList(message));
    }

    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        try {
            Response<BotApiResponse> apiResponse = lineMessagingService
                    .replyMessage(new ReplyMessage(replyToken, messages))
                    .execute();
            log.info("Sent messages: {} {}", apiResponse.message(), apiResponse.code());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void weatherResult(String text, String replyToken) throws IOException {
        text = text.replace("天氣", "").replace("?", "").replace("？", "").replace("臺", "台").trim();
        log.info(text);
        try {
            if (text.length() >= 3) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                String strResult;
                switch (text) {
                    case "台北市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_63.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "新北市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_65.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "桃園市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_68.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "台南市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_67.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "台中市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_66.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "高雄市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_64.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "基隆市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10017.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "新竹市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10018.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "新竹縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10004.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "苗栗縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10005.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "彰化縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10007.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "南投縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10008.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "雲林縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10009.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "嘉義市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10020.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "嘉義縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10010.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "屏東縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10013.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "宜蘭縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10002.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "花蓮縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10015.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "台東縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10014.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "澎湖縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10016.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    default:
                        strResult = "義大利?維大力? \nSorry 我不知道" + text + "是哪裡...";
                }
                strResult = strResult.replace("<BR><BR>", "\n");
                strResult = strResult.replaceAll("<[^<>]*?>", "");
                this.replyText(replyToken, strResult);
            }
        } catch (IOException e) {
            throw e;
        }
    }

    private void weatherResult2(String text, String replyToken) throws IOException {
        text = text.replace("氣象", "").replace("?", "").replace("？", "").replace("臺", "台").trim();
        log.info(text);
        try {
            if (text.length() >= 3) {
                String strResult;
                String url ="";
                switch (text) {
                    case "台北市": {
                        url="Taipei_City.htm";
                        break;
                    }
                    case "新北市": {
                        url="New_Taipei_City.htm";
                        break;
                    }
                    case "桃園市": {
                        url="Taoyuan_City.htm";
                        break;
                    }
                    case "台南市": {
                        url="Tainan_City.htm";
                        break;
                    }
                    case "台中市": {
                        url="Taichung_City.htm";
                        break;
                    }
                    case "高雄市": {
                        url="Kaohsiung_City.htm";
                        break;
                    }
                    case "基隆市": {
                        url="Keelung_City.htm";
                        break;
                    }
                    case "新竹市": {
                        url="Hsinchu_City.htm";
                        break;
                    }
                    case "新竹縣": {
                        url="Hsinchu_County.htm";
                        break;
                    }
                    case "苗栗縣": {
                        url="Miaoli_County.htm";
                        break;
                    }
                    case "彰化縣": {
                        url="Changhua_County.htm";
                        break;
                    }
                    case "南投縣": {
                        url="Nantou_County.htm";
                        break;
                    }
                    case "雲林縣": {
                        url="Chiayi_City.htm";
                        break;
                    }
                    case "嘉義市": {
                        url="Chiayi_City.htm";
                        break;
                    }
                    case "嘉義縣": {
                        url="Chiayi_County.htm";
                        break;
                    }
                    case "屏東縣": {
                        url="Pingtung_County.htm";
                        break;
                    }
                    case "宜蘭縣": {
                        url="Yilan_County.htm";
                        break;
                    }
                    case "花蓮縣": {
                        url="Hualien_County.htm";
                        break;
                    }
                    case "台東縣": {
                        url="Taitung_County.htm";
                        break;
                    }
                    case "澎湖縣": {
                        url="Penghu_County.htm";
                        break;
                    }
                    default:
                        text="";

                }
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/"+url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                strResult = EntityUtils.toString(httpEntity, "utf-8");
                if(text.equals("")){
                    strResult = "義大利?維大力? \nSorry 我不知道" + text + "是哪裡...";
                    this.replyText(replyToken, strResult);
                }else{
                    String dateTime = "";
                    String temperature = "";
                    String comfort = "";
                    String weatherConditions = "";
                    String rainfallRate = "";
                    strResult = strResult.substring(
                            strResult.indexOf("<h3 class=\"CenterTitle\">今明預報<span class=\"Issued\">"), strResult.length());
                    strResult = strResult.substring(0,strResult.indexOf("</tr><tr>"));
                    Pattern pattern = Pattern.compile("<th scope=\"row\">.*?</th>");
                    Matcher matcher = pattern.matcher(strResult);
                    while(matcher.find()){
                        dateTime = matcher.group().replaceAll("<[^>]*>", "");
                    }
                    pattern = Pattern.compile("<td>.*?~.*?</td>");
                    matcher = pattern.matcher(strResult);
                    while(matcher.find()){
                        temperature = matcher.group().replaceAll("<[^>]*>","");
                    }
                    pattern = Pattern.compile("title=\".*?\"");
                    matcher = pattern.matcher(strResult);
                    while(matcher.find()){
                        weatherConditions = matcher.group().replace("title=\"", "").replace("\"", "").replaceAll("[\\s]{0,}","");
                    }
                    pattern = Pattern.compile("<img.*?</td>[\\s]{0,}<td>.*?</td>");
                    matcher = pattern.matcher(strResult);
                    while(matcher.find()){
                        comfort = matcher.group().replaceAll("<[^>]*>", "").replaceAll("[\\s]{0,}","");
                    }
                    pattern = Pattern.compile("<td>[\\d]{0,3} %</td>");
                    matcher = pattern.matcher(strResult);
                    while(matcher.find()){
                        rainfallRate = matcher.group().replaceAll("<[^>]*>", "");
                    }
                    strResult = text+"氣溫 : "+temperature+"\n"+dateTime+"\n天氣狀況 : "+weatherConditions+"\n舒適度 : "+comfort+"\n降雨率 : "+rainfallRate;
                    this.replyText(replyToken, strResult);
                }

            }
        } catch (IOException e) {
            throw e;
        }
    }

    private void taiwanoil(String text, String replyToken) throws IOException {
        try {
            String strResult = "";
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url = "http://taiwanoil.org/";
            log.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();
            strResult = EntityUtils.toString(httpEntity, "utf-8");
            strResult = strResult.substring(strResult.indexOf("<td valign=top align=center>"), strResult.length());
            strResult = strResult.substring(0, strResult.indexOf("</table><br><br><br>"));
            String[] sp = strResult.split("預測下周價格");
            String title = sp[0].replaceAll(".*?<table class=\"topmenu2\">", "").replaceAll(
                    "<div align=center>[\\s]{0,}.*", "").replace("&nbsp;", "").replaceAll("<[^>]*>", "").replaceAll(
                    "\n\t\n\n", "").replaceAll("\n\n", "");
            String content = sp[1].replaceAll("<td style='text-align:right;'>[\\d]{4}/[\\d]{2}/[\\d]{2}</td>", "")
                                  .replaceAll("<td style='text-align:right;'>[\\d]{1,2}\\.[\\d]{1,2}</td></tr>", "")
                                  .replaceAll(
                                          "<td style='text-align:right;'><font color=#00bb11>(\\+|\\-)[\\d]{1,}\\.[\\d]{1,}\\%",
                                          "").replaceAll("</td></font></td>",
                                                         " > ").replaceAll("</font></td>", "\n").replace(
                            "</td></tr>", "").replaceAll("</td>", " : ").replaceAll("<[^>]*>", "");


            strResult = title + "供應商:今日油價 > 預測下周漲跌\n" + content;
            this.replyText(replyToken, strResult);
        } catch (IOException e) {
            throw e;
        }
    }

    private void start(String text, String replyToken) throws IOException {
        text = text.replace("座", "").replace("?", "").replace("？", "").trim();
        log.info(text);
        try {
            if (text.length() == 2) {
                String strResult;
                String url ="";
                switch (text) {
                    case "牡羊": {
                        url="1";
                        break;
                    }
                    case "金牛": {
                        url="2";
                        break;
                    }
                    case "雙子": {
                        url="3";
                        break;
                    }
                    case "巨蟹": {
                        url="4";
                        break;
                    }
                    case "獅子": {
                        url="5";
                        break;
                    }
                    case "處女": {
                        url="6";
                        break;
                    }
                    case "天秤": {
                        url="7";
                        break;
                    }
                    case "天蠍": {
                        url="8";
                        break;
                    }
                    case "射手": {
                        url="9";
                        break;
                    }
                    case "魔羯": {
                        url="10";
                        break;
                    }
                    case "水瓶": {
                        url="11";
                        break;
                    }
                    case "雙魚": {
                        url="12";
                        break;
                    }
                    default:
                        text="";

                }
                if(text.equals("")){
                    strResult = "義大利?維大力? \n09487 沒有" + text + "這個星座...";
                    this.replyText(replyToken, strResult);
                }else{
                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    url = "http://tw.xingbar.com/cgi-bin/v5starfate2?fate=1&type=" + url;
                    log.info(url);
                    HttpGet httpget = new HttpGet(url);
                    CloseableHttpResponse response = httpClient.execute(httpget);
                    log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                    HttpEntity httpEntity = response.getEntity();
                    strResult = EntityUtils.toString(httpEntity, "big5");
                    strResult = strResult.substring(strResult.indexOf("<div id=\"date\">"), strResult.length());
                    strResult = strResult.substring(0, strResult.indexOf("</table><div class=\"google\">"));
                    strResult = strResult.replaceAll("訂閱</a></div></td>", "");
                    strResult = strResult.replaceAll("<[^>]*>", "");
                    strResult = strResult.replaceAll("[\\s]{2,}", "\n");
//                    strResult = strResult.replace("心情：", "(sun)心情：");
//                    strResult = strResult.replace("愛情：", "(2 hearts)愛情：");
//                    strResult = strResult.replace("財運：", "(purse)財運：");
//                    strResult = strResult.replace("工作：", "(bag)工作：");

                    strResult = strResult.replace("心情：", "◎心情：");
                    strResult = strResult.replace("愛情：", "◎愛情：");
                    strResult = strResult.replace("財運：", "◎財運：");
                    strResult = strResult.replace("工作：", "◎工作：");
                    if(url.endsWith("type=1")){
                        this.replyText(replyToken, "愛惜生命 遠離" + text + "座 " + strResult);
                    }else{
                        this.replyText(replyToken, text + "座 " + strResult);
                    }

                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

    private void stock(String text, String replyToken) {
        try {
            text = text.replace("@","").replace("?", "").replace("？","");
            String[] otcs = StockList.otcList;
            HashMap<String, String> otcNoMap = new HashMap<>();
            HashMap<String, String> otcNameMap = new HashMap<>();
            for (String otc : otcs) {
                String[] s = otc.split("=");
                otcNoMap.put(s[0], s[1]);
                otcNameMap.put(s[1], s[0]);
            }

            String[] tses = StockList.tseList;
            HashMap<String, String> tseNoMap = new HashMap<>();
            HashMap<String, String> tseNameMap = new HashMap<>();
            for (String tse : tses) {
                String[] s = tse.split("=");
                tseNoMap.put(s[0], s[1]);
                tseNameMap.put(s[1], s[0]);
            }

            String companyType = "";
            Pattern pattern = Pattern.compile("[\\d]{3,}");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {   //如果是數字
                if (otcNoMap.get(text) != null) {
                    companyType = "otc";
                } else {
                    companyType = "tse";
                }
            } else {    //非數字
                if (otcNameMap.get(text) != null) {
                    companyType = "otc";
                    text = otcNameMap.get(text);
                } else {
                    companyType = "tse";
                    text = tseNameMap.get(text);
                }
            }

            String strResult;
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="http://mis.twse.com.tw/stock/index.jsp";
            log.info(url);
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpget.setHeader("Accept-Encoding","gzip, deflate, sdch");
            httpget.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
            httpget.setHeader("Cache-Control","max-age=0");
            httpget.setHeader("Connection","keep-alive");
            httpget.setHeader("Host","mis.twse.com.tw");
            httpget.setHeader("Upgrade-Insecure-Requests", "1");
            httpget.setHeader("User-Agent",
                              "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
            CloseableHttpResponse response = httpClient.execute(httpget);
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            url = "http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=" + companyType + "_" + text + ".tw&_=" +
                  Instant.now().toEpochMilli();
            log.info(url);
            httpget = new HttpGet(url);
            response = httpClient.execute(httpget);
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();
            strResult = "";

            Gson gson = new GsonBuilder().create();
            StockData stockData = gson.fromJson(EntityUtils.toString(httpEntity, "utf-8"), StockData.class);
            for(MsgArray msgArray:stockData.getMsgArray()){
                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                Double nowPrice = Double.valueOf(msgArray.getZ());
                Double yesterday = Double.valueOf(msgArray.getY());
                Double diff = nowPrice - yesterday;
                String change = "";
                String range = "";
                if (diff == 0) {
                    change = " " + diff;
                    range = " " + "-";
                } else if (diff > 0) {
                    change = " +" + decimalFormat.format(diff);
                    if (nowPrice == Double.parseDouble(msgArray.getU())) {
                        range = EmojiUtils.emojify(":heart:") + decimalFormat.format((diff / yesterday)*100) + "%";
                    }else{
                        range = EmojiUtils.emojify(":chart_with_upwards_trend:") + decimalFormat.format((diff / yesterday)*100) + "%";
                    }
                } else {
                    change = " -" + decimalFormat.format(diff*(-1));
                    if (nowPrice == Double.parseDouble(msgArray.getW())) {
                        range = EmojiUtils.emojify(":green_heart:") + decimalFormat.format((diff / yesterday)*100) + "%";
                    }else{
                        range = EmojiUtils.emojify(":chart_with_downwards_trend:") + decimalFormat.format((diff / yesterday)*100) + "%";
                    }
                }
                //開盤 : "+msgArray.getO()+"\n昨收 : "+msgArray.getY()+"
                strResult =msgArray.getC() + " " + msgArray.getN() + " " + change + range + " \n現價 : " + msgArray.getZ() +
                        " \n成量 : " + msgArray.getV() + "\n更新 : " + msgArray.getT();
            }
            this.replyText(replyToken, strResult);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void aqiResult(String text, String replyToken) throws IOException {
        text = text.replace("空氣", "").replace("?", "").replace("？", "").trim();
        log.info(text);
        try {
            if (text.length() <= 3) {
                String strResult = "";
                String areakey ="";
                switch (text) {
                    case "北部": {
                        areakey="north";
                        break;
                    }
                    case "竹苗": {
                        areakey="chu-miao";
                        break;
                    }
                    case "中部": {
                        areakey="central";
                        break;
                    }
                    case "雲嘉南": {
                        areakey="yun-chia-nan";
                        break;
                    }
                    case "高屏": {
                        areakey="kaoping";
                        break;
                    }
                    case "花東": {
                        areakey="hua-tung";
                        break;
                    }
                    case "宜蘭": {
                        areakey="yilan";
                        break;
                    }
                    case "外島": {
                        areakey="island";
                        break;
                    }
                    default:
                        text="";

                }
                if(text.equals("")){
                    strResult = "義大利?維大力? \n請輸入 這些地區：\n北部 竹苗 中部 \n雲嘉南 高屏 花東 \n宜蘭 外島";
                    this.replyText(replyToken, strResult);
                }else{
                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    String url="http://taqm.epa.gov.tw/taqm/aqs.ashx?lang=tw&act=aqi-epa";
                    log.info(url);
                    HttpGet httpget = new HttpGet(url);
                    httpget.setHeader("Host","taqm.epa.gov.tw");
                    httpget.setHeader("Connection","keep-alive");
                    httpget.setHeader("Accept","*/*");
                    httpget.setHeader("X-Requested-With","XMLHttpRequest");
                    httpget.setHeader("User-Agent",
                                      "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
                    httpget.setHeader("Referer","http://taqm.epa.gov.tw/taqm/aqi-map.aspx");
                    httpget.setHeader("Accept-Encoding", "gzip, deflate, sdch");
                    httpget.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");

                    CloseableHttpResponse response = httpClient.execute(httpget);
                    HttpEntity httpEntity = response.getEntity();
                    String pageContent =  EntityUtils.toString(httpEntity, "big5").toLowerCase();
                    Gson gson = new GsonBuilder().create();
                    AqiResult aqiResult = gson.fromJson(pageContent, AqiResult.class);
                    List<Datum> areaData = new ArrayList<>();
                    for(Datum datums:aqiResult.getData()){
                        if(datums.getAreakey().equals(areakey)){
                            areaData.add(datums);
                        }
                    }
                    for (Datum datums : areaData) {
                        String aqiStyle = datums.getAQI();
                        if (Objects.equals(aqiStyle, "")) {
                            aqiStyle = "999";
                        }
                        log.info(datums.getSitename()+" "+datums.getAQI());
                        if (Integer.parseInt(aqiStyle) <= 50) {
                            aqiStyle = ":blush: " +"良好";
                        } else if (Integer.parseInt(aqiStyle) >= 51 && Integer.parseInt(aqiStyle) <= 100) {
                            aqiStyle = ":no_mouth: " +"普通";
                        } else if (Integer.parseInt(aqiStyle) >= 101 && Integer.parseInt(aqiStyle) <= 150) {
                            aqiStyle = ":triumph: " +"對敏感族群不健康";
                        } else if (Integer.parseInt(aqiStyle) >= 151 && Integer.parseInt(aqiStyle) <= 200) {
                            aqiStyle = ":mask: " +"對所有族群不健康";
                        } else if (Integer.parseInt(aqiStyle) >= 201 && Integer.parseInt(aqiStyle) <= 300) {
                            aqiStyle = ":scream: " +"非常不健康";
                        } else if (Integer.parseInt(aqiStyle) >= 301 && Integer.parseInt(aqiStyle) <= 500) {
                            aqiStyle = ":imp: " +"危害";
                        } else if (Integer.parseInt(aqiStyle) >= 500) {
                            aqiStyle = "監測站資料異常";
                        }
                        strResult = strResult + datums.getSitename() + " AQI : " + datums.getAQI() + aqiStyle+"\n";
                    }
                    this.replyText(replyToken, EmojiUtils.emojify(strResult));
                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

    private void rate(String text, String replyToken) throws IOException {
        text = text.replace("匯率", "").replace("?", "").replace("？", "").trim();
        log.info(text);
        try {
            if (text.length() <= 3) {
                String strResult = "";
                String country ="";
                switch (text) {
                    case "美金": {
                        country="USD";
                        break;
                    }
                    case "日圓": {
                        country="JPY";
                        break;
                    }
                    case "人民幣": {
                        country="CNY";
                        break;
                    }
                    case "歐元": {
                        country="EUR";
                        break;
                    }
                    case "港幣": {
                        country="HKD";
                        break;
                    }
                    case "英鎊": {
                        country="GBP";
                        break;
                    }
                    case "韓元": {
                        country="KRW";
                        break;
                    }
                    case "越南盾": {
                        country="VND";
                        break;
                    }
                    default:
                        text="";

                }
                if(text.equals("")){
                    strResult = "義大利?維大力? \n請輸入 這些幣別：\n美金 日圓 人民幣 歐元 \n港幣 英鎊 韓元 越南盾";
                    this.replyText(replyToken, strResult);
                }else{
                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    String url="http://m.findrate.tw/"+country+"/";
                    log.info(url);
                    HttpGet httpget = new HttpGet(url);
                    CloseableHttpResponse response = httpClient.execute(httpget);
                    log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                    HttpEntity httpEntity = response.getEntity();
                    strResult = EntityUtils.toString(httpEntity, "utf-8");
                    strResult = strResult.substring(strResult.indexOf("<td>現鈔買入</td>"), strResult.length());
                    strResult = strResult.substring(0, strResult.indexOf("</table>"));
                    strResult = strResult.replaceAll("</a></td>", ":moneybag:");
                    strResult = strResult.replaceAll("<[^>]*>", "");
                    strResult = strResult.replaceAll("[\\s]{1,}", "");
                    strResult = strResult.replaceAll("現鈔賣出", "\n:money_with_wings:現鈔賣出");
                    strResult = strResult.replaceAll("現鈔買入", ":dollar:現鈔買入");

                    this.replyText(replyToken, EmojiUtils.emojify(strResult));
                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

    private void tse(String text, String replyToken) throws IOException {
        log.info(text);
        String strResult = "";
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url = "http://www.tse.com.tw/api/get.php?method=home_summary";
            log.info(url);
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpget.setHeader("Accept-Encoding", "gzip, deflate, sdch");
            httpget.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
            httpget.setHeader("Cache-Control", "max-age=0");
            httpget.setHeader("Connection", "keep-alive");
            httpget.setHeader("Host", "mis.twse.com.tw");
            httpget.setHeader("Upgrade-Insecure-Requests", "1");
            httpget.setHeader("User-Agent",
                              "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
            CloseableHttpResponse response = httpClient.execute(httpget);
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            Gson gson = new GsonBuilder().create();
            String content = EntityUtils.toString(response.getEntity(), "utf-8");
            TseStock tseStock = gson.fromJson(content, TseStock.class);
            if (tseStock.getTSE_D() > 0) {
                strResult = "加權 : " + tseStock.getTSE_I() + EmojiUtils.emojify(":chart_with_upwards_trend:") +
                            tseStock.getTSE_D() + EmojiUtils.emojify(":chart_with_upwards_trend:") + tseStock.getTSE_P() +
                            "% \n成交金額(億) : " + tseStock.getTSE_V() + "\n";
            } else {
                strResult = "加權 : " + tseStock.getTSE_I() + EmojiUtils.emojify(":chart_with_downwards_trend:") +
                            tseStock.getTSE_D() + EmojiUtils.emojify(":chart_with_downwards_trend:") + tseStock.getTSE_P() +
                            "% \n成交金額(億) : " + tseStock.getTSE_V() + "\n";
            }
            if (tseStock.getOTC_I() > 0) {
                strResult = strResult + "櫃買 : " + tseStock.getOTC_I() + EmojiUtils.emojify(":chart_with_upwards_trend:") +
                            tseStock.getOTC_D() + EmojiUtils.emojify(":chart_with_upwards_trend:") + tseStock.getOTC_P() +
                            "% \n成交金額(億) : " + tseStock.getOTC_V() + "\n";
            } else {
                strResult = strResult + "櫃買 : " + tseStock.getOTC_I() + EmojiUtils.emojify(":chart_with_downwards_trend:") +
                            tseStock.getOTC_D() + EmojiUtils.emojify(":chart_with_downwards_trend:") + tseStock.getOTC_P() +
                            "% \n成交金額(億) : " + tseStock.getOTC_V() + "\n";
            }

            this.replyText(replyToken, strResult);
        } catch (IOException e) {
            throw e;
        }
    }

    private void help(String text, String replyToken) throws IOException {
        String imageUrl = "https://p1.bqimg.com/524586/f7f88ef91547655cs.png";
        ButtonsTemplate buttonsTemplate = new ButtonsTemplate(imageUrl,"安安","你好",
                Arrays.asList(
                        new MessageAction("查個股股價","輸入 @2331? 或 @台積電?"),
                        new MessageAction("查加權上櫃指數","輸入 呆股?"),
                        new MessageAction("查匯率","輸入 美金匯率? 或 匯率? 檢視可查匯率"),
                        new PostbackAction("更多","more:1")
                )
        );
        TemplateMessage templateMessage = new TemplateMessage("The function Only in mobile. ", buttonsTemplate);
        this.reply(replyToken, templateMessage);
    }
}