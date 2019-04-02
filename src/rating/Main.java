/*
 * Copyright (c) 2019. Aleksey Eremin
 * 29.03.19 8:28
 */

package rating;


public class Main {
  public static void main(String[] args) {
    System.out.println("Рейтинги Ревизора v 1.0");
    Model mdl = new Model();
    // write your code here
    if(args.length > 0) {
      int l;
      // указан номер акта
      l = mdl.load(args[0], 1.0);
      System.out.println("Операторов с рейтингом: " + l);
    } else {
      System.err.println("Укажи дату YYYY-MM-DD");
    }
  }

}