package yangTalkback.Cpt;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import AXLib.Model.KeyValue;
import AXLib.Utility.Event;
import AXLib.Utility.ISelect;
import AXLib.Utility.ListEx;
import AXLib.Utility.Predicate;

public class cptDDLEx<TKey> {
	private Spinner _ddl = null;
	private Context _ctx = null;
	private TKey _selKey;
	private ListEx<KeyValue<TKey, String>> _source = null;
	public final Event<TKey> Selected = new Event<TKey>();

	public cptDDLEx(Context ctx, Spinner ddl) {
		_ctx = ctx;
		_ddl = ddl;
		_ddl.setOnItemSelectedListener(m_SpinnerListener);
	}

	public void setSelectedKey(final TKey key) {
		int index = _source.Index(new Predicate<KeyValue<TKey, String>>() {
			@Override
			public boolean Test(KeyValue<TKey, String> obj) {
				return key == obj.Key;
			}
		});
		if (index == -1)
			throw new RuntimeException("Œ¥’“µΩ∏√œÓ");
		_ddl.setSelection(index);
		_selKey = key;
	}

	public TKey getSelectedKey() {
		return _selKey;
	}

	public void setSource(ListEx<KeyValue<TKey, String>> source) {
		_source = source;
		ListEx<String> items = source.Select(new ISelect<KeyValue<TKey, String>, String>() {
			@Override
			public String Select(KeyValue<TKey, String> t) {
				return t.Value;
			}
		});
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(_ctx, android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_ddl.setAdapter(adapter);
		_ddl.setSelection(-1);
	}

	public void Open() {
		// _ddl.setVisibility(View.VISIBLE);
		_ddl.performClick();
	}

	public void Close() {
		// _ddl.setVisibility(View.GONE);
	}

	private Spinner.OnItemSelectedListener m_SpinnerListener = new Spinner.OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
			KeyValue<TKey, String> item = _source.get(position);
			_selKey = item.Key;
			Selected.Trigger(cptDDLEx.this, item.Key);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};
}
