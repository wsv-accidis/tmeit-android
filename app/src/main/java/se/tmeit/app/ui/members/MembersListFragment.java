package se.tmeit.app.ui.members;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import se.tmeit.app.R;
import se.tmeit.app.model.Member;
import se.tmeit.app.services.HttpClient;
import se.tmeit.app.services.Repository;
import se.tmeit.app.services.TmeitServiceConfig;
import se.tmeit.app.storage.Preferences;
import se.tmeit.app.ui.MainActivity;

/**
 * A fragment representing a list of Items.
 */
public final class MembersListFragment extends MainActivity.MainActivityListFragment {
    private static final Random mRandom = new Random();
    private final Handler mHandler = new Handler();
    private Member.RepositoryData mMembers;
    private Picasso mPicasso;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mPicasso = new Picasso.Builder(activity)
                .downloader(new OkHttpDownloader(HttpClient.getOkHttpClient()))
                .build();

        if (null == mMembers) {
            Preferences prefs = new Preferences(activity);
            String username = prefs.getAuthenticatedUser(), serviceAuth = prefs.getServiceAuthentication();
            Repository repository = new Repository(username, serviceAuth);

            repository.getMembers(new Repository.RepositoryResultHandler<Member.RepositoryData>() {
                @Override
                public void onError(final int errorMessage) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Activity activity = getActivity();
                            if (null != activity && isVisible()) {
                                mMembers = null;
                                initializeList();

                                Toast toast = Toast.makeText(activity, getString(errorMessage), Toast.LENGTH_LONG);
                                toast.show();
                            }
                        }
                    });
                }

                @Override
                public void onSuccess(final Member.RepositoryData result) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (null != getActivity() && isVisible()) {
                                mMembers = result;
                                initializeList();
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mPicasso = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    @Override
    protected int getTitle() {
        return R.string.members_title;
    }

    private void initializeList() {
        List<Member> members = (null != mMembers ? mMembers.getMembers() : Collections.<Member>emptyList());
        setListAdapter(new MembersListAdapter(getActivity(), R.layout.list_item_member, R.id.member_real_name, members));
    }

    private class MembersListAdapter extends ArrayAdapter<Member> {
        public MembersListAdapter(Context context, int resource, int textViewResourceId, List<Member> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            ImageView imageView = (ImageView) view.findViewById(R.id.member_face);
            Member member = getItem(position);

            // TODO Lots of other props here

            List<String> faces = member.getFaces();
            if (!faces.isEmpty()) {
                String face = faces.get(mRandom.nextInt(faces.size()));
                mPicasso.load(Uri.withAppendedPath(Uri.parse(TmeitServiceConfig.ROOT_URL), face))
                        .resizeDimen(R.dimen.tmeit_members_list_face_size, R.dimen.tmeit_members_list_face_size)
                        .centerInside()
                        .placeholder(R.drawable.member_placeholder)
                        .into(imageView);
            }

            return view;
        }
    }
}
