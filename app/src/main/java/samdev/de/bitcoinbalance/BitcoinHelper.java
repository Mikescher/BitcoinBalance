package samdev.de.bitcoinbalance;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mike on 08.11.2015.
 */
public class BitcoinHelper {

    private static Pattern PATTERN_ADDRESS = Pattern.compile("^[A-Za-z0-9]{26}([A-Za-z0-9]?){9}$");

    public static boolean isBitcoinAdress(String addr) {
        return PATTERN_ADDRESS.matcher(addr).matches();
    }
}
