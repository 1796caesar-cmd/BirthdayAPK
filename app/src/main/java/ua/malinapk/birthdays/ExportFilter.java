package ua.malinapk.birthdays;
import java.time.*;import java.text.Collator;import java.util.*;
final class ExportFilter {
 static final String[] MONTHS={"січня","лютого","березня","квітня","травня","червня","липня","серпня","вересня","жовтня","листопада","грудня"};
 static String key(String s){return s.trim().replaceAll("\\s+"," ").toLowerCase(Locale.ROOT);}
 static List<Person> select(List<Person> source,int year,int month,String unit,boolean jub,int sort){
  List<Person> list=new ArrayList<>();for(Person p:source)if(p.ageIn(year)>=0&&(month==0||p.birthday(year).getMonthValue()==month)&&(unit.isEmpty()||key(p.unit).equals(key(unit)))&&(!jub||p.jubilee(year)))list.add(p);
  Collator c=Collator.getInstance(Locale.forLanguageTag("uk-UA"));c.setStrength(Collator.SECONDARY);
  Comparator<Person> date=Comparator.comparing((Person p)->p.birthday(year)).thenComparing(Person::name,c);
  Comparator<Person> cmp=sort==1?Comparator.comparing(Person::name,c):sort==2?Comparator.comparing((Person p)->p.unit,c).thenComparing(date):sort==3?Comparator.comparingInt((Person p)->p.ageIn(year)).reversed().thenComparing(date):date;
  list.sort(cmp);return list;
 }
 static LocalDate standDay(YearMonth month){LocalDate d=month.atDay(1);while(d.getDayOfWeek()==DayOfWeek.SATURDAY||d.getDayOfWeek()==DayOfWeek.SUNDAY)d=d.plusDays(1);return d;}
 static String title(int year,int month,boolean jub){return "Іменинники "+(month==0?year+" року":MONTHS[month-1]);}
}
