package com.friends.zingradio.media;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
 
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
 
public class LocalServiceBindingDemo {
 
    public static class SampleActivity extends Activity {
 
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
 
            bindUI();
 
            // Binding
            MainInterface mainInterface = MyService.getAsyncService(this);
 
            // invoke methods of the service. The code will run in the UI thread asynchronously.
            mainInterface.do1();
            mainInterface.do2();
        }
 
        private void bindUI() {
            // ...
        }
    }
 
    public interface MainInterface {
        void do1();
 
        void do2();
    }
 
    // MyService implements MainInterface
    public static class MyService extends Service implements MainInterface {
 
        private final IBinder binder = new LocalBinder();
 
        @Override
        public IBinder onBind(Intent intent) {
            return binder;
        }
 
        private class LocalBinder extends Binder {
            MyService getService() {
                return MyService.this;
            }
        }
 
        @Override
        public void do1() {
            // The implementation of the service
            Toast.makeText(this, "Doing 111", Toast.LENGTH_LONG).show();
        }
 
        @Override
        public void do2() {
            // The implementation of the service
            Toast.makeText(this, "Doing 222", Toast.LENGTH_LONG).show();
        }
 
        // This is how activities get hold of the service implementation as an interface!!!
        // All invocations will be executed async in the UI thread!!!
        public static MainInterface getAsyncService(final Context context) {
            final Intent intent = new Intent(context, MyService.class);
 
            // Unmark the following if you want the service to keep on leaving even after the passed activity was destroyed.
            // context.startService(intent);
 
            MainInterface proxy = (MainInterface) Proxy.newProxyInstance(context.getClassLoader(), new Class<?>[] { MainInterface.class }, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
                    ServiceConnection connection = new ServiceConnection() {
                        @Override
                        public void onServiceDisconnected(ComponentName name) {
                        }
 
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            // this is the implementation itself!!!
                            MainInterface mainInterface = ((LocalBinder) service).getService();
                            try {
                                method.invoke(mainInterface, args);
                            } catch (Exception e) {
                                // Alternatively, you can notify the service's error mechanism
                                Log.e("", "Error invoking service method: " + method.getName(), e);
                            }
                        }
                    };
                    // This is how you bind async the connection to the service.
                    // The service will be created if it was not yet created.
                    context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
 
                    return null;
                }
            });
 
            return proxy;
        }
    }
}