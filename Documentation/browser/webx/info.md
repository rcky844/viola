# Information for Bussin WebX

Bussin WebX was an alternate implementation of the World Wide Web developed by [FaceDev](https://github.com/face-hh). It differs from the typical standard with the use of Lua instead of JavaScript. Refer to the [official repo](https://github.com/face-hh/webx) for more information.

The implementation also includes a custom domain name system (DNS), which worked with an API request to https://api.buss.lol/domain with the required domain name. The server responses with the IP address of the actual website which can then be loaded. Alternatively, it was alwo supported to host websites on GitHub, which returns the repo's URL and the path to the HTML file.

The project was officially discontinued in August 2024.

## Implementation status inside Viola Browser

The implementation is located in `app/src/main/java/tipz/viola/webview/buss`.

Prior to the shut down of the DNS, Viola Browser could connect and load most website's HTML content. The load process for a website is as follows:

1. Fetch API for the actual `ip` of a buss URL
2. Download the URL from the `ip` provided
3. Perform modifications to the downloaded HTML
4. Page rendering by WebView

Specifically, the modifications include the addition of default CSS styles, removal of any script tag, and converting important incompatibilities with the HTML implementation to something Chromium WebView understands.

As Lua scripts can cause WebView to not render the page at all, it was necessary to remove all script tags. It could however be improved to only remove Lua scripts but not JavaScript.

In current versions, the available features are as follows:
- [x] HTML rendering
- [x] Basic CSS styles
- [ ] Lua script execution

Since the project and APIs were discontinued weeks after implementation was added to Viola, no more work will be conducted to finalize the implementation. Interested individals may submit their own contributions to finish Lua script support, but no further support will be provided for any issues.
