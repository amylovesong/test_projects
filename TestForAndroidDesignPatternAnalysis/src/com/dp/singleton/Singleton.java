package com.dp.singleton;

import java.util.HashMap;
import java.util.Map;

public class Singleton {
	private static Singleton mInstance = null;

	private Singleton() {

	}

	public void doSomething() {
		System.out.println("do sth.");
	}

	public static Singleton getSingleton() {
		if(mInstance==null){
			mInstance = new Singleton();
		}
		return mInstance;
	}

	// 2. double-check for concurrency
	public static Singleton getInstance() {
		if (mInstance == null) {
			synchronized (Singleton.class) {
				if (mInstance == null) {
					mInstance = new Singleton();
				}
			}
		}
		return mInstance;
	}

	// 3. lazy-initialization
	public static Singleton getInstanceFromHolder() {
		return SingletonHolder.mOnlyInstance;
	}

	private static class SingletonHolder {
		private static final Singleton mOnlyInstance = new Singleton();
	}

	// 4. enum
	enum SinlgetonEnum {
		INSTANCE;

		public void doSomething() {
			System.out.println("do sth.");
		}
	}

	// 5. instance container
	private static Map<String, Singleton> objMap = new HashMap<>();

	public static void registerService(String key, Singleton instance) {
		if (!objMap.containsKey(key)) {
			objMap.put(key, instance);
		}
	}

	public static Singleton getService(String key) {
		return objMap.get(key);
	}

}
