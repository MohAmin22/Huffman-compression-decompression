public class Main {
    public static void main(String[] args) {
        /*
        I acknowledge that I am aware of the academic integrity guidelines of this course,
        and that I worked on this assignment independently without any unauthorized help
         */
        String mode = args[0];
        String inputPath = args[1];
        int numberOfBytesPerWord = args.length == 3 ? Integer.parseInt(args[2]) : 0;
        System.out.println(mode + " " + inputPath + " " + numberOfBytesPerWord);
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