#include <jni.h>
#include <stdio.h>  
#include <linux/input.h>  
#include <stdlib.h>  
#include <sys/types.h>  
#include <sys/stat.h>  
#include <fcntl.h>
#include <android/log.h>
#include <errno.h>

#define LOG_TAG "gengj^^^^^^^^^^^^^^^^^^^^^^^"

#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

int init_device(char *dev_path)
{
	int device_fd;

    device_fd=open(dev_path, O_RDONLY);  
    if(device_fd <= 0)  
    {  
        LOGI("init failed! %s\n", strerror(errno));
        return -1;
    }

    LOGI("init succeed\n");

    return device_fd;
}

int isVolumeDown(int fd)
{
    struct input_event t;
    int id = -1;

    if(read(fd, &t, sizeof(t)) == sizeof(t)) {
        if(t.type==EV_KEY) {
        	if(t.code == KEY_VOLUMEDOWN && t.value == 1) {
        		//LOGI("Pressed Volume Button: Down\n");
        		id = 2;

        	} else if (t.code == KEY_VOLUMEUP && t.value == 1) {
        		//LOGI("Pressed Volume Button: Up\n");
        		id = 1;
        	} else if ((t.code == KEY_VOLUMEUP || t.code == KEY_VOLUMEDOWN) && t.value == 0){
        		id = 0;
        	}

        	return id;
        }
            //LOGI("key %d %s\n", t.code, (t.value) ? "Pressed" : "Released");


    }


    return -1;
}

int uinit_device(int fd)
{
    close(fd);
}

jint Java_com_example_volumekey_JNIClass_init(JNIEnv * env, jobject obj, jint id)
{
	int res;

	char dev_path[32] = {0};

	sprintf(dev_path, "/dev/input/event%d", id);


	res = init_device(dev_path);

	return res;

}

jint Java_com_example_volumekey_JNIClass_isKeyDown(JNIEnv * env, jobject obj, jint fd)
{
    int res = isVolumeDown(fd);
//    if (res == 1) {
//        LOGI("Key Up Pressed\n");
//    } else if (res == 0){
//        LOGI("Key released\n");
//    } else if (res == 2) {
//    	LOGI("KEY Down Pressed\n");
//    }

    return res;
}

void Java_com_example_volumekey_JNIClass_unInit(JNIEnv * env, jobject obj, jint fd)
{
    uinit_device(fd);
}
