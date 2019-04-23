# EasyEditText
使用Kotlin编写的多功能EditText。

## 目录
* [功能介绍](#功能介绍)
* [使用方法](#使用方法)
* [自定义布局属性](#自定义布局属性)
* [方法说明](#方法说明)

自定义布局属性

## 功能介绍
- [x] 一键清空内容，可自定义清除图标
- [x] 可设置输入框内容是否为空的监听
- [x] 明暗文形式切换（仅限于输入类型为密码），可自定义切换图标
- [x] 可设置允许输入的最大字符数
- [x] 可设置达到最大字符数的监听事件
- [x] 达到最大字符数时，并可弹出默认或自定义的Toast
- [x] 可自定义达到最大字符数时是否限制输入
- [x] 输入内容监听事件

## 使用方法

在xml布局中添加：
```xml
    <com.lindroid.view.EasyEditText
        android:id="@+id/editText"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:hint="@string/hint_input"
        android:inputType="textPassword"
        app:showClearButton="false"
        app:clearContentIcon="@drawable/ic_eet_clear"
        app:showPlainCipherToggle="true"
        app:cipherTextIcon="@drawable/ic_eet_content_cipher"
        app:plainTextIcon="@drawable/ic_eet_content_plain"
        app:maxCharacters="140"
        app:showMaxCharsAlertToast="true"
        app:maxCharsAlertWithCount="最大输入字数为d%"
        app:maxCharsAlert="你已超过最大限制字数"
        app:maxCharsLimited="true"
        />
```
详细说明请参考下面的[自定义布局属性](#自定义布局属性)

## 自定义布局属性
| 属性 | 作用 | 默认值 | 备注 |
| ------------ | ------------ | :------------: | :------------: |
| showClearButton | 是否显示一键清空按钮 | false | / |
| clearContentIcon | 设置一键清空内容图标 | R.drawable.ic_eet_clear | / |
| showPlainCipherToggle | 是否显示明暗文切换按钮  | false | showClearButton为true时不起作用 |
| plainTextIcon | 设置输入框内容为明文时的按钮图标 | R.drawable.ic_eet_content_plain | / |
| cipherTextIcon | 设置输入框内容为明文时的按钮图标 | R.drawable.ic_eet_content_cipher | / |
| maxCharacters | 设置输入最大字符数 | -1 | 小于或等于0表示不做限制 |
| showMaxCharsAlertToast | 超出最大字符输入数时是否弹出Toast | false | /  |
| maxCharsAlertWithCount | 超出最大字符输入数时的提示文字，包含字数 | 空字符 | 必须是String.format格式 |
| maxCharsAlert | 超出最大字符输入数时的提示文字 | 空字符  | maxCharsAlertWithCount不为空字符时不起作用 |
| maxCharsLimited | 达到最大输入字符数时是否限制输入 | true | / |


### 注意（必读）
- 一键清空功能的优先级别高于明暗文切换功能，如果显示了一键清空按钮，则无法显示明暗文切换按钮。
- 明暗文切换功能必须满足以下两点：
  1. 没有设置一键清空功能；
  2. `inputType`类型为密码类型。
- 如果设置了达到最大输入字符数时弹出Toast但没有设置Toast的内容，则使用String.format形式拼接的"请勿超过%d字"(%d为maxCharacters)，如果同时设置了`maxCharsAlertWithCount`和`maxCharsAlert`，则前者优先起作用。

## 方法说明

### 属性
自定义布局属性列表中的布局属性在Kotlin中均有对应的属性，在Java中均有对于的Getter和Setter方法，这里就不一一列举了。

### 监听事件
`EasyEditText`提供如下常规的监听事件：

```java
        /*
          监听输入框内容是否为空
         */
        editText.setEmptyChangeListener(new EasyEditText.OnEmptyChangeListener() {
            @Override
            public void onEmpty(boolean isEmpty) {
                //输入框内容从有到无或从无到有会回调此方法，如果已经有内容，再继续输入的话则不会调用此方法
            }
        });
        /*
          监听是否达到了最大输入字符数
         */
        editText.setMaxCharsListener(new EasyEditText.OnMaxCharactersListener() {
            @Override
            /*
             * @param maxChars:最大输入字符数
             * @param alertText:超过最大输入字符数时的提示文字
             */
            public void onMaxChars(int maxChars, @NonNull String alertText) {

            }
        });
        /*
          监听输入框内容变化
         */
        editText.setOnContentChangeListener(new EasyEditText.OnContentChangeListener() {
            @Override
            /*
             * @param content:文本内容
             * @param count:当前的文本长度
             */
            public void onChanged(@NonNull CharSequence content, int count) {

            }
        });
```
如果你想使用`EditText`的`TextWatcher`中的三个回调，那么这里也可以单独使用以下三个监听事件，它的参数跟原生的保持一致：

```java
        /*
          设置文本改变前的监听事件
         */
        editText.setBeforeTextChangeListener(new EasyEditText.BeforeTextChangeListener() {
            @Override
            public void onBefore(@NonNull CharSequence s, int start, int count, int after) {

            }
        });
        /*
          设置文本改变监听事件
         */
        editText.setOnTextChangeListener(new EasyEditText.OnTextChangeListener() {
            @Override
            public void onChange(@NonNull CharSequence s, int start, int before, int count) {

            }
        });
        /*
          设置文本改变后的监听
         */
        editText.setAfterTextChangeListener(new EasyEditText.AfterTextChangeListener() {
            @Override
            public void onAfter(Editable s) {

            }
        });
```
