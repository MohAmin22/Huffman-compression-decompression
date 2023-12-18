import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Compression {
    private String inputPath;
    private int numberOfBytesPerWord;
    private long fileSizeInBytes;
    private byte[] lastByteArray;
    //
    private final int MAX_CAPACITY; // Size of input buffer

    public Compression(int numberOfBytesPerWord) {
        this.numberOfBytesPerWord = numberOfBytesPerWord;
        this.MAX_CAPACITY = numberOfBytesPerWord * 50000; // MAX_CAPACITY has to be multiplier of numberOfBytesPerWord
    }

    public void compress(String inputPath) {
        try {
            this.setInput(inputPath);
            this.compressFile();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Don't forget to close streams
    private void setInput(String inputPath) throws Exception {
        this.inputPath = inputPath;

        Path path = Paths.get(inputPath);
        this.fileSizeInBytes = Files.size(path);
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
            while(end < bytesRead) {
                byte[] currentBuffer = Arrays.copyOfRange(buffer, start, end + 1); // To enforce mutability of map keys
                frequencyTable.put(new ByteArrayWrapper(currentBuffer),
                        frequencyTable.getOrDefault(new ByteArrayWrapper(currentBuffer), 0L) + 1L);
                start = end + 1;
                end += numberOfBytesPerWord;
            }
            if(end < MAX_CAPACITY) {
                this.lastByteArray = Arrays.copyOfRange(buffer, start, bytesRead); // lastIndexOfInCompleteWord = bytesRead - 1;
            }
        }
        bufferedInputStream.close();
        //return frequency table
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
    private Map<ByteArrayWrapper, String> constructHuffmanTable(Node root) {
        Map<ByteArrayWrapper, String> huffmanTable = new HashMap<>();
        buildHuffmanTable(huffmanTable, "", root);
        return huffmanTable;
    }

    private void buildHuffmanTable(Map<ByteArrayWrapper, String> huffmanTable, String code, Node node) {
        if (node.getLeft() == null && node.getRight() == null) {
            huffmanTable.put(new ByteArrayWrapper(node.getWord()), code);
        }
        if (node.getLeft() != null) {
            buildHuffmanTable(huffmanTable, code + "0", node.getLeft());
        }
        if (node.getRight() != null) {
            buildHuffmanTable(huffmanTable, code + "1", node.getRight());
        }
    }

    // Don't forget to close streams
    private FileOutputStream storeHuffmanTable(Map<ByteArrayWrapper, String> huffmanTable) throws IOException {
        // Generate output file path
        String inputFileName = this.inputPath.substring(this.inputPath.lastIndexOf(File.separatorChar) + 1);
        String outputFileName = "20011502" + "." + this.numberOfBytesPerWord + "." + inputFileName + "." + "hc";
        String outputPath = this.inputPath.substring(0, this.inputPath.lastIndexOf('/') + 1) + outputFileName;
        // Save the arrayList in the output file
        FileOutputStream fos = new FileOutputStream(outputPath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(huffmanTable);
        System.out.println("Table saved size: " + huffmanTable.size());
        // Store last byte[] size then the array itself
        if (this.lastByteArray != null) { // Aware of null pointer exception if the lastByteArray is never set in the frequency construction
            oos.writeInt(this.lastByteArray.length);
            oos.write(this.lastByteArray);
        } else {
            oos.writeInt(0);
        }
        // Flush the ObjectOutputStream to ensure all buffered data is sent to the OS to write into the file
        oos.flush();
        return fos;
    }

    private void encodeAndCompress(FileOutputStream fos,  Map<ByteArrayWrapper, String> huffmanTable) throws IOException {
        BufferedInputStream bufferedInputStream = createInputStream();
        BitOutputStream bitOutputStream = new BitOutputStream(fos);
        try {
            //read byte[numberOfBytesPerWord] from input
            byte[] buffer = new byte[numberOfBytesPerWord];
            int bytesRead;
            // Set bitOutputStream for bit manipulation
            //BitOutputStream bos = new BitOutputStream(fos);
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) { // bytesRead in the last byte group may be n or less
                if(bytesRead < numberOfBytesPerWord) break; // Last byte[] was saved in the file
                // Find the code of the current word
                String code =  huffmanTable.get(new ByteArrayWrapper(buffer));
                // Write the code to the output file
                for (char c : code.toCharArray()) {
                    if(c == '0') {
                        bitOutputStream.writeBit(false);
                    } else if(c == '1') {
                        bitOutputStream.writeBit(true);
                    }else {
                        throw new IllegalArgumentException();
                    }
                }
            }
            bitOutputStream.endWriting();
            // Flush the bitOutputStream to ensure all buffered data is sent to the OS to write into the file
            bitOutputStream.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }finally {
            bufferedInputStream.close();
            bitOutputStream.close();
        }
    }

    private void closeStreams(FileOutputStream fos) {
        try {
            fos.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void compressFile() throws IOException {
        // Collect statistics
        Map<ByteArrayWrapper, Long> frequencyTable = constructFrequencyMap();
        // Construct huffman tree
        PriorityQueue<Node> queue = convertFrequencyTableMapToPriorityQueue(frequencyTable);
        Node root = constructHuffmanTree(queue);
        // Extract huffman table
        Map<ByteArrayWrapper, String> huffmanTable = constructHuffmanTable(root);
        // Write huffman table to file
        FileOutputStream fos = storeHuffmanTable(huffmanTable);
        // Set bitOutputStream for bit manipulation
        // Encode and compress the file
        encodeAndCompress(fos, huffmanTable);
        // Close the open streams
        //closeStreams(fos);
    }


}
