package com.sun.rxjava_example;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;

import java.io.File;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by sunxiaoling on 16/8/18.
 */
public class RxJavaExample {

    private static final String TAG = RxJavaExample.class.getSimpleName();

    private void create() {
        // 创建Observable
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("RxJava");
                subscriber.onCompleted();
            }
        });

        String[] files = new File("path").list();
        Observable.from(files);

        Observable.just("a", "b", "c");

        // 创建Observer/Subscriber
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onNext(String s) {
                Log.d(TAG, s);
            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }
        };
        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onNext(String s) {
                Log.d(TAG, s);
            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "subscriber e: " + e);
            }
        };

        // 订阅
        observable.subscribe(observer);
        observable.subscribe(subscriber);

        final Subscription subscription = observable.subscribe(subscriber);
        subscription.unsubscribe();

        Observable.just("RxJava").subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Log.d(TAG, s);
            }
        });
    }

    private void callbackHell() {
        requestA(new Callback() {

            @Override
            public void onSuccess() {
                // 下一个请求
                requestB(new Callback(){

                    @Override
                    public void onSuccess() {
                        // 请求成功，执行正常逻辑
                    }

                    @Override
                    public void onFailed() {
                        // 处理请求失败
                    }
                });
            }

            @Override
            public void onFailed() {
                // 处理请求失败
            }
        });
    }

    public static void commonCoding() {
        String[] strings =  {"a-1", "b-2", "c-3", "d-4"};
        for (String str: strings) {
            final String subStr = str.substring(2);
            try {
                int number = Integer.valueOf(subStr);
                Log.d(TAG, "CommonCoding: " + number);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    public static void useRxJava() {
        String[] strings =  {"a-1", "b-2", "c-3", "d-4"};
        Observable.from(strings)
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        return Observable.just(s.substring(2));
                    }
                })
                .map(new Func1<String, Integer>() {
                    @Override
                    public Integer call(String s) {
                        return Integer.valueOf(s);
                    }
                })
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer number) {
                        return number <= 3;
                    }
                })
                .take(2)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer number) {
                        Log.d(TAG, "RxJava: " + number);
                    }
                });

        Context mContext = null;
        final ImageView imageView = new ImageView(mContext);
        String url = "";
        loadImage(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        imageView.setImageBitmap(bitmap);
                    }
                });

        Schedulers.newThread();
        Schedulers.computation();
    }

    private void rxBinding(View view) {

        RxView.clicks(view)
                .subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                // Handle click event
            }
        });

    }

    private static Observable<Bitmap> loadImage(String url) {
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {

            }
        });
    }

    private void requestA(Callback callback) {

    }

    private void requestB(Callback callback) {

    }

    interface Callback {
        void onSuccess();

        void onFailed();
    }

}
