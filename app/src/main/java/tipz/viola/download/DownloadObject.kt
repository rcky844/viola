package tipz.viola.download

import tipz.viola.utils.CommonUtils

class DownloadObject {
    /* DownloadClient specifics */
    internal var taskId = 0
    internal var downloadMode = 0

    /* Download task specifics */
    var uriString = CommonUtils.EMPTY_STRING
    var filename: String? = null
    var contentDisposition: String? = null
    var mimeType: String? = null
    var requestUrl: String? = null

    /* Helper functions / features */
    internal fun getUriProtocol() = uriString.substringBefore(':')
    internal fun compareUriProtocol(value: String) : Boolean = getUriProtocol() == value
}
