class Config {
    static gradle_version = '4.1.1'
    static kotlin_version = '1.4.0'
    static navigation_version = '2.3.0'
    static android_maven_gradle_plugin = '2.1'

    static classpath = [
            gradle      : "com.android.tools.build:gradle:$gradle_version",
            kotlin      : "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version",
            androidMaven: "com.github.dcendents:android-maven-gradle-plugin:$android_maven_gradle_plugin",
    ]

    /* 组件 */
    static FPhoenixCorneaE = [
            CommonUtil    : "com.github.FPhoenixCorneaE:CommonUtil:1.0.1",
            CommonTitlebar: 'com.github.FPhoenixCorneaE:CommonTitlebar:1.0.2',
    ]

    /** Android */
    static android = [
            compileSdkVersion: 30,
            buildToolsVersion: "30.0.1",
            minSdkVersion    : 21,
            targetSdkVersion : 30,
            versionCode      : 103,
            versionName      : "1.0.3"
    ]

    /** Supports */
    static support = [
            appcompat       : "androidx.appcompat:appcompat:1.2.0",
            constraintLayout: "androidx.constraintlayout:constraintlayout:2.0.2",
    ]

    /** navigation 扩展插件 */
    static navigation = [
            common  : "androidx.navigation:navigation-common-ktx:$navigation_version",
            fragment: "androidx.navigation:navigation-fragment-ktx:$navigation_version",
            runtime : "androidx.navigation:navigation-runtime-ktx:$navigation_version",
            ui      : "androidx.navigation:navigation-ui-ktx:$navigation_version",
    ]

    /** Kotlin */
    static kotlin = [
            core            : "androidx.core:core-ktx:1.3.2",
            activity        : "androidx.activity:activity-ktx:1.2.0-alpha02",
            fragment        : "androidx.fragment:fragment-ktx:1.3.0-alpha04",
            kotlinStdlibJdk7: "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version",
            coroutines      : 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.2',
    ]

    /** RxJava2 */
    static rxJava2 = [
            rxJava   : "io.reactivex.rxjava2:rxjava:2.2.0",
            rxAndroid: "io.reactivex.rxjava2:rxandroid:2.1.0",
            rxKotlin : "io.reactivex.rxjava2:rxkotlin:2.2.0",
    ]

    /** logger */
    static logger = "com.orhanobut:logger:2.2.0"
    /** 权限管理 */
    static rxPermissions = 'com.github.tbruyelle:rxpermissions:0.10.2'
    /** Gson */
    static gson = 'com.google.code.gson:gson:2.8.5'
    /** Kotlin Coroutines Image Loader */
    static coil = 'io.coil-kt:coil:0.13.0'


    static supportLibs = support.values()
    static navigationLibs = navigation.values()
    static kotlinLibs = kotlin.values()
    static rxJava2Libs = rxJava2.values()
}