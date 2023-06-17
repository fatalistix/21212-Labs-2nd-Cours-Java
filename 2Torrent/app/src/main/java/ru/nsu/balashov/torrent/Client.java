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
import java.util.*;

public class Client {
    private final Selector channelsSelector;
    private final static int DEFAULT_BUFFER_SIZE = 2 * 1024 * 1024;
    private int byteBufferSize = DEFAULT_BUFFER_SIZE;
    private final PiecesSelector piecesSelector = new PiecesSelector();
    private final SavedFilesManager savedFilesManager;
    private boolean killed = false;

    public Client(SavedFilesManager savedFilesManager) throws IOException {
        this.channelsSelector = Selector.open();
        this.savedFilesManager = savedFilesManager;
    }

    public Client(SavedFilesManager savedFilesManager, int byteBufferSize) throws IOException {
        this.channelsSelector = Selector.open();
        this.byteBufferSize = byteBufferSize;
        this.savedFilesManager = savedFilesManager;
    }

    public void newDownload(ArrayList<String> ipWithPortList, ByteBuffer infoHash) throws KilledException {
        if (killed) {
            throw new KilledException("Killed");
        }
        for (String ipPort : ipWithPortList) {
            List<String> bufList = Splitter.on(':').splitToList(ipPort);
            if (bufList.size() != 2) {
                continue;
            }
            String ip = bufList.get(0);
            int port = Integer.parseInt(bufList.get(1));
            try {
                SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(ip, port));
                socketChannel.configureBlocking(false);
                socketChannel.register(channelsSelector, SelectionKey.OP_READ, new ChannelData(infoHash, byteBufferSize));
                piecesSelector.register(infoHash, savedFilesManager.getExistingParts(infoHash), savedFilesManager.getNumOfPieces(infoHash));
                System.out.println("KEY REGISTERED");
            } catch (IOException ignore) {
            }
        }
    }

    public boolean isDownloading(ByteBuffer infoHash) {
        return piecesSelector.contains(infoHash);
    }

    public void addSources(ByteBuffer infoHash, ArrayList<String> ipWithPortsList) throws KilledException {
        if (killed) {
            throw new KilledException("killed");
        }
        for (String ipPort : ipWithPortsList) {
            List<String> bufList = Splitter.on(':').splitToList(ipPort);
            if (bufList.size() != 2) {
                continue;
            }
            String ip = bufList.get(0);
            int port = Integer.parseInt(bufList.get(1));
            try {
                SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(ip, port));
                socketChannel.configureBlocking(false);
                socketChannel.register(channelsSelector, SelectionKey.OP_READ, new ChannelData(infoHash, byteBufferSize));
            } catch (IOException ignore) {
            }
        }
    }


    public void startDownload() throws KilledException, IOException {
        if (killed) {
            throw new KilledException("killed");
        }
        while (true) {
            int count = channelsSelector.select(3000);
            if (count == 0) {
                continue;
            }
            Iterator<SelectionKey> iter = channelsSelector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isReadable()) {
                    System.out.println("SELECTED");
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ChannelData channelData = (ChannelData) key.attachment();
                    channelData.getByteBuffer().clear();
                    try {
                        int readBytes = TorrentBBMessagesParser.readAllMessageBytes(channelData.getByteBuffer(), socketChannel);
                        if (readBytes == -1) {
                            socketChannel.close();
                            key.cancel();
                            piecesSelector.removeAllAssociations(channelData.getInfoHash(), key);
                            break;
                        }
                        System.out.println("READ");
                    } catch (IOException e) {
                        key.cancel();
                        socketChannel.close();
                        piecesSelector.removeAllAssociations(channelData.getInfoHash(), key);
                        break;
                    }
                    switch (TorrentBBMessagesParser.readMessageType(channelData.getByteBuffer())) {
                        case RESPONSE_AVAILABLE -> {
                            System.out.println("RESPONSE_AVAILABLE");
                            AvailableData availableData = TorrentBBMessagesParser.Client.readResponseAvailable(channelData.getByteBuffer());
                            piecesSelector.addAssociation(availableData.infoHash(), key, availableData.availableBitfield());
                            int pieceIndex = piecesSelector.selectPiece(channelData.getInfoHash(), key);
                            if (pieceIndex == -1) {
                                System.out.println("NOTHING TO DOWNLOAD");
                                key.cancel();
                                socketChannel.close();
                                if (!piecesSelector.havePieces(channelData.getInfoHash())) {
                                    piecesSelector.unregister(channelData.getInfoHash());
                                }
                                break;
                            }
                            TorrentBBMessagesParser.Client.writeRequestPiece(channelData.getByteBuffer(),
                                    channelData.getInfoHash(), pieceIndex);
                            try {
                                socketChannel.write(channelData.getByteBuffer());
                            } catch (IOException e) {
                                key.cancel();
                                socketChannel.close();
                                piecesSelector.removeAllAssociations(channelData.getInfoHash(), key);
                            }
                        }
                        case RESPONSE_PIECE -> {
                            System.out.println("RESPONSE_PIECE");
                            PieceData pieceData = TorrentBBMessagesParser.Client.readResponsePiece(channelData.getByteBuffer());
                            byte[] pieceShaSum = savedFilesManager.getHash(pieceData.infoHash(), pieceData.index());
                            if (pieceShaSum != null) {
                                if (Arrays.equals(DigestUtils.sha1(pieceData.piece()), pieceShaSum)) {
                                    try {
                                        savedFilesManager.writePiece(pieceData.infoHash(), pieceData.piece(), pieceData.index());
                                    } catch (IOException e) {
                                        piecesSelector.deselectPiece(pieceData.infoHash(), key, false);
                                        break;
                                    }
                                    System.out.println("SAVED PIECE " + pieceData.index());
                                    piecesSelector.deselectPiece(pieceData.infoHash(), key, true);
                                } else {
                                    piecesSelector.deselectPiece(pieceData.infoHash(), key, false);
                                }
                            } else {
//                                piecesLists.get(ByteBuffer.wrap(pieceData.infoHash())).removeAssociation(pieceData.index(), key);
                                //??????????????????????????
                                System.out.println("IMPOSSIBLE CLAUSE");
                                piecesSelector.deselectPiece(pieceData.infoHash(), key, false);
                            }
//                            if (piecesLists.get(ByteBuffer.wrap(channelData.getInfoHash())).getIndexesAsSet().isEmpty()) {
//                                socketChannel.close();
//                                key.cancel();
//                            }
//                            for (Integer index : piecesLists.get(ByteBuffer.wrap(channelData.getInfoHash())).getIndexesAsSet()) {
//                                if (piecesLists.get(ByteBuffer.wrap(channelData.getInfoHash())).contains(index, key) &&
//                                        !piecesLists.get(infoHashBB).isInUse(index)) {
//                                    piecesLists.get(infoHashBB).usePiece(index);
//                                    TorrentBBMessagesParser.Client.writeRequestPiece(byteBuffer, channelData.getInfoHash(), index);
//                                    try {
//                                        socketChannel.write(byteBuffer);
//                                        break;
//                                    } catch (IOException e) {
//                                        key.cancel();
//                                        piecesLists.get(channelData.getInfoHash()).removeAssociation(key);
//                                    }
//                                }
//                            }
                            int pieceIndex = piecesSelector.selectPiece(channelData.getInfoHash(), key);
                            if (pieceIndex == -1) {
                                System.out.println("NOTHING TO DOWNLOAD");
                                key.cancel();
                                socketChannel.close();
                                if (piecesSelector.havePieces(channelData.getInfoHash())) {
                                    piecesSelector.unregister(channelData.getInfoHash());
                                }
                                break;
                            }
                            TorrentBBMessagesParser.Client.writeRequestPiece(channelData.getByteBuffer(),
                                    channelData.getInfoHash(), pieceIndex);
                            try {
                                socketChannel.write(channelData.getByteBuffer());
                            } catch (IOException e) {
                                key.cancel();
                                socketChannel.close();
                                piecesSelector.removeAllAssociations(channelData.getInfoHash(), key);
                            }
                        }
                        case HANDSHAKE -> {
                            System.out.println("HANDSHAKE");
                            TorrentBBMessagesParser.Client.writeRequestAvailable(channelData.getByteBuffer(), channelData.getInfoHash());
                            try {
                                socketChannel.write(channelData.getByteBuffer());
                                System.out.println("DATA SENT");
                            } catch (IOException e) {
                                key.cancel();
                                socketChannel.close();
                            }
                        }
                        case UNKNOWN -> {
                            System.out.println("UNKNOWN");
                            key.cancel();
                            socketChannel.close();
                        }
                    }
                }
            }
        }
    }

    public void kill() {
        if (killed) {
            return;
        }
        try {
            for (SelectionKey key : channelsSelector.keys()) {
                key.channel().close();
            }
            channelsSelector.close();
        } catch (IOException ignored) {
        }
        killed = true;
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