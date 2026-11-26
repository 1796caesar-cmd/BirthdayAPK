package ua.malinapk.birthdays;
import android.Manifest;import android.app.*;import android.content.*;import android.content.pm.PackageManager;import android.net.Uri;import android.os.*;import android.provider.Settings;import android.graphics.Color;import android.graphics.Typeface;import android.graphics.drawable.GradientDrawable;import android.view.*;import android.widget.*;import java.io.*;import java.time.*;import java.time.format.*;import java.util.*;
public class MainActivity extends Activity {
 static final int GREEN=0xff235d45, INK=0xff20352d, MUTED=0xff64756a, BG=0xfff2f5f0, GOLD=0xff956000, WHITE=Color.WHITE;
 static final int PICK_EXCEL=101, INSTALL_PERMISSION=103;
 static final DateTimeFormatter DATE=DateTimeFormatter.ofPattern("dd.MM.uuuu");
 final String[] months={"Січень","Лютий","Березень","Квітень","Травень","Червень","Липень","Серпень","Вересень","Жовтень","Листопад","Грудень"};
 List<Person> people=new ArrayList<>();YearMonth selected=YearMonth.now();LinearLayout root;boolean ready=false;String mode="calendar";String status="";
 android.content.SharedPreferences prefs(){return ReminderReceiver.prefs(this);}
 @Override protected void attachBaseContext(Context base){
  android.content.res.Configuration config=new android.content.res.Configuration();
  int percent=ReminderReceiver.prefs(base).getInt("fontPercent",100);
  config.fontScale=base.getResources().getConfiguration().fontScale*Math.max(100,Math.min(150,percent))/100f;
  super.attachBaseContext(base.createConfigurationContext(config));
 }
 @Override protected void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putString("screenMode",mode);b.putString("selectedMonth",selected.toString());}
 int fontDp(float n){return dp(n*Math.max(100,Math.min(150,prefs().getInt("fontPercent",100)))/100f);}
 int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
 @Override public void onCreate(Bundle b){super.onCreate(b);if(b!=null){mode=b.getString("screenMode","calendar");try{selected=YearMonth.parse(b.getString("selectedMonth",YearMonth.now().toString()));}catch(Exception ignored){}}getWindow().setStatusBarColor(GREEN);getWindow().setNavigationBarColor(BG);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);people=ReminderReceiver.cached(this);ReminderReceiver.channel(this);if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},1);reload(false);show();}
 @Override protected void onResume(){super.onResume();if(root!=null){reload(false);show();}}
 void msg(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 byte[] read()throws Exception{return LocalWorkbook.read(this);}
 void reload(boolean announce){try{Workbook w=new Workbook(read());people=w.people;ReminderReceiver.cache(this,people);ReminderReceiver.schedule(this);ready=true;status="Працівників: "+people.size();if(announce)msg("Список оновлено");}catch(Exception e){ready=false;status=e instanceof FileNotFoundException?"Обери Excel-файл у налаштуваннях":"Excel недоступний: "+e.getMessage()+". Показано останні збережені дані.";if(announce)msg(status);}}
 void pickExcel(){
  Runnable open=()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivityForResult(i,PICK_EXCEL);};
  if(ready)new AlertDialog.Builder(this).setTitle("Замінити Excel?").setMessage("Новий файл замінить поточний список. Спочатку вивантаж актуальний Excel, якщо хочеш зберегти внесені зміни.").setPositiveButton("Обрати файл",(d,w)->open.run()).setNegativeButton("Скасувати",null).show();else open.run();
 }
 @Override protected void onActivityResult(int request,int result,Intent data){
  super.onActivityResult(request,result,data);
  if(request==INSTALL_PERMISSION){if(getPackageManager().canRequestPackageInstalls())installUpdate();else msg("Для встановлення оновлення потрібен дозвіл Android");return;}
  if(request!=PICK_EXCEL||result!=RESULT_OK||data==null||data.getData()==null)return;
  try{
   InputStream in=getContentResolver().openInputStream(data.getData());if(in==null)throw new IOException("Не вдалося відкрити файл");
   byte[] bytes=Workbook.bytes(in);Workbook workbook=new Workbook(bytes);
   if(ready){try(FileOutputStream backup=new FileOutputStream(new File(getFilesDir(),"Іменинники-backup.xlsx"))){backup.write(read());}}
   LocalWorkbook.write(this,bytes);prefs().edit().remove("uri").apply();reload(false);show();msg("Завантажено працівників: "+workbook.people.size());
  }catch(Exception e){msg("Не вдалося завантажити Excel: "+e.getMessage());}
 }
 void shareExcel(){try{
  byte[] data=read();new Workbook(data);
  Uri share=ExcelShareProvider.uri(this);
  Intent send=new Intent(Intent.ACTION_SEND).setType(ExcelShareProvider.MIME).putExtra(Intent.EXTRA_STREAM,share).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
  send.setClipData(ClipData.newRawUri("Іменинники.xlsx",share));
  startActivity(Intent.createChooser(send,"Поділитися Excel-файлом"));
 }catch(Exception e){msg("Не вдалося поділитися Excel: "+e.getMessage());}}
 GradientDrawable bg(int c,int rad){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(rad));return d;}
 LinearLayout col(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;}
 LinearLayout row(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.HORIZONTAL);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
 TextView text(String s,int size,int color,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);if(bold)v.setTypeface(null,Typeface.BOLD);return v;}
 void pad(View v,int a){v.setPadding(dp(a),dp(a),dp(a),dp(a));}
 void add(LinearLayout l,View v){l.addView(v,new LinearLayout.LayoutParams(-1,-2));}
 void weight(LinearLayout l,View v){l.addView(v,new LinearLayout.LayoutParams(0,-2,1));}
 Button button(String s,Runnable f){Button b=new Button(this);b.setAllCaps(false);b.setText(s);b.setTextColor(GREEN);b.setOnClickListener(v->f.run());return b;}
 void gap(LinearLayout l,int n){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,dp(n)));}
 LinearLayout card(LinearLayout parent){LinearLayout c=col();c.setBackground(bg(WHITE,13));pad(c,12);add(parent,c);gap(parent,8);return c;}
 void show(){root=col();root.setBackgroundColor(BG);if(Build.VERSION.SDK_INT>=35){root.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets sys=insets.getInsets(WindowInsets.Type.systemBars());v.setPadding(sys.left,sys.top,sys.right,sys.bottom);return insets;});}setContentView(root);LinearLayout header=col();header.setBackgroundColor(GREEN);pad(header,16);add(header,text("Іменинники АПК",24,WHITE,true));add(header,text("ДП «Малинський лісгосп АПК»",13,0xffd7ebdb,false));add(root,header);
 ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);LinearLayout body=col();pad(body,12);scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));TextView st=text(status,12,ready?MUTED:0xffa13c22,false);add(body,st);gap(body,8);if(!ready&&!mode.equals("settings"))add(body,button("Обрати Excel",this::pickExcel));
 if(mode.equals("calendar"))calendar(body);else if(mode.equals("people"))allPeople(body);else if(mode.equals("settings"))settings(body);else reminders(body);
 LinearLayout nav=row();nav.setBackgroundColor(WHITE);String[] labels={"Календар","Працівники","Нагадування","Налаштування"};String[] modes={"calendar","people","reminders","settings"};int[] icons={R.drawable.nav_calendar,R.drawable.nav_people,R.drawable.nav_bell,R.drawable.nav_settings};
 for(int k=0;k<4;k++){final String m=modes[k];boolean active=mode.equals(m);int color=active?GREEN:MUTED;
  LinearLayout item=col();item.setGravity(Gravity.CENTER);item.setPadding(dp(2),dp(8),dp(2),dp(8));item.setMinimumHeight(dp(60));
  ImageView icon=new ImageView(this);icon.setImageResource(icons[k]);icon.setColorFilter(color);item.addView(icon,new LinearLayout.LayoutParams(dp(24),dp(24)));
  TextView t=text(labels[k],10,color,active);t.setGravity(Gravity.CENTER);t.setMaxLines(2);t.setBreakStrategy(android.text.Layout.BREAK_STRATEGY_SIMPLE);add(item,t);
  item.setContentDescription(labels[k]);item.setSelected(active);item.setOnClickListener(v->{mode=m;show();});nav.addView(item,new LinearLayout.LayoutParams(0,-1,1));
 }add(root,nav);}
 List<Person> sorted(List<Person> src,int y){List<Person> a=new ArrayList<>(src);a.sort(Comparator.comparing((Person p)->p.birthday(y)).thenComparing(Person::name,String.CASE_INSENSITIVE_ORDER));return a;}
 void calendar(LinearLayout b){LinearLayout r=row();r.addView(button("‹",()->{selected=selected.minusMonths(1);show();}));TextView title=text(months[selected.getMonthValue()-1]+" "+selected.getYear(),20,INK,true);title.setGravity(Gravity.CENTER);title.setOnClickListener(v->chooseMonth());weight(r,title);r.addView(button("›",()->{selected=selected.plusMonths(1);show();}));add(b,r);if(!selected.equals(YearMonth.now()))add(b,button("Поточний місяць",()->{selected=YearMonth.now();show();}));int y=selected.getYear();List<Person> list=new ArrayList<>();int j=0,today=0;for(Person p:sorted(people,y))if(p.birthday(y).getMonthValue()==selected.getMonthValue()){list.add(p);if(p.jubilee(y))j++;if(p.birthday(y).equals(LocalDate.now()))today++;}LinearLayout stats=card(b);add(stats,text("Іменинників: "+list.size()+"   ·   Ювілярів: "+j+"   ·   Сьогодні: "+today,14,GREEN,true));LinearLayout titles=row();TextView d=text("Дата",12,MUTED,true);titles.addView(d,new LinearLayout.LayoutParams(fontDp(55),-2));weight(titles,text("ПІБ",12,MUTED,true));titles.addView(text("Вік",12,MUTED,true),new LinearLayout.LayoutParams(fontDp(48),-2));pad(titles,8);add(b,titles);if(list.isEmpty())add(b,text("У цьому місяці немає іменинників",15,MUTED,false));for(Person p:list)personRow(b,p,y,true);add(b,button("＋ Додати працівника",()->edit(null)));}
 void chooseMonth(){LinearLayout l=col();pad(l,16);Spinner m=new Spinner(this);m.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,months));m.setSelection(selected.getMonthValue()-1);EditText y=new EditText(this);y.setInputType(2);y.setText(""+selected.getYear());add(l,m);add(l,y);new AlertDialog.Builder(this).setTitle("Місяць і рік").setView(l).setPositiveButton("Вибрати",(d,w)->{try{selected=YearMonth.of(Integer.parseInt(y.getText().toString()),m.getSelectedItemPosition()+1);show();}catch(Exception e){msg("Некоректний рік");}}).setNegativeButton("Скасувати",null).show();}
 void personRow(LinearLayout b,Person p,int y,boolean date){boolean jub=p.jubilee(y),now=p.birthday(y).equals(LocalDate.now());LinearLayout r=row();r.setBackground(bg(now?0xffdcefe0:jub?0xffffedc4:WHITE,9));pad(r,9);if(date)r.addView(text(p.birthday(y).format(DateTimeFormatter.ofPattern("dd.MM")),13,now?GREEN:INK,true),new LinearLayout.LayoutParams(fontDp(55),-2));LinearLayout n=col();add(n,text(p.name(),15,INK,jub));if(!date)add(n,text(p.unit+" · "+p.job,12,MUTED,false));weight(r,n);TextView age=text(p.ageIn(y)+""+(jub?" ★":""),15,jub?GOLD:INK,true);age.setGravity(Gravity.END);r.addView(age,new LinearLayout.LayoutParams(fontDp(48),-2));r.setOnClickListener(v->details(p,y));add(b,r);gap(b,5);}
 void allPeople(LinearLayout b){add(b,button("＋ Додати працівника",()->edit(null)));EditText search=new EditText(this);search.setSingleLine(true);search.setHint("Пошук за ПІБ, посадою, підрозділом");add(b,search);CheckBox jubilees=new CheckBox(this);jubilees.setText("Тільки ювіляри року ("+selected.getYear()+")");add(b,jubilees);LinearLayout list=col();add(b,list);Runnable render=()->{list.removeAllViews();String q=search.getText().toString().toLowerCase(Locale.ROOT);List<Person>a=new ArrayList<>(people);a.sort(Comparator.comparing(Person::name,String.CASE_INSENSITIVE_ORDER));for(Person p:a)if((p.name()+" "+p.job+" "+p.unit).toLowerCase(Locale.ROOT).contains(q)&&(!jubilees.isChecked()||p.jubilee(selected.getYear())))personRow(list,p,selected.getYear(),false);};search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){render.run();}public void afterTextChanged(android.text.Editable e){}});jubilees.setOnCheckedChangeListener((x,v)->render.run());render.run();}
 void field(LinearLayout l,String label,String val){add(l,text(label,12,MUTED,false));add(l,text(val.isEmpty()?"—":val,17,INK,true));gap(l,8);}
 void details(Person p,int y){ScrollView s=new ScrollView(this);LinearLayout l=col();pad(l,18);s.addView(l);field(l,"Прізвище",p.last);field(l,"Ім’я",p.first);field(l,"По батькові",p.middle);field(l,"Посада/підрозділ",(p.job.trim()+" "+p.unit.trim()).trim());field(l,"Повна дата народження",p.born.format(DATE));field(l,"Вік на сьогодні",p.ageNow(LocalDate.now())+" р.");field(l,"Вік у "+y+" році",p.ageIn(y)+" р.");if(p.jubilee(y))add(l,text("★ Ювіляр "+y+" року",16,GOLD,true));new AlertDialog.Builder(this).setTitle("Інформація про працівника").setView(s).setPositiveButton("Редагувати",(d,w)->edit(p)).setNeutralButton("Видалити",(d,w)->new AlertDialog.Builder(this).setMessage("Видалити "+p.name()+" з Excel?").setPositiveButton("Видалити",(a,z)->save(p,p,true)).setNegativeButton("Скасувати",null).show()).setNegativeButton("Закрити",null).show();}
 void edit(Person p){if(!ready){msg("Спочатку обери Excel-файл");return;}ScrollView s=new ScrollView(this);LinearLayout l=col();pad(l,16);s.addView(l);String[] names={"Прізвище","Ім’я","По батькові","Підрозділ","Посада","Дата народження (ДД.ММ.РРРР)"};String[] vals=p==null?new String[6]:new String[]{p.last,p.first,p.middle,p.unit,p.job,p.born.format(DATE)};EditText[] fields=new EditText[6];for(int i=0;i<6;i++){add(l,text(names[i],13,MUTED,true));EditText e=new EditText(this);e.setSingleLine(true);e.setText(vals[i]==null?"":vals[i]);if(i==5)e.setInputType(android.text.InputType.TYPE_CLASS_DATETIME|android.text.InputType.TYPE_DATETIME_VARIATION_DATE);fields[i]=e;add(l,e);}AlertDialog dialog=new AlertDialog.Builder(this).setTitle(p==null?"Новий працівник":"Редагування").setView(s).setPositiveButton("Зберегти",null).setNegativeButton("Скасувати",null).create();dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(x->{try{String[] a=new String[6];for(int i=0;i<6;i++)a[i]=fields[i].getText().toString().trim();if(a[0].isEmpty()||a[1].isEmpty()||a[2].isEmpty())throw new IllegalArgumentException("Заповни ПІБ");LocalDate d=LocalDate.parse(a[5],DATE.withResolverStyle(ResolverStyle.STRICT));if(d.isAfter(LocalDate.now())||d.getYear()<1900)throw new IllegalArgumentException("Некоректна дата");Person q=new Person(p==null?0:p.row,a[0],a[1],a[2],a[4],a[3],d);if(save(p,q,false))dialog.dismiss();}catch(Exception e){msg("Перевір дані: "+e.getMessage());}}));dialog.show();}
 boolean same(Person a,Person b){return a.name().equals(b.name())&&a.born.equals(b.born)&&a.job.equals(b.job)&&a.unit.equals(b.unit);}
 boolean save(Person old,Person p,boolean delete){if(!ready){msg("Excel недоступний");return false;}try{byte[] original=read();Workbook w=new Workbook(original);if(old!=null){Person current=null;for(Person q:w.people)if(q.row==old.row)current=q;if(current==null||!same(current,old))throw new IOException("Excel змінився. Відкрий картку повторно");}if(!delete)for(Person q:w.people)if(q.row!=p.row&&q.name().equalsIgnoreCase(p.name())&&q.born.equals(p.born))throw new IOException("Такий працівник вже є в Excel");w.put(p,delete);byte[] changed=w.save();Workbook check=new Workbook(changed);if(check.people.size()!=w.people.size()+(old==null?1:delete?-1:0))throw new IOException("Помилка перевірки Excel");File backup=new File(getFilesDir(),"Іменинники-backup.xlsx");try(FileOutputStream out=new FileOutputStream(backup)){out.write(original);}try{write(changed);if(!Arrays.equals(read(),changed))throw new IOException("Перевірка запису не пройдена");}catch(Exception ex){try{write(original);}catch(Exception restore){throw new IOException("Не вдалося відновити Excel. Резервна копія збережена у внутрішній пам’яті.");}throw ex;}reload(false);show();msg("Зміни збережено в Excel");return true;}catch(Exception e){msg("Зміни не збережено: "+e.getMessage());return false;}}
 void write(byte[] b)throws Exception{LocalWorkbook.write(this,b);}
 void reminders(LinearLayout b){
  LinearLayout c=card(b);add(c,text("Розклад нагадувань",19,GREEN,true));
  List<Integer> ids=ReminderReceiver.rules(this);
  ids.sort(Comparator.comparingInt((Integer id)->ReminderReceiver.offset(this,id)).reversed().thenComparing(id->ReminderReceiver.time(this,id)));
  if(ids.isEmpty())add(c,text("Немає нагадувань. Додай потрібний день і час.",14,MUTED,false));
  for(int id:ids){
   LinearLayout r=row();CheckBox cb=new CheckBox(this);
   cb.setText(ReminderReceiver.dayLabel(ReminderReceiver.offset(this,id)));cb.setTextColor(INK);
   cb.setChecked(ReminderReceiver.enabled(this,id));weight(r,cb);add(c,r);
   cb.setOnCheckedChangeListener((v,on)->{prefs().edit().putBoolean("enabled"+id,on).apply();ReminderReceiver.scheduleOne(this,id);});
   LinearLayout actions=row();weight(actions,button(ReminderReceiver.time(this,id).format(DateTimeFormatter.ofPattern("HH:mm"))+" · Змінити",()->editReminder(id)));
   weight(actions,button("Видалити",()->new AlertDialog.Builder(this).setTitle("Видалити нагадування?").setPositiveButton("Видалити",(d,w)->{ReminderReceiver.removeRule(this,id);show();}).setNegativeButton("Скасувати",null).show()));
   add(c,actions);gap(c,6);
  }
  add(c,button("＋ Додати нагадування",()->editReminder(-1)));
  CheckBox only=new CheckBox(this);only.setText("Нагадувати тільки про ювілярів");only.setChecked(prefs().getBoolean("onlyJubilee",false));only.setOnCheckedChangeListener((v,on)->prefs().edit().putBoolean("onlyJubilee",on).apply());add(c,only);add(b,button("Перевірити сповіщення",()->ReminderReceiver.notify(this,"Тестове нагадування","Сповіщення працюють",999)));if(Build.VERSION.SDK_INT>=31&&!getSystemService(AlarmManager.class).canScheduleExactAlarms()){add(b,text("Для точного часу дозволь системі встановлювати будильники та нагадування.",14,GOLD,false));add(b,button("Дозволити точні нагадування",()->{try{startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));}catch(Exception e){msg("Відкрий дозволи додатка в налаштуваннях Android");}}));}}
 void editReminder(int id){
  LinearLayout l=col();pad(l,16);
  add(l,text("За скільки днів до дня народження (0 — у цей день)",14,INK,true));
  EditText days=new EditText(this);days.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);days.setSingleLine(true);days.setText(String.valueOf(id<0?3:ReminderReceiver.offset(this,id)));add(l,days);
  final LocalTime[] chosen={id<0?LocalTime.of(8,0):ReminderReceiver.time(this,id)};
  Button clock=button("Час: "+chosen[0].format(DateTimeFormatter.ofPattern("HH:mm")),()->{});
  clock.setOnClickListener(v->new TimePickerDialog(this,(view,h,m)->{chosen[0]=LocalTime.of(h,m);clock.setText("Час: "+chosen[0].format(DateTimeFormatter.ofPattern("HH:mm")));},chosen[0].getHour(),chosen[0].getMinute(),true).show());add(l,clock);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle(id<0?"Нове нагадування":"Змінити нагадування").setView(l).setPositiveButton("Зберегти",null).setNegativeButton("Скасувати",null).create();
  dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(x->{try{
   int offset=Integer.parseInt(days.getText().toString().trim());if(offset<0||offset>365)throw new IllegalArgumentException("Введи число від 0 до 365");
   for(int other:ReminderReceiver.rules(this))if(other!=id&&ReminderReceiver.offset(this,other)==offset&&ReminderReceiver.time(this,other).equals(chosen[0]))throw new IllegalArgumentException("Таке нагадування вже є");
   ReminderReceiver.saveRule(this,id,offset,chosen[0]);dialog.dismiss();show();
  }catch(Exception e){msg(e.getMessage()==null?"Перевір кількість днів":e.getMessage());}}));dialog.show();
 }
 void fontSettings(){
  String[] sizes={"100% (стандартний)","110%","120%","130%","140%","150%"};int current=prefs().getInt("fontPercent",100);
  new AlertDialog.Builder(this).setTitle("Розмір шрифту").setSingleChoiceItems(sizes,Math.max(0,Math.min(5,(current-100)/10)),(d,w)->{prefs().edit().putInt("fontPercent",100+w*10).apply();d.dismiss();recreate();}).setNegativeButton("Скасувати",null).show();
 }
 void settings(LinearLayout b){LinearLayout appearance=card(b);add(appearance,text("Зовнішній вигляд",19,GREEN,true));add(appearance,button("Розмір шрифту: "+prefs().getInt("fontPercent",100)+"%",this::fontSettings));LinearLayout update=card(b);add(update,text("Оновлення додатка",19,GREEN,true));add(update,text("Встановлена версія: "+installedVersion(),14,MUTED,false));add(update,button("Перевірити оновлення",this::checkUpdate));if(ExcelShareProvider.apkFile(this).isFile())add(update,button("Встановити завантажене оновлення",this::installUpdate));LinearLayout c=card(b);add(c,text("Excel — база працівників",19,GREEN,true));add(c,button("Обрати Excel",this::pickExcel));Button export=button("Вивантажити Excel",this::shareExcel);export.setEnabled(ready);add(c,export);add(c,text("Редагується локальна копія. Вивантаження надсилає актуальний файл з усіма змінами.",13,MUTED,false));add(b,button("Системні дозволи додатка",()->startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())))));}


 static final String RELEASE_API="https://api.github.com/repos/1796caesar-cmd/BirthdayAPK/releases/latest";
 static final String RELEASE_DOWNLOAD="https://github.com/1796caesar-cmd/BirthdayAPK/releases/download/";
 boolean checkingUpdate=false;
 String installedVersion(){try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(Exception e){return "невідома";}}
 long installedCode()throws Exception{android.content.pm.PackageInfo info=getPackageManager().getPackageInfo(getPackageName(),0);return Build.VERSION.SDK_INT>=28?info.getLongVersionCode():info.versionCode;}
 void checkUpdate(){
  if(checkingUpdate){msg("Перевірка вже триває");return;}
  checkingUpdate=true;msg("Перевіряємо оновлення…");
  new Thread(()->{
   java.net.HttpURLConnection conn=null;
   try{
    conn=(java.net.HttpURLConnection)new java.net.URL(RELEASE_API).openConnection();
    conn.setConnectTimeout(12000);conn.setReadTimeout(15000);
    conn.setRequestProperty("Accept","application/vnd.github+json");conn.setRequestProperty("User-Agent","BirthdayAPK");
    int code=conn.getResponseCode();
    if(code==404){runOnUiThread(()->{if(!isFinishing()&&!isDestroyed())msg("На GitHub поки немає опублікованих релізів");});return;}
    if(code!=200)throw new IOException("GitHub: HTTP "+code);
    ByteArrayOutputStream out=new ByteArrayOutputStream();
    try(InputStream in=conn.getInputStream()){byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1){if(out.size()+n>1048576)throw new IOException("Завелика відповідь GitHub");out.write(buf,0,n);}}
    org.json.JSONObject release=new org.json.JSONObject(out.toString("UTF-8"));
    String tag=release.getString("tag_name");
    java.util.regex.Matcher match=java.util.regex.Pattern.compile("v1\\.([0-9]+)").matcher(tag);
    if(!match.matches())throw new IOException("Невідомий формат версії: "+tag);
    long remote=Long.parseLong(match.group(1));
    if(remote<=installedCode()){runOnUiThread(()->{if(!isFinishing()&&!isDestroyed())msg("У тебе встановлена актуальна версія");});return;}
    org.json.JSONArray assets=release.getJSONArray("assets");String apkUrl=null;
    for(int i=0;i<assets.length();i++){org.json.JSONObject a=assets.getJSONObject(i);if("BirthdayAPK.apk".equals(a.optString("name"))){String url=a.optString("browser_download_url");if(url.equals(RELEASE_DOWNLOAD+tag+"/BirthdayAPK.apk"))apkUrl=url;break;}}
    if(apkUrl==null)throw new IOException("У релізі відсутній файл BirthdayAPK.apk");
    final String download=apkUrl;
    runOnUiThread(()->{if(isFinishing()||isDestroyed())return;new AlertDialog.Builder(this).setTitle("Доступне оновлення "+tag.substring(1)).setMessage("Встановлено: "+installedVersion()+"\nНова версія: "+tag.substring(1)+"\n\nЗавантажити оновлення у додатку?").setPositiveButton("Оновити",(d,w)->downloadUpdate(download,remote)).setNegativeButton("Пізніше",null).show();});
   }catch(Exception e){runOnUiThread(()->{if(!isFinishing()&&!isDestroyed())msg("Не вдалося перевірити оновлення: "+e.getMessage());});}
   finally{if(conn!=null)conn.disconnect();runOnUiThread(()->checkingUpdate=false);}
  },"birthday-update").start();
 }
 boolean downloading=false;
 @SuppressWarnings("deprecation")
 void verifyApk(File file,long expected)throws Exception{
  android.content.pm.PackageInfo candidate=getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(),PackageManager.GET_SIGNATURES);
  android.content.pm.PackageInfo existing=getPackageManager().getPackageInfo(getPackageName(),PackageManager.GET_SIGNATURES);
  if(candidate==null||!getPackageName().equals(candidate.packageName))throw new IOException("Некоректний APK");
  long code=Build.VERSION.SDK_INT>=28?candidate.getLongVersionCode():candidate.versionCode;
  if(code<=installedCode()||(expected>0&&code!=expected))throw new IOException("Версія APK не відповідає оновленню");
  if(candidate.signatures==null||existing.signatures==null||!new HashSet<>(Arrays.asList(candidate.signatures)).equals(new HashSet<>(Arrays.asList(existing.signatures))))throw new IOException("Підпис APK не збігається з установленим додатком");
 }
 void installUpdate(){
  File apk=ExcelShareProvider.apkFile(this);
  try{verifyApk(apk,0);
   if(!getPackageManager().canRequestPackageInstalls()){
    new AlertDialog.Builder(this).setTitle("Дозвіл на оновлення").setMessage("Дозволь встановлення додатків із цього джерела, щоб оновлювати Іменинники АПК.").setPositiveButton("Надати дозвіл",(d,w)->{try{startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+getPackageName())),INSTALL_PERMISSION);}catch(Exception e){msg("Не вдалося відкрити дозвіл");}}).setNegativeButton("Пізніше",null).show();return;
   }
   Uri u=ExcelShareProvider.apkUri(this);
   Intent i=new Intent(Intent.ACTION_VIEW).setDataAndType(u,ExcelShareProvider.APK_MIME).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
   i.setClipData(ClipData.newRawUri("BirthdayAPK.apk",u));startActivity(i);
  }catch(Exception e){msg("Не вдалося встановити: "+e.getMessage());}
 }
 void downloadUpdate(String url,long expected){
  if(downloading){msg("Завантаження вже триває");return;}downloading=true;
  java.util.concurrent.atomic.AtomicBoolean cancel=new java.util.concurrent.atomic.AtomicBoolean(false);
  ProgressDialog progress=new ProgressDialog(this);progress.setTitle("Завантаження оновлення");progress.setMessage("Завантажуємо APK…");progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);progress.setMax(100);progress.setIndeterminate(true);progress.setCancelable(false);progress.setButton(DialogInterface.BUTTON_NEGATIVE,"Скасувати",(d,w)->cancel.set(true));progress.show();
  File apk=ExcelShareProvider.apkFile(this);File partial=new File(apk.getParentFile(),"BirthdayAPK-"+UUID.randomUUID()+".part.apk");
  new Thread(()->{java.net.HttpURLConnection connection=null;try{
   if(!apk.getParentFile().isDirectory()&&!apk.getParentFile().mkdirs())throw new IOException("Немає папки для оновлення");
   connection=(java.net.HttpURLConnection)new java.net.URL(url).openConnection();connection.setConnectTimeout(15000);connection.setReadTimeout(20000);connection.setRequestProperty("User-Agent","BirthdayAPK");
   if(connection.getResponseCode()!=200)throw new IOException("GitHub: HTTP "+connection.getResponseCode());
   if(!"https".equals(connection.getURL().getProtocol()))throw new IOException("Небезпечне перенаправлення");
   long size=connection.getContentLengthLong(),total=0;final long limit=100L*1024*1024;if(size>limit)throw new IOException("Завеликий APK");
   runOnUiThread(()->{if(!isFinishing()&&!isDestroyed()&&progress.isShowing())progress.setIndeterminate(size<=0);});
   try(InputStream in=connection.getInputStream();FileOutputStream out=new FileOutputStream(partial)){byte[] buffer=new byte[32768];int n;int previous=-1;while((n=in.read(buffer))!=-1){if(cancel.get())throw new InterruptedIOException();total+=n;if(total>limit)throw new IOException("Завеликий APK");out.write(buffer,0,n);if(size>0){int percent=(int)Math.min(100,total*100/size);if(percent!=previous){previous=percent;runOnUiThread(()->{if(progress.isShowing())progress.setProgress(percent);});}}}out.getFD().sync();}
   if(cancel.get())throw new InterruptedIOException();if(size>0&&size!=total)throw new IOException("Неповне завантаження");
   verifyApk(partial,expected);if(apk.exists()&&!apk.delete())throw new IOException("Не вдалося замінити старий APK");if(!partial.renameTo(apk))throw new IOException("Не вдалося зберегти APK");
   runOnUiThread(()->{if(!isFinishing()&&!isDestroyed()){progress.dismiss();show();installUpdate();}});
  }catch(Exception e){runOnUiThread(()->{if(!isFinishing()&&!isDestroyed()){progress.dismiss();msg(cancel.get()?"Завантаження скасовано":"Не вдалося завантажити: "+e.getMessage());}});}finally{if(connection!=null)connection.disconnect();partial.delete();runOnUiThread(()->downloading=false);}},"birthday-apk-download").start();
 }

}
