package yangTalkback.Protocol;

//封包接口定义
public interface IPacketObject 
{
	 byte[] GetBytes();

	 void SetBytes(byte[] buf);

}