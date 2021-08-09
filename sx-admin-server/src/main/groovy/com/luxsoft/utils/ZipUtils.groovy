package com.luxsoft.utils

import java.util.zip.ZipInputStream

class ZipUtils {


  public static Map<String, byte[]> descomprimir(byte[] zipData) {

    Map<String, byte[]> map = [:];

    ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(zipData));
    def entry
    while ((entry = zipStream.getNextEntry()) != null) {
      String entryName = entry.getName();
      byte[] byteBuff = new byte[4096];
      int bytesRead = 0;
      ByteArrayOutputStream out = new ByteArrayOutputStream()
      while ((bytesRead = zipStream.read(byteBuff)) != -1){
        out.write(byteBuff, 0, bytesRead);
      }
      map.put(entryName, out.toByteArray())
    }
    zipStream.close();

    return map
  }
}
