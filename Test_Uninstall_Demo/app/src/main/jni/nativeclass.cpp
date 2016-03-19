#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <android/log.h>
#include <unistd.h>
#include <sys/inotify.h>

#define MEM_ZERO(pDest, destSize) memset(pDest, 0, destSize)

#define LOG_TAG "onEvent"

#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)

JNIEXPORT jstring JNICALL
Java_com_sun_uninstall_demo_NativeClass_init(JNIEnv *env, jobject instance) {

    LOGD("init start...");

    pid_t pid = fork();
    LOGD("pid:" + pid);
    if (pid < 0) {
        LOGD("fork failed...");
    } else if (pid == 0) {//子进程，注册文件目录监听器
        int fileDescriptor = inotify_init();
        LOGD("fileDescriptor=%d", fileDescriptor);
        if(fileDescriptor <0){
            LOGD("inotify_init failed...");
            exit(1);
        }

        int watchDescriptor;
        watchDescriptor = inotify_add_watch(fileDescriptor, "/data/data/com.example.test_uninstall_demo", IN_DELETE);
        LOGD("watchDescriptor=%d", watchDescriptor);
        if(watchDescriptor < 0){
            LOGD("inotify_add_watch failed...");
            exit(1);
        }

        //分配缓存，读取event
        void *pBuff = malloc(sizeof(struct inotify_event));
        if(pBuff==NULL){
            LOGD("malloc failed...");
            exit(1);
        }
        LOGD("start observer...");
        //阻塞式读取
        size_t readBytes = read(fileDescriptor, pBuff, sizeof(struct inotify_event));

        //监听到目录被删除事件
        free(pBuff);
        inotify_rm_watch(fileDescriptor, IN_DELETE);

        LOGD("uninstall");

        //打开指定页面
        execlp("am", "am", "start", "--user", "0", "-a", "android.intent.action.VIEW", "-d",
            "http://shouji.360.cn/web/uninstall/uninstall.html", (char *)NULL);
    } else {//父进程

    }

    return (*env).NewStringUTF("Hello from JNI !");
}