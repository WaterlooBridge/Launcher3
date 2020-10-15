package android.app;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.IconCompat;

/**
 * Created by lin on 2020/10/13.
 */
public class Person implements Parcelable {
    private static final String NAME_KEY = "name";
    private static final String ICON_KEY = "icon";
    private static final String URI_KEY = "uri";
    private static final String KEY_KEY = "key";
    private static final String IS_BOT_KEY = "isBot";
    private static final String IS_IMPORTANT_KEY = "isImportant";

    protected Person(Parcel in) {
        mUri = in.readString();
        mKey = in.readString();
        mIsBot = in.readByte() != 0;
        mIsImportant = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUri);
        dest.writeString(mKey);
        dest.writeByte((byte) (mIsBot ? 1 : 0));
        dest.writeByte((byte) (mIsImportant ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Person> CREATOR = new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    @Nullable
    CharSequence mName;
    @SuppressWarnings("WeakerAccess") /* synthetic access */
    @Nullable
    IconCompat mIcon;
    @SuppressWarnings("WeakerAccess") /* synthetic access */
    @Nullable
    String mUri;
    @SuppressWarnings("WeakerAccess") /* synthetic access */
    @Nullable
    String mKey;
    @SuppressWarnings("WeakerAccess") /* synthetic access */
            boolean mIsBot;
    @SuppressWarnings("WeakerAccess") /* synthetic access */
            boolean mIsImportant;

    /**
     * Returns the name for this {@link Person} or {@code null} if no name was provided. This could
     * be a full name, nickname, username, etc.
     */
    @Nullable
    public CharSequence getName() {
        return mName;
    }

    /**
     * Returns the icon for this {@link Person} or {@code null} if no icon was provided.
     */
    @Nullable
    public IconCompat getIcon() {
        return mIcon;
    }

    /**
     * Returns the raw URI for this {@link Person} or {@code null} if no URI was provided. A URI can
     * be any of the following:
     * <ul>
     *     <li>The {@code String} representation of a
     *     {@link android.provider.ContactsContract.Contacts#CONTENT_LOOKUP_URI}</li>
     *     <li>A {@code mailto:} schema*</li>
     *     <li>A {@code tel:} schema*</li>
     * </ul>
     *
     * <p>*Note for these schemas, the path portion of the URI must exist in the contacts
     * database in their appropriate column, otherwise the reference should be discarded.
     */
    @Nullable
    public String getUri() {
        return mUri;
    }

    /**
     * Returns the key for this {@link Person} or {@code null} if no key was provided. This is
     * provided as a unique identifier between other {@link Person}s.
     */
    @Nullable
    public String getKey() {
        return mKey;
    }

    /**
     * Returns whether or not this {@link Person} is a machine rather than a human. Used primarily
     * to identify automated tooling.
     */
    public boolean isBot() {
        return mIsBot;
    }

    /**
     * Returns whether or not this {@link Person} is important to the user of this device with
     * regards to how frequently they interact.
     */
    public boolean isImportant() {
        return mIsImportant;
    }
}

