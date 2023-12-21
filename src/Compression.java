import java.io.*;
import java.util.*;

public class Compression {
    private String inputPath;
    private final int numberOfBytesPerWord;
    private long numberOfBitsWritten = -1;
    private byte[] lastByteArray;
    private final int MAX_CAPACITY; // Size of input buffer

    public Compression(int numberOfBytesPerWord) {
        this.numberOfBytesPerWord = numberOfBytesPerWord;
        this.MAX_CAPACITY = numberOfBytesPerWord * 64 * 1024; // MAX_CAPACITY has to be multiplier of numberOfBytesPerWord
    } //5000000

    public void compress(String inputPath) {
        try {
            long startTime = System.currentTimeMillis();
            this.setInput(inputPath);
            this.compressFile();
            long endTime = System.currentTimeMillis();
            System.out.println(Utility.getYELLOW() +
                    "The Compression is done in : " + (endTime - startTime) / 1000 + "  second(s)"
                    + Utility.getRESET()
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void setInput(String inputPath) {
        this.inputPath = inputPath;
    }

    private BufferedInputStream createInputStream() throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(inputPath);
        return new BufferedInputStream(inputStream);
    }

    private Map<ByteArrayWrapper, Long> constructFrequencyMap() throws IOException {
        // Create input stream
        BufferedInputStream bufferedInputStream = createInputStream();
        Map<ByteArrayWrapper, Long> frequencyTable = new HashMap<>();
        //read byte[numberOfBytesPerWord] from input
        byte[] buffer = new byte[MAX_CAPACITY];
        //byte[] buffer = new byte[numberOfBytesPerWord];
        int bytesRead;
        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
            int start = 0, end = numberOfBytesPerWord - 1;
            while (end < bytesRead) {
                byte[] currentBuffer = Utility.copyByteArray(buffer, start, end + 1); // To enforce mutability of map keys
                frequencyTable.put(new ByteArrayWrapper(currentBuffer),
                        frequencyTable.getOrDefault(new ByteArrayWrapper(currentBuffer), 0L) + 1L);
                start = end + 1;
                end += numberOfBytesPerWord;
            }
            if (end < MAX_CAPACITY) {  // if it has gone out of the prev while and the end is less than MAX_CAPACITY than it is the last word
                this.lastByteArray = Utility.copyByteArray(buffer, start, bytesRead); // lastIndexOfInCompleteWord = bytesRead - 1;
            }
        }
        bufferedInputStream.close();
        return frequencyTable;
    }

    private PriorityQueue<Node> convertFrequencyTableMapToPriorityQueue(Map<ByteArrayWrapper, Long> frequencyTable) {
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingLong(Node::getFrequency));
        for (Map.Entry<ByteArrayWrapper, Long> entry : frequencyTable.entrySet()) {
            queue.add(new Node(entry.getKey().getBuffer(), entry.getValue()));
        }
        return queue;
    }

    // input : queue of word-frequency pairs
    private Node constructHuffmanTree(PriorityQueue<Node> queue) {
        while (queue.size() > 1) {
            Node left = queue.poll();
            Node right = queue.poll();
            assert right != null; // Throws an Exception when the file is so small that fits in 1 or less byte[n]
            Node parent = new Node(null, left.getFrequency() + right.getFrequency(), left, right);
            queue.add(parent);
        }
        return queue.poll();
    }

    /*
     *  input : root of huffman tree
     *  output : huffman table in ArrayList form
     */
    private Map<ByteArrayWrapper, HuffmanCode> constructHuffmanTable(Node root) {
        Map<ByteArrayWrapper, HuffmanCode> huffmanTable = new HashMap<>();
        buildHuffmanTable(huffmanTable, new BitSet(), 0, root); // the root at level 0
        return huffmanTable;
    }

    private void buildHuffmanTable(Map<ByteArrayWrapper, HuffmanCode> huffmanTable, BitSet code, int level, Node node) {
        if (node.getLeft() == null && node.getRight() == null) {
            BitSet finalCode = (BitSet) code.clone();
            // Clear bits that are not part of the current code, as I just concerned about the previous levels(nodes) only
            if(level <= code.length()) finalCode.clear(level, code.length());
            huffmanTable.put(new ByteArrayWrapper(node.getWord()), new HuffmanCode(finalCode, level));
        }
        if (node.getLeft() != null) {
            code.set(level, false); // set the bit at this level to 0
            buildHuffmanTable(huffmanTable, code, level + 1, node.getLeft());
        }
        if (node.getRight() != null) {
            code.set(level, true);
            buildHuffmanTable(huffmanTable, code, level + 1, node.getRight());
        }
    }

    private void storeLastByteArray(BitOutputStream bitOutputStream) {
        if (this.lastByteArray != null) {
            bitOutputStream.writeInt(this.lastByteArray.length);
            bitOutputStream.writeByteArray(this.lastByteArray);
        } else {
            bitOutputStream.writeInt(0);
        }
    }

    private String getOutputPath() {
        String inputFileName = this.inputPath.substring(this.inputPath.lastIndexOf(File.separatorChar) + 1);
        String outputFileName = "20011502" + "." + this.numberOfBytesPerWord + "." + inputFileName + "." + "hc";
        return this.inputPath.substring(0, this.inputPath.lastIndexOf(File.separatorChar) + 1) + outputFileName;
    }

    private BitOutputStream createBitOutputStream() throws FileNotFoundException {
        String outputPath = getOutputPath();
        FileOutputStream fos = new FileOutputStream(outputPath);
        return new BitOutputStream(fos);
    }

    private void storeHuffmanTree(Node root, BitOutputStream bitOutputStream) {
        if (root.isLeaf()) {
            bitOutputStream.writeBit(false);
            bitOutputStream.writeByteArray(root.getWord());
        } else if (root.hasLeftChild() && root.hasRightChild()) {
            bitOutputStream.writeBit(true);
            storeHuffmanTree(root.getLeft(), bitOutputStream);
            storeHuffmanTree(root.getRight(), bitOutputStream);
        } else {
            System.out.println("Invalid huffman tree to save");
            System.exit(100);
        }
    }

    private void storeNumberOfBytesPerWord(BitOutputStream bitOutputStream) {
        bitOutputStream.writeInt(this.numberOfBytesPerWord);
    }

    private void encodeAndCompress(BitOutputStream bitOutputStream, Map<ByteArrayWrapper, HuffmanCode> huffmanTable) throws IOException {
        BufferedInputStream bufferedInputStream = createInputStream();
        try {
            //read byte[numberOfBytesPerWord] from input
            byte[] buffer = new byte[MAX_CAPACITY];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) { // bytesRead in the last byte group may be n or less
                int start = 0, end = numberOfBytesPerWord - 1;
                while (end < bytesRead) {
                    byte[] currentBuffer = Utility.copyByteArray(buffer, start, end + 1);
                    // Find the code of the current word
                    HuffmanCode code = huffmanTable.get(new ByteArrayWrapper(currentBuffer));
                    // Write the code to the output file
                    for (int i = 0; i < code.getCodeLength(); i++) {
                        bitOutputStream.writeBit(code.getBit(i));
                    }
                    start = end + 1;
                    end += numberOfBytesPerWord;
                }
            } // I don't need to ignore the last byte group encoding as it will break the loop the next iteration
            bitOutputStream.endWriting();
            numberOfBitsWritten = bitOutputStream.getNumberOfBitsWritten();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            bufferedInputStream.close();
            bitOutputStream.close();
        }
    }
    private long huffmanTreeSize(Node root) {
        if (root == null) return 0;
        if (root.getLeft() == null && root.getRight() == null) return 1;
        return 1 + huffmanTreeSize(root.getLeft()) + huffmanTreeSize(root.getRight());
    }

    private void storeNumberOfBitsWritten() throws IOException {
        String outputPath = getOutputPath();
        RandomAccessFile raf = new RandomAccessFile(outputPath, "rw");
        raf.seek(0);
        raf.writeLong(numberOfBitsWritten);
        raf.close();
    }

    private void printCompressionRatio() {
        File inputFile = new File(inputPath);
        File outputFile = new File(getOutputPath());
        String compressionRatio = String.format("%.3f", ((double) outputFile.length() / inputFile.length()));
        System.out.println(Utility.getRED() +
                "Compression ratio : " + compressionRatio
                + Utility.getRESET()
        );
    }

    private void compressFile() throws IOException {
        // Collect statistics
        Map<ByteArrayWrapper, Long> frequencyTable = constructFrequencyMap();
        if (frequencyTable.isEmpty()) System.exit(22);

        System.out.println(Utility.getCYAN()
                + "Frequency map size: " + frequencyTable.size()
                + Utility.getRESET()
        );

        // Construct huffman tree
        PriorityQueue<Node> queue = convertFrequencyTableMapToPriorityQueue(frequencyTable);
        Node root = constructHuffmanTree(queue);

        System.out.println(Utility.getGREEN()
                + "Huffman Tree size : " + huffmanTreeSize(root)
                + Utility.getRESET()
        );

        BitOutputStream bitOutputStream = createBitOutputStream();
        // Preserve a place for number of bits written
        bitOutputStream.writeLong(numberOfBitsWritten);
        // Store number of bytes per word (n)
        storeNumberOfBytesPerWord(bitOutputStream);
        // Store last byte[] information
        storeLastByteArray(bitOutputStream);
        // Store huffman tree in the file
        storeHuffmanTree(root, bitOutputStream);
        // Extract huffman table ( word -> code )
        Map<ByteArrayWrapper, HuffmanCode> huffmanTable = constructHuffmanTable(root);
        // Encode and compress the file
        encodeAndCompress(bitOutputStream, huffmanTable);
        // Store the number of bits written in the beginning of the file
        System.out.println(Utility.getBLUE() +
                "Number of bits written: " + numberOfBitsWritten
                + Utility.getRESET()
        );
        storeNumberOfBitsWritten();
        printCompressionRatio();
    }


}
