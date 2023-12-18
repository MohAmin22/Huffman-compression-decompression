import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Decompression {
    private String inputPath;
    private int numberOfBytesPerWord;
    private byte[] lastByteArray;

    public void decompress(String inputPath) {
        try {
            this.setInput(inputPath);
            this.deCompressFile();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void setInput(String inputPath) throws Exception {
        this.inputPath = inputPath;
    }
    private BitInputStreamInf createBitInputStream() throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(inputPath);
        return new BitInputStream(inputStream);
    }
    private byte[] extractLastByteArray(BitInputStreamInf bitInputStream){
        int lastByteArraySize = bitInputStream.readInt();
        return bitInputStream.readNBytes(lastByteArraySize);
    }
    private Node extractHuffmanTree(BitInputStreamInf bitInputStream) {
        return new Node(null, 1L, null, null);
    }
    private String getOutputPath() { // edited here to restore the original name of the file
        String inputFileName = this.inputPath.substring(this.inputPath.lastIndexOf(File.separatorChar) + 1);
        String outputFileName = "20011502" + "." + this.numberOfBytesPerWord + "." + inputFileName + "." + "hc";
        return this.inputPath.substring(0, this.inputPath.lastIndexOf('/') + 1) + outputFileName;
    }
    private BitOutputStream createBitOutputStream() throws FileNotFoundException {
        String outputPath = getOutputPath();
        FileOutputStream fos = new FileOutputStream(outputPath);
        return new BitOutputStream(fos);
    }
    private void decode(BitInputStreamInf bitInputStream, Node root) throws FileNotFoundException {
        BitOutputStream bitOutputStream = createBitOutputStream();

    }
    private void deCompressFile() throws FileNotFoundException {
        BitInputStreamInf bitInputStream = createBitInputStream();
        // Extract number of bytes per word
        numberOfBytesPerWord = bitInputStream.readInt();
        // Extract the last byte[]
        lastByteArray = extractLastByteArray(bitInputStream);
        // Extract huffman tree
        Node root = extractHuffmanTree(bitInputStream);
        // Decompress and Decode the compressed file
        decode(bitInputStream, root);
    }



}
