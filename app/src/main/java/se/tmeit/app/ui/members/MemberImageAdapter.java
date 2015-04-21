package se.tmeit.app.ui.members;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

import se.tmeit.app.R;

public class MemberImageAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mFaces;
    private MemberFaceHelper mFaceHelper;

    public MemberImageAdapter(Context c, List<String> faces, MemberFaceHelper faceHelper) {
        mContext = c;
        mFaces = faces;
        mFaceHelper = faceHelper;
    }

    @Override
    public int getCount() {
        return mFaces.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if(convertView == null) {
            imageView = new ImageView(mContext);
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = (size.x / 3);
            imageView.setLayoutParams(new GridView.LayoutParams(width, width * (12/11)));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setClickable(false);
        }
        else {
            imageView = (ImageView) convertView;
        }

        mFaceHelper.picasso(mFaces, position)
                .placeholder(R.drawable.member_placeholder)
                .into(imageView);

        return imageView;
    }
}
