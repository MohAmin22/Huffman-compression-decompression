# Huffman-compression-decompression
Implemented Huffman's algorithm. The implementation allow compressing and decompressing arbitrary files, collecting statistics from the input file first, 
then applying the compression algorithm, storing a representation of the codewords in the compressed file, so that you can decompress the file back. 
The program should have the capability of considering more than one byte. For example, instead of just collecting the frequencies and finding codewords for single bytes. 
The same can be done assuming the basic unit is n bytes, where n is an integer.

## How to Run

In case of compression
```console
java -jar huffman_20011502.jar c absolute_path_to_input_file n 
```
where n : the number of bytes per word

### Example

![image](https://github.com/MohAmin22/Huffman-compression-decompression/assets/71905033/c7ad71ab-7a78-426f-922d-a233b97f48bd)


In case of decompression
```console
java -jar huffman_20011502.jar d absolute_path_to_input_file
```
### Example

![image](https://github.com/MohAmin22/Huffman-compression-decompression/assets/71905033/0b21a3d6-5f5e-4b58-8388-9fa4ee730d24)

## Where to find?
the compressed file will be generated in the same folder of the original file under the name : "20011502.<n>.<file_name>.hc".<br>
the extracted file will follow the foramt : "extracted.<file_name>"
