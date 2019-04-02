/*
 * Copyright (c) 2019. Aleksey Eremin
 * 29.03.19 11:30
 */
/*
  Читаем рейтинги операторов
 */

package rating;

import ae.Database;
import ae.DatabaseSqlite;
import ae.LoadJSON;
import org.json.JSONArray;

import static ae.R.*;

class Model {
  private static final String DatabaseName = "rating.db";      // имя базы данных для рейтингов
  // запрос создания таблицы хранения списков IP-адресов
  private Database  db; // база данных
  private static final String create_table =
      "CREATE TABLE ratings (" +
          "dat  DATE," +
          "inn  VARCHAR(255)," +
          "reg  INTEGER," +
          "rate REAL," +
          "nam  VARCHAR(255)," +
          "wdat DATETIME DEFAULT (DATETIME('now', 'localtime'))," +
          "pkid INTEGER PRIMARY KEY AUTOINCREMENT," +
          "UNIQUE (dat,inn)" +
      ");";

  Model()
  {
    // проверим таблицу в БД
    testDB();     // проверим БД и если надо сделаем таблицу
  }

  /**
   * Загрузить данные о рейтинге за указанную дату
   * @param sdat  дата в формате YYYY-MM-DD
   * @param pcrt  процент рейтинга
   * @return  кол-во операторов с рейтингом
   */
  int load(String sdat, double pcrt)
  {
    int   cnt = 0;
    long  t1 = datetime2unix(sdat);
    String  url;
    final String dtt = unix2date(t1);
    // https://www.rfc-revizor.ru/rev/reports/oprateresourse/table?from=1546894800&to=0&gtStep=86400&custom=4&f_float-pcrt=1
    url = "https://www.rfc-revizor.ru/rev/reports/oprateresourse/table?gtStep=86400&custom=4&f_float-pcrt=" + pcrt + "&to=0&from=" + t1;
    LoadJSON lo;
    JSONArray ja;
    lo = new LoadJSON(url, null, "data");
    ja = (JSONArray) lo.getObj(); // массив с данными рейтига
    if(ja == null || ja.length() < 1) {
      System.out.println("Данных о рейтинге нет");
      return 0;
    }
    int Na = ja.length();
    for(int i = 0; i < Na; i++) {
      Object    jo = ja.get(i);
      LoadJSON  l1 = new LoadJSON(jo);
      try {
        String inn = l1.jStr("inn");            // ИНН оператора
        String nam = l1.jStr("op_name");        // название оператора
        String nme = nam.replaceAll("'","`");   // заменить апостроф
        int reg = l1.jInt("id_region");         // номер региона
        String rt = l1.jStr("not_block_pcrt");  // процепнт рейтинга
        double d = Double.parseDouble(rt);
        String sql;
        sql = "INSERT INTO ratings(dat,inn,reg,rate,nam) " +
              " VALUES('"+dtt+"','"+inn+"',"+reg+","+d+",'"+nme+"');";
        int a = db.ExecSql(sql);
        if(a > 0) {
          //System.out.print(".");
          System.out.println("Регион: " + reg + " ИНН: " + inn + " рейтинг: " + rt);
          cnt++;
        }
      } catch (Exception e) {
        System.err.println(e.getMessage());
      }
    }
    System.out.println(" ");
    return cnt;
  }

  private void testDB()
  {
    if(db == null) {
      db = new DatabaseSqlite(DatabaseName);
      //
      String str = db.Dlookup("SELECT COUNT(*) FROM ratings;");
      if (str == null) {
        // ошибка чтения из БД - создадим таблицу
        db.ExecSql(create_table);
      }
    }
  }

} // end of class


/*

https://www.rfc-revizor.ru/rev/reports/oprateresourse/table?from=1546894800&to=0&gtStep=86400&custom=4&f_float-pcrt=1



 */