package ae;

/*
 Загрузка объекта JSON из АС Р.
 */

import job.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Базовый класс загрузки данных
 */
public class LoadJSON
{
  private Object jobj;

  public LoadJSON()
  {
    this.jobj = null;
  }

  /**
   * Создать объект JSON из уже существующего JSON объекта
   * @param obj JSON объект или массив
   */
  public LoadJSON(Object obj)
  {
    this.jobj = obj;
  }

  /**
   * Загружает текст из WEB в структуру данных JSON
   * @param url   URL страницы WEB
   * @param args  аргументы, передаваемые POST
   * @param par   название объекта в JSON, через точку можно указать вложенный объект
   */
  public LoadJSON(String url, Map<String,String> args, String par)
  {
    loadJSON(url,args,par);
  }

  /**
   * Вернуть имеющийся объект JSON
   * @return иеющийся JSON
   */
  public Object getObj()
  {
    return jobj;
  }

  /**
   * Загрузить JSON объект из другого JSON объекта
   * @param jobj  JSON объект
   * @param par название объекта в JSON, через точку можно указать вложенный объект
   * @return JSON объект
   */
  public Object  loadJSON(Object jobj, String par)
  {
    this.jobj = null;
    Object jo = jobj;
    int n = 0;
    String[] apar = null;
    if(par.length() > 0) {
      apar = par.split("\\.", 12);
      n = apar.length;
    }
    try {
      for (int i = 0; i < n; i++) {
        jo = ((JSONObject) jo).get(apar[i]);
      }
      this.jobj = jo;
      return this.jobj;
    } catch (Exception e) {
      System.out.println("?-Error-неправильная структура JSON объекта");
    }
    return null;
  }

  /**
   * Загружает текст из WEB в структуру данных JSON
   * @param url   URL страницы WEB
   * @param args  аргументы, передаваемые POST
   * @param par   название объекта в JSON, через точку можно указать вложенный объект
   * @return  структура данных массив JSON
   */
  public Object  loadJSON(String url, Map<String,String> args, String par)
  {
    this.jobj = null;
    ContentHttp conth = new ContentHttp();
    R.sleep(600); // перед чтением небольшая задержка
    String txt = conth.getContent(url, args); // загрузим
    if (txt == null) {
      System.out.println("?-Error-Не могу загрузить страницу - " + url);
      return null;
    }
    //
    int i, n;
    i = txt.indexOf("{");
    if(i >= 0) {
      n = txt.lastIndexOf('}') ; //, i+1);
      if(n > i) {
        // выделим текст внутри фигурных скобок
        txt = txt.substring(i, n+1);
        Object jo = new JSONObject(txt);
        return loadJSON(jo, par);
      }
    }
    System.out.println("?-Error-получен неверный ответ JSON");
    return null;
  }

  /*
  static String s2s(String str)
  {
    if(null == str) {
      return "null";
    }
    int l = str.length();
    if(l < 1) {
      return "null";
    }
    String s;
    s = str.substring(0,l);
    s = s.replace("'", "`");
    return "'" + s + "'";
  }
*/

  /*
  Получим JSON объект и имя его параметра на основе входных данных
   */
  private class pairval {
    JSONObject  jo;
    String      nam;
  }

  /**
   * Получить JSON-объект и имя его параметра (возможно, что вложенного),
   * чтобы получить из него значение параметра
   * @param object  JSON объект
   * @param par   имя параметра в узле (или через точку подузла),
   *              если отделено "/" то первый параметр содержит строку с описанием JSON
   * @return  внутренний класс
   */
  private pairval getPairval(JSONObject object, String par)
  {
    if(object == null) return null;
    pairval pv = null;
    try {
      String[] subs = par.split("/",2);
      // указана строка-параметр, содержащая JSON-объект?
      if(subs.length > 1) {
        String str = object.getString(subs[0]);
        object = new JSONObject(str);
        par = subs[1];
      }
      String[]  nams = par.split("\\.",8);
      Object ob = object;
      int n = nams.length - 1;
      for(int i=0; i < n; i++) {
        ob = ((JSONObject) ob).get(nams[0]);
      }
      pv = new pairval();
      pv.jo  = (JSONObject) ob;
      pv.nam = nams[n];
    } catch (Exception e) {
      // System.out.println(par + " - не найден - " + e.getMessage());
    }
    return pv;
  }

  /**
   * возвращает строковое значение параметра par объекта JSON
   * @param par   имя параметра в узле (или через точку подузле) если отделено "/" то первый параметр содержит строку с описанием JSON
   * @return  строковое значение или null
   */
  public String jStr(String par)
  {
    String val = null;
    pairval pv = getPairval((JSONObject)jobj, par);
    if(pv != null) {
      try {
        val = pv.jo.getString(pv.nam);
      } catch (JSONException e) {
        // System.out.println(e.getMessage());
      }
    }
    return val;
  }

  /**
   * возвращает числовое значение параметра par объекта JSON
   * @param par   имя параметра в узле если отделено "/" то первый параметр содержит строку c описанием JSON
   * @return  числовое значение
   */
  public int jInt(String par)
  {
    int val = 0;
    pairval pv = getPairval((JSONObject)jobj, par);
    if(pv != null) {
      try {
        val = pv.jo.getInt(pv.nam);
      } catch (JSONException e) {
        // System.out.println(e.getMessage());
      }
    }
    return val;
  }

} // end of class


