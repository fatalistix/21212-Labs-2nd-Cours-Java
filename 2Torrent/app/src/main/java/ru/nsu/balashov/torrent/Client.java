package ru.nsu.balashov.torrent;

import com.google.common.base.Splitter;
import org.apache.commons.codec.digest.DigestUtils;
import ru.nsu.balashov.torrent.TorrentBBMessagesParser.Client.AvailableData;
import ru.nsu.balashov.torrent.TorrentBBMessagesParser.Client.PieceData;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Client {
    private final Selector selector;
    private final static int DEFAULT_BUFFER_SIZE = 2 * 1024 * 1024;
    private int byteBufferSize = DEFAULT_BUFFER_SIZE;
    //    private final HashMap<String, SelectionKey> channelsIpToKey = new HashMap<>();
    private final HashMap<ByteBuffer, PieceConnectionsList> piecesLists = new HashMap<>();
    private final ByteBuffer byteBuffer;
    private boolean killed = false;

    public Client() throws IOException {
        this.selector = Selector.open();
        this.byteBuffer = ByteBuffer.allocate(byteBufferSize);
    }

    public Client(int byteBufferSize) throws IOException {
        this.selector = Selector.open();
        this.byteBufferSize = byteBufferSize;
        this.byteBuffer = ByteBuffer.allocate(byteBufferSize);
    }

    public void startDownload(SavedFilesManager savedFilesManager) throws KilledException, IOException {
        if (killed) {
            throw new KilledException("killed");
        }
        while (true) {
            int count = selector.select();
            if (count == 0) {
                continue;
            }
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isReadable()) {
                    System.out.println("SELECTED");
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ChannelData channelData = (ChannelData) key.attachment();
                    byteBuffer.clear();
                    try {
                        while (socketChannel.read(byteBuffer) != 0);
                        System.out.println(byteBuffer);
                        System.out.println("READ");
                    } catch (IOException e) {
                        key.cancel();
                        piecesLists.get(ByteBuffer.wrap(channelData.getInfoHash())).removeAssociation(key);
                    }
                    ByteBuffer infoHashBB = ByteBuffer.wrap(channelData.getInfoHash());
                    switch (TorrentBBMessagesParser.readMessageType(byteBuffer)) {
                        case RESPONSE_AVAILABLE -> {
                            System.out.println("RESPONSE_AVAILABLE");
                            AvailableData availableData = TorrentBBMessagesParser.Client.readResponseAvailable(byteBuffer);
                            piecesLists.get(infoHashBB).addAssociation(key, availableData.availableBitfield());
                            if (piecesLists.get(ByteBuffer.wrap(channelData.getInfoHash())).getIndexesAsSet().isEmpty()) {
                                socketChannel.close();
                                key.cancel();
                            }
                            for (Integer index : piecesLists.get(infoHashBB).getIndexesAsSet()) {
//                                System.out.println(index);
                                if (piecesLists.get(infoHashBB).contains(index, key) &&
                                        !piecesLists.get(infoHashBB).isInUse(index)) {
                                    piecesLists.get(infoHashBB).usePiece(index);
                                    TorrentBBMessagesParser.Client.writeRequestPiece(byteBuffer, channelData.getInfoHash(), index);
                                    try {
                                        socketChannel.write(byteBuffer);
                                        break;
                                    } catch (IOException e) {
                                        key.cancel();
                                        piecesLists.get(infoHashBB).removeAssociation(key);
                                    }
                                }
                            }
                        }
                        case RESPONSE_PIECE -> {
                            System.out.println("RESPONSE_PIECE");
//                            System.out.println(byteBuffer);
                            PieceData pieceData = TorrentBBMessagesParser.Client.readResponsePiece(byteBuffer);
                            piecesLists.get(infoHashBB).releasePiece(pieceData.index());
                            byte[] shaSumIndex = savedFilesManager.getHash(pieceData.infoHash(), pieceData.index());
                            if (shaSumIndex != null) {
                                if (Arrays.equals(DigestUtils.sha1(pieceData.piece()), shaSumIndex)) {
                                    savedFilesManager.writePiece(pieceData.infoHash(), pieceData.piece(), pieceData.index());
                                    System.out.println("SAVED PIECE " + pieceData.index());
                                    piecesLists.get(infoHashBB).remove(pieceData.index());
                                } else {
//                                    for (int i = 0; i < 20; ++i) {
//                                        System.out.println(shaSumIndex[i] + " " + DigestUtils.sha1(pieceData.piece())[i]);
//                                    }
//                                    System.out.println(byteBuffer);
//                                    for (byte b : pieceData.piece()) {
//                                        System.out.print(b + " ");
//                                    }
//                                    System.out.println(pieceData.piece().length);
                                    piecesLists.get(ByteBuffer.wrap(pieceData.infoHash())).removeAssociation(pieceData.index(), key);
                                }
                            } else {
                                piecesLists.get(ByteBuffer.wrap(pieceData.infoHash())).removeAssociation(pieceData.index(), key);
                            }
                            if (piecesLists.get(ByteBuffer.wrap(channelData.getInfoHash())).getIndexesAsSet().isEmpty()) {
                                socketChannel.close();
                                key.cancel();
                            }
                            for (Integer index : piecesLists.get(ByteBuffer.wrap(channelData.getInfoHash())).getIndexesAsSet()) {
                                if (piecesLists.get(ByteBuffer.wrap(channelData.getInfoHash())).contains(index, key) &&
                                        !piecesLists.get(infoHashBB).isInUse(index)) {
                                    piecesLists.get(infoHashBB).usePiece(index);
                                    TorrentBBMessagesParser.Client.writeRequestPiece(byteBuffer, channelData.getInfoHash(), index);
                                    try {
                                        socketChannel.write(byteBuffer);
                                        break;
                                    } catch (IOException e) {
                                        key.cancel();
                                        piecesLists.get(channelData.getInfoHash()).removeAssociation(key);
                                    }
                                }
                            }
                        }
                        case HANDSHAKE -> {
//                            System.out.println("HANDSHAKE");
                            TorrentBBMessagesParser.Client.writeRequestAvailable(byteBuffer, channelData.getInfoHash());
                            if (!piecesLists.containsKey(ByteBuffer.wrap(channelData.getInfoHash()))) {
                                piecesLists.put(ByteBuffer.wrap(channelData.getInfoHash()), new PieceConnectionsList(savedFilesManager.getExistingParts(channelData.getInfoHash())));
                            }
                            try {
                                socketChannel.write(byteBuffer);
                                System.out.println("DATA SENT");
                            } catch (IOException e) {
                                key.cancel();
                            }
                        }
                        case UNKNOWN -> {
                            System.out.println("UNKNOWN");

                        }
                    }
                }
            }
        }
    }

//                if (key.isWritable()) {
//                    SocketChannel socketChannel = (SocketChannel) key.channel();
//                    ChannelData channelData = (ChannelData) key.attachment();
//                    byteBuffer.clear();
//                    if (!channelData.isDataActual()) {
//                        TorrentBBMessagesParser.Client.writeRequestAvailable(byteBuffer, channelData.getInfoHash());
//                        System.out.println("REQUEST AVAILABLE WROTE");
//                        if (!piecesLists.containsKey(channelData.getInfoHash())) {
//                            piecesLists.put(channelData.getInfoHash(), new PieceConnectionsList(channelData.getInfoHash()));
//                            System.out.println("DATA PUT");
//                        }
//                        try {
//                            byteBuffer.flip();
//                            socketChannel.write(byteBuffer);
//                            System.out.println("DATA SENT");
//                        } catch (IOException e) {
//                            key.cancel();
//                        }
//                    } else {
//                        for (Integer index : piecesLists.get(channelData.getInfoHash()).getIndexesAsSet()) {
//                            if (piecesLists.get(channelData.getInfoHash()).contains(index, key)) {
//                                TorrentBBMessagesParser.Client.writeRequestPiece(byteBuffer, channelData.getInfoHash(), index);
//                                byteBuffer.flip();
//                                try {
//                                    socketChannel.write(byteBuffer);
//                                } catch (IOException e) {
//                                    key.cancel();
//                                    piecesLists.get(channelData.getInfoHash()).removeAssociation(key);
//                                }
//                            }
//                        }
//                    }
//                }


//                if (key.isReadable()) {
//                    SocketChannel socketChannel = (SocketChannel) key.channel();
////                    ConcurrentLinkedQueue<ChannelData> dataQueue = (ConcurrentLinkedQueue<ChannelData>) key.attachment();
//                    byteBuffer.clear();
//                    System.out.println("DATA READ");
//                    try {
//                        socketChannel.read(byteBuffer);
//                        switch (TorrentBBMessagesParser.readMessageType(byteBuffer)) {
//                            case RESPONSE_AVAILABLE -> {
//                                AvailableData availableData = TorrentBBMessagesParser.Client.readResponseAvailable(byteBuffer);
//                                piecesLists.get(availableData.infoHash()).addAssociation(key, availableData.availableBitfield());
//                                for (ChannelData channelData : dataQueue) {
//                                    if (Arrays.equals(channelData.getInfoHash(), availableData.infoHash())) {
//                                        channelData.setDataActual(true);
//                                    }
//                                }
//                            }
//                            case RESPONSE_PIECE -> {
//                                PieceData data = TorrentBBMessagesParser.Client.readResponsePiece(byteBuffer);
//                                byte[] shaSumIndex = savedFilesManager.getHash(data.infoHash(), data.index());
//                                if (shaSumIndex != null) {
//                                    if (Arrays.equals(DigestUtils.sha1(data.piece()), shaSumIndex)) {
//                                        savedFilesManager.writePiece(data.infoHash(), data.piece(), data.index());
//                                        iter.remove();
//                                        oneWrote = true;
//                                        System.out.println("wrote " + data.index());
//                                        break LOOP;
//                                    } else {
//                                        piecesChannels.keys().remove(key);
//                                        if (piecesChannels.keys().isEmpty()) {
//                                            iter.remove();
//                                        }
//                                    }
//                                } else {
//                                    piecesChannels.keys().remove(key);
//                                    if (piecesChannels.keys().isEmpty()) {
//                                        iter.remove();
//                                    }
//                                }
//                            }
//                            case UNKNOWN -> {
//                                System.out.println("UNKNOWN");
//                            }
//                        }
//                    } catch (IOException e) {
//                        key.cancel();
//                    }
//                }
//                if (key.isWritable()) {
//                    SocketChannel socketChannel = (SocketChannel) key.channel();
//                    ConcurrentLinkedQueue<ChannelData> dataQueue = (ConcurrentLinkedQueue<ChannelData>) key.attachment();
//                }
//                if (key.isWritable()) {
//                    System.out.println("IS WRITABLE PASSED");
//                    SocketChannel socketChannel = (SocketChannel) key.channel();
//                    ConcurrentLinkedQueue<ChannelData> dataQueue = (ConcurrentLinkedQueue<ChannelData>) key.attachment();
//                    byteBuffer.clear();
//                    if (dataQueue.isEmpty()) {
//                        System.out.println("DATA IS EMPTY");
//                        TorrentBBMessagesParser.writeRequestCancel(byteBuffer);
//                        try {
//                            socketChannel.write(byteBuffer);
//                            key.cancel();
//                            socketChannel.close();
//                        } catch (IOException ignore) {
//                        }
//                    }
//                    for (ChannelData channelData : dataQueue) {
//                        if (!channelData.isDataActual()) {
//                            TorrentBBMessagesParser.Client.writeRequestAvailable(byteBuffer, channelData.getInfoHash());
//                            System.out.println("REQUEST AVAILABLE WROTE");
//                            if (!piecesLists.containsKey(channelData.getInfoHash())) {
//                                piecesLists.put(channelData.getInfoHash(), new PieceConnectionsList(channelData.getInfoHash()));
//                                System.out.println("DATA PUT");
//                            }
//                            try {
//                                socketChannel.write(byteBuffer);
//                                System.out.println("DATA SENT");
//                                byteBuffer.clear();
//                                socketChannel.read(byteBuffer);
//                                System.out.println("DATA READ");
//                                switch (TorrentBBMessagesParser.readMessageType(byteBuffer)) {
//                                    case RESPONSE_AVAILABLE -> {
//                                        AvailableData availableData = TorrentBBMessagesParser.Client.readResponseAvailable(byteBuffer);
//                                        piecesLists.get(channelData.getInfoHash()).addAssociation(key, availableData.availableBitfield());
//                                        channelData.setDataActual(true);
//                                    }
//                                    case UNKNOWN -> {
//                                        System.out.println("UNKNOWN");
//                                    }
//                                }
//                            } catch (IOException e) {
//                                key.cancel();
//                            }
//                        } else {
//                            if (!piecesLists.containsKey(channelData.getInfoHash())) {
//                                dataQueue.remove(channelData);
//                                continue;
//                            }
//                            Iterator<PiecesChannels> iter = piecesLists.get(channelData.getInfoHash()).iterator();
//                            boolean oneWrote = false;
//                            LOOP:
//                            while (iter.hasNext()) {
//                                PiecesChannels piecesChannels = iter.next();
//                                if (piecesChannels.keys().contains(key)) {
//                                    TorrentBBMessagesParser.Client.writeRequestPiece(byteBuffer, channelData.getInfoHash(), piecesChannels.pieceIndex());
//                                    try {
//                                        socketChannel.write(byteBuffer);
//                                        byteBuffer.clear();
//                                        socketChannel.read(byteBuffer);
//                                        switch (TorrentBBMessagesParser.readMessageType(byteBuffer)) {
//                                            case RESPONSE_PIECE -> {
//                                                PieceData data = TorrentBBMessagesParser.Client.readResponsePiece(byteBuffer);
//                                                byte[] shaSumIndex = savedFilesManager.getHash(data.infoHash(), data.index());
//                                                if (shaSumIndex != null) {
//                                                    if (Arrays.equals(DigestUtils.sha1(data.piece()), shaSumIndex)) {
//                                                        savedFilesManager.writePiece(data.infoHash(), data.piece(), data.index());
//                                                        iter.remove();
//                                                        oneWrote = true;
//                                                        System.out.println("wrote " + data.index());
//                                                        break LOOP;
//                                                    } else {
//                                                        piecesChannels.keys().remove(key);
//                                                        if (piecesChannels.keys().isEmpty()) {
//                                                            iter.remove();
//                                                        }
//                                                    }
//                                                } else {
//                                                    piecesChannels.keys().remove(key);
//                                                    if (piecesChannels.keys().isEmpty()) {
//                                                        iter.remove();
//                                                    }
//                                                }
//                                            }
//                                            case UNKNOWN -> {
//                                            }
//                                        }
//                                    } catch (IOException e) {
//                                        key.cancel();
//                                        piecesChannels.keys().remove(key);
//                                    }
//                                }
//                            }
//                            if (!oneWrote) {
//                                dataQueue.remove(channelData);
//                            }
//                        }
//                    }
//                }


    public void newDownload(ArrayList<String> ipWithPortList, byte[] infoHash) throws KilledException {
        if (killed) {
            throw new KilledException("Killed");
        }
        for (String ipPort : ipWithPortList) {
//            if (channelsIpToKey.containsKey(ipPort)) {
//                ((ConcurrentLinkedQueue<ChannelData>) channelsIpToKey.get(ipPort).attachment()).add(new ChannelData(infoHash, false));
//            } else {
            List<String> bufList = Splitter.on(':').splitToList(ipPort);
            if (bufList.size() != 2) {
                continue;
            }
            String ip = bufList.get(0);
            int port = Integer.parseInt(bufList.get(1));
            try {
                SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(ip, port));
                socketChannel.configureBlocking(false);
//                ConcurrentLinkedQueue<ChannelData> dataQueue = new ConcurrentLinkedQueue<>();
//                dataQueue.add(new ChannelData(infoHash, false));
                SelectionKey key = socketChannel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, new ChannelData(infoHash, false));
                System.out.println("KEY REGISTERED");
//                channelsIpToKey.put(ipPort, key);
            } catch (IOException ignore) {
            }
//            }
        }
    }

    public void kill() {
        if (killed) {
            return;
        }
        try {
            for (SelectionKey key : selector.keys()) {
                key.channel().close();
            }
            selector.close();
        } catch (IOException ignored) {
        }
        killed = true;
    }

}
