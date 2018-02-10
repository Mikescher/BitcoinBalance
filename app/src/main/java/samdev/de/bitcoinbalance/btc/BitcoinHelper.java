package samdev.de.bitcoinbalance.btc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class BitcoinHelper {
	private final static String BTC_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

	public static boolean ValidateBitcoinAddress(String addr) {

		if (addr.startsWith("1"))   return ValidateBitcoinAddress_P2PKH(addr);
		if (addr.startsWith("3"))   return ValidateBitcoinAddress_P2SH(addr);
		if (addr.startsWith("bc1")) return ValidateBitcoinAddress_BECH32(addr);

		return false;
	}

	private static boolean ValidateBitcoinAddress_P2PKH(String addr) {
		if (addr.length() < 26 || addr.length() > 35) return false;
		byte[] decoded = DecodeBase58(addr, 58, 25);
		if (decoded == null) return false;

		byte[] hash = Sha256(decoded, 0, 21, 2);

		return hash != null && Arrays.equals(Arrays.copyOfRange(hash, 0, 4), Arrays.copyOfRange(decoded, 21, 25));
	}

	private static boolean ValidateBitcoinAddress_P2SH(String addr) {
		if (addr.length() < 26 || addr.length() > 35) return false;
		byte[] decoded = DecodeBase58(addr, 58, 25);
		if (decoded == null) return false;

		return true;
	}

	private static boolean ValidateBitcoinAddress_BECH32(String addr) {
		return false; // activate when API's support BECH32 addresses
	}

	private static byte[] DecodeBase58(String input, int base, int len) {
		byte[] output = new byte[len];
		for (int i = 0; i < input.length(); i++) {
			char t = input.charAt(i);

			int p = BTC_ALPHABET.indexOf(t);
			if (p == -1)
				return null;
			for (int j = len - 1; j > 0; j--, p /= 256) {
				p += base * (output[j] & 0xFF);
				output[j] = (byte) (p % 256);
			}
			//if (p != 0) return null;
		}

		return output;
	}

	private static byte[] Sha256(byte[] data, int start, int len, int recursion) {
		if (recursion == 0) return data;

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(Arrays.copyOfRange(data, start, start + len));
			return Sha256(md.digest(), 0, 32, recursion - 1);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
}
