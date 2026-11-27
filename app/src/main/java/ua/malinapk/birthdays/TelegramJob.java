package ua.malinapk.birthdays;
import android.app.job.*;
import org.json.JSONObject;
public class TelegramJob extends JobService {
 private volatile Thread worker;
 @Override public boolean onStartJob(JobParameters params){Thread t=new Thread(()->{boolean retry=false;try{
  for(int i=0;i<20&&!Thread.currentThread().isInterrupted();i++)synchronized(Telegram.DELIVERY_LOCK){
   if(Thread.currentThread().isInterrupted())break;
   JSONObject msg;String token,chat;
   synchronized(Telegram.class){msg=Telegram.peek(this);if(msg==null)break;token=Telegram.token(this);chat=Telegram.p(this).getString("chat","");if(token.isEmpty()||chat.isEmpty())break;}
   if(!Telegram.allowed(this,msg.optString("kind")))continue;
   Telegram.send(token,chat,msg.getString("body"));Telegram.remove(this,msg.getString("id"));
  }
  retry=Telegram.queue(this).length()>0;
 }catch(Exception e){retry=true;}finally{if(worker==Thread.currentThread()){worker=null;jobFinished(params,retry);}}},"telegram-delivery");worker=t;t.start();return true;}
 @Override public boolean onStopJob(JobParameters params){Thread t=worker;worker=null;if(t!=null)t.interrupt();return true;}
}
