package yangTalkback.Act;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


/**
 * Created by Administrator on 2017/3/4.
 */

public class DialogAdapter extends BaseAdapter {
    private Context context;
    private String[] infos;
    private ViewHoder hoder;

    public DialogAdapter(Context context, String description) {
        this.context = context;
        if (description!=null&&!TextUtils.isEmpty(description)) {
            this.infos = description.split("\n");
        }
    }

    @Override
    public int getCount() {
        return infos==null?0:infos.length;
    }

    @Override
    public Object getItem(int position) {
        return infos[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            hoder = new ViewHoder();
            convertView = LayoutInflater.from(context).inflate(R.layout.feature_layout, null);
            hoder.tv = (TextView) convertView.findViewById(R.id.feature_tv);
            convertView.setTag(hoder);
        } else {
            hoder = (ViewHoder) convertView.getTag();
        }
        hoder.tv.setText(infos[position]);
        return convertView;
    }

    class ViewHoder {
        private TextView tv;
    }
}
