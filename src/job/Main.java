/*
 * Copyright (c) 2019. Aleksey Eremin
 * 05.02.19 9:29
 */

/*
  Отслеживаем изменения скриншотов в АС Ревизор
 */

package job;

public class Main {

  public static void main(String[] args) {
    System.out.println("Версия " + R.Ver);
    Model mdl = new Model();
    // write your code here
    if(args.length > 0) {
      // указан номер акта
      mdl.loadAct(args[0]);
    } else {
      mdl.CheckUrls();
    }
  }
}
