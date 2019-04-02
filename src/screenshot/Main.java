/*
 * Copyright (c) 2019. Aleksey Eremin
 * 29.03.19 11:22
 */

/*
  Считываю рейнтинги операторов из АС Ревизор
 */

package screenshot;

public class Main {

  public static void main(String[] args) {
    System.out.println("Рейтинги Ревизора v 1.0");
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
