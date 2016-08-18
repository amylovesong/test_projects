package com.sun.rxjava_example;

import java.io.File;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by sunxiaoling on 16/8/18.
 */
public class RxJavaExample {

    private void create() {
        // 创建Observable
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("RxJava");
                subscriber.onCompleted();
            }
        });

        String[] files = new File("path").list();
        Observable.from(files);

        Observable.just("a", "b", "c");
    }

}
