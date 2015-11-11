package samdev.de.bitcoinbalance.btc;

/**
 * Created by Mike on 11.11.2015.
 */
public class BitcoinAddress {
    public final String address;

    public int balance = -1;

    public BitcoinAddress(String addr) {
        address = addr;
    }

}
