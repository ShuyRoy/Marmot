# Marmot开发文档

开发分为两部分：Java负责与用户交互及UI；CPP(LLama)负责模型推理等功能。

## Java与LLama的协同

LLama原本由llama/examples/main/main.cpp main()在控制台交互，所以Marmot在实现时，直接将LLama的该main()作为一个线程启动(main()改名为run_llama())，则Java只需要将用户在文本框中的输入传递给该LLama线程，推理的结果再传递给Java即可完成一次对话。

具体来说，两者的协同是由Java中的类NativeMessageReceiver和CPP中的Semaphore来实现的。Java中有两个线程： 输入线程和输出线程，CPP中有一个LLama线程。输入线程接收用户输入传递给LLama线程，LLama线程推理完将结果传递给输出线程，输出线程将结果显示在输出框里。
其中，输入线程和LLama线程之间通过CPP中的Semaphore进行同步，输出线程和LLama线程之间通过NativeMessageReceiver进行同步和结果的传递。
1、输入前，LLama线程阻塞于Semaphore，输入后，输入线程唤醒Semaphore，并将输入的字符串传递给LLama线程进行推理。
2、得到推理结果之前，输出线程阻塞于NativeMessageReceiver，LLama线程推理结束后，唤醒阻塞于NativeMessageReceiver的输出线程，并通过NativeMessageReceiver将结果传递给输出线程进行展示。

注：由于Android安全框架的问题，Java若要调用CPP代码，CPP的LLama代码需要编译为动态库(.so)，并通过JNI机制调用CPP相关函数，编译方法参考llama/examples/main/CMakeLists.txt。

## 模型的增删

模型的下载删除等功能由类ModelOperation负责实现。
其中ModelOperaton.getAllSupportModels() 用于获取当前支持的模型：期望将模型名称，下载链接等信息以JSON数据的形式放在线上，该接口在线获取支持的模型，进行后续下载等操作（源码中已给出JSON数据格式样式以及获取JSON数据的接口）。

## 其他功能

略
