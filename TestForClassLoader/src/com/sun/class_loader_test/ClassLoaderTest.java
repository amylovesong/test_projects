package com.sun.class_loader_test;

public class ClassLoaderTest {
	public static void main(String[] args) {

		// String url[] = System.getProperty("sun.boot.class.path").split(";");
		// for (String str : url) {
		// System.out.println(str);
		// }
		//
		// System.out.println("==============================");

		ClassLoader loader = ClassLoaderTest.class.getClassLoader();
		while (loader != null) {
			System.out.println(loader);
			loader = loader.getParent();
		}
		System.out.println(loader);

		// System.out.println("==============================");
		//
		// try {
		// String rootUrl = "http://localhost:8080/httpweb/classes";
		// NetworkClassLoader networkClassLoader = new NetworkClassLoader(
		// rootUrl);
		// String classname = "org.classloader.simple.NetClassLoaderTest";
		// Class clazz = networkClassLoader.loadClass(classname);
		// System.out.println(clazz.getClassLoader());
		// } catch (ClassNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
}
