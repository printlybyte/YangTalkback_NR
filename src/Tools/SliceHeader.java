package Tools;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class SliceHeader {

	public enum SliceType {
		P, B, I, SP, SI
	}

	public int first_mb_in_slice;
	public SliceType slice_type;
	public int pic_parameter_set_id;
	public int colour_plane_id;
	public int frame_num;
	public boolean field_pic_flag = false;
	public boolean bottom_field_flag = false;
	public int idr_pic_id;
	public int pic_order_cnt_lsb;
	public int delta_pic_order_cnt_bottom;

	public SliceHeader(InputStream is, boolean IdrPicFlag) throws IOException {
		is.read();
		CAVLCReader reader = new CAVLCReader(is);
		 
		first_mb_in_slice = reader.readUE("SliceHeader: first_mb_in_slice");
		switch (reader.readUE("SliceHeader: slice_type")) {
		case 0:
		case 5:
			slice_type = SliceType.P;
			break;

		case 1:
		case 6:
			slice_type = SliceType.B;
			break;

		case 2:
		case 7:
			slice_type = SliceType.I;
			break;

		case 3:
		case 8:
			slice_type = SliceType.SP;
			break;

		case 4:
		case 9:
			slice_type = SliceType.SI;
			break;

		}
	 
	}

	@Override
	public String toString() {
		return "SliceHeader{" + "first_mb_in_slice=" + first_mb_in_slice + ", slice_type=" + slice_type + ", pic_parameter_set_id=" + pic_parameter_set_id + ", colour_plane_id=" + colour_plane_id
				+ ", frame_num=" + frame_num + ", field_pic_flag=" + field_pic_flag + ", bottom_field_flag=" + bottom_field_flag + ", idr_pic_id=" + idr_pic_id + ", pic_order_cnt_lsb="
				+ pic_order_cnt_lsb + ", delta_pic_order_cnt_bottom=" + delta_pic_order_cnt_bottom + '}';
	}
}
