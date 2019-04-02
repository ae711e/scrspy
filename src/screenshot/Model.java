/*
 * Copyright (c) 2019. Aleksey Eremin
 * 05.02.19 9:29
 */

/*
  Модель, выполняет прием данных из АС Р., вычисления и т.д.
 */

package screenshot;

import ae.LoadJSON;
import image.UrlImage;
import org.json.JSONArray;

import java.util.ArrayList;

import static ae.R.datetime2unix;

// модель
class Model {
  private static String DatabaseName = "scrspy.db";      // имя базы данных для кэшей

  private String              act_num;      // номер акта (справочно)
  private int                 id_act;       // код акта
  private int                 id_task;      // код задачи, если акт на основе задачи составлен
  private ArrayList<protocol> protocols;    // массив протоколов
  private ArrayList<protorec> recs;         // записи нарушений в протоколах
  private String              result;       // строка результата

  Model()
  {
    clear();
  }

  /**
   * сбросить состояние модели
   */
  public void clear()
  {
    act_num = "";
    id_act = 0;
    id_task = 0;
    protocols = null;
    recs   = null;
    result = "";
  }

  /**
   * Проверить IP адреса в протоколе для задачи
   * @param inputStr строка с номером акта или кодом задачи
   * @return  текст результата
   */
  String loadAct(String inputStr)
  {
    // сначала все почистим
    clear();
    // получим номера протоколов для данного акта
    if(getIdProtocols(inputStr) > 0) {
      for(protocol prot: protocols) {
        int a;
        a = getProtocolData(prot);  // получим сведения протокола по индексу
        System.out.println("Прочитано в протоколе " + prot.num + " записей: " + a);
      }
    }
    return result;
  }

  /**
   * Выдает номер обрабатываемого акта
   * @return номер акта
   */
  public String getActNum()
  {
    return act_num;
  }

  /**
   * Получить коды протоколов по номеру акта
   * @param numAct  строка с номером акта (содержит слэш / ) или задачи
   * @return код протокола
   */
  private int getIdProtocols(String numAct)
  {
    clear();
    // выявим номер акта вида NNN / NNN - указан номер акта
    int n1 = 0;
    int n2 = 0;
    String[] part = numAct.split("/");  // акт имеем номер вида "21 / 97"
    if (part.length == 2) {
      try {
        n1 = Integer.parseInt(part[0].trim());
        n2 = Integer.parseInt(part[1].trim());
      } catch (NumberFormatException e) {
        System.out.println(e.getMessage());
      }
    }
    if(n1 <=0 || n2 <= 0) {
      strResult("Номер акта введен неправильно");
      return 0;
    }
    // сделаем запрос по поиску указанного номера акта
    //
    // https://www.rfc-revizor.ru/lens/monitoring/info/getInfo?id_act=26731
    // https://www.rfc-revizor.ru/lens/monitoring/check/table?filter_act_num=21%2F97&
    // =
    String url;
    url = "https://www.rfc-revizor.ru/lens/monitoring/check/table?filter_act_num="
          + n1 + "%2F" + n2;
    LoadJSON lo;
    JSONArray ja;
    lo = new LoadJSON(url, null, "results.data");
    ja = (JSONArray) lo.getObj();
    if(ja != null && ja.length() > 0) {
      Object jo = ja.get(0);
      LoadJSON l1 = new LoadJSON(jo);
      id_act = l1.jInt("id_act");   // ид. акта
      id_task = l1.jInt("id_task"); // если на основе задачи, то не 0
    } else {
      strResult("Указанный акт не найден");
      return 0;
    }
    //
    this.act_num = n1 + "/" + n2; // номер акта (справочно)
    //
    String  tstart = null; // время запуска задачи, если протокол сформирован по задаче
    String  tstop = null;  // время остановки задачи
    // протокол сформирован по задаче, извлечем время выполнения
    if(id_task != 0) {
      String urlt = "https://www.rfc-revizor.ru/lens/info/taskinfo/getInfo?id_creator=1&id_task="
          + id_task;
      LoadJSON  l3 = new LoadJSON(urlt, null, "info");
      tstart = l3.jStr("ts_start"); // время старта задачи (и мониторинга)
      tstop  = l3.jStr("ts_stop");   // время остановки задачи (и мониторинга)
      System.out.println("Прочитали задачу " + id_task + " время " + tstart + " - " + tstop);
    }
    // по ид. акты выявим номера протоколов
    //url = "https://www.rfc-revizor.ru/lens/monitoring/info/getInfo?id_act=" + id_act;
    url = "https://www.rfc-revizor.ru/lens/monitoring/info/getInfo?id_act=" + id_act;
    lo = new LoadJSON(url, null, "info.files");
    ja = (JSONArray) lo.getObj(); // массив с данными по проколам
    if(ja == null || ja.length() < 1) {
      strResult("Данных о протоколах нет");
      return 0;
    }
    int Na = ja.length();
    // заведем массивы-списки под протоколы и записи
    protocols = new ArrayList<>();  // протоколы
    recs      = new ArrayList<>();  // все записи в одном массиве
    for(int i = 0; i < Na; i++) {
      Object    jo   = ja.get(i);
      LoadJSON  l1   = new LoadJSON(jo);
      protocol  prot = new protocol();    // заведем протокол
      prot.id = l1.jInt("id_protocol");   // ид. протокола
      String t1, t2;
      // если протокол по одной задаче, то время задачи
      if(id_task != 0) {
        t1 = tstart;
        t2 = tstop;
      } else {
        t1 = t2 = l1.jStr("ts");  // дата формирования протокола (За период)
      }
      prot.setTs_start(t1);
      prot.setTs_stop(t2);
      // сформируем номер протокола
      prot.num = l1.jStr("id_region") + "/" + l1.jStr("num");
      prot.screenshots = l1.jStr("scr_filename");
      // запомним протокол в массив
      this.protocols.add(prot);
    }
    return Na;
  }

  /**
   * Прочитать данные протокола и заполнить массив записей
   * @param   prot  протокол из массива протоколов
   * @return кол-во записей нарушений добавленных из протоколов
   */
  private int getProtocolData(protocol prot)
  {
    final int idprotocol = prot.id;
    strResult("Протокол " + prot.num);
    int cnt = 0;
    // https://www.rfc-revizor.ru/lens/monitoring/info/getProtocolCorrectInfo?id_protocol=36504
    String url = "https://www.rfc-revizor.ru/lens/monitoring/info/getProtocolCorrectInfo?id_protocol="
        + idprotocol;
    LoadJSON  l1 = new LoadJSON(url, null, "records");
    int n;
    JSONArray ja;
    try {
      ja = (JSONArray) l1.getObj();
      n = ja.length();
    } catch (Exception e) {
      System.out.println("?-Error-нет данных о протоколе [" + prot.num + "]");
      return 0;
    }
    //
    boolean isscr = (prot.screenshots != null); // если нет скриншота, то все плохо
    if(!isscr) {
      strResult("Скриншотов нет, все записи можно удалять");
    }
    UrlImage uimage = new UrlImage(DatabaseName, act_num);
    for(int i=0; i < n; i++) {
      try {
        LoadJSON ll = new LoadJSON(ja.get(i));
        //ll.loadJSON(ja.get(i), "");
        int eaisId   = ll.jInt("id0");          // код записи в ЕАИС
        int idx      = ll.jInt("index");        // индекс записи в протоколе
        String vh    = ll.jStr("virtualHost");  // хост с которого сделан скриншот
        String uscr  = ll.jStr("screenshot");   // ссылка на скриншот
        String  ts   = ll.jStr("ts");           // время выполнения мониторинга данной записи
        // заполним данные о записи в протоколе
        protorec re = new protorec();
        re.id_protocol = idprotocol;
        re.index  = idx;          // индекс записи в протоколе
        re.virtualHost = vh;      // IP адрес хоста с которого сделан скиншот
        re.ts     = ts;           // время мониторинга данной записи
        re.eais   = eaisId;       // ид. в реестре ЕАИС
        re.urlscr = uscr;         // ссылка на скриншот
        re.hashsсr = uimage.hash(uscr); // вычислим хэш
        this.recs.add(re);  // вставим в массив
        cnt++;
        strResult(cnt + ") ID:" + eaisId);
      } catch (Exception e) {
        strResult("[" + i + "] - ошибка получения данных из АС. " + e.getMessage());
      }
    }
    //
    return cnt;
  }

  /**
   * Проверяет имеющиеся URL
   */
  public void CheckUrls()
  {
    UrlImage uimage = new UrlImage(DatabaseName, "check");
    int a  = uimage.checkAllHash();
    System.out.println("Изменилось URL: " + a);
  }

  /**
   * добавляем к строке Результата строку сообщения и
   * выводим на стандартный вывод.
   * @param str строка сообщения
   */
  private void strResult(String str)
  {
    //result += str + "\r\n";
    System.out.println(str);
  }

  //////////////////////////////////////////////////////////////
  // вложенные классы
  //

  // данные о протоколе
  private class protocol {
    int     id;   // код протокола
    String  num;  // номер проткола (для вывода на экран)
    String  screenshots;    // имя файлов со скриншотами
    // (на основе задачи) (2018-08-29 19:37:19, может несколько дней)
    // (на основе оператора) (2018-08-29 1 день).
    private String  ts_start;       // время начала формирования протокола
    private String  ts_stop;        // время конца формирования протокола
    private long tm1=0, tm2=Long.MAX_VALUE;  // время проверки протокола в сек эпохи Юникс

    //------------------------------------------------------------------------------

    /**
     * установить время начала мониторинга и начало интервала проверки
     * @param ts дата в виде YYYY-MM-DD hh:mm:ss
     */
    void setTs_start(String ts)
    {
      this.ts_start = ts;
      tm1 = datetime2unix(ts_start);    // начало мониторинга
    }

    /**
     * Установить время конца мониторинга и конец интервал проверки (на конец суток, указанного дня)
     * @param ts дата в виде YYYY-MM-DD hh:mm:ss
     */
    void setTs_stop(String ts) {
      this.tm2 = Long.MAX_VALUE;
      try {
        String[] part = ts.split(" ");  // выделим дату
        // конец мониторинга это конец данных суток,
        // т.е начало суток + 24 часа
        this.ts_stop = part[0] + " 23:59:00";
        tm2 = datetime2unix(ts_stop);   // на конец суток, указанного дня
      } catch (Exception e) {
        System.out.println("?-Error setTs_stop(" + ts + "): " + e.getMessage());
      }
    }

  } // end of class "protocol"

  // данные по записям в протоколе
  private class protorec {
    int     id_protocol;    // ид. протокола
    String  virtualHost;    // IP адрес, где был сделан скриншот
    String  ips;            // список запрещенных IP
    int     index;          // индекс записи в протоколе
    String  ts;             // время проверки записи
    int     eais;           // ID записи в ЕАИС
    String  urlscr;         // URL для загрузки скриншота
    String  hashsсr;        // хэш скриншота
  } // enf of class "protorec"

} // end of class "Model"
