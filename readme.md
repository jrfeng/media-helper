Android 多媒体工具，用于帮助处理耳机的 Headset Hook 点击事件（实现线控播放）、处理音频焦点的丢失与获取和帮助处理 become noise 事件，并用于帮助扫描本地媒体项目。

**帮助类:**

1. [**`HeadsetHookHelper`**](https://jrfeng.github.io/media-helper/media/helper/HeadsetHookHelper.html)：用于帮助处理耳机的 Headset Hook 点击事件（实现线控播放）。
2. [**`AudioFocusHelper`**](https://jrfeng.github.io/media-helper/media/helper/AudioFocusHelper.html)：用于帮助处理音频焦点的丢失与获取事件。
3. [**`BecomeNoiseHelper`**](https://jrfeng.github.io/media-helper/media/helper/BecomeNoiseHelper.html)：用于帮助处理 become noise 事件.
4. [**`MediaStoreHelper`**](https://jrfeng.github.io/media-helper/media/helper/MediaStoreHelper.html)：用于帮助扫描本地媒体项目.

[**`Document`**](https://jrfeng.github.io/media-helper/)

## 项目配置

**第 1 步**：在你的项目的根目录下的 build.gradle 文件中添加以下配置：

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

**第 2 步**：添加依赖 [![](https://jitpack.io/v/jrfeng/media-helper.svg)](https://jitpack.io/#jrfeng/media-helper)

```gradle
dependencies {
    implementation 'com.github.jrfeng:media-helper:1.0.7'
}
```

## LICENSE

```
MIT License

Copyright (c) 2020 jrfeng

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```