package ua.malinapk.birthdays;
import android.content.Context;import android.graphics.*;import java.io.*;import java.time.format.DateTimeFormatter;import java.util.*;
final class StandImage {
 static final int WIDTH=3072,HEIGHT=2048,MAX_ROWS=10;
 static final DateTimeFormatter DATE=DateTimeFormatter.ofPattern("dd.MM");
 static List<File> create(Context c,List<Person> people,int year,int month,boolean jub)throws IOException{
  List<File> result=new ArrayList<>();Bitmap bg;try(InputStream in=c.getAssets().open("stand_background.webp")){bg=BitmapFactory.decodeStream(in);}if(bg==null)throw new IOException("Не вдалося відкрити шаблон стенда");
  try{int pages=(people.size()+MAX_ROWS-1)/MAX_ROWS;String token=UUID.randomUUID().toString();for(int page=0;page<pages;page++){
   List<Person> list=people.subList(page*MAX_ROWS,Math.min(people.size(),(page+1)*MAX_ROWS));Bitmap image=Bitmap.createBitmap(WIDTH,HEIGHT,Bitmap.Config.ARGB_8888);
   try{Canvas canvas=new Canvas(image);Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);canvas.drawBitmap(bg,null,new Rect(0,0,WIDTH,HEIGHT),paint);paint.setColor(Color.BLACK);paint.setTypeface(font(c,true));paint.setTextSize(132);String title=ExportFilter.title(year,month,jub);while(paint.measureText(title)>1740)paint.setTextSize(paint.getTextSize()-2);paint.setTextAlign(Paint.Align.CENTER);at(canvas,paint,title,1536,486);
    paint.setTypeface(font(c,false));float step=Math.min(138,928f/Math.max(1,list.size()-1));float size=Math.min(118,step*.85f);float widest=0,dateWidth=0;while(true){paint.setTextSize(size);widest=0;for(Person person:list)widest=Math.max(widest,paint.measureText(person.name()));dateWidth=paint.measureText("00.00");if(widest+105+dateWidth<=1960||size<=35)break;size-=1;}
    float center=1650,left=center-(widest+105+dateWidth)/2,right=left+widest+105+dateWidth;
    for(int i=0;i<list.size();i++){Person person=list.get(i);float y=688+i*step;paint.setTextAlign(Paint.Align.LEFT);at(canvas,paint,person.name(),left,y);paint.setTextAlign(Paint.Align.RIGHT);at(canvas,paint,person.born.format(DATE),right,y);}
    String filename="Стенд_"+year+(month==0?"":String.format(Locale.ROOT,"-%02d",month))+"_"+token+"_"+(page+1)+".png";File file=ExcelShareProvider.generatedFile(c,filename);try(FileOutputStream out=new FileOutputStream(file)){if(!image.compress(Bitmap.CompressFormat.PNG,100,out))throw new IOException("Не вдалося записати PNG");}result.add(file);
   }finally{image.recycle();}
  }}finally{bg.recycle();}return result;
 }
 static Typeface font(Context c,boolean bold){try{return Typeface.createFromAsset(c.getAssets(),bold?"stand_bold.ttf":"stand_regular.ttf");}catch(RuntimeException missing){return Typeface.create("serif",bold?Typeface.BOLD:Typeface.NORMAL);}}
 static void at(Canvas c,Paint p,String text,float x,float y){Paint.FontMetrics f=p.getFontMetrics();c.drawText(text,x,y-(f.ascent+f.descent)/2,p);}
}
