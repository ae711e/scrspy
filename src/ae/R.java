/*
 * Copyright (c) 2019. Aleksey Eremin
 * 29.03.19 8:24
 */

/*
 * (c) 2018-2019. Алексей Еремин
 * 07.09.18 10:59
 */

package ae;

/*
 * Ресурсный класс
*/

/*
Modify:
05.02.19  первая версия.

*/

import java.io.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class R {
  public final static String  sep  = System.getProperty("file.separator"); // разделитель имени каталогов
  public final static String  TMP  = System.getProperty("java.io.tmpdir"); // временный каталог
  //
  public static String ProxyServer = _r.proxyserv;  // proxy-сервер
  public static int    ProxyPort   = _r.proxyport;  // порт proxy
  public static int    TimeOut     = 30000;         // тайм-аут мс
  public static String ProxyUser   = _r.proxyuser;  // user name for proxy-server
  public static String ProxyPass   = _r.proxypass;  // password for proxy-server
  public static String siteRevizor = _r.site;       // сайт
  public static String SiteUsr     = _r.usr;        // пользователь на сайте
  public static String SitePwd     = _r.pwd;        // пароль на сайт
  //
  public static int    Pause        = 300;          // пауза перед запросом изображения (мс)
  public static int    ImageTTL    = 365*86400;          // время жизни записи в списке кэша изображения (сек) - 365 сут
  public static String CacheImageDir  = "ScrspyCACHE";  // каталог кэша изображений протоколов (создается во временном каталоге)

  /**
     * Пауза выполнения программы
     * @param time   время задержки, мсек
     */
    public static void sleep(long time)
    {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

  /*
   * Преобразование строки времени вида ЧЧ:ММ:СС в кол-во секунд
   * @param str   входная строка времени (0:0:2)
   * @return  кол-во секунд
   */
/*
  public static int hms2sec(String str)
  {
    String[] sar;
    int result = 0;
    try {
      sar = str.split(":", 3);
      int ih = Integer.parseInt(sar[0]);
      int im = Integer.parseInt(sar[1]);
      int is = Integer.parseInt(sar[2]);
      result = ih * 3600 + im * 60 + is;
    } catch (Exception e) {
      //e.printStackTrace();
      result = -1;
    }
    return result;
  }
*/

  /**
   * Записать в файл байтовый массив
   * @param arrayObByte байтовый массив
   * @param fileNam     выходной файл
   * @return  запись выполнена
   */
  public static boolean write2file(byte[] arrayObByte, String fileNam)
  {
    if(null == arrayObByte) return false;
    try {
      FileOutputStream fos = new FileOutputStream(new File(fileNam));
      fos.write(arrayObByte);
      fos.close();
    } catch (Exception e) {
      System.out.println("?-Error write file: " + fileNam + ". " + e.getMessage());
      return false;
    }
    return true;
  }

  /**
   * Прочитать из файла в байтовый массив
   * @param fileNam имя файла
   * @return  байтовый массив
   */
  public static byte[] file2byte(String fileNam)
  {
    ByteArrayOutputStream out = null;
    InputStream input   = null;
    byte[]      outbyte = null;
    try{
      out = new ByteArrayOutputStream();
      input = new BufferedInputStream(new FileInputStream(fileNam));
      int data;
      while ((data = input.read()) != -1){
        out.write(data);
      }
      outbyte = out.toByteArray();
    } catch (IOException e) {
      System.out.println("?-error open or read file: " + fileNam);
    }
    finally{
      try {
        if (null != input) input.close();
        if (null != out)   out.close();
      } catch (IOException e) {
        System.out.println("?-error close file: " + fileNam);
      }
    }
    return outbyte;
  }

  public static void deleteFileDir(String elementName)
  {
    if(elementName != null)
      deleteFileDir(new File(elementName));
  }
  /**
   * Удалить файл или каталог, если они есть
   * @param element файл или каталог для удаления
   */
  public static void deleteFileDir(File element)
  {
    if(element == null || !element.exists()) return;
    // удаление файла или рекурсивное удаление каталога
    if(element.isDirectory()) {
      File[] list = element.listFiles();
      if(list != null) {
        for(File sub : list) {
          deleteFileDir(sub);
        }
      }
    }
    element.delete();
  }

  // смещение текущей временной зоны (для вычисление в datetime2unux)
  private static final ZoneOffset zoneOffset = OffsetDateTime.now(ZoneId.systemDefault()).getOffset();

  /**
   * перевод строки дата-время в секунды эпохи Unix-epoch, если указана только дата, то время считается 00:00:00
   * @param sdat дата в виде YYYY-MM-DD[ hh:mm[:ss]] или DD.MM.YYYY[ hh:mm[:ss]]
   * @return секунды Юникс-эпохи
   */
  public static long datetime2unix(String sdat)
  {
    long t;
    String str;
    DateTimeFormatter dtf;  // формат преобразования времени
    if(sdat.matches("\\d+-\\d+-\\d+.*"))
      dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // дата вида YYYY-MM-DD
    else
      dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"); // дата вида DD.MM.YYYY
    // указана только дата ?
    if(sdat.matches("\\d+[-.]\\d+[-.]\\d+$")) {
      str = sdat + " 00:00:00"; // добавим время
    } else {
      if(sdat.matches("\\d+:\\d+:\\d+$"))
        str = sdat;         //  дата указана со временем
      else
        str = sdat + ":00"; // допишем секунды ко времени если их нет
    }
    try {
      // DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      LocalDateTime dt = LocalDateTime.parse(str, dtf);
      // @see https://stackoverflow.com/questions/41427384/how-to-get-default-zoneoffset-in-java8
      // ZoneOffset zoneOffset = OffsetDateTime.now(ZoneId.systemDefault()).getOffset ();
      t = dt.toEpochSecond(zoneOffset);
    } catch(Exception e) {
      System.out.println("?-Error datetime2unix(" + str + "): " + e.getMessage());
      return 0;
    }
    return t;
  }

  /**
   * Преобразовать Unix-epoch секунды в строку даты-времени по шаблону
   * @param tm  юникс секунды
   * @param pattern шаблон форматирования строки дата-время
   * @return  строка даты, времени в заданном формате
   */
  public static String  unix2str(long tm, String pattern)
  {
    DateTimeFormatter dft = DateTimeFormatter.ofPattern(pattern);
    LocalDateTime ld = LocalDateTime.ofEpochSecond(tm,0,zoneOffset);
    String str;
    str = dft.format(ld);
    return str;
  }

  /**
   * Преобразовать Unix-epoch секунды в строку даты
   * @param tm  юникс секунды
   * @return  строка даты, времени в формате YYYY-MM-DD
   */
  public static String  unix2date(long tm)
  {
    return unix2str(tm, "yyyy-MM-dd");
  }

} // end of class
