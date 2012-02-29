package fr.meteociel.adapter;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import fr.meteociel.activity.R;
import fr.meteociel.om.Observation;
import fr.meteociel.util.ImageLoader;

public class LazyAdapter extends BaseAdapter {
    
    private Activity activity;
    private Observation[] observations;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader; 
    
    public LazyAdapter(Activity a, Observation[] o) {
        activity = a;
        observations=o;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return observations.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.item, null);

        TextView titre=(TextView)vi.findViewById(R.id.titre);
        TextView text=(TextView)vi.findViewById(R.id.text);
        ImageView image=(ImageView)vi.findViewById(R.id.image);
        titre.setText(observations[position].getTitre());
        text.setText(observations[position].getTexte());
		imageLoader.DisplayImage(observations[position].getUrlImage(), image);
        return vi;
    }
}