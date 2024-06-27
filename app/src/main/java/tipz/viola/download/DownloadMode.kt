package tipz.viola.download

enum class DownloadMode(val value: Int) {
    AUTO_DOWNLOAD_PROVIDER(0),
    ANDROID_DOWNLOAD_PROVIDER(1),
    INTERNAL_DOWNLOAD_PROVIDER(2);
}