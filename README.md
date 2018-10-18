# DownloadManagerHelperSDK
DownloadManager帮助类自动下载url对应的apk并安装，兼容android 6.0、android 8.0

引用方法：

    repositories {
        maven {
            //指定Github上SDK项目的路径
            url "https://github.com/tonightstudio/DownloadManagerHelperSDK/raw/master"
        }
    }
    dependencies {
        implementation 'com.github.tonightstudio:downloadmanagerhelper:1.0'
    }

使用方法：

    DownloadManagerHelper mdownloadManagerHelper = new DownloadManagerHelper(this);

    mdownloadManagerHelper.enqueue("http://192.168.0.226/app-release.apk");
