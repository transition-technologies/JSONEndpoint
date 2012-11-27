package pl.com.tt.play.modules.json;

import play.Play;
import play.PlayPlugin;

import java.util.regex.Pattern;

/**
 * @author Marek Piechut <m.piechut@tt.com.pl>
 */
public class JsonEndpointPlugin extends PlayPlugin {

    public static final String IGNORE_DEFAULT_PATTERN = "^(org\\.hibernate|javax\\.jpa)\\..*";
    public static Pattern CLASS_IGNORE_PATTERN;


    @Override
    public void onConfigurationRead() {
        String regexp = Play.configuration.getProperty("json.endpoint.ignore.class", IGNORE_DEFAULT_PATTERN);
        CLASS_IGNORE_PATTERN = Pattern.compile(regexp);
    }
}
