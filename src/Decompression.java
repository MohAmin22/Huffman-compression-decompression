import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Decompression {
    private String inputPath;
    private int numberOfBytesPerWord;
    private byte[] lastByteArray;
    private long numberOfbitsWritten = 0;


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

    private BitInputStreamInf createBitInputStream() throws IOException {
        FileInputStream inputStream = new FileInputStream(inputPath);
        return new BitInputStream(inputStream);
    }

    private void extractLastByteArray(BitInputStreamInf bitInputStream) throws IOException {
        bitInputStream.fetch();
        int lastByteArraySize = bitInputStream.readInt();
        lastByteArray = bitInputStream.readNBytes(lastByteArraySize);
    }

//    private Node extractHuffmanTree(BitInputStreamInf bitInputStream) throws IOException {
//        // TODO : handle status value
//        if (bitInputStream.fetch() != -1) {
//            boolean isInternal = bitInputStream.readBit();
//            if (!isInternal) {
//                return new Node(bitInputStream.readNBytes(numberOfBytesPerWord), 0L, null, null);
//            } else {
//                Node left = extractHuffmanTree(bitInputStream);
//                Node right = extractHuffmanTree(bitInputStream);
//                return new Node(null, 0L, left, right);
//            }
//        }
//    }

    private Node extractHuffmanTree(BitInputStreamInf bitInputStream) throws IOException {
        // TODO : handle status value
        bitInputStream.fetch();
        boolean isInternal = bitInputStream.readBit();
        if (!isInternal) {
            return new Node(bitInputStream.readNBytes(numberOfBytesPerWord), 0L, null, null);
        } else {
            Node left = extractHuffmanTree(bitInputStream);
            Node right = extractHuffmanTree(bitInputStream);
            return new Node(null, 0L, left, right);
        }
    }

    private String getOutputPath() { // edited here to restore the original name of the file
        String inputFileName = this.inputPath.substring(this.inputPath.lastIndexOf(File.separatorChar) + 1);
        int indexOfSecondDot = inputFileName.indexOf('.');
        indexOfSecondDot = inputFileName.indexOf('.', indexOfSecondDot + 1);
        int indexOfLastDot = inputFileName.lastIndexOf('.');
        String outputFileName = inputFileName.substring(indexOfSecondDot + 1, indexOfLastDot);
        return this.inputPath.substring(0, this.inputPath.lastIndexOf(File.separatorChar) + 1) + "extracted" + '.' + outputFileName;
    }

    private BitOutputStream createBitOutputStream() throws FileNotFoundException {
        String outputPath = getOutputPath();
        FileOutputStream fos = new FileOutputStream(outputPath);
        return new BitOutputStream(fos);
    }

    private void decode(BitInputStreamInf bitInputStream, Node root, Node currentNode) throws IOException {
        BitOutputStream bitOutputStream = createBitOutputStream();
        while (bitInputStream.fetch() != -1) {
            if (currentNode.isLeaf()) {
                bitOutputStream.writeByteArray(currentNode.getWord());
                currentNode = root;
            } else {
                boolean bit = bitInputStream.readBit();
                if (bit) {
                    currentNode = currentNode.getRight();
                } else {
                    currentNode = currentNode.getLeft();
                }
            }
        }
        bitOutputStream.endWriting();
        bitOutputStream.close();
    }

    //
    void readBitSequence(BitInputStreamInf bitInputStream) throws IOException {
        while (bitInputStream.fetch() != -1) {
            for (int i = 0; i < 8; i++) {
                System.out.print((bitInputStream.readBit() ? '1' : '0'));
            }
            System.out.println();
        }
    }

    private void extractNumberOfBytesPerWord(BitInputStreamInf bitInputStream) throws IOException {
        bitInputStream.fetch();
        numberOfBytesPerWord = bitInputStream.readInt();
    }

    private void extractNumberOfBitsWritten(BitInputStreamInf bitInputStream) throws IOException {
        bitInputStream.fetch();
        numberOfbitsWritten = bitInputStream.readLong();
        bitInputStream.setGetNumberOfBitsWrittenInCompressedFile(numberOfbitsWritten);
    }

    private void deCompressFile() throws IOException {
        BitInputStreamInf bitInputStream = createBitInputStream();
        // Extract number of bits written
        extractNumberOfBitsWritten(bitInputStream);
        // Extract number of bytes per word
        extractNumberOfBytesPerWord(bitInputStream);
        // Extract the last byte[]
        extractLastByteArray(bitInputStream);
        // Extract huffman tree
        Node root = extractHuffmanTree(bitInputStream);

        //readBitSequence(bitInputStream);

        // Decompress and Decode the compressed file
        decode(bitInputStream, root, root);
    }


}
