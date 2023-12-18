public class Main {
    public static void main(String[] args) {
        String mode = args[0];
        String inputPath = args[1];
        int numberOfBytesPerWord = args.length == 3 ? Integer.parseInt(args[2]) : 0;
        System.out.println(mode + " " + inputPath + " " + numberOfBytesPerWord);
//        try {
//            new Test().testIntegerToByteArray();
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
        try {
            if (mode.equalsIgnoreCase("c")) {
                new Compression(numberOfBytesPerWord).compress(inputPath);
            } else if (mode.equalsIgnoreCase("d")) {
                new Decompression().decompress(inputPath);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}