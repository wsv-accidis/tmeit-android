package se.tmeit.app.ui.members;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

/**
 * Canned actions for taking specific actions on members that interact with the Android system.
 */
public final class MemberActions {
	private static final String TAG = MemberActions.class.getSimpleName();

	private MemberActions() {
	}

	public static boolean addAsContact(String realName, String phoneNo, String email, ContentResolver contentResolver) {
		ArrayList<ContentProviderOperation> batch = new ArrayList<>();

		batch.add(ContentProviderOperation
			.newInsert(ContactsContract.RawContacts.CONTENT_URI)
			.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
			.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
			.build());

		batch.add(ContentProviderOperation
			.newInsert(ContactsContract.Data.CONTENT_URI)
			.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
			.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
			.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, realName).build());

		if (!TextUtils.isEmpty(phoneNo)) {
			batch.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNo)
				.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
				.build());
		}

		if (!TextUtils.isEmpty(email)) {
			batch.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
				.withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME)
				.build());
		}

		try {
			contentResolver.applyBatch(ContactsContract.AUTHORITY, batch);
			return true;
		} catch (Exception e) {
			Log.e(TAG, "Exception while trying to add a contact.", e);
			return false;
		}
	}

	public static void makeCallTo(String phoneNo, Fragment fragment) {
		try {
			Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNo, null));
			fragment.startActivity(intent);
		} catch (Exception e) {
			Log.e(TAG, "Exception while trying to start a call.", e);
		}
	}

	public static void sendEmailTo(String email, Fragment fragment) {
		try {
			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null));
			fragment.startActivity(intent);
		} catch (Exception e) {
			Log.e(TAG, "Exception while trying to send e-mail.", e);
		}
	}

	public static void sendSmsTo(String phoneNo, Fragment fragment) {
		try {
			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", phoneNo, null));
			fragment.startActivity(intent);
		} catch (Exception e) {
			Log.e(TAG, "Exception while trying to send message.", e);
		}
	}
}
