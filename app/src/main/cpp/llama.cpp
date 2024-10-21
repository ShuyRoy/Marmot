#include <iostream>
#include <string>

int main() {
    std::string user_input;

    std::cout << "Enter any key to print 'Hello World' or type 'exit' to quit: ";

    while (std::getline(std::cin, user_input)) {
        if (user_input == "exit")
            break;
        else
            std::cout << "Hello World" << std::endl;
        
        std::cout << "Enter any key to print 'Hello World' or type 'exit' to quit: ";
    }

    return 0;
}

#include <jni.h>
#include <thread>
#include <semaphore>
#include <mutex>
#include <condition_variable>

//std::counting_semaphore<1> sem(0);
//std::counting_semaphore<1> sem2(0);

class Semaphore {
public:
    Semaphore(int count = 0) : count_(count) {}

    void acquire() {
        std::unique_lock<std::mutex> lock(mutex_);
        while (count_ == 0) { // 等待资源可用
            cond_.wait(lock);
        }
        --count_;
    }

    void release() {
        std::unique_lock<std::mutex> lock(mutex_);
        ++count_;
        cond_.notify_one(); // 通知一个等待线程
    }

private:
    std::mutex mutex_;
    std::condition_variable cond_;
    int count_;
};

int test = 0;
Semaphore sem(0);
Semaphore sem2(0);

int stop = 0;

void thread_f(int a, int *arr) {
    int i = 0;
    while (true) {
        sem.acquire();
        test = arr[i];
//        test++;
        sem2.release();
        if (stop != 0) {
            break;
        }
        i += 1;
    }
}

//std::thread t(thread_f);

extern "C"
JNIEXPORT jstring JNICALL
Java_me_jinheng_cityullm_models_LLama_test_1so(JNIEnv *env, jobject thiz) {
    // TODO: implement test_so()
    return env->NewStringUTF("Hello from C++");
}

extern "C"
JNIEXPORT jstring JNICALL
Java_me_jinheng_cityullm_models_LLama_getOutput(JNIEnv *env, jclass clazz) {
    // TODO: implement getOutput()
    sem2.acquire();
    return env->NewStringUTF(std::to_string(test).c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_me_jinheng_cityullm_models_LLama_inputString(JNIEnv *env, jclass clazz, jstring s) {
    // TODO: implement inputString()
    sem.release();
}

extern "C"
JNIEXPORT void JNICALL
Java_me_jinheng_cityullm_models_LLama_stop_1llama(JNIEnv *env, jclass clazz) {
    // TODO: implement stop_llama()
    stop = 1;
}

//bool stop = false;
//extern "C"
//JNIEXPORT void JNICALL
//Java_me_jinheng_cityullm_models_LLama_stop(JNIEnv *env, jclass clazz) {
//    // TODO: implement stop()
//    stop = true;
//}

//bool destroy = false;

//extern "C"
//JNIEXPORT void JNICALL
//Java_me_jinheng_cityullm_models_LLama_kill(JNIEnv *env, jclass clazz) {
//    // TODO: implement kill()
//    destroy = true;
//}
extern "C"
JNIEXPORT void JNICALL
Java_me_jinheng_cityullm_models_LLama_startLLama(JNIEnv *env, jclass clazz, jobject msg,
                                                 jstring local_model_path, jint thread_num) {
    // TODO: implement startLLama()
    jclass cls = env->GetObjectClass(msg);
    jmethodID sendString = env->GetMethodID(cls, "receiveStringFromNative", "(Ljava/lang/String;)V");
    jmethodID sendEnd = env->GetMethodID(cls, "receiveEndFromNative", "()V");
    std::string path = env->GetStringUTFChars(local_model_path, nullptr);
    // 转换 thread_num 为 std::string 然后为其分配持久的字符数组
    std::string thread_num_str = std::to_string(thread_num);
    char* thread_num_cstr = new char[thread_num_str.length() + 1];
    std::strcpy(thread_num_cstr, thread_num_str.c_str());

    // 为 path 同样分配一个字符数组
    char* path_cstr = new char[path.length() + 1];
    std::strcpy(path_cstr, path.c_str());

//     int arr[3] = {1, 2, 3};
     int *arr = new int[3];
     arr[0] = thread_num;
     arr[1] = 4;
     arr[2] = 3;
    std::thread t(thread_f, 3, arr);
    t.detach();
}
extern "C"
JNIEXPORT void JNICALL
Java_me_jinheng_cityullm_models_LLama_stop(JNIEnv *env, jclass clazz) {
    // TODO: implement stop()
}
extern "C"
JNIEXPORT void JNICALL
Java_me_jinheng_cityullm_models_LLama_kill(JNIEnv *env, jclass clazz) {
    // TODO: implement kill()
}

extern "C"
JNIEXPORT void JNICALL
Java_me_jinheng_cityullm_models_LLama_startLLamaPrefetch(JNIEnv *env, jclass clazz, jobject msg,
                                                         jstring local_model_path, jint thread_num,
                                                         jfloat prefetch_size_in_gb,
                                                         jfloat l_size) {
    // TODO: implement startLLamaPrefetch()
}