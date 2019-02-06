/*
 * (C) 2016-2018. Aleksey Eremin
 * HashImage.java created by ae on 07.09.16 15:21
 */
/*
  Расситывает хэш изображения
  по алгоритму MD5
 */

package image;

import java.security.MessageDigest;

/**
 * Created by ae on 07.09.2016.
 */
public class HashImage {

  // хэш изображения из файла
  public HashImage()
  {
  }

  /**
   * Вернуть хэш изображения из массива байт
   * @param arrayImage  массив байт с изображением
   * @return строка хэш изображения
   */
  public String hash(byte[] arrayImage)
  {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(arrayImage);
      byte[] byteData = md.digest();
      //конвертируем байт в шестнадцатеричный формат первым способом
      StringBuilder sb = new StringBuilder(64);
      for (byte aByteData : byteData) {
        sb.append(Integer.toString((aByteData & 0xff) + 0x100, 16).substring(1));
      }
      return sb.toString();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return "?-error hash";
  }

} // end class HashImage
