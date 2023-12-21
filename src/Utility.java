public class Utility {
    // Manual copy to increase performance as System.arraycopy is in small (N) That is more frequent to be
    public static byte[] copyByteArray(byte[] original, int from, int to) { // Inclusive start - Exclusive end
        int length = to - from;
        byte[] copy = new byte[length];
        if (length > 1000) {
            System.arraycopy(original, from, copy, 0, length);
        } else {
            for (int i = 0; i < length; i++) {
                copy[i] = original[from + i];
            }
        }
        return copy;
    }

    public static String getRED() {
        return "\u001b[31m";
    }

    public static String getGREEN() {
        return "\u001b[32m";
    }

    public static String getRESET() {
        return "\u001b[0m";
    }

    public static String getYELLOW() {
        return "\u001b[33m";
    }

    public static String getBLUE() {
        return "\u001b[34m";
    }

    public static String getMAGENTA() {
        return "\u001b[35m";
    }

    public static String getCYAN() {
        return "\u001b[36m";
    }
}
