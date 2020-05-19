# LSBluetoothDemo-Android 快速接入
## 1、添加仓库
```groovy
maven {  
    url "https://maven.pkg.github.com/leshiguang/maven-repository"  
    credentials {  
        username GITHUB_USERNAMNE  
        password GITHUB_TOKEN  
    }  
}
```

参数说明：
-  username为接入方申请appid时提交的github profile 名称
-  password为接入方在github生成的token， https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line



## 2、配置gradle插件
- 添加依赖
```groovy
dependencies {
    classpath 'com.android.tools.build:gradle:3.3.1'
    classpath "com.lifesense.android:lifesense-android-service-plugin:0.1.0"
}
```
- 在application工程中应用插件
```groovy
apply plugin: 'com.android.application'
apply plugin: 'lifesense-android-service'
```

## 3、在library||application工程中添加依赖
```groovy

dependencies {  
    api 'com.lifesense.bluetooth:lifesense-ble-module:1.7'  
}  
```

## 4、Api文档

[android 开发接入文档](Android-docs.zip  "android开发文档")
[ios 开发接入文档]("iOS-SDK.zip"  "ios开发文档")
