package client;

public class Packet {
	public static final short PACKET_TYPE_STANDARD = 0;
	public static final short PACKET_TYPE_LOGIN_REQ = 1;
	public static final short PACKET_TYPE_LOGIN_RES = 2;
	public static final short PACKET_TYPE_REGISTER_REQ = 3;
	public static final short PACKET_TYPE_REGISTER_RES = 4;
	public static final short PACKET_TYPE_ENTER_LOBBY_REQ = 5;
	public static final short PACKET_TYPE_USER_INFO_RES = 6;
	public static final short PACKET_TYPE_ROOM_LIST_RES = 7;
	public static final short PACKET_TYPE_ROOM_MAKE_REQ = 8;
	public static final short PACKET_TYPE_ROOM_MAKE_RES = 9;
	public static final short PACKET_TYPE_ENTER_ROOM_REQ = 10;
	public static final short PACKET_TYPE_ENTER_ROOM_RES = 11;
	public static final short PACKET_TYPE_ROOM_INFO_REQ = 12;
	public static final short PACKET_TYPE_ROOM_INFO_RES = 13;
	public static final short PACKET_TYPE_ROOM_USER_RES = 14;
	public static final short PACKET_TYPE_EXIT_ROOM_REQ = 15;

	private short type;
	private String content;

	public Packet(String content, short type) {
		this.type = type;
		this.content = content;
	}

	public byte[] Encode() {

		byte[] ret = new byte[content.length() + 12];

		byte[] head = "\0end".getBytes();
		byte[] type = shortToByteArray(this.type);
		byte[] size = shortToByteArray((short)this.content.length());
		byte[] content = this.content.getBytes();
		byte[] end = "dne\0".getBytes();

		int k = 0;
		for (int i = 0; i < 4; i++)
			ret[k++] = head[i];
		for (int i = 0; i < 2; i++)
			ret[k++] = type[i];
		for (int i = 0; i < 2; i++)
			ret[k++] = size[i];
		for (int i = 0; i < this.content.length(); i++)
			ret[k++] = content[i];
		for (int i = 0; i < 4; i++)
			ret[k++] = end[i];

		return ret;
	}

	public byte[] shortToByteArray(short n) {
		byte[] ret = new byte[2];

		ret[0] = (byte) ((n >> 8) & 0xFF);
		ret[1] = (byte) ((n >> 0) & 0xFF);

		return ret;
	}

	public short byteArrayToShort(byte[] arr)
	{
		return (short)(((arr[0] & 0xFF) << 8) + (arr[1] & 0xFF));
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
