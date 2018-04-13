package com.gdu.remotecontrol.coretask;

import com.gdu.remotecontrol.model.GDUConstants;
import com.gdu.remotecontrol.util.RCUtil;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * ServerSocketChannel: 是一个可以监听新进来的TCP连接的通道, 就像标准IO中的ServerSocket一样。
 * 通过 ServerSocketChannel.accept() 方法监听新进来的连接。
 * 当 accept()方法返回的时候,它返回一个包含新进来的连接的
 * 阻塞模式 configureBlocking（true） 为true时，accept()方法会一直阻塞到有新连接到达。
 * 非阻塞模式 configureBlocking（false） 为false时,accept() 方法会立刻返回,如果还没有新进来的连接,返回的将是null
 *
 * Selector（选择器）能够检测一到多个NIO通道,并能够知晓通道是否为诸如读写事件做好准备的组件,
 * 可以监听四种不同类型的事件Connect，Accept，Read，Write 通道触发了一个事件意思是该事件已经就绪。
 * （1）某个channel成功连接到另一个服务器称为“连接就绪”。
 * （2）一个server socket channel准备好接收新进入的连接称为“接收就绪”。
 * （3）一个有数据可读的通道可以说是“读就绪”。
 * （4）等待写数据的通道可以说是“写就绪”。
 *
 * 1.发送数据到USB
 * USB代理服务器管理类，负责接收client端数据，对数据进行包装 ，并发送到USB，
 *
 * 2.接收USB数据
 * （1）解析出传输类型UDP/TCP,
 * （2）解析出端口号
 * （3）根据传输类型和端口号，将数据回复给client
 * Created by zhangzhilai on 2017/12/9.
 */

public class ProxyServerTask extends GDUTask {

    private static final int MAX_CHANNEL_SIZE = 20;

    private Map<Integer, AbstractSelectableChannel> mSocketMap;  //存放serverSocket的map
    private ByteBuffer mUsbByteBuffer;  //???
    private Selector mChannelSelector;

    public ProxyServerTask() {
        mSocketMap = new HashMap<>(MAX_CHANNEL_SIZE);
        mUsbByteBuffer = ByteBuffer.allocate(GDUConstants.BUFFER_SIZE_4104);  //??
    }

    public void setUsbData(byte[] data) {
        try {
            if (data[0] == GDUConstants.TCP) {
                sendDataToTcp(data);
            } else if (data[0] == GDUConstants.UDP) {
                sendUsbDataToUdp(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 首先创建各个端口的通道，开始监听
     *
     */
    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        mChannelSelector = createSelector();
        run(mChannelSelector);
    }

    /**
     * 根据各个通道注册时的兴趣，进行相关处理
     * 1.是Acceptable的将其改为read   //TODO ?????为什么不开始就注册为read
     * 2.是Readable的就开始读取数据
     * @param selector
     */
    private void run(Selector selector) {
        try {
            while (!isClose) {
                int n = selector.select();
                if (n == 0) {  //没有端口处于就绪状态
                    continue;
                }
                if (!selector.isOpen()) {  //selector没有打开
                    return;
                }
                Iterator it = selector.selectedKeys().iterator();
                while (it.hasNext() && !isClose) {
                    SelectionKey key = (SelectionKey) it.next();
                    int att = (int) key.attachment();  //附加对象，这里就是channel的端口号
                    try {
                        if (key.isAcceptable()) { //如果channel可接收，接受就绪，将channel的兴趣设置为Read
                            registerChannel(selector, key, att);
                        }
                        if (key.isReadable()) {
                            readData(key, att);
                        }
                    } catch (Exception e) {
                        key.cancel();  //???
                    }
                    it.remove();  //解决接受一次请求后，不再响应的问题
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * channel可读后，开始读取数据
     *
     * @param key
     * @param att  注册时的端口号
     */
    private void readData(SelectionKey key, int att) {
        try {
            switch (att) {
                case GDUConstants.LOCAL_PORT_TCP:
                    readTcpData(key);
                    break;
                case GDUConstants.LOCAL_PORT_RC_USB:
                    readTcpDataWifi(key);
                    break;
                case GDUConstants.LOCAL_PORT_UDP:
                    readUdpData(key);
                    break;
                case GDUConstants.REMOTE_PORT_UDP_UPGRADE:
                    if (RCUtil.csIsLastVersion == 2) {  //TODO 需要全局设置
                        readUdpDataUpdate(key);
                    }
                    break;
                case GDUConstants.LOCAL_PORT_FTP_CMD:
                    readCmdFromFtp(key);
                    break;
                case GDUConstants.LOCAL_PORT_FTP_DATA:
                    readDataFromFtp(key);
                    break;
                case GDUConstants.LOCAL_PORT_PROXY_SERVER:  //从USB读取数据
                    readDataFromUsb(key);  //TODO
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readTcpDataWifi(SelectionKey selKey) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(GDUConstants.BUFFER_SIZE_1024);
        SocketChannel socketChannel = (SocketChannel) selKey.channel();
        Socket socket = socketChannel.socket();
        int port = socket.getPort();
        int readLen = socketChannel.read(byteBuffer);
        if ( readLen > 0) {
            byte [] data = RCUtil.getTcpBytes(3, port, GDUConstants.LOCAL_PORT_RC_USB , readLen );
            System.arraycopy(byteBuffer.array(), 0, data, 8, GDUConstants.BUFFER_SIZE_1024);
            sendDataToUsb(data);
        }else{//关闭 socket
            closeChannel(selKey, port);
        }
    }

    /**从FTP client 端读取数据发送给 USB*/
    private void readDataFromFtp(SelectionKey key) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(GDUConstants.BUFFER_SIZE_1024);
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            Socket socket = channel.socket();
            int port = socket.getPort();
            int readLen = channel.read(byteBuffer);
            if (readLen > 0) {
                byte[] data = RCUtil.getTcpBytes(GDUConstants.DATA, port, GDUConstants.REMOTE_PORT_FTP_DATA_3412, readLen);
                System.arraycopy(byteBuffer.array(), 0, data, GDUConstants.BUFFER_HEADER_LENGTH, GDUConstants.BUFFER_SIZE_1024);
                sendDataToUsb(data);
            } else {
                byte[] data = RCUtil.getTcpBytes(GDUConstants.DISCONNECTED, port, GDUConstants.REMOTE_PORT_FTP_DATA_3412, 0);
                sendDataToUsb(data);
                closeChannel(key, port);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从FTP Client 端读取命名发送给USB
     *
     * @param key
     * @throws IOException
     */
    private void readCmdFromFtp(SelectionKey key) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(GDUConstants.BUFFER_SIZE_1024);
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            Socket socket = socketChannel.socket();
            int port = socket.getPort();
            int readLen = socketChannel.read(byteBuffer);
            if (readLen > 0) {
                byte[] data = RCUtil.getTcpBytes(GDUConstants.DATA, port, GDUConstants.REMOTE_PORT_FTP_CMD_23, readLen);
                System.arraycopy(byteBuffer.array(), 0, data, 8, GDUConstants.BUFFER_SIZE_1024);
                sendDataToUsb(data);
            } else {
                closeChannel(key, port);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将usb通过socket上传的数据，根据不同网络类型和端口分发给不同的UDP或者TCP
     * @param key
     * @throws IOException
     */
    private void readDataFromUsb(SelectionKey key) throws IOException {
        mUsbByteBuffer.clear();
        SocketChannel channel = (SocketChannel) key.channel();
        channel.read(mUsbByteBuffer);
        byte[] data = mUsbByteBuffer.array();
        if (data[0] == GDUConstants.TCP) {
            sendDataToTcp(data);
        } else if (data[0] == GDUConstants.UDP) {
            sendUsbDataToUdp(data);
        }
    }

    /**
     * 将USB返回的数据发送给
     * 各个请求的UDP client
     * 从data中取出端口号，包括CE(数传)和CS（图传）
     */
    private void sendUsbDataToUdp(byte[] data) throws IOException {
        int destPort = RCUtil.get2ByteToInt(data[4], data[5]);
        int length = RCUtil.get2ByteToInt(data[6], data[7]);
        if (destPort <= 0) {
            return;
        }
        DatagramChannel datagramChannel = (DatagramChannel) mSocketMap.get(GDUConstants.LOCAL_PORT_UDP);
        datagramChannel.send(ByteBuffer.wrap(data, GDUConstants.BUFFER_HEADER_LENGTH,
                length), new InetSocketAddress(InetAddress.getLocalHost(), destPort));
    }

    /**
     * 将USB返回的数据发送给 各个请求的TCP client
     *  根据端口号取出相应的Socket，然后根据当前channel的状态进行数据的处理
     *  当处于DATA状态时，将数据写入到socket
     * @param data
     */
    private void sendDataToTcp(byte[] data) throws IOException {
        System.out.println("test data 1" + data[1] + " 0: " + data[0]);
        int destPort = RCUtil.get2ByteToInt(data[4], data[5]);
        SocketChannel serverChannel = (SocketChannel) mSocketMap.get(destPort);
        int dataType = data[1];
        switch (dataType) {
            case GDUConstants.CONNECTING:
                break;
            case GDUConstants.CONNECTED:
                break;
            case GDUConstants.DATA:
                System.out.println("test DATA" + destPort);
                if (serverChannel != null && serverChannel.isOpen()) {
                    int length = RCUtil.get2ByteToInt(data[6], data[7]);
                    serverChannel.write(ByteBuffer.wrap(data, 8, length));
                }
                break;
            case GDUConstants.DISCONNECTED:
                System.out.println("test DISCONNECTED" + destPort);
                mSocketMap.remove(destPort);
                if (serverChannel != null) {
                    serverChannel.close();
                }
                break;
        }
    }

    /**
     * 接收 UDP client 端发来的数据，然后放在队列中
     */
    private void readUdpDataUpdate(SelectionKey key) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(GDUConstants.BUFFER_SIZE_1024);
        DatagramChannel channel = (DatagramChannel) key.channel();
        InetSocketAddress address = (InetSocketAddress) channel.receive(byteBuffer);
        int len = RCUtil.getUnsignedByte((byte) (byteBuffer.array()[1] + 4));
        //固件升级的端口为 9000
        byte[] data = RCUtil.getUdpBytes(byteBuffer, address.getPort(), GDUConstants.REMOTE_PORT_UDP_UPGRADE, len);
        sendDataToUsb(data);
    }

    /**
     * 接收 UDP client 端发来的数据，然后放在队列中
     * 对UDP数据进行包装加头，通过RC发给飞机
     */
    private void readUdpData(SelectionKey key) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(GDUConstants.BUFFER_SIZE_1024);
        DatagramChannel channel = (DatagramChannel) key.channel();
        InetSocketAddress address = (InetSocketAddress) channel.receive(byteBuffer);
        int len = RCUtil.getUnsignedByte((byte) (byteBuffer.array()[1] + 4));

        byte[] data = RCUtil.getUdpBytes(byteBuffer, address.getPort(),
                (byteBuffer.array()[2] == 0xFF || byteBuffer.array()[2] == 0xFE) ? GDUConstants.REMOTE_PORT_UDP_UPGRADE : GDUConstants.REMOTE_PORT_UDP,
                len);
        sendDataToUsb(data);
    }

    /**
     * 接收app client端发来的数据，然后放在队列中
     * 对数据进行封装，加头
     * @param key
     */
    private void readTcpData(SelectionKey key) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(GDUConstants.BUFFER_SIZE_1024);
        SocketChannel socketChannel = (SocketChannel) key.channel();
        Socket socket = socketChannel.socket();
        int port = socket.getPort();
        int readLen = socketChannel.read(byteBuffer);
        if (readLen > 0) {
            byte[] data = RCUtil.getTcpBytes(GDUConstants.DATA, port, GDUConstants.REMOTE_PORT_TCP, readLen);
            System.arraycopy(byteBuffer.array(), 0, data, 8, GDUConstants.BUFFER_SIZE_1024);
            sendDataToUsb(data);
        } else {
            closeChannel(key, port);
        }
    }

    /**
     * channel处于接受就绪
     * server socket 接收到client 连接后将该client注册到selected 中
     * 开始接受数据
     * @param selector
     * @param key
     * @param netType  端口号
     */
    private void registerChannel(Selector selector, SelectionKey key, int netType) {
        try {
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel channel = server.accept();  //获取一个新的连接
            if (channel != null) {
                channel.configureBlocking(false);
                channel.register(selector, SelectionKey.OP_READ, netType);  //将接收就绪的通道，加读的兴趣
                //给usb发送 已接受连接的消息
                sendConnecting(netType, channel);
                //TODO????
                //http连接后执行
                //保存端口对应的channel，后面write数据的时候使用
                if (!mSocketMap.containsKey(channel.socket().getPort())) {
                    mSocketMap.put(GDUConstants.LOCAL_PORT_PROXY_SERVER == netType ?
                            GDUConstants.LOCAL_PORT_PROXY_CLIENT : channel.socket().getPort(), channel);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送正在连接的状态给USB。usb转发到飞机  只有TCP连接有该状态
     * 根据client传递过来的端口号，修改到飞机端对应的端口号
     * @param netType 端口号，也可以表示某种网络类型
     * @param channel
     */
    private void sendConnecting(int netType, SocketChannel channel) throws IOException {
        byte[] data = null;
        int localPort = channel.socket().getPort();
        int remotePort = 0;
        switch (netType) {
            case GDUConstants.LOCAL_PORT_FTP_CMD:
                remotePort = GDUConstants.REMOTE_PORT_FTP_CMD_23;
                break;
            case GDUConstants.LOCAL_PORT_FTP_DATA:
                remotePort = GDUConstants.REMOTE_PORT_FTP_DATA_3412;
                break;
            case GDUConstants.LOCAL_PORT_TCP:
                remotePort = GDUConstants.REMOTE_PORT_TCP;
                break;
            case GDUConstants.LOCAL_PORT_RC_USB:
                remotePort = GDUConstants.LOCAL_PORT_RC_USB;
                break;
        }
        data = RCUtil.getTcpBytes(GDUConstants.CONNECTING, localPort, remotePort, 0);
        sendDataToUsb(data);
    }

    /**
     * 关闭通道
     *
     * @param key
     * @param port
     * @throws IOException
     */
    private void closeChannel(SelectionKey key, int port) throws IOException {
        if (mSocketMap.containsKey(port)) {
            mSocketMap.get(port).close();
            mSocketMap.remove(port);
        }
        key.cancel();
        if (key.channel() != null) {
            key.channel().close();
        }
    }

    /**
     * 发送数据到usb
     *
     * @param data
     * @throws IOException
     */
    private void sendDataToUsb(byte[] data) throws IOException {
        SocketChannel socketProxy = (SocketChannel) mSocketMap.get(GDUConstants.LOCAL_PORT_PROXY_CLIENT);
        if (socketProxy != null && data != null) {
            socketProxy.write(ByteBuffer.wrap(data));
        }
    }

    /**
     * 建立各个通道，包括client到proxy server 和 usb到proxy server
     *
     * @return
     */
    private Selector createSelector() {
        Selector selector = null;
        try {
            selector = Selector.open();
            createUsbChannel(selector);   //通道通往USB
            createUSBWifiChannel(selector);
            /** 通道通往app client */
            createTcpChannel(selector);
            createUDPChannel(selector);
            createUpdateUDPChannel(selector);
            createFtpCmdChannel(selector);
            createFtpDataChannel(selector);
            /** 通道通往app client */
        } catch (Exception e) {

        }
        return selector;
    }

    /**
     * 创建TCP监听通道
     *
     * @param selector
     */
    private void createTcpChannel(Selector selector) {
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = serverChannel.socket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), GDUConstants.LOCAL_PORT_TCP));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT, GDUConstants.LOCAL_PORT_TCP);
            mSocketMap.put(GDUConstants.LOCAL_PORT_TCP, serverChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createUSBWifiChannel(Selector selector){
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open( );
            ServerSocket serverSocket = serverChannel.socket( );
            serverSocket.setReuseAddress(true);
            serverSocket.bind (new InetSocketAddress(InetAddress.getLocalHost(), GDUConstants.LOCAL_PORT_RC_USB));
            serverChannel.configureBlocking (false);
            serverChannel.register (selector, SelectionKey.OP_ACCEPT, GDUConstants.LOCAL_PORT_RC_USB );
            mSocketMap.put(GDUConstants.LOCAL_PORT_RC_USB,serverChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建UDP监听通道
     *
     * @param selector
     */
    private void createUDPChannel(Selector selector) {
        try {
            DatagramChannel datagramChannel = DatagramChannel.open();
            DatagramSocket datagramSocket = datagramChannel.socket();
            datagramSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), GDUConstants.LOCAL_PORT_UDP));
            datagramChannel.configureBlocking(false);
            datagramChannel.register(selector, SelectionKey.OP_READ, GDUConstants.LOCAL_PORT_UDP);
            mSocketMap.put(GDUConstants.LOCAL_PORT_UDP, datagramChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建升级的udp通道
     *
     * @param selector
     */
    private void createUpdateUDPChannel(Selector selector) {
        int updatePort = GDUConstants.REMOTE_PORT_UDP_UPGRADE;
        try {
            DatagramChannel updateDatagramChannel = DatagramChannel.open();
            DatagramSocket datagramSocket = updateDatagramChannel.socket();
            datagramSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), updatePort));
            updateDatagramChannel.configureBlocking(false);
            updateDatagramChannel.register(selector, SelectionKey.OP_READ, updatePort);
            mSocketMap.put(updatePort, updateDatagramChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建监听usb数据的监听通道
     *
     * @param selector
     */
    private void createUsbChannel(Selector selector) {
        int usbProxyPort = GDUConstants.LOCAL_PORT_PROXY_SERVER;
        try {
            ServerSocketChannel socketChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = socketChannel.socket();
            serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), usbProxyPort));
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_ACCEPT, usbProxyPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建ftp命令的通道
     *
     * @param selector
     */
    private void createFtpCmdChannel(Selector selector) {
        int ftpCmdPort = GDUConstants.LOCAL_PORT_FTP_CMD;
        try {
            ServerSocketChannel socketChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = socketChannel.socket();
            serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), ftpCmdPort));
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_ACCEPT, ftpCmdPort);
            mSocketMap.put(ftpCmdPort, socketChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建ftp数据的通道
     *
     * @param selector
     */
    private void createFtpDataChannel(Selector selector) {
        int ftpDataPort = GDUConstants.LOCAL_PORT_FTP_DATA;
        try {
            ServerSocketChannel socketChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = socketChannel.socket();
            serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), ftpDataPort));
            socketChannel.register(selector, SelectionKey.OP_ACCEPT, ftpDataPort);
            mSocketMap.put(ftpDataPort, socketChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void close() throws IOException {
        isClose = true;
        if (mSocketMap != null) {
            for (Integer port : mSocketMap.keySet()) {
                AbstractSelectableChannel channel = mSocketMap.get(port);
                if (port == GDUConstants.LOCAL_PORT_PROXY_SERVER) {
                    closeServerSocket(channel);
                } else {
                    closeChannel(channel);
                }
            }
            mSocketMap.clear();
            mSocketMap = null;
        }
        closeSelector();
    }

    /******************************
     *  关闭SocketChannel
     */
    private void closeSelector() {
        if (mChannelSelector != null) {
            if (mChannelSelector == null) return;
            Iterator it = mChannelSelector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = (SelectionKey) it.next();
                try {
                    key.channel().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void closeServerSocket(AbstractSelectableChannel channel) throws IOException {
        SocketChannel socketChannel = (SocketChannel) channel;
        if (socketChannel.socket().isBound() && !socketChannel.socket().isClosed()) {
            socketChannel.socket().close();
            socketChannel = null;
        }
    }

    private void closeChannel(AbstractSelectableChannel channel) throws IOException {
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }
}
