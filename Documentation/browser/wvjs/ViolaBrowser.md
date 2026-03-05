# JavaScript Interface: `ViolaBrowser`
This is the basic JavaScript Interface provided to web scripts for simple interactions with Viola Browser.

## API reference
### getProductVersion
```
function getProductVersion()
```
Gets the version of the current browser.

| Returns | Description |
| :-----: | :---------- |
| `String` | Browser version. |

### getProductBuildTag
```
function getProductBuildTag()
```
Gets the full build tag of the current browser.

| Returns | Description |
| :-----: | :---------- |
| `String` | Browser full build tag. |

### getBase64FromBlobData
```
function getBase64FromBlobData(downloadPath, uriString, mimeType)
```
Internal function for downloading blob data to file. Developers should not call this function.

| Parameters | Description |
| :--------: | :---------- |
| `downloadPath` | `String`: Path to a local folder to download the blob |
| `uriString` | `String`: URI string prefixed with `blob:` with data |
| `mimeType` | `String`: Mime type of the file for downloading |

