package com.abt.http.stream;

import com.abt.http.global.GlobalConstants;
import com.orhanobut.logger.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Mp4FileInputStream extends FileInputStream {
    
    private Mp4Box mMoovBox = null;
    private Mp4Box mDatBox = null;
    private Mp4Box mFtypBox = null;
    private boolean mNeedProcess = false;
    private long mReadPosition = 0;

    private long mFileSize = 0;
    private long mFixedLength = 0;
    private RandomAccessFile mInputFile = null;
    private byte[] mHeaderData = null;

    public Mp4FileInputStream(File infile) throws FileNotFoundException {
        super(infile);

        mInputFile = new RandomAccessFile(infile.getPath(), "r");
        try {
            mFileSize = available();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (parseMp4Boxs(mInputFile)) {
            if (mMoovBox != null && mDatBox != null){
                if (mMoovBox.offset > mDatBox.offset) {
                    mNeedProcess = true;

                    mHeaderData = new byte[(int)(mFtypBox.size + mMoovBox.size)];
                    readHeaderBoxData(mHeaderData, mInputFile);
                    mFixedLength = mMoovBox.size;
                    modifyHeaderBox(mHeaderData);
                }
            }
        }
    }

    @Override
    public int read() throws IOException {
        Logger.d("read()");
        return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        int len = super.read(b);
        Logger.d("read(byte[] b): read_size=" + len);
        return len;
    }

    @Override
    public int read(byte[] buffer, int off, int len) throws IOException {
        int length = 0;
        RandomAccessFile input = mInputFile;
        if (mNeedProcess) {
            //firstly read ftyp box, secondly read moov box, then read others
            long readLen = 0;
            if (mReadPosition < mHeaderData.length){
                Logger.d( "read ftyp and moov box");
                long needRead = min(len, mHeaderData.length - mReadPosition);
                System.arraycopy(mHeaderData, (int) mReadPosition, buffer, 0, (int)needRead);
                readLen = needRead;
                if (readLen > 0){
                    length += readLen;
                    off += readLen;
                    len -= readLen;
                    mReadPosition += readLen;
                }
            }
            if (len <= 0)
                return length;
            //moov has red, others box can just be read normal
            //reset and skip ftyp box and read all other boxs
            input.seek(mFtypBox.size);
            input.read(buffer, (int)readLen, len);
            length += len;
            mReadPosition = 0;
            mNeedProcess = false;
            Logger.d( "moov box has red, we just seek to the next box of ftyp box");
        } else {
            length = input.read(buffer, off, len);
        }
        return length;
    }

    @Override
    public long skip(long n) throws IOException {
        long size = super.skip(n);
        Logger.d( "skip byte:" + n);
        return size;
    }

    @Override
    public int available() throws IOException {
        int size = super.available();
        Logger.d( "available size:" +size);
        return size;
    }

    @Override
    public void close() throws IOException {
        safeClose(mInputFile);
        super.close();
    }

    private final void safeClose(Object closeable) {
        try {
            if (closeable != null) {
                if (closeable instanceof Closeable) {
                    ((Closeable) closeable).close();
                } else if (closeable instanceof Socket) {
                    ((Socket) closeable).close();
                } else if (closeable instanceof ServerSocket) {
                    ((ServerSocket) closeable).close();
                } else {
                    throw new IllegalArgumentException("Unknown object to close");
                }
            }
        } catch (IOException e) {
            Logger.e( "Could not close");
        }
    }

    private long min(long a, long b){
        return a < b ? a : b;
    }

    private boolean parseMp4Boxs(RandomAccessFile input) {
        long offset = 0;
        try {
//            RandomAccessFile
            input.seek(0);
            do {
                Mp4Box box = getNextBox(input, offset);
                if (box == null) {
                    break;
                }
                Logger.d( "box info:" + box.type + ", offset=" + box.offset + ", size=" + box.size);
                //save moov box and mdat box
                if (box.type.equals(GlobalConstants.boxTypeMoov)) {
                    mMoovBox = box;
                } else if (box.type.equals(GlobalConstants.boxTypeMdat)) {
                    mDatBox = box;
                } else if (box.type.equals(GlobalConstants.boxTypeFtyp)) {
                    mFtypBox = box;
                }
                offset += box.size;
            } while(true);

            mInputFile.seek(0);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * mp4 box struct:
     * Field name          Type       Size(bits)
     * box size             uint32      32
     * box type             uint32      32
     * largesize            uint64      0/64
     * if box_size=1, the real box size is the largesize
     * if box_size=0, the box will last the end of the file.
     * */
    private Mp4Box getNextBox(RandomAccessFile input, long offset) {
        long size = 0;
        String type = "";
        byte[] buf = new byte[16];

        try {
            int headerSize = 8;
            //read box size and type area
            int len = input.read(buf, 0, 8);
            if (len < 0) {
                return null;
            }
            //parse size, big-endian
            size = getNetInt(buf, 0);
            //parse box type area
            type = getBoxType(buf, 4);

            if (1 == size) {
                //read large size area
                input.read(buf, 0, 8);
                size = getNetLong(buf, 0);
                headerSize += 8;
            }
            input.skipBytes((int)(size - headerSize));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new Mp4Box(type, offset, size);
    }

    class Mp4Box {
        long size;
        String type;
        long offset;
        Mp4Box(String type, long offset, long size) {
            this.type = type;
            this.offset = offset;
            this.size = size;
        }
    }

    /**
     * --moov
     * ----track
     * ------mdia
     * --------minf
     * ----------stbl
     * ------------stco or co64
     * */
    private int modifyChunkOffsetTablePos(byte[] buffer) {
        int position = 0;
        //skip ftyp box
        position += mFtypBox.size;
        //skip moov header
        position += 8;
        //find trak boxs
        List<Mp4BoxInfo> trakBoxList = new ArrayList<Mp4BoxInfo>();
        do{
            Mp4BoxInfo box = getNextBoxInfo(buffer, position);
            if (box.type.equals(GlobalConstants.boxTypeTrak)) {
                trakBoxList.add(box);
                Logger.d( "trak box info: offset=" + Long.toHexString(box.offset)
                        + ", size=" + Long.toHexString(box.offset));
            }
            position += box.size;
        } while (position < buffer.length);

        for (int trakIndex = 0; trakIndex < trakBoxList.size(); trakIndex++) {
            Mp4BoxInfo trak = trakBoxList.get(trakIndex);
            Mp4BoxInfo chunkOffsetBox = getChunkOffsetBox(trak, buffer);
            //fix chunk offset box
            int pos = (int)chunkOffsetBox.offset + 12; //size(32bit)  type(32bit) version(8bit) flag(24bit)
            int chunkCount = getNetInt(buffer, pos);
            pos += 4;
            if (8 == chunkOffsetBox.headerLen) {
                //32 size, stco box
                for (int chunkIndex = 0; chunkIndex < chunkCount; chunkIndex++) {
                    int oldOffset = getNetInt(buffer, pos);
                    int newOffset = oldOffset + (int) mFixedLength;
                    setNetInt(buffer, pos, newOffset);

                    int realOffset = getNetInt(buffer, pos);
                    Logger.d( "stco box modify: " + Integer.toHexString(oldOffset)
                            + " --> " + Integer.toHexString(newOffset) +
                            ",  real=" + Integer.toHexString(realOffset));
                    pos += 4;
                }
            } else if (16 == chunkOffsetBox.headerLen) {
                //64 size, co64 box
                for (int chunkIndex = 0; chunkIndex < chunkCount; chunkIndex++){
                    long oldOffset = getNetLong(buffer, pos);
                    long newOffset = oldOffset + mFixedLength;
                    setNetLong(buffer, pos, newOffset);
                    Logger.d( "co64 box modify: " + Long.toHexString(oldOffset)
                            + " --> " + Long.toHexString(newOffset));
                    pos += 8;
                }
            } else {
                Logger.e( "error occur, invalid chunkOffsetBox");
            }
        }
        return position;
    }

    /**
     * stco or co64 box has a table of the absolute position for whole file, so we need mofify it.
     * 32bit:
     * aligned(8) class ChunkOffsetBox
     extends FullBox(‘stco’, version = 0, 0) {
     unsigned int(32) entry_count;
     for (i=1; i u entry_count; i++) {
     unsigned int(32) chunk_offset;
     }
     }

     64bit:
     aligned(8) class ChunkLargeOffsetBox
     extends FullBox(‘co64’, version = 0, 0) {
     unsigned int(32) entry_count;
     for (i=1; i u entry_count; i++) {
     unsigned int(64) chunk_offset;
     }
     }
     * */
    private void modifyHeaderBox(byte[] headerDataBuffer) {
        //find stco or co64 and mofidy it
        modifyChunkOffsetTablePos(headerDataBuffer);
    }

    private Mp4BoxInfo getChunkOffsetBox(Mp4BoxInfo trakBox, byte[] buffer) {
        //find mdia box
        int position = (int)trakBox.offset + trakBox.headerLen; //skip trak box header
        Mp4BoxInfo mdiaBox = null;
        do {
            Mp4BoxInfo box = getNextBoxInfo(buffer, position);
            if (box.type.equals(GlobalConstants.boxTypeMdia)){
                mdiaBox = box;
                Logger.d( "mdia box info: offset=" + Long.toHexString(box.offset)
                        + ", size=" + Long.toHexString(box.offset));
                break;
            }
            position += box.size;
        } while (position < buffer.length);

        //find minf box
        position = (int) mdiaBox.offset + mdiaBox.headerLen;
        Mp4BoxInfo minfBox = null;
        do {
            Mp4BoxInfo box = getNextBoxInfo(buffer, position);
            if (box.type.equals(GlobalConstants.boxTypeMinf)) {
                minfBox = box;
                Logger.d( "minf box info: offset=" + Long.toHexString(box.offset)
                        + ", size=" + Long.toHexString(box.offset));
                break;
            }
            position += box.size;
        } while (position < buffer.length);

        //find stbl box
        position = (int) minfBox.offset + minfBox.headerLen;
        Mp4BoxInfo stblBox = null;
        do {
            Mp4BoxInfo box = getNextBoxInfo(buffer, position);
            if (box.type.equals(GlobalConstants.boxTypeStbl)) {
                stblBox = box;
                Logger.d( "stbl box info: offset=" + Long.toHexString(box.offset)
                        + ", size=" + Long.toHexString(box.offset));
                break;
            }
            position += box.size;

        } while(position < buffer.length);

        //find stco or co64 box
        position = (int)stblBox.offset + stblBox.headerLen;
        Mp4BoxInfo chunkOffsetBox = null;
        do{
            Mp4BoxInfo box = getNextBoxInfo(buffer, position);
            if (box.type.equals(GlobalConstants.boxTypeStco) || box.type.equals(GlobalConstants.boxTypeCo64)) {
                chunkOffsetBox = box;
                Logger.d( "chunk offset box info: offset=" + Long.toHexString(box.offset)
                        + ", size=" + Long.toHexString(box.offset));
                break;
            }
            position += box.size;
        } while(position < buffer.length);
        return chunkOffsetBox;
    }

    private Mp4BoxInfo getNextBoxInfo(byte[] buf, int offset) {
        long size = getNetInt(buf, offset);
        String type = getBoxType(buf, offset + 4);
        int headerLen = 8;
        if (1 == size){
            size = getNetLong(buf, offset + 8);
            headerLen += 8;
        }
        return new Mp4BoxInfo(type, offset, size, headerLen);
    }

    class Mp4BoxInfo {
        long offset = 0; //the pisition for parent box
        long size = 0; //whole box size, include box header
        String type = ""; //box name
        int headerLen = 0; //header include 4 byte size, 4 byte type, maybe include 8 byte large size if size value is 1.
        List<Mp4BoxInfo> childrenBoxs;

        Mp4BoxInfo(String type, long offset, long size, int headerLen){
            this.type = type;
            this.offset = offset;
            this.size = size;
            this.headerLen = headerLen;

            childrenBoxs = new ArrayList<Mp4BoxInfo>();
        }

        void addChildBox(Mp4BoxInfo box){
            childrenBoxs.add(box);
        }
    }

    private void setNetInt(byte[] buf, int offset, int val){
        for (int i = 3; i >= 0; i--){
            buf[offset+i] = (byte)(val & 0xFF);
            val = val >> 8;
        }
    }

    private void setNetLong(byte[] buf, int offset, long val){
        for (int i = 7; i <=0; i--){
            buf[offset+i] = (byte)(val & 0xFF);
            val = val >> 8;
        }
    }

    private int getNetInt(byte[] buf, int offset){
        int size = 0;
        for (int i = 0; i < 4; i++){
            size = (size << 8) + (buf[offset]&0xFF);
            offset++;
        }
        return size;
    }

    private long getNetLong(byte[] buf, int offset){
        int size = 0;
        for (int i = 0; i < 8; i++){
            size = (size << 8) + (buf[offset]&0xFF);
            offset++;
        }
        return size;
    }

    private String getBoxType(byte[] buf, int offset){
        String str = "";
        for (int i = 0; i < 4; i++){
            str += String.valueOf((char)buf[offset]);
            offset++;
        }
        return str;
    }

    private void readHeaderBoxData(byte[] buf, RandomAccessFile inputStream){
        //read ftyp box data
        try {
            inputStream.seek(mFtypBox.offset);
            inputStream.read(buf, 0, (int) mFtypBox.size);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //read moov box data
        try {
            inputStream.seek(mMoovBox.offset);
            inputStream.read(buf, (int) mFtypBox.size, (int) mMoovBox.size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
