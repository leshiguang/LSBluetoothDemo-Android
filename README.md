# LSBluetoothDemo-Android Quick Integrate guideline

## 1、Add github repository
```groovy
maven {  
    url "https://maven.pkg.github.com/leshiguang/maven-repository"  
    credentials {  
        username GITHUB_USERNAMNE  
        password GITHUB_TOKEN  
    }  
}
```

- USERNAME: Replace USERNAME with your GitHub username

- TOKEN: Replace TOKEN with your personal access token https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line



## 2、Add bluetooth dependency to your project
```groovy

dependencies {  
    api 'com.lifesense.bluetooth:lifesense-ble-module:1.7.9'  
}  
```

## 3、Initialize

init LSBleManager instance with Application Context
```java

	LsBleManager.getInstance().initialize(getApplicationContext(), "com.leshiguang.saas.rbac.demo.appid");

```

## 4、Api Documents

[android sdk api](Android-docs.zip  "android开发文档")

