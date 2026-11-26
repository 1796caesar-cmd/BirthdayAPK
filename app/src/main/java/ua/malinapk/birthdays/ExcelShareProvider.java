package ua.malinapk.birthdays;
import android.content.*;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.*;

public final class ExcelShareProvider extends ContentProvider {
 static final String MIME="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
 static Uri uri(Context c){return new Uri.Builder().scheme("content").authority(c.getPackageName()+".excelshare").appendPath(LocalWorkbook.NAME).build();}
 static final String APK_MIME="application/vnd.android.package-archive";
 static File apkFile(Context c){return new File(new File(c.getFilesDir(),"updates"),"BirthdayAPK.apk");}
 static Uri apkUri(Context c){return new Uri.Builder().scheme("content").authority(c.getPackageName()+".excelshare").appendPath("BirthdayAPK.apk").build();}
 private File file(Uri u){Context c=getContext();if(uri(c).equals(u))return new File(c.getFilesDir(),LocalWorkbook.NAME);if(apkUri(c).equals(u))return apkFile(c);throw new IllegalArgumentException("Unknown file");}
 @Override public boolean onCreate(){return true;}
 @Override public String getType(Uri u){file(u);return apkUri(getContext()).equals(u)?APK_MIME:MIME;}
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
