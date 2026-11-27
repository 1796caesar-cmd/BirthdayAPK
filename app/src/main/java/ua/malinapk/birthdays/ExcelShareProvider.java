package ua.malinapk.birthdays;
import android.content.*;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.*;
import java.time.YearMonth;

public final class ExcelShareProvider extends ContentProvider {
 static final String MIME="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
 static Uri uri(Context c){return new Uri.Builder().scheme("content").authority(c.getPackageName()+".excelshare").appendPath(LocalWorkbook.NAME).build();}
 static final String APK_MIME="application/vnd.android.package-archive";
 static File apkFile(Context c){return new File(new File(c.getFilesDir(),"updates"),"BirthdayAPK.apk");}
 static Uri apkUri(Context c){return new Uri.Builder().scheme("content").authority(c.getPackageName()+".excelshare").appendPath("BirthdayAPK.apk").build();}
 static File reportFile(Context c,int year){if(year<1900||year>9999)throw new IllegalArgumentException("Invalid year");return new File(c.getFilesDir(),"Ювіляри_"+year+".xlsx");}
 static Uri reportUri(Context c,int year){return new Uri.Builder().scheme("content").authority(c.getPackageName()+".excelshare").appendPath(reportFile(c,year).getName()).build();}
 static File exportFile(Context c){File dir=new File(c.getFilesDir(),"exports");if(!dir.isDirectory()&&!dir.mkdirs()&&!dir.isDirectory())throw new IllegalStateException("Не вдалося створити папку експорту");return new File(dir,LocalWorkbook.NAME);}
 static Uri exportUri(Context c){return new Uri.Builder().scheme("content").authority(c.getPackageName()+".excelshare").appendPath("export").appendPath(LocalWorkbook.NAME).build();}
 static File monthFile(Context c,YearMonth month){return new File(c.getFilesDir(),"Іменинники_"+month+".xlsx");}
 static Uri monthUri(Context c,YearMonth month){return new Uri.Builder().scheme("content").authority(c.getPackageName()+".excelshare").appendPath(monthFile(c,month).getName()).build();}
 static boolean allowed(String name){return name!=null&&(name.matches("(?:Стенд|Іменинники)_[0-9]{4}(?:-[0-9]{2})?_[0-9a-f-]{36}(?:_[0-9]+)?\\.(?:png|xlsx)")||name.matches("Грамота_[\\p{L}\\p{M}0-9 '’ʼ-]{1,70}_[0-9a-f-]{36}_[0-9]+\\.png"));}
 static File generatedFile(Context c,String name){if(!allowed(name))throw new IllegalArgumentException("Unknown file");File dir=new File(c.getFilesDir(),"generated");if(!dir.isDirectory()&&!dir.mkdirs()&&!dir.isDirectory())throw new IllegalStateException("Не вдалося створити папку експорту");return new File(dir,name);}
 static Uri generatedUri(Context c,String name){return new Uri.Builder().scheme("content").authority(c.getPackageName()+".excelshare").appendPath("generated").appendPath(name).build();}
 private File file(Uri u){Context c=getContext();if(exportUri(c).equals(u))return exportFile(c);if(uri(c).equals(u))return new File(c.getFilesDir(),LocalWorkbook.NAME);if(apkUri(c).equals(u))return apkFile(c);String name=u.getLastPathSegment();if(allowed(name)&&generatedUri(c,name).equals(u))return generatedFile(c,name);if(name!=null&&name.matches("Ювіляри_[0-9]{4}\\.xlsx")){int year=Integer.parseInt(name.substring(8,12));if(reportUri(c,year).equals(u))return reportFile(c,year);}if(name!=null&&name.matches("Іменинники_[0-9]{4}-(0[1-9]|1[0-2])\\.xlsx")){YearMonth month=YearMonth.parse(name.substring("Іменинники_".length(),"Іменинники_".length()+7));if(monthUri(c,month).equals(u))return monthFile(c,month);}throw new IllegalArgumentException("Unknown file");}
 @Override public boolean onCreate(){return true;}
 @Override public String getType(Uri u){file(u);return apkUri(getContext()).equals(u)?APK_MIME:u.getLastPathSegment().endsWith(".png")?"image/png":MIME;}
 @Override public Cursor query(Uri u,String[] projection,String selection,String[] selectionArgs,String sortOrder){
  File file=file(u);String[] cols=projection==null?new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE}:projection;
  MatrixCursor c=new MatrixCursor(cols);Object[] values=new Object[cols.length];
  for(int i=0;i<cols.length;i++){if(OpenableColumns.DISPLAY_NAME.equals(cols[i]))values[i]=file.getName();else if(OpenableColumns.SIZE.equals(cols[i]))values[i]=file.length();}c.addRow(values);return c;
 }
 @Override public ParcelFileDescriptor openFile(Uri u,String mode)throws FileNotFoundException{File file=file(u);if(!"r".equals(mode))throw new FileNotFoundException("Read only");return ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY);}
 @Override public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
 @Override public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
 @Override public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
}
