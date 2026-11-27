package ua.malinapk.birthdays;

import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.util.*;

/** Renders one original-template PNG for each selected jubilee employee. */
final class CertificateImage {
 static String safeName(String name){
  String cleaned=name.replaceAll("[^\\p{L}\\p{M}0-9 '’ʼ-]"," ").replaceAll(" +"," ").trim();
  if(cleaned.isEmpty())cleaned="Ювіляр";
  int length=cleaned.codePointCount(0,cleaned.length());
  if(length>70)cleaned=cleaned.substring(0,cleaned.offsetByCodePoints(0,70)).trim();
  return cleaned;
 }
 static List<File> create(Context c,List<Person> people)throws IOException{
  Bitmap background;
  try(InputStream in=c.getAssets().open("certificate_background.webp")){background=BitmapFactory.decodeStream(in);}
  if(background==null)throw new IOException("Не вдалося відкрити шаблон грамоти");
  List<File> files=new ArrayList<>();String token=UUID.randomUUID().toString();
  final int w=background.getWidth()*2,h=background.getHeight()*2;
  try{
   Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
   Typeface face=StandImage.font(c,true);
   for(int i=0;i<people.size();i++){
    Person person=people.get(i);
    Bitmap image=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
    File file=null;
    try{
     Canvas canvas=new Canvas(image);
     paint.setColor(Color.BLACK);paint.setTextAlign(Paint.Align.CENTER);
     canvas.drawBitmap(background,null,new Rect(0,0,w,h),paint);
     paint.setTypeface(face);paint.setTextSize(w*94f/2110f);
     String name=person.name();float maxWidth=w*.79f;
     float width=paint.measureText(name);if(width>maxWidth)paint.setTextSize(paint.getTextSize()*maxWidth/width);
     // Match the approved sample: center the visible glyph bounds in the empty line.
     Rect bounds=new Rect();paint.getTextBounds(name,0,name.length(),bounds);
     canvas.drawText(name,w*.505f,h*.414f-(bounds.top+bounds.bottom)/2f,paint);
     String filename="Грамота_"+safeName(name)+"_"+token+"_"+(i+1)+".png";
     file=ExcelShareProvider.generatedFile(c,filename);
     try(FileOutputStream out=new FileOutputStream(file)){if(!image.compress(Bitmap.CompressFormat.PNG,100,out))throw new IOException("Не вдалося записати PNG");}
     files.add(file);
    }catch(IOException|RuntimeException e){if(file!=null)file.delete();throw e;}finally{image.recycle();}
   }
   return files;
  }catch(IOException|RuntimeException e){for(File file:files)file.delete();throw e;}finally{background.recycle();}
 }
}
