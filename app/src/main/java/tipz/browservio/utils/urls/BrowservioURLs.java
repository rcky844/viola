package tipz.browservio.utils.urls;

import tipz.browservio.BuildConfig;

public class BrowservioURLs {
    public static final String prefix = "browservio://";

    public static final String realChangelogUrl = "https://gitlab.com/TipzTeam/browservio/-/releases/"
            .concat(BuildConfig.VERSION_NAME).concat(BuildConfig.VERSION_TECHNICAL_EXTRA);

    public static final String licenseUrl = prefix.concat("license");
    public static final String realLicenseUrl = "file:///android_asset/LICENSE.txt";

    public static final String reloadUrl = prefix.concat("reload");

    public static final String sourceUrl = "https://gitlab.com/TipzTeam/browservio/";
    public static final String feedbackUrl = sourceUrl.concat("-/issues");
}
