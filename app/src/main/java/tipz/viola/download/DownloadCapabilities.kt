package tipz.viola.download

enum class DownloadCapabilities(val value: String) {
    PROTOCOL_HTTP("http"), /* http */
    PROTOCOL_HTTPS("https"), /* https */
    PROTOCOL_FILE("file"), /* file */
    PROTOCOL_FTP("ftp"), /* ftp */
    PROTOCOL_DATA("data"), /* data */
    PROTOCOL_BLOB("blob");  /* blob */

    /* Helper functions */
    companion object {
        fun fromString(value: String) = entries.first { it.value == value }
    }
}