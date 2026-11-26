package ua.malinapk.birthdays;
import android.Manifest;import android.app.*;import android.content.*;import android.content.pm.PackageManager;import android.net.Uri;import android.os.*;import android.provider.Settings;import android.graphics.Color;import android.graphics.Typeface;import android.graphics.drawable.GradientDrawable;import android.view.*;import android.widget.*;import java.io.*;import java.time.*;import java.time.format.*;import java.util.*;
public class MainActivity extends Activity {
 int GREEN,INK,MUTED,BG,GOLD,WHITE,HEADER,ACCENT,JUBILEE,TODAY,NAV,LINE,ERROR,CARD;
 boolean dark; String activeTheme="light"; ScrollView calendarScroll; View calendarSwipeArea; float downX,downY; boolean swipeCandidate; int slideDirection=0;
 String themeMode(){return prefs().getString("appearanceTheme","auto");}
 String resolveTheme(){String choice=themeMode();if("gray".equals(choice)||"dark".equals(choice)||"light".equals(choice))return choice;return (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK)==android.content.res.Configuration.UI_MODE_NIGHT_YES?"dark":"light";}
 String themeName(String key){return "gray".equals(key)?"Сіра":"dark".equals(key)?"Темна":"Світла";}
 void palette(){activeTheme=resolveTheme();dark=!"light".equals(activeTheme);boolean gray="gray".equals(activeTheme);
  GREEN=dark?0xffa0dab5:0xff235d45; INK=dark?Color.WHITE:Color.BLACK; MUTED=dark?0xffb0c2b5:0xff64756a; BG=gray?0xff686868:dark?0xff171717:0xfff2f2f2; WHITE=gray?0xff505050:dark?0xff282828:Color.WHITE; CARD=gray?0xff585858:dark?0xff303030:0xffe5e5e5; GOLD=dark?0xffffd78a:0xff80500a; HEADER=appearanceColor(activeTheme,0); ACCENT=HEADER; NAV=HEADER; JUBILEE=appearanceColor(activeTheme,1); TODAY=appearanceColor(activeTheme,2); LINE=gray?0xff777777:dark?0xff405047:0xffdce5dd; ERROR=dark?0xffffa29a:0xffb13d37;
 }
 void bars(){getWindow().setStatusBarColor(HEADER);getWindow().setNavigationBarColor(BG);int flags=dark?0:View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;if(onColor(HEADER)==Color.BLACK)flags|=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;getWindow().getDecorView().setSystemUiVisibility(flags);}
 static final String[] COLOR_NAMES={"Лісовий","Смарагдовий","Синій","Бірюзовий","Фіолетовий","Золотистий","Помаранчевий","М’ятний","Рожевий","Бордовий","Графітовий","Блакитний","Оливковий","Кавовий","Темно-зелений","Темно-бурштиновий","Темно-смарагдовий","Темно-синій","Темно-фіолетовий","Темно-графітовий"};
 static final int[] COLOR_VALUES={0xff326747,0xff00866b,0xff316cb3,0xff16858b,0xff8056ac,0xffffd078,0xfff5a04b,0xffbfe5c8,0xffe9aec2,0xffa04459,0xff59636e,0xffa9d5f5,0xff747d36,0xff916744,0xff344e3e,0xff79531c,0xff28583d,0xff244773,0xff50366c,0xff383f48};
 static final String[] COLOR_ROLES={"Основний колір","Ювіляри","Сьогоднішні іменинники"};
 String colorKey(String theme,int role){return "appearanceColor_"+theme+"_"+role;}
 int defaultColor(String theme,int role){return COLOR_VALUES[("gray".equals(theme)?new int[]{0,5,7}:"dark".equals(theme)?new int[]{14,15,16}:new int[]{0,5,7})[role]];}
 int appearanceColor(String theme,int role){return prefs().getInt(colorKey(theme,role),defaultColor(theme,role))|0xff000000;}
 static double linearChannel(int channel){double x=channel/255.0;return x<=0.04045?x/12.92:Math.pow((x+0.055)/1.055,2.4);}
 static int onColor(int fill){fill|=0xff000000;if(fill==0xff00866b||fill==0xff16858b||fill==0xff747d36)return Color.WHITE;double l=.2126*linearChannel(Color.red(fill))+.7152*linearChannel(Color.green(fill))+.0722*linearChannel(Color.blue(fill));return (l+.05)/.05>=1.05/(l+.05)?Color.BLACK:Color.WHITE;}
 int rippleColor(int fill){return onColor(fill)==Color.WHITE?0x33ffffff:0x22000000;}
 int shadowLevel(){return Math.max(0,Math.min(2,prefs().getInt("appearanceShadow",1)));}
 String shadowLabel(){return new String[]{"Без тіней","М’які","Виражені"}[shadowLevel()];}
 int volumeLevel(){return Math.max(0,Math.min(2,prefs().getInt("appearanceVolume",1)));}
 String volumeLabel(){return new String[]{"Плоский","М’який об’єм","Виражений об’єм"}[volumeLevel()];}
 void shadow(View v,boolean small){int level=shadowLevel();v.setStateListAnimator(null);v.setElevation(dp(level==0?0:level==1?(small?2:3):(small?4:6)));v.setTranslationZ(0);v.setOutlineProvider(ViewOutlineProvider.BACKGROUND);}
 void shadowSettings(){String[] labels={"Без тіней","М’які (стандартні)","Виражені"};new AlertDialog.Builder(this).setTitle("Тіні елементів").setSingleChoiceItems(labels,shadowLevel(),(d,w)->{prefs().edit().putInt("appearanceShadow",w).apply();d.dismiss();show();}).setNegativeButton("Скасувати",null).show();}
 void volumeSettings(){String[] labels={"Плоский","М’який об’єм","Виражений об’єм"};new AlertDialog.Builder(this).setTitle("Об’єм елементів").setSingleChoiceItems(labels,volumeLevel(),(d,w)->{prefs().edit().putInt("appearanceVolume",w).apply();d.dismiss();show();}).setNegativeButton("Скасувати",null).show();}
 void colorSettings(String theme,int role){ScrollView scroll=new ScrollView(this);LinearLayout l=col();pad(l,12);scroll.addView(l);AlertDialog dialog=new AlertDialog.Builder(this).setTitle(COLOR_ROLES[role]+" — "+themeName(theme)+" тема").setView(scroll).setNegativeButton("Скасувати",null).setNeutralButton("За замовчуванням",(d,w)->{prefs().edit().remove(colorKey(theme,role)).apply();palette();bars();show();}).create();int current=appearanceColor(theme,role);for(int start=0;start<COLOR_VALUES.length;start+=2){LinearLayout r=row();for(int i=start;i<Math.min(start+2,COLOR_VALUES.length);i++){final int index=i;int fill=COLOR_VALUES[i];Button swatch=button((current==fill?"✓ ":"")+COLOR_NAMES[i],()->{prefs().edit().putInt(colorKey(theme,role),COLOR_VALUES[index]).apply();dialog.dismiss();palette();bars();show();});swatch.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(rippleColor(fill)),bg(fill,12),null));swatch.setTextColor(onColor(fill));swatch.setMinimumHeight(dp(56));swatch.setMinHeight(dp(56));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);lp.setMargins(dp(4),dp(4),dp(4),dp(4));r.addView(swatch,lp);}add(l,r);}dialog.show();}
 void colorOptions(LinearLayout parent,String theme){add(parent,text("Активна тема: "+themeName(theme),16,INK,true));gap(parent,6);for(int role=0;role<3;role++){final int selectedRole=role;int fill=appearanceColor(theme,role);Button option=button(COLOR_ROLES[role],()->colorSettings(theme,selectedRole));option.setTextColor(onColor(fill));option.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(rippleColor(fill)),bg(fill,12),null));action(parent,option);}}

 void themeSettings(){String[] labels={"Як у телефоні (автоматично)","Світла","Сіра","Темна"};String[] keys={"auto","light","gray","dark"};int current=Arrays.asList(keys).indexOf(themeMode());new AlertDialog.Builder(this).setTitle("Тема оформлення").setSingleChoiceItems(labels,Math.max(0,current),(d,w)->{prefs().edit().putString("appearanceTheme",keys[w]).apply();d.dismiss();recreate();}).setNegativeButton("Скасувати",null).show();}
 String themeLabel(){return "auto".equals(themeMode())?"Як у телефоні":themeName(activeTheme);}

 static final int PICK_EXCEL=101, INSTALL_PERMISSION=103;
 static final DateTimeFormatter DATE=DateTimeFormatter.ofPattern("dd.MM.uuuu");
 final String[] months={"Січень","Лютий","Березень","Квітень","Травень","Червень","Липень","Серпень","Вересень","Жовтень","Листопад","Грудень"};
 List<Person> people=new ArrayList<>();YearMonth selected=YearMonth.now(),observedMonth=YearMonth.now();LinearLayout root;boolean ready=false;String mode="calendar";String status="";String employeeUnitFilter="",employeeJobFilter="",employeeSearch="";boolean employeeJubileesOnly=false;
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
 @Override public void onCreate(Bundle b){palette();setTheme("gray".equals(activeTheme)?R.style.BirthdayGray:dark?R.style.BirthdayDark:R.style.BirthdayLight);super.onCreate(b);if(b!=null){mode=b.getString("screenMode","calendar");try{selected=YearMonth.parse(b.getString("selectedMonth",YearMonth.now().toString()));}catch(Exception ignored){}}bars();people=ReminderReceiver.cached(this);ReminderReceiver.channel(this);if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},1);reload(false);show();}
 @Override protected void onResume(){super.onResume();if(!activeTheme.equals(resolveTheme())){recreate();return;}YearMonth current=YearMonth.now();if(selected.equals(observedMonth))selected=current;observedMonth=current;if(root!=null){reload(false);show();}}
 void msg(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 byte[] read()throws Exception{return LocalWorkbook.read(this);}
 void reload(boolean announce){try{Workbook w=new Workbook(read());people=w.people;rememberDirectories();ReminderReceiver.cache(this,people);ReminderReceiver.schedule(this);ready=true;status="Працівників: "+people.size();if(announce)msg("Список оновлено");}catch(Exception e){ready=false;status=e instanceof FileNotFoundException?"Обери Excel-файл у налаштуваннях":"Excel недоступний: "+e.getMessage()+". Показано останні збережені дані.";if(announce)msg(status);}}
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
  Workbook workbook=new Workbook(read());
  writeExport(ExcelShareProvider.exportFile(this),workbook.formattedExport());
  Uri share=ExcelShareProvider.exportUri(this);
  Intent send=new Intent(Intent.ACTION_SEND).setType(ExcelShareProvider.MIME).putExtra(Intent.EXTRA_STREAM,share).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
  send.setClipData(ClipData.newRawUri("Іменинники.xlsx",share));
  startActivity(Intent.createChooser(send,"Поділитися Excel-файлом"));
 }catch(Exception e){msg("Не вдалося поділитися Excel: "+e.getMessage());}}
 void writeExport(File report,byte[] bytes)throws IOException{
  android.util.AtomicFile atomic=new android.util.AtomicFile(report);FileOutputStream out=null;
  try{out=atomic.startWrite();out.write(bytes);atomic.finishWrite(out);}catch(IOException|RuntimeException e){if(out!=null)atomic.failWrite(out);throw e;}
 }
 void shareReport(File report,Uri share,byte[] bytes,String title)throws IOException{
  writeExport(report,bytes);
  Intent send=new Intent(Intent.ACTION_SEND).setType(ExcelShareProvider.MIME).putExtra(Intent.EXTRA_STREAM,share).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
  send.setClipData(ClipData.newRawUri(report.getName(),share));startActivity(Intent.createChooser(send,title));
 }
 void shareJubilees(int year){try{
  Workbook w=new Workbook(read());shareReport(ExcelShareProvider.reportFile(this,year),ExcelShareProvider.reportUri(this,year),JubileeReport.create(w.people,year),"Вивантажити ювілярів за "+year+" рік");
 }catch(Exception e){msg("Не вдалося вивантажити ювілярів: "+e.getMessage());}}
 void shareBirthdays(YearMonth month){try{
  Workbook w=new Workbook(read());shareReport(ExcelShareProvider.monthFile(this,month),ExcelShareProvider.monthUri(this,month),JubileeReport.createMonth(w.people,month),"Вивантажити іменинників — "+months[month.getMonthValue()-1]+" "+month.getYear());
 }catch(Exception e){msg("Не вдалося вивантажити іменинників: "+e.getMessage());}}
 int mix(int a,int b,float t){int ar=Color.red(a),ag=Color.green(a),ab=Color.blue(a),aa=Color.alpha(a);int br=Color.red(b),bg=Color.green(b),bb=Color.blue(b),ba=Color.alpha(b);return Color.argb(Math.round(aa+(ba-aa)*t),Math.round(ar+(br-ar)*t),Math.round(ag+(bg-ag)*t),Math.round(ab+(bb-ab)*t));}
 int lighten(int c,float f){return mix(c,Color.WHITE,Math.max(0f,Math.min(1f,f)));}
 int darken(int c,float f){return mix(c,Color.BLACK,Math.max(0f,Math.min(1f,f)));}
 GradientDrawable bg(int c,int rad){GradientDrawable d=new GradientDrawable();d.setCornerRadius(dp(rad));int level=volumeLevel();if(level==0){d.setColor(c);}else if(level==1){d.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);d.setColors(new int[]{lighten(c,0.08f),c,darken(c,0.06f)});d.setStroke(dp(1),darken(c,0.16f));}else{d.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);d.setColors(new int[]{lighten(c,0.14f),lighten(c,0.04f),darken(c,0.10f)});d.setStroke(dp(1),darken(c,0.22f));}return d;}
 LinearLayout col(){LinearLayout v=new LinearLayout(this);v.setClipChildren(false);v.setClipToPadding(false);v.setOrientation(LinearLayout.VERTICAL);return v;}
 LinearLayout row(){LinearLayout v=new LinearLayout(this);v.setClipChildren(false);v.setClipToPadding(false);v.setOrientation(LinearLayout.HORIZONTAL);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
 TextView text(String s,int size,int color,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(INK);if(bold)v.setTypeface(null,Typeface.BOLD);return v;}
 void pad(View v,int a){v.setPadding(dp(a),dp(a),dp(a),dp(a));}
 void add(LinearLayout l,View v){l.addView(v,new LinearLayout.LayoutParams(-1,-2));}
 void weight(LinearLayout l,View v){l.addView(v,new LinearLayout.LayoutParams(0,-2,1));}
 Button button(String s,Runnable f){Button b=new Button(this);b.setAllCaps(false);b.setText(s);b.setTextColor(onColor(NAV));b.setTextSize(14);b.setMinHeight(dp(44));b.setMinimumHeight(dp(44));b.setMinWidth(0);b.setMinimumWidth(0);b.setPadding(dp(10),dp(8),dp(10),dp(8));b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(rippleColor(NAV)),bg(NAV,12),null));b.setBackgroundTintList(null);shadow(b,true);b.setOnClickListener(v->f.run());return b;}
 Button primary(String s,Runnable f){Button b=button(s,f);b.setTextColor(onColor(ACCENT));b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(rippleColor(ACCENT)),bg(ACCENT,12),null));return b;}
 void action(LinearLayout parent,Button b){if(!b.isEnabled())b.setAlpha(.45f);add(parent,b);gap(parent,8);}
 void section(LinearLayout parent,String label){add(parent,text(label,21,INK,true));gap(parent,8);}
 void statsTile(LinearLayout r,String label,int value,int color,int fill){LinearLayout tile=col();tile.setBackground(bg(fill,15));shadow(tile,false);pad(tile,11);TextView number=text(String.valueOf(value),24,color,true),caption=text(label,12,color,false);number.setTextColor(onColor(fill));caption.setTextColor(onColor(fill));add(tile,number);add(tile,caption);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);lp.setMargins(dp(5),dp(2),dp(5),dp(4));r.addView(tile,lp);}

 void gap(LinearLayout l,int n){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,dp(n)));}
 LinearLayout card(LinearLayout parent){LinearLayout c=col();c.setBackground(bg(CARD,17));shadow(c,false);pad(c,12);add(parent,c);gap(parent,12);return c;}
 void show(){calendarScroll=null;calendarSwipeArea=null;root=col();root.setClipChildren(true);root.setClipToPadding(true);root.setBackgroundColor(BG);if(Build.VERSION.SDK_INT>=35){root.setOnApplyWindowInsetsListener((v,insets)->{android.graphics.Insets sys=insets.getInsets(WindowInsets.Type.systemBars());v.setPadding(sys.left,sys.top,sys.right,sys.bottom);return insets;});}setContentView(root);LinearLayout header=col();header.setBackgroundColor(HEADER);pad(header,14);LinearLayout brand=row();ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.ic_app);brand.addView(logo,new LinearLayout.LayoutParams(dp(46),dp(46)));LinearLayout brandText=col();brandText.setPadding(dp(10),0,0,0);TextView brandTitle=text("Іменинники АПК",23,INK,true),brandSubtitle=text("ДП «Малинський лісгосп АПК»",12,INK,false);brandTitle.setTextColor(onColor(HEADER));brandSubtitle.setTextColor(onColor(HEADER));add(brandText,brandTitle);add(brandText,brandSubtitle);weight(brand,brandText);add(header,brand);add(root,header);
 boolean fixedList=mode.equals("calendar")||mode.equals("people");LinearLayout body=col();pad(body,12);if(mode.equals("calendar"))calendarSwipeArea=body;
 FrameLayout viewport=new FrameLayout(this);viewport.setClipChildren(true);viewport.setClipToPadding(true);
 if(fixedList){viewport.addView(body,new FrameLayout.LayoutParams(-1,-1));}else{ScrollView scroll=boundedScroll();scroll.setFillViewport(true);scroll.addView(body,new ScrollView.LayoutParams(-1,-2));viewport.addView(scroll,new FrameLayout.LayoutParams(-1,-1));}
 root.addView(viewport,new LinearLayout.LayoutParams(-1,0,1));TextView st=text(status,12,ready?MUTED:ERROR,false);add(body,st);gap(body,8);if(!ready&&!mode.equals("settings"))action(body,button("Обрати Excel",this::pickExcel));
 if(mode.equals("calendar"))calendar(body);else if(mode.equals("people"))allPeople(body);else if(mode.equals("settings"))settings(body);else reminders(body);
 LinearLayout nav=row();nav.setBackgroundColor(WHITE);String[] labels={"Календар","Працівники","Нагадування","Налаштування"};String[] modes={"calendar","people","reminders","settings"};int[] icons={R.drawable.nav_calendar,R.drawable.nav_people,R.drawable.nav_bell,R.drawable.nav_settings};
 for(int k=0;k<4;k++){final String m=modes[k];boolean active=mode.equals(m);int color=active?GREEN:MUTED;
  LinearLayout item=col();item.setGravity(Gravity.CENTER);item.setPadding(dp(2),dp(8),dp(2),dp(8));item.setMinimumHeight(dp(60));
  LinearLayout pill=row();pill.setGravity(Gravity.CENTER);pill.setBackground(bg(active?NAV:WHITE,20));ImageView icon=new ImageView(this);icon.setImageResource(icons[k]);icon.setColorFilter(active?onColor(NAV):INK);pill.addView(icon,new LinearLayout.LayoutParams(dp(24),dp(24)));item.addView(pill,new LinearLayout.LayoutParams(dp(55),dp(32)));
  TextView t=text(labels[k],10,color,active);t.setGravity(Gravity.CENTER);t.setMaxLines(2);t.setBreakStrategy(android.text.Layout.BREAK_STRATEGY_SIMPLE);add(item,t);
  item.setContentDescription(labels[k]);item.setSelected(active);item.setOnClickListener(v->{mode=m;show();});nav.addView(item,new LinearLayout.LayoutParams(0,-1,1));
 }add(root,nav);if(slideDirection!=0){final int direction=slideDirection;slideDirection=0;if(calendarScroll!=null&&calendarScroll.getChildCount()>0){View rows=calendarScroll.getChildAt(0);rows.setTranslationX(direction*dp(64));rows.setAlpha(.55f);rows.animate().translationX(0).alpha(1).setDuration(190).start();}}}
 ScrollView boundedScroll(){ScrollView scroll=new ScrollView(this);scroll.setClipChildren(true);scroll.setClipToPadding(true);scroll.setVerticalScrollBarEnabled(true);return scroll;}
 // A weighted and clipped viewport: only the person cards scroll, not the controls.
 LinearLayout scrollingPeople(LinearLayout parent,boolean calendar){FrameLayout viewport=new FrameLayout(this);viewport.setClipChildren(true);viewport.setClipToPadding(true);ScrollView scroll=boundedScroll();LinearLayout list=col();list.setPadding(0,dp(5),0,dp(8));scroll.addView(list,new ScrollView.LayoutParams(-1,-2));viewport.addView(scroll,new FrameLayout.LayoutParams(-1,-1));parent.addView(viewport,new LinearLayout.LayoutParams(-1,0,1));if(calendar)calendarScroll=scroll;return list;}
 Button monthArrow(int step){Button b=button(step<0?"‹":"›",()->changeMonth(step));b.setTextSize(34);b.setTypeface(null,Typeface.BOLD);b.setIncludeFontPadding(false);b.setGravity(Gravity.CENTER);b.setPadding(0,0,0,0);b.setMinHeight(dp(48));b.setMinimumHeight(dp(48));b.setContentDescription(step<0?"Попередній місяць":"Наступний місяць");return b;}
 void changeMonth(int step){try{YearMonth next=selected.plusMonths(step);if(next.getYear()<1900||next.getYear()>9999)return;selected=next;slideDirection=step;show();}catch(Exception ignored){}}
 @Override public boolean dispatchTouchEvent(android.view.MotionEvent e){int a=e.getActionMasked();if(a==MotionEvent.ACTION_DOWN){swipeCandidate=false;if(calendarSwipeArea!=null&&mode.equals("calendar")){int[] loc=new int[2];calendarSwipeArea.getLocationOnScreen(loc);float x=e.getRawX(),y=e.getRawY();swipeCandidate=x>=loc[0]&&x<=loc[0]+calendarSwipeArea.getWidth()&&y>=loc[1]&&y<=loc[1]+calendarSwipeArea.getHeight();downX=x;downY=y;}}else if(a==MotionEvent.ACTION_POINTER_DOWN||a==MotionEvent.ACTION_CANCEL){swipeCandidate=false;}else if(swipeCandidate&&a==MotionEvent.ACTION_MOVE){float dx=e.getRawX()-downX,dy=e.getRawY()-downY;if(Math.abs(dy)>dp(22)&&Math.abs(dy)>Math.abs(dx))swipeCandidate=false;}else if(a==MotionEvent.ACTION_UP&&swipeCandidate){swipeCandidate=false;float dx=e.getRawX()-downX,dy=e.getRawY()-downY;if(Math.abs(dx)>dp(65)&&Math.abs(dx)>Math.abs(dy)*1.6f){MotionEvent cancel=MotionEvent.obtain(e);cancel.setAction(MotionEvent.ACTION_CANCEL);super.dispatchTouchEvent(cancel);cancel.recycle();changeMonth(dx<0?1:-1);return true;}}return super.dispatchTouchEvent(e);}

 List<Person> sorted(List<Person> src,int y){List<Person> a=new ArrayList<>(src);a.sort(Comparator.comparing((Person p)->p.birthday(y)).thenComparing(Person::name,String.CASE_INSENSITIVE_ORDER));return a;}
 void calendar(LinearLayout b){LinearLayout r=row();r.addView(monthArrow(-1),new LinearLayout.LayoutParams(dp(48),-2));TextView title=text(months[selected.getMonthValue()-1]+" "+selected.getYear(),20,INK,true);title.setGravity(Gravity.CENTER);title.setOnClickListener(v->chooseMonth());weight(r,title);r.addView(monthArrow(1),new LinearLayout.LayoutParams(dp(48),-2));add(b,r);gap(b,12);if(!selected.equals(YearMonth.now()))action(b,button("Поточний місяць",()->{selected=YearMonth.now();show();}));int y=selected.getYear();List<Person> list=new ArrayList<>();int j=0,today=0;for(Person p:sorted(people,y))if(p.birthday(y).getMonthValue()==selected.getMonthValue()){list.add(p);if(p.jubilee(y))j++;if(p.birthday(y).equals(LocalDate.now()))today++;}LinearLayout stats=row();statsTile(stats,"Іменинники",list.size(),GREEN,NAV);statsTile(stats,"Ювіляри",j,INK,JUBILEE);statsTile(stats,"Сьогодні",today,INK,TODAY);add(b,stats);gap(b,14);section(b,"Іменинники місяця");LinearLayout titles=row();TextView d=text("Дата",12,INK,true);titles.addView(d,new LinearLayout.LayoutParams(fontDp(55),-2));weight(titles,text("ПІБ",12,INK,true));titles.addView(text("Вік",12,INK,true),new LinearLayout.LayoutParams(fontDp(48),-2));pad(titles,8);add(b,titles);LinearLayout rows=scrollingPeople(b,true);if(list.isEmpty())add(rows,text("У цьому місяці немає іменинників",15,MUTED,false));for(Person p:list)personRow(rows,p,y,true);gap(b,6);add(b,primary("＋ Додати працівника",()->edit(null)));}
 void chooseMonth(){LinearLayout l=col();pad(l,16);Spinner m=new Spinner(this);m.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,months));m.setSelection(selected.getMonthValue()-1);EditText y=new EditText(this);y.setInputType(2);y.setText(""+selected.getYear());add(l,m);add(l,y);new AlertDialog.Builder(this).setTitle("Місяць і рік").setView(l).setPositiveButton("Вибрати",(d,w)->{try{int year=Integer.parseInt(y.getText().toString());if(year<1900||year>9999)throw new IllegalArgumentException();selected=YearMonth.of(year,m.getSelectedItemPosition()+1);show();}catch(Exception e){msg("Некоректний рік");}}).setNegativeButton("Скасувати",null).show();}

 void rowText(View v,int color){if(v instanceof TextView)((TextView)v).setTextColor(color);else if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)rowText(g.getChildAt(i),color);}} void personRow(LinearLayout b,Person p,int y,boolean date){boolean jub=date&&p.jubilee(y),now=date&&p.birthday(y).equals(LocalDate.now());LinearLayout r=row();int fill=now?TODAY:(jub?JUBILEE:WHITE);r.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(rippleColor(fill)),bg(fill,13),null));shadow(r,true);pad(r,9);if(date)r.addView(text(p.birthday(y).format(DateTimeFormatter.ofPattern("dd.MM")),13,INK,true),new LinearLayout.LayoutParams(fontDp(55),-2));LinearLayout n=col();add(n,text(p.name(),15,INK,jub));String role=p.job.isEmpty()?p.unit:p.unit.isEmpty()?p.job:p.job+" · "+p.unit;if(!role.isEmpty())add(n,text(role,12,MUTED,false));if(now)add(n,text("Сьогодні",12,INK,false));weight(r,n);if(!date){TextView birth=text(p.born.format(DateTimeFormatter.ofPattern("dd.MM")),13,INK,true);birth.setGravity(Gravity.CENTER);r.addView(birth,new LinearLayout.LayoutParams(fontDp(55),-2));}TextView age=text((jub?"★ ":"")+(date?p.ageIn(y):p.ageNow(LocalDate.now())),15,INK,true);age.setGravity(Gravity.END);r.addView(age,new LinearLayout.LayoutParams(fontDp(48),-2));rowText(r,onColor(fill));r.setOnClickListener(v->details(p,y));add(b,r);gap(b,shadowLevel()==2?10:7);}
 Spinner employeeFilter(boolean job,String selectedValue){
  List<String> values=directory(job);values.add(0,job?"Усі посади":"Усі підрозділи");
  Spinner spinner=new Spinner(this,Spinner.MODE_DIALOG);
  ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,values);
  adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
  spinner.setAdapter(adapter);spinner.setPrompt(job?"Оберіть посаду":"Оберіть підрозділ");
  spinner.setMinimumHeight(dp(44));
  for(int i=1;i<values.size();i++)if(nameKey(values.get(i)).equals(nameKey(selectedValue))){spinner.setSelection(i);break;}
  return spinner;
 }
 String employeeFilterValue(Spinner spinner){return spinner.getSelectedItemPosition()>0&&spinner.getSelectedItem()!=null?spinner.getSelectedItem().toString():"";}
 boolean employeeMatches(Person p,String unit,String job){return (unit.isEmpty()||nameKey(p.unit).equals(nameKey(unit)))&&(job.isEmpty()||nameKey(p.job).equals(nameKey(job)));}
 void employeeFilters(){
  ScrollView scroll=boundedScroll();LinearLayout panel=col();pad(panel,16);scroll.addView(panel,new ScrollView.LayoutParams(-1,-2));
  add(panel,text("Пошук",14,INK,true));EditText search=new EditText(this);search.setSingleLine(true);search.setHint("ПІБ, посада, підрозділ");search.setText(employeeSearch);search.setTextColor(INK);search.setHintTextColor(INK);search.setBackground(bg(WHITE,13));pad(search,12);add(panel,search);gap(panel,10);
  add(panel,text("Підрозділ",14,INK,true));Spinner unit=employeeFilter(false,employeeUnitFilter);add(panel,unit);gap(panel,8);
  add(panel,text("Посада",14,INK,true));Spinner job=employeeFilter(true,employeeJobFilter);add(panel,job);gap(panel,8);
  CheckBox jubilees=new CheckBox(this);jubilees.setText("Тільки ювіляри року ("+LocalDate.now().getYear()+")");jubilees.setTextColor(INK);jubilees.setChecked(employeeJubileesOnly);add(panel,jubilees);gap(panel,8);
  add(panel,text("Сортування",14,INK,true));RadioGroup order=new RadioGroup(this);order.setOrientation(RadioGroup.VERTICAL);
  RadioButton alpha=new RadioButton(this);alpha.setId(View.generateViewId());alpha.setText("За алфавітом");alpha.setTextColor(INK);
  RadioButton byDate=new RadioButton(this);byDate.setId(View.generateViewId());byDate.setText("За датою");byDate.setTextColor(INK);
  order.addView(alpha);order.addView(byDate);order.check(prefs().getBoolean("employeesSortByDate",false)?byDate.getId():alpha.getId());add(panel,order);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Фільтри працівників").setView(scroll).setPositiveButton("Застосувати",(d,w)->{employeeSearch=search.getText().toString().trim();employeeUnitFilter=employeeFilterValue(unit);employeeJobFilter=employeeFilterValue(job);employeeJubileesOnly=jubilees.isChecked();prefs().edit().putBoolean("employeesSortByDate",order.getCheckedRadioButtonId()==byDate.getId()).apply();show();}).setNegativeButton("Скасувати",null).setNeutralButton("Скинути",(d,w)->{employeeSearch="";employeeUnitFilter="";employeeJobFilter="";employeeJubileesOnly=false;show();}).create();
  panel.setFocusableInTouchMode(true);panel.requestFocus();dialog.show();if(dialog.getWindow()!=null)dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 }
 int employeeFilterCount(){return (employeeSearch.isEmpty()?0:1)+(employeeUnitFilter.isEmpty()?0:1)+(employeeJobFilter.isEmpty()?0:1)+(employeeJubileesOnly?1:0);}
 void allPeople(LinearLayout b){
 LocalDate today=LocalDate.now();final int year=today.getYear();
 int totalJubilees=0,remainingJubilees=0;for(Person p:people){if(p.jubilee(year)){totalJubilees++;if(!p.birthday(year).isBefore(today))remainingJubilees++;}}
 section(b,"Працівники");LinearLayout stats=row();statsTile(stats,"Ювілярів за "+year+" рік",totalJubilees,GREEN,NAV);statsTile(stats,"Ще до кінця року",remainingJubilees,INK,JUBILEE);add(b,stats);add(b,text("Включно із сьогоднішнім днем",12,MUTED,false));gap(b,8);
 // Clear a stale filter if the corresponding directory entry was removed or renamed.
 if(directory(false).stream().noneMatch(v->nameKey(v).equals(nameKey(employeeUnitFilter))))employeeUnitFilter="";
 if(directory(true).stream().noneMatch(v->nameKey(v).equals(nameKey(employeeJobFilter))))employeeJobFilter="";
 int count=employeeFilterCount();action(b,button("Фільтри та сортування"+(count>0?" ("+count+")":"")+"  ▾",this::employeeFilters));
 String q=employeeSearch.toLowerCase(Locale.ROOT);List<Person> matches=new ArrayList<>();for(Person p:people)if((p.name()+" "+p.job+" "+p.unit).toLowerCase(Locale.ROOT).contains(q)&&(!employeeJubileesOnly||p.jubilee(year))&&employeeMatches(p,employeeUnitFilter,employeeJobFilter))matches.add(p);
 java.text.Collator collator=java.text.Collator.getInstance(Locale.forLanguageTag("uk-UA"));collator.setStrength(java.text.Collator.SECONDARY);Comparator<Person> alphabet=Comparator.comparing(Person::name,collator);matches.sort(prefs().getBoolean("employeesSortByDate",false)?Comparator.comparing((Person p)->MonthDay.from(p.born)).thenComparing(alphabet):alphabet);
 add(b,text("Показано: "+matches.size()+" з "+people.size(),13,INK,false));gap(b,4);
 LinearLayout titles=row();weight(titles,text("ПІБ",12,INK,true));TextView birthTitle=text("Дата",12,INK,true);birthTitle.setGravity(Gravity.CENTER);titles.addView(birthTitle,new LinearLayout.LayoutParams(fontDp(55),-2));TextView ageTitle=text("Вік",12,INK,true);ageTitle.setGravity(Gravity.END);titles.addView(ageTitle,new LinearLayout.LayoutParams(fontDp(48),-2));pad(titles,9);add(b,titles);
 LinearLayout list=scrollingPeople(b,false);for(Person p:matches)personRow(list,p,year,false);if(matches.isEmpty())add(list,text("Працівників за обраними умовами не знайдено",14,INK,false));gap(b,6);add(b,primary("＋ Додати працівника",()->edit(null)));
 }
 void field(LinearLayout l,String label,String val){add(l,text(label,12,MUTED,false));add(l,text(val.isEmpty()?"—":val,17,INK,true));gap(l,8);}
 void details(Person p,int y){ScrollView s=new ScrollView(this);LinearLayout l=col();pad(l,18);s.addView(l);TextView name=text(p.name(),22,INK,true);name.setGravity(Gravity.CENTER);add(l,name);gap(l,10);LinearLayout info=card(l);field(info,"Прізвище",p.last);field(info,"Ім’я",p.first);field(info,"По батькові",p.middle);field(info,"Посада/підрозділ",(p.job.trim()+" "+p.unit.trim()).trim());field(info,"Повна дата народження",p.born.format(DATE));LinearLayout age=row();statsTile(age,"Вік на сьогодні",p.ageNow(LocalDate.now()),INK,NAV);statsTile(age,"Вік у "+y+" році",p.ageIn(y),INK,p.jubilee(y)?JUBILEE:NAV);add(l,age);if(p.jubilee(y))add(l,text("★ Ювіляр "+y+" року",16,GOLD,true));new AlertDialog.Builder(this).setTitle("Картка працівника").setView(s).setPositiveButton("Редагувати",(d,w)->edit(p)).setNeutralButton("Видалити",(d,w)->new AlertDialog.Builder(this).setMessage("Видалити "+p.name()+" з Excel?").setPositiveButton("Видалити",(a,z)->save(p,p,true)).setNegativeButton("Скасувати",null).show()).setNegativeButton("Закрити",null).show();}
 void edit(Person p){if(!ready){msg("Спочатку обери Excel-файл");return;}ScrollView s=new ScrollView(this);LinearLayout l=col();pad(l,16);s.addView(l);String[] names={"Прізвище","Ім’я","По батькові"};String[] vals=p==null?new String[3]:new String[]{p.last,p.first,p.middle};EditText[] fields=new EditText[3];for(int i=0;i<3;i++){add(l,text(names[i],13,MUTED,true));EditText e=new EditText(this);e.setSingleLine(true);e.setText(vals[i]==null?"":vals[i]);fields[i]=e;add(l,e);gap(l,6);}
  add(l,text("Посада",13,MUTED,true));Spinner jobs=directorySpinner(true,p==null?null:p.job);add(l,jobs);gap(l,10);add(l,text("Підрозділ",13,MUTED,true));Spinner units=directorySpinner(false,p==null?null:p.unit);add(l,units);gap(l,10);
  add(l,text("Дата народження",13,MUTED,true));final LocalDate[] chosen={p==null?null:p.born};Button date=button(chosen[0]==null?"Обрати дату народження":chosen[0].format(DATE),()->{});date.setOnClickListener(v->birthPicker(chosen[0],value->{chosen[0]=value;date.setText(value.format(DATE));}));add(l,date);gap(l,8);add(l,text("Посади та підрозділи редагуються у налаштуваннях.",12,MUTED,false));
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle(p==null?"Новий працівник":"Редагування").setView(s).setPositiveButton("Зберегти",null).setNegativeButton("Скасувати",null).create();dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(x->{try{String[] a=new String[3];for(int i=0;i<3;i++)a[i]=fields[i].getText().toString().trim();if(a[0].isEmpty()||a[1].isEmpty()||a[2].isEmpty())throw new IllegalArgumentException("Заповни ПІБ");if(jobs.getSelectedItemPosition()<=0||units.getSelectedItemPosition()<=0)throw new IllegalArgumentException("Обери посаду та підрозділ");LocalDate d=chosen[0];if(d==null||d.isAfter(LocalDate.now())||d.getYear()<1900)throw new IllegalArgumentException("Обери коректну дату народження");Person q=new Person(p==null?0:p.row,a[0],a[1],a[2],jobs.getSelectedItem().toString(),units.getSelectedItem().toString(),d);if(save(p,q,false))dialog.dismiss();}catch(Exception e){msg("Перевір дані: "+e.getMessage());}}));dialog.show();}
 NumberPicker wheel(int min,int max,int value){NumberPicker n=new NumberPicker(this);n.setMaxValue(max);n.setMinValue(min);n.setValue(value);n.setWrapSelectorWheel(false);n.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);return n;}
 void wheelRange(NumberPicker n,int max,int val){if(n.getMaxValue()!=max){n.setDisplayedValues(null);n.setMaxValue(max);}if(n.getValue()!=Math.min(max,val))n.setValue(Math.min(max,val));}
 void birthPicker(LocalDate initial,java.util.function.Consumer<LocalDate> chosen){LocalDate today=LocalDate.now();LocalDate start=initial==null?LocalDate.of(1980,1,1):initial;if(start.isAfter(today))start=today;if(start.getYear()<1900)start=LocalDate.of(1900,1,1);LinearLayout l=col();pad(l,12);LinearLayout r=row();NumberPicker day=wheel(1,31,start.getDayOfMonth()),month=wheel(1,12,start.getMonthValue()),year=wheel(1900,today.getYear(),start.getYear());day.setFormatter(v->String.format(Locale.ROOT,"%02d",v));month.setDisplayedValues(months.clone());NumberPicker[] pickers={day,month,year};String[] labels={"День","Місяць","Рік"};for(int i=0;i<3;i++){LinearLayout cell=col();TextView t=text(labels[i],13,MUTED,true);t.setGravity(Gravity.CENTER);add(cell,t);cell.addView(pickers[i],new LinearLayout.LayoutParams(-1,dp(160)));r.addView(cell,new LinearLayout.LayoutParams(0,-2,i==1?1.6f:1f));}add(l,r);TextView preview=text("",19,INK,true);preview.setGravity(Gravity.CENTER);add(l,preview);Runnable update=()->{int mmax=year.getValue()==today.getYear()?today.getMonthValue():12;if(month.getMaxValue()!=mmax){wheelRange(month,mmax,month.getValue());month.setDisplayedValues(Arrays.copyOf(months,mmax));}int dmax=YearMonth.of(year.getValue(),month.getValue()).lengthOfMonth();if(year.getValue()==today.getYear()&&month.getValue()==today.getMonthValue())dmax=Math.min(dmax,today.getDayOfMonth());wheelRange(day,dmax,day.getValue());preview.setText(LocalDate.of(year.getValue(),month.getValue(),day.getValue()).format(DATE));};year.setOnValueChangedListener((n,a,b)->update.run());month.setOnValueChangedListener((n,a,b)->update.run());day.setOnValueChangedListener((n,a,b)->preview.setText(LocalDate.of(year.getValue(),month.getValue(),day.getValue()).format(DATE)));update.run();new AlertDialog.Builder(this).setTitle("Оберіть дату народження").setView(l).setPositiveButton("Підтвердити",(d,w)->chosen.accept(LocalDate.of(year.getValue(),month.getValue(),day.getValue()))).setNegativeButton("Скасувати",null).show();}
 void rememberDirectories(){for(boolean job:new boolean[]{true,false}){Set<String> extras=customDirectory(job);for(Person p:people){String v=cleanName(personDirectory(p,job));if(!v.isEmpty()&&extras.stream().noneMatch(a->nameKey(a).equals(nameKey(v))))extras.add(v);}prefs().edit().putStringSet(directoryKey(job),extras).apply();}}
 String directoryKey(boolean job){return job?"customJobs":"customUnits";}
 String directoryName(boolean job){return job?"Посади":"Підрозділи";}
 String cleanName(String value){return value.trim().replaceAll("\\s+"," ");}
 String nameKey(String value){return cleanName(value).toLowerCase(Locale.ROOT);}
 String personDirectory(Person p,boolean job){return job?p.job:p.unit;}
 Set<String> customDirectory(boolean job){return new HashSet<>(prefs().getStringSet(directoryKey(job),Collections.emptySet()));}
 List<String> directory(boolean job){Map<String,String> names=new LinkedHashMap<>();for(Person p:people){String v=cleanName(personDirectory(p,job));if(!v.isEmpty())names.putIfAbsent(nameKey(v),v);}for(String raw:customDirectory(job)){String v=cleanName(raw);if(!v.isEmpty())names.putIfAbsent(nameKey(v),v);}List<String> list=new ArrayList<>(names.values());java.text.Collator c=java.text.Collator.getInstance(Locale.forLanguageTag("uk-UA"));c.setStrength(java.text.Collator.SECONDARY);list.sort(c);return list;}
 Spinner directorySpinner(boolean job,String selectedValue){List<String> values=directory(job);values.add(0,job?"Оберіть посаду":"Оберіть підрозділ");Spinner sp=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,values);adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);sp.setAdapter(adapter);if(selectedValue!=null)for(int i=1;i<values.size();i++)if(nameKey(values.get(i)).equals(nameKey(selectedValue))){sp.setSelection(i);break;}return sp;}
 void directoryDialog(boolean job){ScrollView scroll=new ScrollView(this);LinearLayout list=col();pad(list,12);scroll.addView(list);AlertDialog dialog=new AlertDialog.Builder(this).setTitle(directoryName(job)).setView(scroll).setPositiveButton("Закрити",null).create();Runnable render=()->{list.removeAllViews();List<String> items=directory(job);if(items.isEmpty())add(list,text("Список порожній",14,MUTED,false));for(String value:items){LinearLayout item=card(list);add(item,text(value,16,INK,true));int used=0;for(Person p:people)if(nameKey(personDirectory(p,job)).equals(nameKey(value)))used++;add(item,text("Працівників: "+used,12,MUTED,false));LinearLayout actions=row();weight(actions,button("Редагувати",()->{dialog.dismiss();directoryEdit(job,value);}));actions.addView(new View(this),new LinearLayout.LayoutParams(dp(10),1));weight(actions,button("Видалити",()->{if(!ready&&!people.isEmpty()){msg("Excel недоступний");return;}for(Person p:people)if(nameKey(personDirectory(p,job)).equals(nameKey(value))){msg("Спочатку перепризнач працівників, які використовують цю назву");return;}new AlertDialog.Builder(this).setTitle("Видалити?").setMessage(value).setPositiveButton("Видалити",(d,w)->{Set<String> extras=customDirectory(job);extras.removeIf(v->nameKey(v).equals(nameKey(value)));prefs().edit().putStringSet(directoryKey(job),extras).apply();dialog.dismiss();show();directoryDialog(job);}).setNegativeButton("Скасувати",null).show();}));add(item,actions);}add(list,primary(job?"＋ Додати посаду":"＋ Додати підрозділ",()->{dialog.dismiss();directoryEdit(job,null);}));};render.run();dialog.show();}
 void directoryEdit(boolean job,String old){LinearLayout l=col();pad(l,16);EditText input=new EditText(this);input.setSingleLine(true);input.setHint(job?"Назва посади":"Назва підрозділу");if(old!=null)input.setText(old);add(l,input);if(old!=null)add(l,text("Назва зміниться також у відповідних працівників Excel.",13,MUTED,false));AlertDialog dialog=new AlertDialog.Builder(this).setTitle(old==null?"Додати":"Редагувати").setView(l).setPositiveButton("Зберегти",null).setNegativeButton("Скасувати",(d,w)->directoryDialog(job)).create();dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(x->{String value=cleanName(input.getText().toString());if(value.isEmpty()){msg("Введи назву");return;}for(String existing:directory(job))if(nameKey(existing).equals(nameKey(value))&&(old==null||!nameKey(existing).equals(nameKey(old)))){msg("Така назва вже існує");return;}try{if(old!=null)renameDirectory(job,old,value);Set<String> extras=customDirectory(job);if(old!=null)extras.removeIf(a->nameKey(a).equals(nameKey(old)));extras.add(value);prefs().edit().putStringSet(directoryKey(job),extras).apply();dialog.dismiss();show();directoryDialog(job);}catch(Exception e){msg("Зміни не збережено: "+e.getMessage());}}));dialog.setOnCancelListener(d->directoryDialog(job));dialog.show();}
 void renameDirectory(boolean job,String old,String value)throws Exception{boolean used=false;for(Person p:people)if(nameKey(personDirectory(p,job)).equals(nameKey(old)))used=true;if(!used)return;if(!ready)throw new IOException("Excel недоступний");byte[] original=read();Workbook w=new Workbook(original);int changed=0;for(Person p:w.people){if(nameKey(personDirectory(p,job)).equals(nameKey(old))){w.setDirectory(p.row,job,value);changed++;}}if(changed==0)throw new IOException("Excel змінився");byte[] result=w.save();Workbook check=new Workbook(result);if(check.people.size()!=w.people.size())throw new IOException("Помилка перевірки Excel");try(FileOutputStream out=new FileOutputStream(new File(getFilesDir(),"Іменинники-backup.xlsx"))){out.write(original);}try{write(result);if(!Arrays.equals(read(),result))throw new IOException("Помилка запису Excel");}catch(Exception e){write(original);throw e;}reload(false);show();}

 boolean same(Person a,Person b){return a.name().equals(b.name())&&a.born.equals(b.born)&&a.job.equals(b.job)&&a.unit.equals(b.unit);}
 boolean save(Person old,Person p,boolean delete){if(!ready){msg("Excel недоступний");return false;}try{byte[] original=read();Workbook w=new Workbook(original);if(old!=null){Person current=null;for(Person q:w.people)if(q.row==old.row)current=q;if(current==null||!same(current,old))throw new IOException("Excel змінився. Відкрий картку повторно");}if(!delete)for(Person q:w.people)if(q.row!=p.row&&q.name().equalsIgnoreCase(p.name())&&q.born.equals(p.born))throw new IOException("Такий працівник вже є в Excel");w.put(p,delete);byte[] changed=w.save();Workbook check=new Workbook(changed);if(check.people.size()!=w.people.size()+(old==null?1:delete?-1:0))throw new IOException("Помилка перевірки Excel");File backup=new File(getFilesDir(),"Іменинники-backup.xlsx");try(FileOutputStream out=new FileOutputStream(backup)){out.write(original);}try{write(changed);if(!Arrays.equals(read(),changed))throw new IOException("Перевірка запису не пройдена");}catch(Exception ex){try{write(original);}catch(Exception restore){throw new IOException("Не вдалося відновити Excel. Резервна копія збережена у внутрішній пам’яті.");}throw ex;}reload(false);show();msg("Зміни збережено в Excel");return true;}catch(Exception e){msg("Зміни не збережено: "+e.getMessage());return false;}}
 void write(byte[] b)throws Exception{LocalWorkbook.write(this,b);}
 void reminders(LinearLayout b){
  section(b,"Нагадування");LinearLayout c=card(b);add(c,text("Розклад нагадувань",19,GREEN,true));
  List<Integer> ids=ReminderReceiver.rules(this);
  ids.sort(Comparator.comparingInt((Integer id)->ReminderReceiver.offset(this,id)).reversed().thenComparing(id->ReminderReceiver.time(this,id)));
  int activeCount=0;for(int id:ids)if(ReminderReceiver.enabled(this,id))activeCount++;TextView activeLabel=text("Активних: "+activeCount,14,MUTED,false);add(c,activeLabel);if(ids.isEmpty())add(c,text("Немає нагадувань. Додай потрібний день і час.",14,MUTED,false));
  for(int id:ids){
   LinearLayout item=card(b);LinearLayout r=row();LinearLayout labels=col();add(labels,text(ReminderReceiver.dayLabel(ReminderReceiver.offset(this,id)),15,INK,true));add(labels,text(ReminderReceiver.time(this,id).format(DateTimeFormatter.ofPattern("HH:mm")),26,INK,true));weight(r,labels);Switch cb=new Switch(this);cb.setContentDescription("Увімкнути нагадування");cb.setChecked(ReminderReceiver.enabled(this,id));r.addView(cb);add(item,r);
   cb.setOnCheckedChangeListener((v,on)->{prefs().edit().putBoolean("enabled"+id,on).apply();ReminderReceiver.scheduleOne(this,id);int count=0;for(int rule:ids)if(ReminderReceiver.enabled(this,rule))count++;activeLabel.setText("Активних: "+count);});
   gap(item,10);LinearLayout actions=row();weight(actions,button("Редагувати",()->editReminder(id)));
   actions.addView(new View(this),new LinearLayout.LayoutParams(dp(10),1));
   weight(actions,button("Видалити",()->new AlertDialog.Builder(this).setTitle("Видалити нагадування?").setPositiveButton("Видалити",(d,w)->{ReminderReceiver.removeRule(this,id);show();}).setNegativeButton("Скасувати",null).show()));
   add(item,actions);gap(item,6);
  }
  action(b,primary("＋ Додати нагадування",()->editReminder(-1)));LinearLayout options=card(b);
  CheckBox only=new CheckBox(this);only.setText("Нагадувати тільки про ювілярів");only.setChecked(prefs().getBoolean("onlyJubilee",false));only.setOnCheckedChangeListener((v,on)->prefs().edit().putBoolean("onlyJubilee",on).apply());add(options,only);add(b,button("Перевірити сповіщення",()->ReminderReceiver.notify(this,"Тестове нагадування","Сповіщення працюють",999)));if(Build.VERSION.SDK_INT>=31&&!getSystemService(AlarmManager.class).canScheduleExactAlarms()){add(b,text("Для точного часу дозволь системі встановлювати будильники та нагадування.",14,GOLD,false));add(b,button("Дозволити точні нагадування",()->{try{startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));}catch(Exception e){msg("Відкрий дозволи додатка в налаштуваннях Android");}}));}}
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
 void settings(LinearLayout b){section(b,"Налаштування");LinearLayout appearance=card(b);add(appearance,text("Зовнішній вигляд",19,GREEN,true));gap(appearance,8);action(appearance,button("Тема: "+themeLabel(),this::themeSettings));action(appearance,button("Розмір шрифту: "+prefs().getInt("fontPercent",100)+"%",this::fontSettings));action(appearance,button("Тіні елементів: "+shadowLabel(),this::shadowSettings));add(appearance,button("Об’єм елементів: "+volumeLabel(),this::volumeSettings));LinearLayout colors=card(b);add(colors,text("Кольори оформлення",19,INK,true));gap(colors,8);colorOptions(colors,activeTheme);LinearLayout dirs=card(b);add(dirs,text("Довідники",19,GREEN,true));gap(dirs,8);action(dirs,button("Посади ("+directory(true).size()+")",()->directoryDialog(true)));add(dirs,button("Підрозділи ("+directory(false).size()+")",()->directoryDialog(false)));LinearLayout update=card(b);add(update,text("Оновлення додатка",19,GREEN,true));add(update,text("Встановлена версія: "+installedVersion(),14,MUTED,false));gap(update,8);add(update,button("Перевірити оновлення",this::checkUpdate));LinearLayout c=card(b);add(c,text("Excel — база працівників",19,GREEN,true));gap(c,8);action(c,button("Обрати Excel",this::pickExcel));Button export=button("Вивантажити Excel",this::shareExcel);export.setEnabled(ready);action(c,export);int year=LocalDate.now().getYear();Button current=button("Вивантажити ювілярів за "+year+" рік",()->shareJubilees(year));current.setEnabled(ready);action(c,current);Button next=button("Вивантажити ювілярів за "+(year+1)+" рік",()->shareJubilees(year+1));next.setEnabled(ready);action(c,next);YearMonth currentMonth=YearMonth.now(),nextMonth=currentMonth.plusMonths(1);Button thisMonth=button("Вивантажити іменинників - "+months[currentMonth.getMonthValue()-1]+" "+currentMonth.getYear(),()->shareBirthdays(currentMonth));thisMonth.setEnabled(ready);action(c,thisMonth);Button followingMonth=button("Вивантажити іменинників - "+months[nextMonth.getMonthValue()-1]+" "+nextMonth.getYear(),()->shareBirthdays(nextMonth));followingMonth.setEnabled(ready);action(c,followingMonth);add(c,text("Редагується локальна копія. Вивантаження надсилає актуальний файл з усіма змінами.",13,MUTED,false));add(b,button("Системні дозволи додатка",()->startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())))));}


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
