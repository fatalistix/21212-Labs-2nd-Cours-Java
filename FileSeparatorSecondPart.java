import java.io.IOException;
import java.io.RandomAccessFile;

public class FileSeparatorSecondPart {
    public static void main(String args[]) throws IOException {
        RandomAccessFile rAccessFile = new RandomAccessFile("/home/vyacheslav/Documents/forTorrent/Original Second/EndeavourOS_Cassini_Nova-03-2023_R1.iso", "rw");
        long length = rAccessFile.length();
        rAccessFile.seek(0);
        rAccessFile.write(new byte[256 * 1024 * 4000]);
        rAccessFile.setLength(length);
    }
}
