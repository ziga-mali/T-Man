package si.uni_lj.fe.tnuv.taskman;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class CustomSimpleAdapter extends SimpleAdapter {

    private List<? extends Map<String, String>> data;
    private Context context;

    public CustomSimpleAdapter(Context context, List<? extends Map<String, String>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        Map<String, String> item = data.get(position);
        String koncano = item.get("koncano");

        TextView nameTextView = view.findViewById(R.id.projectTask);
        TextView descriptionTextView = view.findViewById(R.id.projectTaskDescription);

        if ("1".equals(koncano)) {
            int greenColor = context.getResources().getColor(android.R.color.holo_green_light);
            nameTextView.setTextColor(greenColor);
            descriptionTextView.setTextColor(greenColor);
        } else if ("0".equals(koncano)) {
            int blueColor = context.getResources().getColor(android.R.color.holo_blue_light);
            nameTextView.setTextColor(blueColor);
            descriptionTextView.setTextColor(blueColor);
        }

        return view;
    }
}
