package tipz.viola.download

enum class DownloadMode(val value: Int) {
    AUTO_DOWNLOAD_PROVIDER(-1), // TODO: Remove
    ANDROID_DOWNLOAD_PROVIDER(0),
    INTERNAL_DOWNLOAD_PROVIDER(1);
}