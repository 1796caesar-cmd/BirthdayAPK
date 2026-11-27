package ua.malinapk.birthdays;
import android.app.*;import android.content.*;import android.net.Uri;import android.os.Build;import org.json.*;import java.time.*;import java.util.*;
public class ReminderReceiver extends BroadcastReceiver {
 static final int STAND_ID=-100;
 static final String CHANNEL="birthdays", ACTION="ua.malinapk.birthdays.REMINDER";
 static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("birthday",0);}
 static void cache(Context c,List<Person> list)throws Exception{JSONArray a=new JSONArray();for(Person p:list)a.put(p.json());prefs(c).edit().putString("cache",a.toString()).apply();}
 static List<Person> cached(Context c){List<Person> p=new ArrayList<>();try{JSONArray a=new JSONArray(prefs(c).getString("cache","[]"));for(int i=0;i<a.length();i++)p.add(Person.from(a.getJSONObject(i)));}catch(Exception ignored){}return p;}
 static void channel(Context c){NotificationChannel n=new NotificationChannel(CHANNEL,"Дні народження",NotificationManager.IMPORTANCE_HIGH);n.setDescription("Нагадування про іменинників підприємства");c.getSystemService(NotificationManager.class).createNotificationChannel(n);}
 // IDs are stable; legacy rules 0, 1 and 2 retain their settings and pending intents.
 static List<Integer> rules(Context c){
  String raw=prefs(c).getString("ruleIds","0,1,2");List<Integer> ids=new ArrayList<>();
  for(String part:raw.split(","))try{int id=Integer.parseInt(part);if(id>=0&&!ids.contains(id))ids.add(id);}catch(NumberFormatException ignored){}
  return ids;
 }
 static String encode(List<Integer> ids){StringBuilder s=new StringBuilder();for(int id:ids){if(s.length()>0)s.append(',');s.append(id);}return s.toString();}
 static int offset(Context c,int id){return prefs(c).getInt("offset"+id,id<=2?id:0);}
 static boolean enabled(Context c,int id){return rules(c).contains(id)&&prefs(c).getBoolean("enabled"+id,true);}
 static String dayLabel(int o){return o==0?"У день народження":o==1?"За 1 день":"За "+o+" "+dayWord(o);}
 static String dayWord(int o){int n=o%100;return n>=11&&n<=14?"днів":o%10==1?"день":o%10>=2&&o%10<=4?"дні":"днів";}
 static PendingIntent alarm(Context c,int id){Intent i=new Intent(c,ReminderReceiver.class).setAction(ACTION).putExtra("offset",id);return PendingIntent.getBroadcast(c,id,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 static LocalTime time(Context c,int id){return LocalTime.of(prefs(c).getInt("hour"+id,8),prefs(c).getInt("minute"+id,0));}
 static void saveRule(Context c,int id,int offset,LocalTime time){
  List<Integer> ids=rules(c);android.content.SharedPreferences.Editor e=prefs(c).edit();
  if(id<0){id=Math.max(3,prefs(c).getInt("nextRuleId",3));while(ids.contains(id))id++;ids.add(id);e.putInt("nextRuleId",id+1).putString("ruleIds",encode(ids)).putBoolean("enabled"+id,true);}
  e.putInt("offset"+id,offset).putInt("hour"+id,time.getHour()).putInt("minute"+id,time.getMinute()).apply();scheduleOne(c,id);scheduleStand(c);
 }
 static void removeRule(Context c,int id){List<Integer> ids=rules(c);ids.remove(Integer.valueOf(id));prefs(c).edit().putString("ruleIds",encode(ids)).remove("offset"+id).remove("hour"+id).remove("minute"+id).remove("enabled"+id).remove("last"+id).apply();AlarmManager am=c.getSystemService(AlarmManager.class);PendingIntent pi=alarm(c,id);am.cancel(pi);pi.cancel();scheduleStand(c);}
 static void schedule(Context c){for(int id:rules(c))scheduleOne(c,id);scheduleStand(c);}
 static LocalTime standTime(Context c){LocalTime earliest=null;for(int id:rules(c))if(enabled(c,id)&&(earliest==null||time(c,id).isBefore(earliest)))earliest=time(c,id);return earliest==null?LocalTime.of(8,0):earliest;}
 static void scheduleStand(Context c){AlarmManager am=c.getSystemService(AlarmManager.class);PendingIntent pi=alarm(c,STAND_ID);am.cancel(pi);if(!prefs(c).getBoolean("standReminder",false)){pi.cancel();return;}LocalDateTime now=LocalDateTime.now();YearMonth month=YearMonth.from(now);LocalDate day=ExportFilter.standDay(month);if(now.toLocalDate().isAfter(day)||month.toString().equals(prefs(c).getString("lastStand","")))day=ExportFilter.standDay(month.plusMonths(1));LocalDateTime next=LocalDateTime.of(day,standTime(c));if(!next.isAfter(now))next=now.plusSeconds(2);setAlarm(c,am,pi,next);}
 static void setAlarm(Context c,AlarmManager am,PendingIntent pi,LocalDateTime next){long when=next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();try{if(Build.VERSION.SDK_INT<31||am.canScheduleExactAlarms())am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);}catch(SecurityException e){am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);}}
 static void sendStand(Context c){if(!prefs(c).getBoolean("standReminder",false))return;YearMonth month=YearMonth.now();if(LocalDate.now().equals(ExportFilter.standDay(month))&&!month.toString().equals(prefs(c).getString("lastStand",""))){notify(c,"Оновлення стенда іменинників","Не забудьте оновити список на стенді: іменинники "+ExportFilter.MONTHS[month.getMonthValue()-1]+".",STAND_ID);prefs(c).edit().putString("lastStand",month.toString()).apply();}scheduleStand(c);}

 static void scheduleOne(Context c,int id){AlarmManager am=c.getSystemService(AlarmManager.class);LocalDateTime now=LocalDateTime.now();PendingIntent pi=alarm(c,id);am.cancel(pi);if(!enabled(c,id)){pi.cancel();return;}LocalDateTime next=LocalDateTime.of(now.toLocalDate(),time(c,id));if(!next.isAfter(now))next=next.plusDays(1);long when=next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();try{if(Build.VERSION.SDK_INT<31||am.canScheduleExactAlarms())am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);}catch(SecurityException e){am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);}}
 static List<Person> current(Context c){try{Workbook w=new Workbook(LocalWorkbook.read(c));cache(c,w.people);return w.people;}catch(Exception ignored){return cached(c);}}
 static void notify(Context c,String title,String text,int id){channel(c);Intent i=new Intent(c,MainActivity.class);PendingIntent open=PendingIntent.getActivity(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);Notification n=new Notification.Builder(c,CHANNEL).setSmallIcon(android.R.drawable.ic_menu_my_calendar).setContentTitle(title).setContentText(text).setStyle(new Notification.BigTextStyle().bigText(text)).setAutoCancel(true).setContentIntent(open).build();try{c.getSystemService(NotificationManager.class).notify(id,n);}catch(SecurityException ignored){}}
 @Override public void onReceive(Context c,Intent i){
  if(!ACTION.equals(i.getAction())){schedule(c);return;}
  int id=i.getIntExtra("offset",-1);if(id==STAND_ID){sendStand(c);return;}if(!enabled(c,id))return;
  int offset=offset(c,id);LocalDate today=LocalDate.now();String date=today.toString();String key="last"+id;
  if(!date.equals(prefs(c).getString(key,""))){
   List<Person> list=current(c);LocalDate target=today.plusDays(offset);int yr=target.getYear();
   for(Person p:list)if(p.birthday(yr).equals(target)&&(!prefs(c).getBoolean("onlyJubilee",false)||p.jubilee(yr))){
    String title=(offset==0?"Сьогодні":offset==1?"Завтра":"Через "+offset+" "+dayWord(offset))+(p.jubilee(yr)?" — ювілей!":" — день народження!");
    notify(c,title,p.name()+"\n"+(offset==0?"Виповнилося ":"Виповниться ")+p.ageIn(yr)+" р.",Math.abs(Objects.hash(p.name(),p.born,id,target)));
   }
   if(today.getDayOfWeek()==DayOfWeek.MONDAY&&prefs(c).getBoolean("repeatWeekend",false))for(int back=2;back>=1;back--){LocalDate weekend=today.minusDays(back),birthday=weekend.plusDays(offset);int year=birthday.getYear();for(Person p:list)if(p.birthday(year).equals(birthday)&&(!prefs(c).getBoolean("onlyJubilee",false)||p.jubilee(year))){String dateLabel=birthday.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM"));String title="Повтор за "+(back==2?"суботу":"неділю")+" — "+(p.jubilee(year)?"ювілей":"день народження");notify(c,title,p.name()+"\nДень народження: "+dateLabel+" · "+p.ageIn(year)+" р.",Objects.hash("weekend",p.name(),p.born,id,weekend));}}
   prefs(c).edit().putString(key,date).apply();
  }
  scheduleOne(c,id);
 }
}
