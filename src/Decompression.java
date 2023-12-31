import java.io.*;

public class Decompression {
    private String inputPath;
    private int numberOfBytesPerWord;
    private byte[] lastByteArray;


    public void decompress(String inputPath) {
        try {
            long startTime = System.currentTimeMillis();
            this.setInput(inputPath);
            this.deCompressFile();
            long endTime = System.currentTimeMillis();
            System.out.println(Utility.getYELLOW() +
                    "The DeCompression is done in : " + (endTime - startTime) / 1000 + "  second(s)" +
                    Utility.getRESET()
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void setInput(String inputPath) {
        this.inputPath = inputPath;
    }

    private IBitInputStream createBitInputStream() throws IOException {
        FileInputStream inputStream = new FileInputStream(inputPath);
        return new BitInputStream(inputStream);
    }

    private void extractLastByteArray(IBitInputStream bitInputStream) throws IOException {
        bitInputStream.fetch();
        int lastByteArraySize = bitInputStream.readInt();
        lastByteArray = bitInputStream.readNBytes(lastByteArraySize);
    }

    private Node extractHuffmanTree(IBitInputStream bitInputStream) throws IOException {
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

    private void decode(IBitInputStream bitInputStream, Node root, Node currentNode) throws IOException {
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
        if(currentNode.isLeaf() && bitInputStream.getOperationCounter() == 0) {
            bitOutputStream.writeByteArray(currentNode.getWord());
        }
        // Store the Last byte[] before closing the stream
        storeLastByteArray(bitOutputStream);
        bitOutputStream.endWriting();
        bitOutputStream.close();
    }

    private void extractNumberOfBytesPerWord(IBitInputStream bitInputStream) throws IOException {
        bitInputStream.fetch();
        numberOfBytesPerWord = bitInputStream.readInt();
    }

    private void extractNumberOfBitsWritten(IBitInputStream bitInputStream) throws IOException {
        bitInputStream.fetch();
        long numberOfBitsWritten = bitInputStream.readLong();
        bitInputStream.setGetNumberOfBitsWrittenInCompressedFile(numberOfBitsWritten);
    }

    private void storeLastByteArray(BitOutputStream bitOutputStream) {
        if (lastByteArray != null) {
            bitOutputStream.writeByteArray(lastByteArray);
        }
    }
    private long huffmanTreeSize(Node root) {
        if (root == null) return 0;
        if (root.getLeft() == null && root.getRight() == null) return 1;
        return 1 + huffmanTreeSize(root.getLeft()) + huffmanTreeSize(root.getRight());
    }


    private void deCompressFile() throws IOException {
        IBitInputStream bitInputStream = createBitInputStream();
        // Extract number of bits written
        extractNumberOfBitsWritten(bitInputStream);
        System.out.println(Utility.getCYAN() +
                "Number of bits written: " + bitInputStream.getNumberOfBitsWrittenInCompressedFile() +
                Utility.getRESET()
        );
        // Extract number of bytes per word
        extractNumberOfBytesPerWord(bitInputStream);
        System.out.println(Utility.getMAGENTA() +
                "Number of bytes per word: " + numberOfBytesPerWord +
                Utility.getRESET()
        );
        // Extract the last byte[]
        extractLastByteArray(bitInputStream);
        // Extract huffman tree
        Node root = extractHuffmanTree(bitInputStream);
        System.out.println(Utility.getRED() +
                "Huffman Tree size : " + huffmanTreeSize(root) +
                Utility.getRESET()
        );
        // Decompress and Decode the compressed file
        decode(bitInputStream, root, root);
    }
}
