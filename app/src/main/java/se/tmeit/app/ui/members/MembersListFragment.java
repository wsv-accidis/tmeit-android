package se.tmeit.app.ui.members;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import se.tmeit.app.R;
import se.tmeit.app.model.Member;
import se.tmeit.app.services.Repository;
import se.tmeit.app.storage.Preferences;
import se.tmeit.app.ui.MainActivity;

/**
 * A fragment representing a list of Items.
 */
public final class MembersListFragment extends MainActivity.MainActivityListFragment {
    private final Handler mHandler = new Handler();
    private List<Member> mMembersList;

    public MembersListFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (null == mMembersList) {
            Preferences prefs = new Preferences(activity);
            String username = prefs.getAuthenticatedUser(), serviceAuth = prefs.getServiceAuthentication();
            Repository repository = new Repository(username, serviceAuth);
            repository.getMembers(new Repository.RepositoryResultHandler<List<Member>>() {
                @Override
                public void onError(int errorMessage) {
                    // TODO
                }

                @Override
                public void onSuccess(List<Member> result) {
                    mMembersList = result;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Activity activity = getActivity();
                            if (null != activity && isVisible()) {
                                setListAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, android.R.id.text1, mMembersList));
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

    }

    @Override
    protected int getTitle() {
        return R.string.members_title;
    }
}
