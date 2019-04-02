/*
 * Copyright (c) 2018. Алексей Еремин
 * 07.12.18 11:34
 */
/*
   Получение изображений скриншотов, вычисление хэш строки для изображений,
   хранение их в локальном кэше.
 */

package image;

import ae.ContentHttp;
import ae.Database;
import ae.DatabaseSqlite;
import ae.R;

import java.io.File;
import java.util.ArrayList;

public class UrlImage {
  private Database  db;                   // база данных кэшей
  // имя БД
  private String dbname;
  private String  act;  // акт
  // запрос создания таблицы хранения списков IP-адресов
  private final String create_table =
      "CREATE TABLE images (" +
          "url    TEXT PRIMARY KEY," +
          "wdat   DATETIME DEFAULT (DATETIME('now', 'localtime'))," +
          "act    TEXT," +
          "hash   TEXT," +
          "fname  TEXT," +
          "datc   DATETIME," +
          // 'флаг: 1 - есть на сервере, 0 нет на сервере, -1 есть но другой
          "flag   INT DEFAULT 1 " +
          ");";
  // время жизни записи в списке кэша изображения (сек)
  private int ImageTTL = R.ImageTTL;
  //
  private String  cacheTmpDir;  // имя временного каталога для кэша изображений
  //
  private HashImage hashImage = new HashImage();  // объект для вычисления хэш

  public UrlImage(String dbName, String act)
  {
    this.dbname = dbName;
    this.act = act; // акт, для которого записываем url
    init();
  }

  private void  init()
  {
    try {
      // получим имя системного временного каталога
//      File ftmp = File.createTempFile("testip-file", ".tmp");
//      String s = ftmp.getParent();
//      ftmp.delete();
      // выдать временный каталог (завершается обратным слэшем)
      // System.out.println("Temporary dir: " + s);
      String s = R.TMP + R.sep + R.CacheImageDir;
      File f = new File(s);
      if(!f.exists()) {
        f.mkdirs();
      }
      // запомним имя временного каталога для кэша изображений
      this.cacheTmpDir = f.getAbsolutePath() + R.sep;

      // проверим таблицу в БД
      testDB();     // проверим БД и если надо сделаем таблицу
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  public String getCacheTmpDir()
  {
    return this.cacheTmpDir;
  }

  /**
   * Вернуть хэш строку для изображения в Интернете.
   * Если хэша нет в БД, то считаем из АС изображение,
   * сохраним его во временную папку (это для отладки, для самой программы файл не нужен)
   * и вычислить хэш. Сохранить данные в БД и вернуть строку с хэш.
   * @param url   URL ресурса с изображением
   * @return  хэш-строка
   */
  public String hash(String url)
  {
    String hash = getDBhash(url);
    if(hash != null) {
      return hash;
    }
    byte[] jpg = getImage(url); // загрузим картинку
    if(null == jpg) {
      // по указанному URL нет изображения
      System.out.println("Нет изображения: " + url);
      return "null_array"; // хэш если картинки по ссылке нет
  }
    // вставим запись в БД
    hash = insertDBhash(url, jpg);
    return hash;
  }

  /**
   * Сверить все хэши сохраненных изображенийс сайтом АС
   * @return
   */
  public int  checkAllHash()
  {
    int cnt = 0;
    String sql = "SELECT url,hash FROM images";
    ArrayList<String[]> arr = db.DlookupArray(sql);
    for (String[] rst: arr) {
      String url = rst[0];  // URL
      String hash = rst[1]; // хэш
      byte[] img  = getImage(url);
      int flag = 0;
      if(img != null) {
        String has2 = hashImage.hash(img);  // хэш считанного изображения
        flag = 1;
        if(hash.compareTo(has2) != 0) {
          flag = -1;
          cnt++;
          System.out.println("изм: " + url);
        } else {
          System.out.print(".");
        }
      } else {
        System.out.println("нет: " + url);
      }
      updateDB(url, flag);
    }
    System.out.println(" ");
    return cnt;
  }

  /**
   * Загрузить изображение по URL
   * @param url URL изображения
   * @return  массив байт с изображением
   */
  private byte[]  getImage(String url)
  {
    ContentHttp conth = new ContentHttp();
    R.sleep(R.Pause); // перед чтением небольшая задержка
    byte[] jpg = conth.getImage(url); // загрузим
    return jpg;
  }

  ////////////////////////////////////////////////////////////////
  // работа с БД

  private void    testDB()
  {
    if(db == null) {
      db = new DatabaseSqlite(dbname);
      //
      String str = db.Dlookup("SELECT COUNT(*) FROM images;");
      if (str == null) {
        // ошибка чтения из БД - создадим таблицу hash
        db.ExecSql(create_table);
      }
      purge();  // очистка только при первом вызове init(), хотя он и так вызывается только 1 раз
    }
  }

  /**
   * Вставить в БД данныен о изображении
   * @param url   URL изображения
   * @param img   массив изображения
   * @return хэш вставленного изображения
   */
  private String  insertDBhash(String url, byte[] img)
  {
    String surl = url.replaceAll("'","");
    // сформируем имя файла для сохранения на диск
    String hash = hashImage.hash(img);
    String fn   = url.replaceAll("http.*shot/","").replaceAll("[:/?|<>']","-");
    String fnam = this.cacheTmpDir + fn; // имя файла для сохранения во временном каталоге
    String sql  = "INSERT INTO images (url,act,hash,fname) " +
        "VALUES('" + surl + "','"+ act + "','"+ hash +"','" + fnam +"')";
    db.ExecSql(sql);
    System.out.println(" + " + surl);
    // запишем на диск
    R.write2file(img, fnam);         // сохраним на диск изображение (для программы он не нужен)
    return hash;
  }

  /**
   * Получить хэш из БД
   * @param url URL изображения
   * @return  значение поля hash или NULL если нет записи
   */
  private String  getDBhash(String url)
  {
    String surl = url.replaceAll("'","");
    String hash;
    hash = db.Dlookup("SELECT hash FROM images WHERE url='" + surl + "'");
    return hash;
  }

  /**
   * Обновить запись url новым значением флага
   * @param url   URL записи
   * @param flag  флаг: 1 есть на сервере, 0 нет на сервере, -1 есть но другой
   */
  private void  updateDB(String url, int flag)
  {
    String surl = url.replaceAll("'","");
    String sql ="UPDATE images " +
        " SET datc=DATETIME('now','localtime'), flag=" + flag +
        " WHERE url='" + surl + "'";
    db.ExecSql(sql);
  }

  /**
   * Очистка кэша от устаревших элементов
   */
  private void purge()
  {
    // удалим старые записи
    String wh = "(strftime('%s','now','localtime')-strftime('%s',wdat)) > " + ImageTTL;
    String sql = "SELECT fname FROM images WHERE " + wh;
    ArrayList<String[]> arr = db.DlookupArray(sql);
    for (String[] rst: arr) {
      if(rst[0].length() > 3) { // короткие имена файлов не удаляем (они фейковые)
        File f = new File(rst[0]);
        f.delete();
      }
    }
    int a = db.ExecSql("DELETE FROM images WHERE " + wh);
    if(a > 0)
      System.out.println("Удалено старых изображений: " + a);
  }


} // END OF CLASS
