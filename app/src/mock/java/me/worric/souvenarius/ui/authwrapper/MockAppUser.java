package me.worric.souvenarius.ui.authwrapper;

public class MockAppUser implements AppUser {

    private final String mUid;
    private final String mEmail;
    private final String mDisplayName;

    public MockAppUser(String uid, String email, String displayName) {
        mUid = uid;
        mEmail = email;
        mDisplayName = displayName;
    }

    @Override
    public String getUid() {
        return mUid;
    }

    @Override
    public String getEmail() {
        return mEmail;
    }

    @Override
    public String getDisplayName() {
        return mDisplayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MockAppUser mockAppUser = (MockAppUser) o;

        if (!mUid.equals(mockAppUser.mUid)) return false;
        if (!mEmail.equals(mockAppUser.mEmail)) return false;

        return mDisplayName.equals(mockAppUser.mDisplayName);
    }

    @Override
    public int hashCode() {
        int result = mUid.hashCode();
        result = 31 * result + mEmail.hashCode();
        result = 31 * result + mDisplayName.hashCode();
        
        return result;
    }

    @Override
    public String toString() {
        return "MockAppUser{" +
                "mUid='" + mUid + '\'' +
                ", mEmail='" + mEmail + '\'' +
                ", mDisplayName='" + mDisplayName + '\'' +
                '}';
    }
}
