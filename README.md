# android-translate-plugin
一个自动化翻译插件，将字符串资源（汉字）转为英文的插件



## 集成

```java
plugins {
    id "com.fzm.translate.plugin" version "1.0.1"
}
```

## 使用

```groovy
./gradlew translate
```

## 结果

执行成功后会在values-en目录生成对应的英文资源文件


## 预览

- values中所有命名包含string的字符串资源文件

  ![pic](https://github.com/xiaobaoyihao/android-translate-plugin/blob/master/pic.png)

- values-en目录的strings.xml文件内容

  ![执行后生成的文件](https://github.com/xiaobaoyihao/android-translate-plugin/blob/master/pic2.png)
