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

/**
 * Adapter for the member images grid.
 */
public final class MemberImageAdapter extends BaseAdapter {
    private static final int IMAGES_PER_ROW = 3;
    private static final double IMAGE_PROPORTIONS = (12 / 11);
    private Context mContext;
    private MemberFaceHelper mFaceHelper;
    private List<String> mFaces;
    private int mImageWidth = -1;

    public MemberImageAdapter(Context context, List<String> faces, MemberFaceHelper faceHelper) {
        mContext = context;
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

        if (convertView == null) {
            imageView = initializeImageView();
        } else {
            imageView = (ImageView) convertView;
        }

        mFaceHelper.picasso(mFaces, position)
                .placeholder(R.drawable.member_placeholder)
                .into(imageView);

        return imageView;
    }

    private int getImageWidth() {
        if (mImageWidth <= 0) {
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            mImageWidth = (size.x / IMAGES_PER_ROW);
        }

        return mImageWidth;
    }

    private ImageView initializeImageView() {
        ImageView imageView = new ImageView(mContext);
        int width = getImageWidth();
        imageView.setLayoutParams(new GridView.LayoutParams(width, (int) (width * IMAGE_PROPORTIONS)));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setClickable(false);
        return imageView;
    }
}
