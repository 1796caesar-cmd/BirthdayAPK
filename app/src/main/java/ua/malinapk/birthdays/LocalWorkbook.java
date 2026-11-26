package ua.malinapk.birthdays;
import android.content.Context;
import android.net.Uri;
import android.util.AtomicFile;
import java.io.*;
import java.util.*;

final class LocalWorkbook {
 static final String NAME="Іменинники.xlsx";
 private static AtomicFile atomic(Context c){return new AtomicFile(new File(c.getFilesDir(),NAME));}
 static synchronized byte[] read(Context c)throws Exception{
  AtomicFile file=atomic(c);
  if(file.getBaseFile().exists()||new File(file.getBaseFile().getPath()+".bak").exists())return Workbook.bytes(file.openRead());
  byte[] data=null;
  String old=ReminderReceiver.prefs(c).getString("uri","");
  if(!old.isEmpty()){
   try{InputStream in=c.getContentResolver().openInputStream(Uri.parse(old));if(in!=null){byte[] previous=Workbook.bytes(in);new Workbook(previous);data=previous;}}catch(Exception ignored){}
  }
  if(data==null)throw new FileNotFoundException("Спочатку обери Excel-файл");
  new Workbook(data);write(c,data);
  ReminderReceiver.prefs(c).edit().remove("uri").apply();
  return data;
 }
 static synchronized void write(Context c,byte[] data)throws IOException{
  AtomicFile file=atomic(c);FileOutputStream out=null;
  try{out=file.startWrite();out.write(data);file.finishWrite(out);}catch(IOException|RuntimeException e){if(out!=null)file.failWrite(out);throw e;}
 }
}
