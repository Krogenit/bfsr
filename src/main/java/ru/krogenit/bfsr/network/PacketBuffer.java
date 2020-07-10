package ru.krogenit.bfsr.network;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ByteProcessor;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class PacketBuffer extends ByteBuf {

	private final ByteBuf buffer;

	public PacketBuffer(ByteBuf buffer) {
		this.buffer = buffer;
	}

	/**
	 * Calculates the number of bytes required to fit the supplied int (0-5) if it were to be read/written using readVarIntFromBuffer or writeVarIntToBuffer
	 */
	public static int getVarIntSize(int value) {
		return (value & -128) == 0 ? 1 : ((value & -16384) == 0 ? 2 : ((value & -2097152) == 0 ? 3 : ((value & -268435456) == 0 ? 4 : 5)));
	}

	/**
	 * Reads a compressed int from the buffer. To do so it maximally reads 5 byte-sized chunks whose most significant bit dictates whether another byte should
	 * be read.
	 */
	public int readVarIntFromBuffer() {
		int i = 0;
		int j = 0;
		byte b0;

		do {
			b0 = this.readByte();
			i |= (b0 & 127) << j++ * 7;

			if (j > 5) { throw new RuntimeException("VarInt too big"); }
		} while ((b0 & 128) == 128);

		return i;
	}

	/**
	 * Writes a compressed int to the buffer. The smallest number of bytes to fit the passed int will be written. Of each such byte only 7 bits will be used to
	 * describe the actual value since its most significant bit dictates whether the next byte is part of that same int. Micro-optimization for int values that
	 * are expected to have values below 128.
	 */
	public void writeVarIntToBuffer(int value) {
		while ((value & -128) != 0) {
			this.writeByte(value & 127 | 128);
			value >>>= 7;
		}

		this.writeByte(value);
	}

	/**
	 * Reads a string from this buffer. Expected parameter is maximum allowed string length. Will throw IOException if string length exceeds this value!
	 */
	public String readStringFromBuffer(int value) throws IOException {
		int j = this.readVarIntFromBuffer();

		if (j > value * 4) {
			throw new IOException("The received encoded string buffer length is longer than maximum allowed (" + j + " > " + value * 4 + ")");
		} else if (j < 0) {
			throw new IOException("The received encoded string buffer length is less than zero! Weird string!");
		} else {
			String s = new String(ByteBufUtil.getBytes(this.readBytes(j)), StandardCharsets.UTF_8);
			
			if (s.length() > value) {
				throw new IOException("The received string length is longer than maximum allowed (" + j + " > " + value + ")");
			} else {
				return s;
			}
		}
	}

	/**
	 * Writes a (UTF-8 encoded) String to this buffer. Will throw IOException if String length exceeds 32767 bytes
	 */
	public void writeStringToBuffer(String s) throws IOException {
		byte[] abyte = s.getBytes(Charsets.UTF_8);

		if (abyte.length > 32767) {
			throw new IOException("String too big (was " + s.length() + " bytes encoded, max " + 32767 + ")");
		} else {
			this.writeVarIntToBuffer(abyte.length);
			this.writeBytes(abyte);
		}
	}
	
	public Vector2f readVector2f() {
		return new Vector2f(readFloat(), readFloat());
	}
	
	public void writeVector2f(Vector2f value) {
		writeFloat(value.x);
		writeFloat(value.y);
	}
	
	public Vector4f readVector4f() {
		return new Vector4f(readFloat(), readFloat(), readFloat(), readFloat());
	}
	
	public void writeVector4f(Vector4f value) {
		writeFloat(value.x);
		writeFloat(value.y);
		writeFloat(value.z);
		writeFloat(value.w);
	}

	public int capacity() {
		return this.buffer.capacity();
	}

	public ByteBuf capacity(int index) {
		return this.buffer.capacity(index);
	}

	public int maxCapacity() {
		return this.buffer.maxCapacity();
	}

	public ByteBufAllocator alloc() {
		return this.buffer.alloc();
	}

	public ByteOrder order() {
		return this.buffer.order();
	}

	public ByteBuf order(ByteOrder byteOrder) {
		return this.buffer.order(byteOrder);
	}

	public ByteBuf unwrap() {
		return this.buffer.unwrap();
	}

	public boolean isDirect() {
		return this.buffer.isDirect();
	}

	public int readerIndex() {
		return this.buffer.readerIndex();
	}

	public ByteBuf readerIndex(int i) {
		return this.buffer.readerIndex(i);
	}

	public int writerIndex() {
		return this.buffer.writerIndex();
	}

	public ByteBuf writerIndex(int i) {
		return this.buffer.writerIndex(i);
	}

	public ByteBuf setIndex(int readerIndex, int writerIndex) {
		return this.buffer.setIndex(readerIndex, writerIndex);
	}

	public int readableBytes() {
		return this.buffer.readableBytes();
	}

	public int writableBytes() {
		return this.buffer.writableBytes();
	}

	public int maxWritableBytes() {
		return this.buffer.maxWritableBytes();
	}

	public boolean isReadable() {
		return this.buffer.isReadable();
	}

	public boolean isReadable(int i) {
		return this.buffer.isReadable(i);
	}

	public boolean isWritable() {
		return this.buffer.isWritable();
	}

	public boolean isWritable(int i) {
		return this.buffer.isWritable(i);
	}

	public ByteBuf clear() {
		return this.buffer.clear();
	}

	public ByteBuf markReaderIndex() {
		return this.buffer.markReaderIndex();
	}

	public ByteBuf resetReaderIndex() {
		return this.buffer.resetReaderIndex();
	}

	public ByteBuf markWriterIndex() {
		return this.buffer.markWriterIndex();
	}

	public ByteBuf resetWriterIndex() {
		return this.buffer.resetWriterIndex();
	}

	public ByteBuf discardReadBytes() {
		return this.buffer.discardReadBytes();
	}

	public ByteBuf discardSomeReadBytes() {
		return this.buffer.discardSomeReadBytes();
	}

	public ByteBuf ensureWritable(int index) {
		return this.buffer.ensureWritable(index);
	}

	public int ensureWritable(int index, boolean value) {
		return this.buffer.ensureWritable(index, value);
	}

	public boolean getBoolean(int index) {
		return this.buffer.getBoolean(index);
	}

	public byte getByte(int index) {
		return this.buffer.getByte(index);
	}

	public short getUnsignedByte(int index) {
		return this.buffer.getUnsignedByte(index);
	}

	public short getShort(int index) {
		return this.buffer.getShort(index);
	}

	public int getUnsignedShort(int index) {
		return this.buffer.getUnsignedShort(index);
	}

	public int getMedium(int index) {
		return this.buffer.getMedium(index);
	}

	public int getUnsignedMedium(int index) {
		return this.buffer.getUnsignedMedium(index);
	}

	public int getInt(int index) {
		return this.buffer.getInt(index);
	}

	public long getUnsignedInt(int index) {
		return this.buffer.getUnsignedInt(index);
	}

	public long getLong(int index) {
		return this.buffer.getLong(index);
	}

	public char getChar(int index) {
		return this.buffer.getChar(index);
	}

	public float getFloat(int index) {
		return this.buffer.getFloat(index);
	}

	public double getDouble(int index) {
		return this.buffer.getDouble(index);
	}

	public ByteBuf getBytes(int index, ByteBuf byteBuf) {
		return this.buffer.getBytes(index, byteBuf);
	}

	public ByteBuf getBytes(int index, ByteBuf byteBuf, int size) {
		return this.buffer.getBytes(index, byteBuf, size);
	}

	public ByteBuf getBytes(int index, ByteBuf byteBuf, int dstIndex, int length) {
		return this.buffer.getBytes(index, byteBuf, dstIndex, length);
	}

	public ByteBuf getBytes(int index, byte[] value) {
		return this.buffer.getBytes(index, value);
	}

	public ByteBuf getBytes(int index, byte[] value, int dstIndex, int length) {
		return this.buffer.getBytes(index, value, dstIndex, length);
	}

	public ByteBuf getBytes(int index, ByteBuffer buffer) {
		return this.buffer.getBytes(index, buffer);
	}

	public ByteBuf getBytes(int index, OutputStream outputStream, int length) throws IOException {
		return this.buffer.getBytes(index, outputStream, length);
	}

	public int getBytes(int index, GatheringByteChannel gatheringByteChannel, int length) throws IOException {
		return this.buffer.getBytes(index, gatheringByteChannel, length);
	}

	public ByteBuf setBoolean(int index, boolean b) {
		return this.buffer.setBoolean(index, b);
	}

	public ByteBuf setByte(int index, int value) {
		return this.buffer.setByte(index, value);
	}

	public ByteBuf setShort(int index, int value) {
		return this.buffer.setShort(index, value);
	}

	public ByteBuf setMedium(int index, int value) {
		return this.buffer.setMedium(index, value);
	}

	public ByteBuf setInt(int index, int value) {
		return this.buffer.setInt(index, value);
	}

	public ByteBuf setLong(int index, long value) {
		return this.buffer.setLong(index, value);
	}

	public ByteBuf setChar(int index, int value) {
		return this.buffer.setChar(index, value);
	}

	public ByteBuf setFloat(int index, float value) {
		return this.buffer.setFloat(index, value);
	}

	public ByteBuf setDouble(int index, double value) {
		return this.buffer.setDouble(index, value);
	}

	public ByteBuf setBytes(int index, ByteBuf buffer) {
		return this.buffer.setBytes(index, buffer);
	}

	public ByteBuf setBytes(int index, ByteBuf buffer, int length) {
		return this.buffer.setBytes(index, buffer, length);
	}

	public ByteBuf setBytes(int index, ByteBuf buffer, int srcIndex, int length) {
		return this.buffer.setBytes(index, buffer, srcIndex, length);
	}

	public ByteBuf setBytes(int index, byte[] buffer) {
		return this.buffer.setBytes(index, buffer);
	}

	public ByteBuf setBytes(int index, byte[] buffer, int srcIndex, int length) {
		return this.buffer.setBytes(index, buffer, srcIndex, length);
	}

	public ByteBuf setBytes(int index, ByteBuffer buffer) {
		return this.buffer.setBytes(index, buffer);
	}

	public int setBytes(int index, InputStream buffer, int length) throws IOException {
		return this.buffer.setBytes(index, buffer, length);
	}

	public int setBytes(int index, ScatteringByteChannel buffer, int length) throws IOException {
		return this.buffer.setBytes(index, buffer, length);
	}

	public ByteBuf setZero(int index, int value) {
		return this.buffer.setZero(index, value);
	}

	public boolean readBoolean() {
		return this.buffer.readBoolean();
	}

	public byte readByte() {
		return this.buffer.readByte();
	}

	public short readUnsignedByte() {
		return this.buffer.readUnsignedByte();
	}

	public short readShort() {
		return this.buffer.readShort();
	}

	public int readUnsignedShort() {
		return this.buffer.readUnsignedShort();
	}

	public int readMedium() {
		return this.buffer.readMedium();
	}

	public int readUnsignedMedium() {
		return this.buffer.readUnsignedMedium();
	}

	public int readInt() {
		return this.buffer.readInt();
	}

	public long readUnsignedInt() {
		return this.buffer.readUnsignedInt();
	}

	public long readLong() {
		return this.buffer.readLong();
	}

	public char readChar() {
		return this.buffer.readChar();
	}

	public float readFloat() {
		return this.buffer.readFloat();
	}

	public double readDouble() {
		return this.buffer.readDouble();
	}

	public ByteBuf readBytes(int length) {
		return this.buffer.readBytes(length);
	}

	public ByteBuf readSlice(int length) {
		return this.buffer.readSlice(length);
	}

	public ByteBuf readBytes(ByteBuf length) {
		return this.buffer.readBytes(length);
	}

	public ByteBuf readBytes(ByteBuf buffer, int length) {
		return this.buffer.readBytes(buffer, length);
	}

	public ByteBuf readBytes(ByteBuf buffer, int dstIndex, int length) {
		return this.buffer.readBytes(buffer, dstIndex, length);
	}

	public ByteBuf readBytes(byte[] length) {
		return this.buffer.readBytes(length);
	}

	public ByteBuf readBytes(byte[] bytes, int dstIndex, int length) {
		return this.buffer.readBytes(bytes, dstIndex, length);
	}

	public ByteBuf readBytes(ByteBuffer buffer) {
		return this.buffer.readBytes(buffer);
	}

	public ByteBuf readBytes(OutputStream outputStream, int length) throws IOException {
		return this.buffer.readBytes(outputStream, length);
	}

	public int readBytes(GatheringByteChannel gatheringByteChannel, int length) throws IOException {
		return this.buffer.readBytes(gatheringByteChannel, length);
	}

	public ByteBuf skipBytes(int index) {
		return this.buffer.skipBytes(index);
	}

	public ByteBuf writeBoolean(boolean value) {
		return this.buffer.writeBoolean(value);
	}

	public ByteBuf writeByte(int value) {
		return this.buffer.writeByte(value);
	}

	public ByteBuf writeShort(int value) {
		return this.buffer.writeShort(value);
	}

	public ByteBuf writeMedium(int value) {
		return this.buffer.writeMedium(value);
	}

	public ByteBuf writeInt(int value) {
		return this.buffer.writeInt(value);
	}

	public ByteBuf writeLong(long value) {
		return this.buffer.writeLong(value);
	}

	public ByteBuf writeChar(int value) {
		return this.buffer.writeChar(value);
	}

	public ByteBuf writeFloat(float value) {
		return this.buffer.writeFloat(value);
	}

	public ByteBuf writeDouble(double value) {
		return this.buffer.writeDouble(value);
	}

	public ByteBuf writeBytes(ByteBuf buffer) {
		return this.buffer.writeBytes(buffer);
	}

	public ByteBuf writeBytes(ByteBuf value, int length) {
		return this.buffer.writeBytes(value, length);
	}

	public ByteBuf writeBytes(ByteBuf value, int srcIndex, int length) {
		return this.buffer.writeBytes(value, srcIndex, length);
	}

	public ByteBuf writeBytes(byte[] bytes) {
		return this.buffer.writeBytes(bytes);
	}

	public ByteBuf writeBytes(byte[] bytes, int srcIndex, int length) {
		return this.buffer.writeBytes(bytes, srcIndex, length);
	}

	public ByteBuf writeBytes(ByteBuffer byteBuffer) {
		return this.buffer.writeBytes(byteBuffer);
	}

	public int writeBytes(InputStream inputStream, int length) throws IOException {
		return this.buffer.writeBytes(inputStream, length);
	}

	public int writeBytes(ScatteringByteChannel scatteringByteChannel, int length) throws IOException {
		return this.buffer.writeBytes(scatteringByteChannel, length);
	}

	public ByteBuf writeZero(int value) {
		return this.buffer.writeZero(value);
	}

	public int indexOf(int fromIndex, int toIndex, byte value) {
		return this.buffer.indexOf(fromIndex, toIndex, value);
	}

	public int bytesBefore(byte value) {
		return this.buffer.bytesBefore(value);
	}

	public int bytesBefore(int length, byte value) {
		return this.buffer.bytesBefore(length, value);
	}

	public int bytesBefore(int index, int length, byte value) {
		return this.buffer.bytesBefore(index, length, value);
	}

	public ByteBuf copy() {
		return this.buffer.copy();
	}

	public ByteBuf copy(int index, int length) {
		return this.buffer.copy(index, length);
	}

	public ByteBuf slice() {
		return this.buffer.slice();
	}

	public ByteBuf slice(int index, int length) {
		return this.buffer.slice(index, length);
	}

	public ByteBuf duplicate() {
		return this.buffer.duplicate();
	}

	public int nioBufferCount() {
		return this.buffer.nioBufferCount();
	}

	public ByteBuffer nioBuffer() {
		return this.buffer.nioBuffer();
	}

	public ByteBuffer nioBuffer(int index, int length) {
		return this.buffer.nioBuffer(index, length);
	}

	public ByteBuffer internalNioBuffer(int index, int length) {
		return this.buffer.internalNioBuffer(index, length);
	}

	public ByteBuffer[] nioBuffers() {
		return this.buffer.nioBuffers();
	}

	public ByteBuffer[] nioBuffers(int index, int length) {
		return this.buffer.nioBuffers(index, length);
	}

	public boolean hasArray() {
		return this.buffer.hasArray();
	}

	public byte[] array() {
		return this.buffer.array();
	}

	public int arrayOffset() {
		return this.buffer.arrayOffset();
	}

	public boolean hasMemoryAddress() {
		return this.buffer.hasMemoryAddress();
	}

	public long memoryAddress() {
		return this.buffer.memoryAddress();
	}

	public String toString(Charset charset) {
		return this.buffer.toString(charset);
	}

	public String toString(int index, int length, Charset charset) {
		return this.buffer.toString(index, length, charset);
	}

	public int hashCode() {
		return this.buffer.hashCode();
	}

	public boolean equals(Object value) {
		return this.buffer.equals(value);
	}

	public int compareTo(ByteBuf value) {
		return this.buffer.compareTo(value);
	}

	public String toString() {
		return this.buffer.toString();
	}

	public ByteBuf retain(int value) {
		return this.buffer.retain(value);
	}

	public ByteBuf retain() {
		return this.buffer.retain();
	}

	public int refCnt() {
		return this.buffer.refCnt();
	}

	public boolean release() {
		return this.buffer.release();
	}

	public boolean release(int value) {
		return this.buffer.release(value);
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public ByteBuf asReadOnly() {
		return buffer.asReadOnly();
	}

	@Override
	public short getShortLE(int index) {
		return buffer.getShortLE(index);
	}

	@Override
	public int getUnsignedShortLE(int index) {
		return buffer.getUnsignedShortLE(index);
	}

	@Override
	public int getMediumLE(int index) {
		return buffer.getMediumLE(index);
	}

	@Override
	public int getUnsignedMediumLE(int index) {
		return buffer.getUnsignedMediumLE(index);
	}

	@Override
	public int getIntLE(int index) {
		return buffer.getIntLE(index);
	}

	@Override
	public long getUnsignedIntLE(int index) {
		return buffer.getUnsignedIntLE(index);
	}

	@Override
	public long getLongLE(int index) {
		return buffer.getLongLE(index);
	}

	@Override
	public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
		return buffer.getBytes(index, out, position, length);
	}

	@Override
	public CharSequence getCharSequence(int index, int length, Charset charset) {
		return buffer.getCharSequence(index, length, charset);
	}

	@Override
	public ByteBuf setShortLE(int index, int value) {
		return buffer.setShortLE(index, value);
	}

	@Override
	public ByteBuf setMediumLE(int index, int value) {
		return buffer.setMediumLE(index, value);
	}

	@Override
	public ByteBuf setIntLE(int index, int value) {
		return buffer.setIntLE(index, value);
	}

	@Override
	public ByteBuf setLongLE(int index, long value) {
		return buffer.setLongLE(index, value);
	}

	@Override
	public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
		return buffer.setBytes(index, in, position, length);
	}

	@Override
	public int setCharSequence(int index, CharSequence sequence, Charset charset) {
		return buffer.setCharSequence(index, sequence, charset);
	}

	@Override
	public short readShortLE() {
		return buffer.readShortLE();
	}

	@Override
	public int readUnsignedShortLE() {
		return buffer.readUnsignedShortLE();
	}

	@Override
	public int readMediumLE() {
		return buffer.readMediumLE();
	}

	@Override
	public int readUnsignedMediumLE() {
		return buffer.readUnsignedMediumLE();
	}

	@Override
	public int readIntLE() {
		return buffer.readIntLE();
	}

	@Override
	public long readUnsignedIntLE() {
		return buffer.readUnsignedIntLE();
	}

	@Override
	public long readLongLE() {
		return buffer.readLongLE();
	}

	@Override
	public ByteBuf readRetainedSlice(int length) {
		return buffer.readRetainedSlice(length);
	}

	@Override
	public CharSequence readCharSequence(int length, Charset charset) {
		return buffer.readCharSequence(length, charset);
	}

	@Override
	public int readBytes(FileChannel out, long position, int length) throws IOException {
		return buffer.readBytes(out, position, length);
	}

	@Override
	public ByteBuf writeShortLE(int value) {
		return buffer.writeShortLE(value);
	}

	@Override
	public ByteBuf writeMediumLE(int value) {
		return buffer.writeMediumLE(value);
	}

	@Override
	public ByteBuf writeIntLE(int value) {
		return buffer.writeIntLE(value);
	}

	@Override
	public ByteBuf writeLongLE(long value) {
		return buffer.writeLongLE(value);
	}

	@Override
	public int writeBytes(FileChannel in, long position, int length) throws IOException {
		return buffer.writeBytes(in, position, length);
	}

	@Override
	public int writeCharSequence(CharSequence sequence, Charset charset) {
		return buffer.writeCharSequence(sequence, charset);
	}

	@Override
	public int forEachByte(ByteProcessor processor) {
		return buffer.forEachByte(processor);
	}

	@Override
	public int forEachByte(int index, int length, ByteProcessor processor) {
		return buffer.forEachByte(index, length, processor);
	}

	@Override
	public int forEachByteDesc(ByteProcessor processor) {
		return buffer.forEachByte(processor);
	}

	@Override
	public int forEachByteDesc(int index, int length, ByteProcessor processor) {
		return buffer.forEachByteDesc(index, length, processor);
	}

	@Override
	public ByteBuf retainedSlice() {
		return buffer.retainedSlice();
	}

	@Override
	public ByteBuf retainedSlice(int index, int length) {
		return buffer.retainedSlice(index, length);
	}

	@Override
	public ByteBuf retainedDuplicate() {
		return buffer.retainedDuplicate();
	}

	@Override
	public ByteBuf touch() {
		return buffer.touch();
	}

	@Override
	public ByteBuf touch(Object hint) {
		return buffer.touch(hint);
	}
}