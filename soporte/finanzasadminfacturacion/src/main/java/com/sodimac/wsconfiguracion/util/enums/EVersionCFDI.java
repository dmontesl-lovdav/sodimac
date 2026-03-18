package com.sodimac.wsconfiguracion.util.enums;

public enum EVersionCFDI {
   VERSION_33("3.3", 1),
   VERSION_40("4.0", 2);

   private String version;
   private int id;

   private EVersionCFDI(String version, int id) {
      this.version = version;
      this.id = id;
   }

   public String getVersion() {
      return this.version;
   }

   public void setVersion(String version) {
      this.version = version;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public static EVersionCFDI getVersionByDesc(String version) {
      EVersionCFDI[] var4;
      int var3 = (var4 = values()).length;

      for(int var2 = 0; var2 < var3; ++var2) {
         EVersionCFDI ver = var4[var2];
         if (ver.getVersion().equals(version)) {
            return ver;
         }
      }

      return null;
   }
}
