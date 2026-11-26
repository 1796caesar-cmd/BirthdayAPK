package ua.malinapk.birthdays;
import java.time.*;
import java.time.format.DateTimeFormatter;
import org.json.*;
final class Person {
 final int row; final String last, first, middle, job, unit; final LocalDate born;
 Person(int r,String a,String b,String c,String d,String e,LocalDate f){row=r;last=a;first=b;middle=c;job=d;unit=e;born=f;}
 String name(){return (last+" "+first+" "+middle).trim();}
 LocalDate birthday(int y){return born.withYear(y);}
 int ageIn(int y){return y-born.getYear();}
 int ageNow(LocalDate today){return ageIn(today.getYear())-(today.isBefore(birthday(today.getYear()))?1:0);}
 boolean jubilee(int y){int a=ageIn(y);return a>=25&&a%5==0;}
 JSONObject json() throws JSONException {return new JSONObject().put("row",row).put("last",last).put("first",first).put("middle",middle).put("job",job).put("unit",unit).put("born",born.toString());}
 static Person from(JSONObject j) throws JSONException{return new Person(j.getInt("row"),j.getString("last"),j.getString("first"),j.getString("middle"),j.getString("job"),j.getString("unit"),LocalDate.parse(j.getString("born")));}
}
