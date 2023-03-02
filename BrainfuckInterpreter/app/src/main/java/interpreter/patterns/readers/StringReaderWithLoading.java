package interpreter.patterns.readers;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Objects;

public class StringReaderWithLoading extends Reader {

    private String str;
    private int length;
    private int next = 0;
    // private int mark = 0;
    private ArrayList <Integer> marks = new ArrayList<>();

    public StringReaderWithLoading(String s) {
        this.str = s;
        this.length = s.length();
    }

    private void ensureOpen() throws IOException {
        if (str == null)
            throw new IOException("Stream closed");
    }

    public int read() throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (next >= length)
                return -1;
            return str.charAt(next++);
        }
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();
            Objects.checkFromIndexSize(off, len, cbuf.length);
            if (len == 0) {
                return 0;
            }
            if (next >= length)
                return -1;
            int n = Math.min(length - next, len);
            str.getChars(next, next + n, cbuf, off);
            next += n;
            return n;
        }
    }

    public long skip(long n) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (next >= length)
                return 0;
            // Bound skip by beginning and end of the source
            long r = Math.min(length - next, n);
            r = Math.max(-next, r);
            next += (int)r;
            return r;
        }
    }

    public boolean markSupported() {
        return true;
    }

    public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0){
            throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        synchronized (lock) {
            ensureOpen();
            // mark = next;
            marks.add(next);
        }
    }

    public boolean ready() throws IOException {
        synchronized (lock) {
            ensureOpen();
            return true;
        }
    }

    public void reset() throws IOException {
        synchronized (lock) {
            ensureOpen();
            next = marks.get(marks.size() - 1);
            // demark(); //??????
        }
    }

    public void close() {
        synchronized (lock) {
            str = null;
        }
    }
    

    // methods from xf, other from StreamReader + modified
    public void toLoad(String s) throws IOException {
        synchronized (lock) {
            ensureOpen();
            str += s;
            length = str.length();
        }
    }

    public String toString() {
        synchronized (lock) {
            return str;
        }
    }

    public boolean isMarked() {
        synchronized (lock) {
            return (marks.size() != 0);
        }
    }

    public void resetToStart() {
        synchronized (lock) {
            marks.clear();
            next = 0;
        }
    }

    public void demark() {
        synchronized (lock) {
            if (marks.size() != 0) {
                marks.remove(marks.size() - 1);
            }
        }
    }

    public void resetToFirst() {
        synchronized (lock) {
            if (marks.size() > 0) {
                next = marks.get(0);
                marks.clear();
            } else {
                next = 0;
            }
        }
    }

    public void markCurrentCommand() {
        synchronized (lock) {
            if (next != 0) {
                marks.add(next - 1);
            } 
        }
    }
}
