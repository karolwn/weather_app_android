package com.example.weather.model;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Custom data type, object consist of two elements:
 * - String cityName
 * - Boolean isSelected is used in selection of item in ListView
 * Interface for classes whose instances can be written to and restored from a Parcel.
 * Classes implementing the Parcelable interface must also have a non-null static field
 * called CREATOR of a type that implements the Parcelable.Creator interface.
 */
public class FavouritesModel implements Parcelable {
    private String cityName;
    private Boolean isSelected;

    public FavouritesModel(String cityName, Boolean isSelected) {
        this.cityName = cityName;
        this.isSelected = isSelected;
    }

    protected FavouritesModel(Parcel in) {
        cityName = in.readString();
        byte tmpIsSelected = in.readByte();
        isSelected = tmpIsSelected == 0 ? null : tmpIsSelected == 1;
    }

    /**
     * Flatten this object in to a Parcel.
     * @param dest: The Parcel in which the object should be written.
     * @param flags: Additional flags about how the object should be written. May be 0 or
     *             PARCELABLE_WRITE_RETURN_VALUE. Value is either 0 or a combination of
     *             PARCELABLE_WRITE_RETURN_VALUE, and android.os.Parcelable.PARCELABLE_ELIDE_DUPLICATES
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cityName);
        dest.writeByte((byte) (isSelected == null ? 0 : isSelected ? 1 : 2));
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable instance's marshaled
     * representation. For example, if the object will include a file descriptor in the output
     * of writeToParcel(android.os.Parcel, int), the return value of this method must include
     * the CONTENTS_FILE_DESCRIPTOR bit.
     * @return Describe the kinds of special objects contained in this Parcelable instance's
     * marshaled representation. For example, if the object will include a file descriptor in
     * the output of writeToParcel(android.os.Parcel, int), the return value of this method must
     * include the CONTENTS_FILE_DESCRIPTOR bit.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Interface that must be implemented and provided as a public CREATOR field that generates
     * instances of your Parcelable class from a Parcel.
     */
    public static final Creator<FavouritesModel> CREATOR = new Creator<FavouritesModel>() {

        /**
         * Create a new instance of the Parcelable class, instantiating it from the given
         * Parcel whose data had previously been written by Parcelable#writeToParcel.
         * @param in: The Parcel to read the object's data from.
         */
        @Override
        public FavouritesModel createFromParcel(Parcel in) {
            return new FavouritesModel(in);
        }

        /**
         * Create a new array of the Parcelable class.
         * @param size: Size of the array.
         * @return Returns an array of the Parcelable class, with every entry initialized to null.
         */
        @Override
        public FavouritesModel[] newArray(int size) {
            return new FavouritesModel[size];
        }
    };

    /**
     * @return city name
     */
    public String getCityName() {
        return this.cityName;
    }

    /**
     * Sets city name.
     * @param cityName: new name
     */
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    /**
     * @return boolean which determines if city is selected or not
     */
    public Boolean isSelected() {
        return this.isSelected;
    }

    /**
     * Updates isSelected.
     * @param selected: new state of selection
     */
    public void setSelected(Boolean selected) {
        isSelected = selected;
    }
}