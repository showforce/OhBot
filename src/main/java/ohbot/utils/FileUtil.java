package ohbot.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Slf4j
public class FileUtil {
    /**
     * 讀取檔案的內容
     * 以一行一行的方式進行讀取,讀出後放入集合中
     *
     * @param file     檔案
     * @param encoding 編碼
     * @return 檔案內容
     */

    public List<String> readTextFile(File file, String encoding) {
        List<String> contents = new ArrayList<String>();
        if (file == null || !file.exists()) return contents;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
            String line = null;
            while ((line = br.readLine()) != null) contents.add(line);
        } catch (Exception ex) {
            log.error("Error in readTextFile : " + ex.toString());
        } finally {
            ioClose(br);
        }
        return contents;
    }

    /**
     * 關閉io串流,將會呼叫close()方法進行關閉
     *
     * @param o 欲關閉的io物件
     */
    private void ioClose(Object o) {
        if (o != null) {
            try {
                Method m = o.getClass().getMethod("close");
                m.invoke(o);
            } catch (Exception e) {
                log.error("Error in ioClose : " + e.toString());
            }
        }
    }
}
