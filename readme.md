Android media helper. Help handle media button, audio focus and become noise.

**Helper:**

1. [**`HeadsetHookHelper`**](https://jrfeng.github.io/media-helper/media/helper/HeadsetHookHelper.html)
2. [**`AudioFocusHelper`**](https://jrfeng.github.io/media-helper/media/helper/AudioFocusHelper.html)
3. [**`BecomeNoiseHelper`**](https://jrfeng.github.io/media-helper/media/helper/BecomeNoiseHelper.html)

[**`Document`**](https://jrfeng.github.io/media-helper/)

## How to use

**Step 1**. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

**Step 2**. Add the dependency [![](https://jitpack.io/v/jrfeng/media-helper.svg)](https://jitpack.io/#jrfeng/media-helper)

```gradle
dependencies {
    implementation 'com.github.jrfeng:media-helper:1.0.6'
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